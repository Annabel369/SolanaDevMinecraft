package com.SolanaDevMinecraft;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StoreManager {
    private final DatabaseManager databaseManager;

    public StoreManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void giveStarterBalance(Player player) {
        String playerName = player.getName().getString().toLowerCase();
        try (Connection conn = databaseManager.getConnection()) {
            // Verifica se o jogador já existe
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT saldo FROM banco WHERE jogador = ?")) {
                checkStmt.setString(1, playerName);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        // Se não existe, insere com 500 PandaCoins
                        try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO banco (jogador, saldo) VALUES (?, ?)")) {
                            insertStmt.setString(1, playerName);
                            insertStmt.setInt(2, 500);
                            insertStmt.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getBalance(String playerName) {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT saldo FROM banco WHERE jogador = ?")) {
            stmt.setString(1, playerName.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("saldo");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean processPurchase(Player player, int price) {
        String playerName = player.getName().getString().toLowerCase();
        try (Connection conn = databaseManager.getConnection()) {
            int currentBalance = getBalance(playerName);
            if (currentBalance >= price) {
                try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE banco SET saldo = saldo - ? WHERE jogador = ?")) {
                    updateStmt.setInt(1, price);
                    updateStmt.setString(2, playerName);
                    updateStmt.executeUpdate();
                    return true;
                }
            } else {
                player.sendSystemMessage(Component.literal("§cSaldo insuficiente! Faltam §e" + (price - currentBalance) + "§c moedas."));
                return false;
            }
        } catch (SQLException e) {
            player.sendSystemMessage(Component.literal("§cErro ao acessar o banco de dados."));
            e.printStackTrace();
            return false;
        }
    }

    public void buyEnchantedApple(Player player) {
        int price = ConfigManager.PRICE_APPLE.get();
        if (processPurchase(player, price)) {
            player.getInventory().add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
            player.sendSystemMessage(Component.literal("§6🍎 Você comprou uma Maçã Encantada por §e$" + price + "§6!"));
        }
    }

    public void buyEmerald(Player player) {
        int price = ConfigManager.PRICE_EMERALD.get();
        if (processPurchase(player, price)) {
            player.getInventory().add(new ItemStack(Items.EMERALD));
            player.sendSystemMessage(Component.literal("§a💎 Você comprou uma Esmeralda por §e$" + price + "§a!"));
        }
    }

    public void buyNetherRelic(Player player) {
        int price = ConfigManager.PRICE_NETHER_RELIC.get();
        if (processPurchase(player, price)) {
            player.getInventory().add(new ItemStack(ItemInit.NETHER_RELIC.get()));
            player.sendSystemMessage(Component.literal("§6🔥 Você comprou a Relíquia do Nether por §e$" + price + "§6!"));
        }
    }

    public void buyThorAxe(Player player) {
        int price = ConfigManager.PRICE_THOR_AXE.get();
        if (processPurchase(player, price)) {
            player.getInventory().add(new ItemStack(ItemInit.THOR_AXE.get()));
            player.sendSystemMessage(Component.literal("§b⚡ Você comprou o Machado de Thor por §e$" + price + "§b!"));
        }
    }
}
