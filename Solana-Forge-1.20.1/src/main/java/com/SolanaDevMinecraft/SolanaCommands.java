package com.SolanaDevMinecraft;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SolanaCommands {
    private final SolanaManager solanaManager;
    private final StoreManager storeManager;
    private final HomeManager homeManager;
    private final TeleportManager teleportManager;
    private final ChestLockManager chestLockManager;

    public SolanaCommands(SolanaManager solanaManager, StoreManager storeManager, HomeManager homeManager, TeleportManager teleportManager, ChestLockManager chestLockManager) {
        this.solanaManager = solanaManager;
        this.storeManager = storeManager;
        this.homeManager = homeManager;
        this.teleportManager = teleportManager;
        this.chestLockManager = chestLockManager;
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Economy & Solana
        dispatcher.register(Commands.literal("saldo")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    int balance = storeManager.getBalance(player.getName().getString());
                    player.sendSystemMessage(Component.literal("§6Seu saldo é: §e$" + balance));
                    return 1;
                }));

        dispatcher.register(Commands.literal("solsaldo")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    solanaManager.handleSolBalance(player);
                    return 1;
                }));

        dispatcher.register(Commands.literal("criarcarteira")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    solanaManager.createWallet(player);
                    return 1;
                }));

        // Store
        dispatcher.register(Commands.literal("comprar_maca")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    storeManager.buyEnchantedApple(player);
                    return 1;
                }));

        dispatcher.register(Commands.literal("comprar_esmeralda")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    storeManager.buyEmerald(player);
                    return 1;
                }));

        dispatcher.register(Commands.literal("comprar_reliquia_nether")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    storeManager.buyNetherRelic(player);
                    return 1;
                }));

        // Utility
        dispatcher.register(Commands.literal("sethome")
                .then(Commands.argument("nome", StringArgumentType.string())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            String nome = StringArgumentType.getString(context, "nome");
                            homeManager.setHome(player, nome);
                            return 1;
                        }))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    homeManager.setHome(player, "default");
                    return 1;
                }));

        dispatcher.register(Commands.literal("home")
                .then(Commands.argument("nome", StringArgumentType.string())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            String nome = StringArgumentType.getString(context, "nome");
                            teleportManager.saveLastLocation(player);
                            homeManager.teleportToHome(player, nome);
                            return 1;
                        }))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    teleportManager.saveLastLocation(player);
                    homeManager.teleportToHome(player, "default");
                    return 1;
                }));

        dispatcher.register(Commands.literal("back")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    teleportManager.teleportBack(player);
                    return 1;
                }));

        dispatcher.register(Commands.literal("tpa")
                .then(Commands.argument("jogador", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer sender = context.getSource().getPlayerOrException();
                            ServerPlayer target = EntityArgument.getPlayer(context, "jogador");
                            teleportManager.sendTpaRequest(sender, target);
                            return 1;
                        })));

        dispatcher.register(Commands.literal("tpaceitar")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    teleportManager.acceptTpa(player);
                    return 1;
                }));

        dispatcher.register(Commands.literal("tprecusar")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    teleportManager.denyTpa(player);
                    return 1;
                }));

        // Chest Lock
        dispatcher.register(Commands.literal("trancarbau")
                .then(Commands.argument("senha", StringArgumentType.string())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            String senha = StringArgumentType.getString(context, "senha");
                            // Simplificado: tranca o bloco que o jogador está olhando
                            // chestLockManager.lockChest(player, pos, senha);
                            return 1;
                        })));
    }
}

