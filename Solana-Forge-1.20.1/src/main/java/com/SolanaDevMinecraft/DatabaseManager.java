package com.SolanaDevMinecraft;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger("SolanaForge");
    private final String url;
    private final String user;
    private final String password;

    public DatabaseManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        
        try {
            // Load the driver explicitly to avoid issues in some environments
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.severe("Driver MySQL não encontrado!");
        }
    }

    public Connection getConnection() throws SQLException {
        java.util.Properties props = new java.util.Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("useSSL", ConfigManager.DB_USE_SSL.get().toString());
        props.setProperty("verifyServerCertificate", ConfigManager.DB_VERIFY_CERT.get().toString());
        props.setProperty("autoReconnect", "true");
        props.setProperty("allowPublicKeyRetrieval", "true"); // Added for convenience
        
        return DriverManager.getConnection(url, props);
    }

    public void setupTables() {
        try (Connection conn = getConnection(); java.sql.Statement stmt = conn.createStatement()) {
            // Tabela de Economia
            stmt.execute("CREATE TABLE IF NOT EXISTS banco (" +
                    "jogador VARCHAR(255) PRIMARY KEY, " +
                    "saldo INT DEFAULT 0)");

            // Tabela de Jogadores (para o sistema de carteiras)
            stmt.execute("CREATE TABLE IF NOT EXISTS jogadores (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nome VARCHAR(255) UNIQUE)");

            // Tabela de Carteiras
            stmt.execute("CREATE TABLE IF NOT EXISTS carteiras (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "jogador_id INT, " +
                    "endereco VARCHAR(255), " +
                    "FOREIGN KEY (jogador_id) REFERENCES jogadores(id))");

            // Tabela de Homes
            stmt.execute("CREATE TABLE IF NOT EXISTS homes (" +
                    "player_uuid VARCHAR(36), " +
                    "home_name VARCHAR(255), " +
                    "world VARCHAR(255), " +
                    "x DOUBLE, y DOUBLE, z DOUBLE, " +
                    "PRIMARY KEY (player_uuid, home_name))");

            // Tabela de Baús Trancados
            stmt.execute("CREATE TABLE IF NOT EXISTS locked_chests (" +
                    "world VARCHAR(255), " +
                    "x DOUBLE, y DOUBLE, z DOUBLE, " +
                    "password VARCHAR(255), " +
                    "PRIMARY KEY (world, x, y, z))");

            LOGGER.info("Tabelas do banco de dados verificadas/criadas com sucesso!");
        } catch (SQLException e) {
            LOGGER.severe("Erro ao configurar tabelas: " + e.getMessage());
        }
    }
}
