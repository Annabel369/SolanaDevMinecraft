package com.SolanaDevMinecraft;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.Registries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class HomeManager {
    private final DatabaseManager databaseManager;

    public HomeManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void setHome(ServerPlayer player, String homeName) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO homes (player_uuid, home_name, world, x, y, z) VALUES (?, ?, ?, ?, ?, ?) " +
                                 "ON DUPLICATE KEY UPDATE world = VALUES(world), x = VALUES(x), y = VALUES(y), z = VALUES(z)")) {

                stmt.setString(1, player.getUUID().toString());
                stmt.setString(2, homeName);
                stmt.setString(3, player.level().dimension().location().toString());
                stmt.setDouble(4, player.getX());
                stmt.setDouble(5, player.getY());
                stmt.setDouble(6, player.getZ());

                stmt.executeUpdate();
                player.sendSystemMessage(Component.literal("§aCasa '§e" + homeName + "§a' definida com sucesso!"));

            } catch (SQLException e) {
                player.sendSystemMessage(Component.literal("§cErro ao salvar casa no banco de dados."));
                e.printStackTrace();
            }
        });
    }

    public void teleportToHome(ServerPlayer player, String homeName) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT world, x, y, z FROM homes WHERE player_uuid = ? AND home_name = ?")) {

                stmt.setString(1, player.getUUID().toString());
                stmt.setString(2, homeName);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String worldName = rs.getString("world");
                    double x = rs.getDouble("x");
                    double y = rs.getDouble("y");
                    double z = rs.getDouble("z");

                    player.getServer().execute(() -> {
                        ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(worldName));
                        ServerLevel level = player.getServer().getLevel(levelKey);
                        if (level != null) {
                            player.teleportTo(level, x, y, z, player.getYRot(), player.getXRot());
                            player.sendSystemMessage(Component.literal("§6Bem-vindo à sua casa '§e" + homeName + "§6'!"));
                        } else {
                            player.sendSystemMessage(Component.literal("§cMundo '§e" + worldName + "§c' não encontrado!"));
                        }
                    });
                } else {
                    player.sendSystemMessage(Component.literal("§cCasa '§e" + homeName + "§c' não encontrada!"));
                }

            } catch (SQLException e) {
                player.sendSystemMessage(Component.literal("§cErro ao consultar casa no banco de dados."));
                e.printStackTrace();
            }
        });
    }
}
