package com.SolanaDevMinecraft;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SolanaManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private final DatabaseManager databaseManager;

    public SolanaManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public double getSolanaBalance(String walletAddress) throws Exception {
        String host = ConfigManager.DOCKER_HOST.get();
        String apiwebkey = ConfigManager.API_WEB_KEY.get();
        String solanaCmd = ConfigManager.SOLANA_COMMAND.get();
        String comando = solanaCmd + " balance " + walletAddress;

        String url = String.format("http://%s/consulta.php?apikey=%s&comando=%s", host, apiwebkey, URLEncoder.encode(comando, StandardCharsets.UTF_8));

        String response = executeHttpGet(url);
        
        try {
            JSONObject json = new JSONObject(response);
            if (json.has("status") && json.getString("status").equalsIgnoreCase("success")) {
                String output = json.getString("output").replace(" SOL", "").trim();
                return Double.parseDouble(output);
            } else {
                throw new Exception("API retornou erro: " + (json.has("message") ? json.getString("message") : response));
            }
        } catch (Exception e) {
            throw new Exception("Erro ao processar resposta do saldo: " + e.getMessage());
        }
    }

    private String executeHttpGet(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    public String getWalletFromDatabase(String username) {
        String walletAddress = null;
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT c.endereco FROM carteiras c JOIN jogadores j ON c.jogador_id = j.id WHERE LOWER(j.nome) = LOWER(?)")) {
            stmt.setString(1, username.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    walletAddress = rs.getString("endereco");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Erro ao buscar carteira no banco para " + username + ": " + e.getMessage());
        }
        return walletAddress;
    }

    public void handleSolBalance(Player player) {
        String walletAddress = getWalletFromDatabase(player.getName().getString());
        if (walletAddress == null) {
            player.sendSystemMessage(Component.literal("§cVocê ainda não possui uma carteira registrada no banco de dados."));
            LOGGER.warn("Jogador " + player.getName().getString() + " tentou ver saldo sem carteira no banco.");
            return;
        }

        player.sendSystemMessage(Component.literal("§6Carteira SOL: §b" + walletAddress)
                .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, walletAddress))));

        CompletableFuture.runAsync(() -> {
            try {
                double balance = getSolanaBalance(walletAddress);
                player.sendSystemMessage(Component.literal("§5Seu saldo de SOL é: §6" + balance + " SOL"));
            } catch (Exception e) {
                player.sendSystemMessage(Component.literal("§cErro na API: " + e.getMessage()));
                LOGGER.error("Erro ao verificar saldo Solana: " + e.getMessage());
            }
        });
    }

    public void createWallet(Player player) {
        String playerName = player.getName().getString();
        String walletAddress = getWalletFromDatabase(playerName);

        if (walletAddress != null) {
            player.sendSystemMessage(Component.literal("§cVocê já possui uma carteira registrada: §b" + walletAddress));
            return;
        }

        CompletableFuture.runAsync(() -> {
            try (Connection conn = databaseManager.getConnection()) {
                // Garante que o jogador existe na tabela 'jogadores'
                int jogadorId = -1;
                try (PreparedStatement stmt = conn.prepareStatement("INSERT IGNORE INTO jogadores (nome) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, playerName);
                    stmt.executeUpdate();
                }
                
                try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM jogadores WHERE nome = ?")) {
                    stmt.setString(1, playerName);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) jogadorId = rs.getInt("id");
                    }
                }

                if (jogadorId == -1) throw new Exception("Não foi possível registrar o jogador no banco.");

                String host = ConfigManager.DOCKER_HOST.get();
                String apiwebkey = ConfigManager.API_WEB_KEY.get();
                String basePath = ConfigManager.BASE_PATH.get();
                String solanaCmd = ConfigManager.SOLANA_COMMAND.get();

                String walletPath = String.format("%s/wallets/%s_wallet.json", basePath, playerName.replace(" ", "_").toLowerCase());
                
                String comandoGerar = String.format("%s keygen new --no-passphrase --outfile %s --force", solanaCmd, walletPath);
                LOGGER.info("Executando comando API: " + comandoGerar);
                
                String urlGerar = String.format("http://%s/consulta.php?apikey=%s&comando=%s", host, apiwebkey, URLEncoder.encode(comandoGerar, StandardCharsets.UTF_8));
                
                String responseGerar = executeHttpGet(urlGerar);
                JSONObject json = new JSONObject(responseGerar);
                
                if (!json.has("status") || !json.getString("status").equalsIgnoreCase("success")) {
                    throw new Exception("Resposta da API: " + responseGerar);
                }

                player.sendSystemMessage(Component.literal("§aCarteira solicitada com sucesso via API! Verifique em instantes."));
                LOGGER.info("Carteira solicitada via API para " + playerName);
                
            } catch (Exception e) {
                player.sendSystemMessage(Component.literal("§cErro ao criar carteira: " + e.getMessage()));
                LOGGER.error("Erro no createWallet para " + playerName + ": " + e.getMessage());
            }
        });
    }

    public void transferSolana(Player player, String recipient, double amount) {
        String senderWallet = getWalletFromDatabase(player.getName().getString());
        if (senderWallet == null) {
            player.sendSystemMessage(Component.literal("§cVocê não possui uma carteira registrada."));
            return;
        }

        player.sendSystemMessage(Component.literal("§6Iniciando transferência de " + amount + " SOL para " + recipient + "..."));
        // Implementação similar ao original usando executeHttpGet e o comando solana transfer
    }
}
