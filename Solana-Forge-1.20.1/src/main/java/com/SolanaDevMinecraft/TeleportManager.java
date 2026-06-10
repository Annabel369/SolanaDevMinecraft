package com.SolanaDevMinecraft;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {
    private final Map<UUID, LastLocation> lastLocations = new HashMap<>();
    private final Map<UUID, UUID> tpaRequests = new HashMap<>();

    public void saveLastLocation(ServerPlayer player) {
        lastLocations.put(player.getUUID(), new LastLocation(player.serverLevel(), player.position(), player.getYRot(), player.getXRot()));
    }

    public void teleportBack(ServerPlayer player) {
        LastLocation loc = lastLocations.get(player.getUUID());
        if (loc != null) {
            player.teleportTo(loc.level, loc.pos.x, loc.pos.y, loc.pos.z, loc.yRot, loc.xRot);
            player.sendSystemMessage(Component.literal("§6Você voltou para sua última posição!"));
        } else {
            player.sendSystemMessage(Component.literal("§cNenhuma posição anterior encontrada."));
        }
    }

    public void sendTpaRequest(ServerPlayer sender, ServerPlayer target) {
        tpaRequests.put(target.getUUID(), sender.getUUID());
        sender.sendSystemMessage(Component.literal("§6Pedido de teleporte enviado para §e" + target.getName().getString() + "§6."));
        target.sendSystemMessage(Component.literal("§e" + sender.getName().getString() + " §6deseja se teleportar até você! Use §a/tpaceitar §6para aceitar ou §c/tprecusar §6para negar."));
    }

    public void acceptTpa(ServerPlayer target) {
        UUID requesterUUID = tpaRequests.remove(target.getUUID());
        if (requesterUUID != null) {
            ServerPlayer requester = target.getServer().getPlayerList().getPlayer(requesterUUID);
            if (requester != null) {
                saveLastLocation(requester);
                requester.teleportTo(target.serverLevel(), target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
                requester.sendSystemMessage(Component.literal("§aTeleporte aceito! Você foi movido até §e" + target.getName().getString() + "§a."));
                target.sendSystemMessage(Component.literal("§aTeleporte realizado com sucesso."));
            }
        } else {
            target.sendSystemMessage(Component.literal("§cNenhum pedido de teleporte pendente."));
        }
    }

    public void denyTpa(ServerPlayer target) {
        UUID requesterUUID = tpaRequests.remove(target.getUUID());
        if (requesterUUID != null) {
            ServerPlayer requester = target.getServer().getPlayerList().getPlayer(requesterUUID);
            if (requester != null) {
                requester.sendSystemMessage(Component.literal("§cPedido de teleporte recusado por §e" + target.getName().getString() + "§c."));
            }
            target.sendSystemMessage(Component.literal("§cVocê recusou o pedido de teleporte."));
        } else {
            target.sendSystemMessage(Component.literal("§cNenhum pedido de teleporte pendente."));
        }
    }

    private static class LastLocation {
        final ServerLevel level;
        final Vec3 pos;
        final float yRot;
        final float xRot;

        LastLocation(ServerLevel level, Vec3 pos, float yRot, float xRot) {
            this.level = level;
            this.pos = pos;
            this.yRot = yRot;
            this.xRot = xRot;
        }
    }
}
