package com.SolanaDevMinecraft;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SolanaForge.MODID)
public class PlayerJoinHandler {

    private static StoreManager storeManager;

    public static void init(StoreManager manager) {
        storeManager = manager;
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        
        if (storeManager != null) {
            storeManager.giveStarterBalance(player);
        }

        // 🌈 Cria a mensagem colorida "Flolia 🍁" usando o sistema nativo do Minecraft (Forge)
        MutableComponent floliaLogo = Component.empty()
            .append(Component.literal("F").withStyle(style -> style.withColor(TextColor.parseColor("#FF0000")))) // Vermelho
            .append(Component.literal("l").withStyle(style -> style.withColor(TextColor.parseColor("#FFA500")))) // Laranja
            .append(Component.literal("o").withStyle(style -> style.withColor(TextColor.parseColor("#FFFF00")))) // Amarelo
            .append(Component.literal("l").withStyle(style -> style.withColor(TextColor.parseColor("#008000")))) // Verde
            .append(Component.literal("i").withStyle(style -> style.withColor(TextColor.parseColor("#00FFFF")))) // Aqua
            .append(Component.literal("a").withStyle(style -> style.withColor(TextColor.parseColor("#EE82EE")))) // Violeta
            .append(Component.literal(" 🍁").withStyle(style -> style.withColor(TextColor.parseColor("#FFA500")))); // Laranja (🍁)

        Component welcomeMessage = Component.literal("§7Bem-vindo ao ")
            .append(floliaLogo)
            .append(Component.literal("§7! Você recebeu §e500 PandaCoins §7de bônus inicial."));

        player.sendSystemMessage(welcomeMessage);
    }
}
