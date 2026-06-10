package com.SolanaDevMinecraft;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = SolanaForge.MODID)
public class ChestLockManager {
    private final DatabaseManager databaseManager;
    private static final Map<BlockPos, String> pendingUnlock = new HashMap<>();

    public ChestLockManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void lockChest(ServerPlayer player, BlockPos pos, String password) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO locked_chests (world, x, y, z, password) VALUES (?, ?, ?, ?, ?)")) {

                stmt.setString(1, player.level().dimension().location().toString());
                stmt.setDouble(2, pos.getX());
                stmt.setDouble(3, pos.getY());
                stmt.setDouble(4, pos.getZ());
                stmt.setString(5, password);

                stmt.executeUpdate();
                player.sendSystemMessage(Component.literal("§aBaú trancado com sucesso!"));

            } catch (SQLException e) {
                player.sendSystemMessage(Component.literal("§cErro ao trancar baú: " + e.getMessage()));
            }
        });
    }

    public void unlockChest(ServerPlayer player, BlockPos pos, String password) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT password FROM locked_chests WHERE world = ? AND x = ? AND y = ? AND z = ?")) {

                stmt.setString(1, player.level().dimension().location().toString());
                stmt.setDouble(2, pos.getX());
                stmt.setDouble(3, pos.getY());
                stmt.setDouble(4, pos.getZ());

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    if (rs.getString("password").equals(password)) {
                        try (PreparedStatement delStmt = conn.prepareStatement(
                                "DELETE FROM locked_chests WHERE world = ? AND x = ? AND y = ? AND z = ?")) {
                            delStmt.setString(1, player.level().dimension().location().toString());
                            delStmt.setDouble(2, pos.getX());
                            delStmt.setDouble(3, pos.getY());
                            delStmt.setDouble(4, pos.getZ());
                            delStmt.executeUpdate();
                            player.sendSystemMessage(Component.literal("§aBaú destrancado com sucesso!"));
                        }
                    } else {
                        player.sendSystemMessage(Component.literal("§cSenha incorreta!"));
                    }
                } else {
                    player.sendSystemMessage(Component.literal("§cEste baú não está trancado."));
                }
            } catch (SQLException e) {
                player.sendSystemMessage(Component.literal("§cErro ao destrancar baú."));
            }
        });
    }

    @SubscribeEvent
    public static void onChestOpen(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) return;

        BlockPos pos = event.getPos();
        BlockEntity be = event.getLevel().getBlockEntity(pos);

        if (be instanceof ChestBlockEntity) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            // Aqui precisariamos de uma forma rápida de verificar se o baú está trancado
            // Sem consultar o banco sincronamente. Em um sistema real, usaríamos um cache.
        }
    }
}
