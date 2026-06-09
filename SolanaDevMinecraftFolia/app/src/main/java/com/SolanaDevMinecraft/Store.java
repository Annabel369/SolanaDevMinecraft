package com.SolanaDevMinecraft;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionData;
import org.bukkit.inventory.meta.PotionMeta; // 🔹 Correto para poções
import org.bukkit.inventory.meta.BlockStateMeta; // 🔹 Necessário para Shulker Box

import org.bukkit.enchantments.Enchantment; // 🔹 Correto para encantamentos
import org.bukkit.inventory.meta.EnchantmentStorageMeta; // 🔹 Necessário para livros encantados
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import java.util.Arrays;
import java.sql.SQLException;



import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Sound;
import org.bukkit.Particle;
import java.util.concurrent.CompletableFuture;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerToggleFlightEvent;


public class Store {
    private final Connection connection;
    private final FileConfiguration config;
    private final JavaPlugin plugin; // 🔹 Corrigido: Agora 'plugin' é final e corretamente inicializado.

    // 🔹 Construtor correto que inicializa 'config', 'connection' e 'plugin'
    public Store(JavaPlugin plugin, FileConfiguration config, Connection connection) {
        this.plugin = plugin;   // 🔹 Corrigido: Agora 'plugin' recebe a instância correta!
        this.config = config;
        this.connection = connection;
    }


    @SuppressWarnings("deprecation")
    public String getPlayerLanguage(Player player) {
        String locale = player.getLocale(); // Obtém o idioma do jogador como String
        List<String> supportedLanguages = config.getStringList("language.supported"); // Obtém a lista de idiomas do config.yml

        return supportedLanguages.contains(locale) ? locale : config.getString("language.default", "pt-BR");
    }

    // 📌 Método genérico para verificar saldo e processar compras
    private boolean processPurchase(Player player, int price) {
    try (PreparedStatement stmt = connection.prepareStatement("SELECT saldo FROM banco WHERE jogador = ?")) {
        stmt.setString(1, player.getName());

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int saldo = rs.getInt("saldo");
                if (saldo >= price) {
                    try (PreparedStatement updateStmt = connection.prepareStatement("UPDATE banco SET saldo = saldo - ? WHERE jogador = ?")) {
                        updateStmt.setInt(1, price);
                        updateStmt.setString(2, player.getName());
                        updateStmt.executeUpdate();
                    }
                    return true;
                } else {
                    int falta = price - saldo;
                    String lang = getPlayerLanguage(player);
                    player.sendMessage(lang.equals("pt-BR") ?
                        Component.text("💰 Saldo insuficiente para realizar a compra. Falta: ", NamedTextColor.RED)
                        .append(Component.text(falta, NamedTextColor.YELLOW)) :
                        Component.text("💰 Insufficient balance to make the purchase. Missing: ", NamedTextColor.RED)
                        .append(Component.text(falta, NamedTextColor.YELLOW)));
                    return false;
                }
            }
        }
    } catch (Exception e) {
        player.sendMessage(Component.text("⚠ Erro ao acessar o banco de dados.", NamedTextColor.RED));
        e.printStackTrace();
        return false;
    }

    return false;
    }


    public void venderEscadas(Player jogador) {
    int quantidade = 33; // Quantidade fixa de escadas vendidas
    int precoPorEscada = config.getInt("store.self.stairs", 10); // Obtém o preço do config.yml
    int total = precoPorEscada * quantidade;

    // Lista de todos os tipos de escadas no Minecraft
    Material[] escadas = {
    Material.OAK_STAIRS, Material.SPRUCE_STAIRS, Material.BIRCH_STAIRS, Material.JUNGLE_STAIRS,
    Material.ACACIA_STAIRS, Material.DARK_OAK_STAIRS, Material.MANGROVE_STAIRS, Material.CHERRY_STAIRS,
    Material.BAMBOO_STAIRS, Material.CRIMSON_STAIRS, Material.WARPED_STAIRS, Material.BAMBOO_MOSAIC_STAIRS,
    Material.COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS, Material.STONE_STAIRS,
    Material.GRANITE_STAIRS, Material.POLISHED_GRANITE_STAIRS, Material.DIORITE_STAIRS,
    Material.POLISHED_DIORITE_STAIRS, Material.ANDESITE_STAIRS, Material.POLISHED_ANDESITE_STAIRS,
    Material.BLACKSTONE_STAIRS, Material.POLISHED_BLACKSTONE_STAIRS, Material.POLISHED_BLACKSTONE_BRICK_STAIRS,
    Material.NETHER_BRICK_STAIRS, Material.RED_NETHER_BRICK_STAIRS, Material.END_STONE_BRICK_STAIRS,
    Material.PURPUR_STAIRS, Material.QUARTZ_STAIRS, Material.SMOOTH_QUARTZ_STAIRS,
    Material.PRISMARINE_STAIRS, Material.PRISMARINE_BRICK_STAIRS, Material.DARK_PRISMARINE_STAIRS,
    Material.SANDSTONE_STAIRS, Material.SMOOTH_SANDSTONE_STAIRS, Material.RED_SANDSTONE_STAIRS,
    Material.SMOOTH_RED_SANDSTONE_STAIRS, Material.BRICK_STAIRS, Material.MUD_BRICK_STAIRS,
    Material.LADDER // Escada de mão
};

    boolean temEscadas = false;

    for (Material tipoEscada : escadas) {
        ItemStack escada = new ItemStack(tipoEscada, quantidade);
        if (jogador.getInventory().containsAtLeast(escada, quantidade)) {
            // Remove as escadas do inventário
            jogador.getInventory().removeItem(escada);
            temEscadas = true;
        }
    }

    if (temEscadas) {
        // Converte o dinheiro para Solana (opcional)
        int taxaConversao = config.getInt("store.value_of_in_game_currency", 1000);
        double solanaRecebida = (double) total / taxaConversao;

        // Adiciona dinheiro ao banco do jogador (moeda do jogo)
        boolean vendaEfetuada = processPurchase(jogador, -total); // Usa valor negativo para adicionar dinheiro

        if (vendaEfetuada) {
            jogador.sendMessage(Component.text("💰 Você vendeu 33 escadas por $" + total + " (" + solanaRecebida + " SOL)!").color(NamedTextColor.GREEN));
        } else {
            jogador.sendMessage(Component.text("⚠ Erro ao vender escadas. Verifique seu saldo no banco.").color(NamedTextColor.RED));
        }
    } else {
        jogador.sendMessage(Component.text("❌ Você não tem 33 escadas para vender!").color(NamedTextColor.RED));
    }
}

    public void buyNetherRelic(Player player) {
    // 🔹 Obtém o preço da relíquia do config.yml, com fallback de 25000
    int price = config.getInt("store.price.nether_relic", 25000);

    // 🔹 Verifica se o jogador tem saldo suficiente e processa a compra
    if (!processPurchase(player, price)) return;

    // 🔹 Cria o capacete especial da Relíquia do Nether
    ItemStack netherRelic = new ItemStack(Material.GOLDEN_HELMET);
    ItemMeta meta = netherRelic.getItemMeta();
    
    if (meta != null) {
        meta.setUnbreakable(true); // 🔥 Torna o capacete indestrutível
        meta.displayName(Component.text("Relíquia ELmo Arcanjo Uriel").color(NamedTextColor.GOLD)); // 🔥 Define nome personalizado
        
        // 🔹 Adiciona encantamentos essenciais
        meta.addEnchant(Enchantment.FIRE_PROTECTION, 4, true); // 🔥 Proteção contra fogo máxima
        meta.addEnchant(Enchantment.MENDING, 1, true); // 🔥 Reparação automática
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);  // 🔥 Resistência extra
        
        netherRelic.setItemMeta(meta);
    }

    // 🔹 Entrega o item dentro da região global para evitar problemas no Folia
    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        player.getInventory().addItem(netherRelic);
    });

    // 🔹 Define mensagem conforme o idioma do jogador
    String lang = getPlayerLanguage(player);
    String message = switch (lang) {
        case "pt-BR" -> "🔥 Você comprou a Relíquia do Nether por $" + price + "!";
        case "es-ES" -> "🔥 ¡Has comprado la Reliquia del Nether por $" + price + "!";
        default -> "🔥 You bought the Nether Relic for $" + price + "!";
    };

    // 🔹 Envia mensagem ao jogador
    player.sendMessage(Component.text(message).color(NamedTextColor.GOLD));
}

// 🔥 Listener para conceder imunidade ao fogo ao usar a Relíquia do Nether
public class NetherRelicListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        applyFireResistance(player);
    }

    @EventHandler
    public void onEquipArmor(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        applyFireResistance(player);
    }

    private void applyFireResistance(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();

        if (helmet != null && helmet.hasItemMeta() &&
            helmet.getItemMeta().displayName().equals(Component.text("Relíquia do Nether").color(NamedTextColor.GOLD))) {
            
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));
        } else {
            player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
        }
    }
}






public class TreeDebuggerAxeListener implements Listener {
    @EventHandler
    public void onTreeChop(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // 🔹 Verifica se o jogador está usando o Machado Debugger
        if (item.hasItemMeta() && item.getItemMeta().displayName().equals(Component.text("Machado Debugger").color(NamedTextColor.GOLD))) {
            Block block = event.getBlock();

            // 🔹 Define os tipos de madeira que podem ser quebrados
            Set<Material> logTypes = new HashSet<>(Set.of(
                Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG,
                Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
                Material.MANGROVE_LOG, Material.CHERRY_LOG
            ));

            // 🔹 Se o bloco quebrado for um tronco, quebra toda a árvore
            if (logTypes.contains(block.getType())) {
                breakWholeTree(block);
            }
        }
    }

    private void breakWholeTree(Block block) {
        Set<Block> blocksToBreak = new HashSet<>();
        collectTreeBlocks(block, blocksToBreak);

        for (Block treeBlock : blocksToBreak) {
            treeBlock.breakNaturally();
        }
    }

    private void collectTreeBlocks(Block block, Set<Block> blocks) {
        if (!blocks.contains(block)) {
            blocks.add(block);
            for (Block relative : List.of(block.getRelative(0, 1, 0), block.getRelative(0, -1, 0))) {
                if (!blocks.contains(relative)) collectTreeBlocks(relative, blocks);
            }
        }
    }
}

public void buyTreeDebuggerAxe(Player player) {
    int price = config.getInt("store.price.tree_debugger", 15000);
    if (!processPurchase(player, price)) return;

    ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
    ItemMeta meta = axe.getItemMeta();
    if (meta != null) {
        meta.setUnbreakable(true);
        meta.displayName(Component.text("Machado Debugger").color(NamedTextColor.GOLD));
        meta.addEnchant(Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft("efficiency")), 5, true);
        meta.addEnchant(Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft("fortune")), 3, true);


        axe.setItemMeta(meta);
    }

    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        player.getInventory().addItem(axe);
    });

    String lang = getPlayerLanguage(player);
    String message = switch (lang) {
        case "pt-BR" -> "🪓 Você comprou o Machado Debugger por $" + price + "!";
        case "es-ES" -> "🪓 ¡Has comprado el Hacha Debugger por $" + price + "!";
        default -> "🪓 You bought the Tree Debugger Axe for $" + price + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.GOLD));
}

public void buyBootRelic(Player player) {
    int price = config.getInt("store.price.boot_relic", 40000);

    if (!processPurchase(player, price)) return;

    ItemStack bootRelic = new ItemStack(Material.NETHERITE_BOOTS);
    ItemMeta meta = bootRelic.getItemMeta();

    if (meta != null) {
        meta.setUnbreakable(true);
        meta.displayName(Component.text("👢 Relíquia Meow Cat das Botas Celestiais").color(NamedTextColor.LIGHT_PURPLE));
        meta.addEnchant(Enchantment.DEPTH_STRIDER, 3, true);   // Passos profundos
        meta.addEnchant(Enchantment.MENDING, 1, true);         // Reparação
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);      // Durabilidade
        //meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);   // Maldição de vínculo

        bootRelic.setItemMeta(meta);
    }

    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        player.getInventory().addItem(bootRelic);
    });

    String message = switch (getPlayerLanguage(player)) {
        case "pt-BR" -> "👢 Você comprou a Relíquia Meow Cat das Botas Celestiais por $" + price + "!";
        case "es-ES" -> "👢 ¡Has comprado las Botas Reliquia Meow Cat Celestiales por $" + price + "!";
        default -> "👢 You bought the Meow Cat Celestial Boots Relic for $" + price + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.LIGHT_PURPLE));
}

public void buyShulkerKit(Player player) {
    int price = config.getInt("store.price.shulker_kit", 50000);

    if (!processPurchase(player, price)) return;

    // Lista com todas as cores de shulker box
    Material[] shulkerColors = {
        Material.WHITE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX,
        Material.LIGHT_BLUE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX, Material.LIME_SHULKER_BOX,
        Material.PINK_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX,
        Material.CYAN_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.BLUE_SHULKER_BOX,
        Material.BROWN_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.RED_SHULKER_BOX,
        Material.BLACK_SHULKER_BOX
    };

    for (Material shulker : shulkerColors) {
        ItemStack item = new ItemStack(shulker);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("📦 Shulker Colorida").color(NamedTextColor.GOLD));
            item.setItemMeta(meta);
        }
        player.getInventory().addItem(item);
    }

    String message = switch (getPlayerLanguage(player)) {
        case "pt-BR" -> "📦 Você comprou o Kit Shulker Colorida por $" + price + "!";
        case "es-ES" -> "📦 ¡Has comprado el Kit Shulker Colorida por $" + price + "!";
        default -> "📦 You bought the Colorful Shulker Kit for $" + price + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.GOLD));
}

public void buyThorAxe(Player player) {
    int price = config.getInt("store.price.thor_axe", 60000);

    if (!processPurchase(player, price)) return;

    ItemStack thorAxe = new ItemStack(Material.NETHERITE_AXE);
    ItemMeta meta = thorAxe.getItemMeta();

    if (meta != null) {
        meta.setUnbreakable(true);
        meta.displayName(Component.text("⚡ Machado de Thor: Stormbreaker").color(NamedTextColor.AQUA));

        meta.addEnchant(Enchantment.MENDING, 1, true);         // Reparação
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);      // Durabilidade
        meta.addEnchant(Enchantment.KNOCKBACK, 2, true);       // Repulsão
        //meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);   // Maldição de vínculo

        thorAxe.setItemMeta(meta);
    }

    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        player.getInventory().addItem(thorAxe);
    });

    String message = switch (getPlayerLanguage(player)) {
        case "pt-BR" -> "⚡ Você empunha agora o Machado de Thor por $" + price + "!";
        case "es-ES" -> "⚡ ¡Has adquirido el Hacha de Thor por $" + price + "!";
        default -> "⚡ You now wield Thor's Axe for $" + price + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.AQUA));
}



public void buyWingRelic(Player player) {
    // 🔹 Obtém o preço da relíquia das asas no config.yml, com fallback de 50000
    int price = config.getInt("store.price.wing_relic", 50000);

    // 🔹 Verifica se o jogador tem saldo suficiente e processa a compra
    if (!processPurchase(player, price)) return;

    // 🔹 Cria o item da Relíquia das Asas
    ItemStack wingRelic = new ItemStack(Material.ELYTRA);
    ItemMeta meta = wingRelic.getItemMeta();

    if (meta != null) {
        meta.setUnbreakable(true); // 🔥 Torna indestrutível
        meta.displayName(Component.text("🚀 Relíquia Amauris gênero de borboletas").color(NamedTextColor.GOLD)); // 🔥 Define nome personalizado

        // 🔹 Adiciona encantamentos essenciais
        meta.addEnchant(Enchantment.MENDING, 1, true); // 🔥 Reparação automática
        meta.addEnchant(Enchantment.UNBREAKING, 3, true); // 🔥 Resistência extra
        meta.addEnchant(Enchantment.BINDING_CURSE, 1, true); // 🔥 Maldição de vínculo (não pode ser removida)

        wingRelic.setItemMeta(meta);
    }

    // 🔹 Entrega o item dentro da região global para evitar problemas no Folia
    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        player.getInventory().addItem(wingRelic);
    });

    // 🔹 Define mensagem conforme o idioma do jogador
    String message = switch (getPlayerLanguage(player)) {
        case "pt-BR" -> "🚀 Você comprou a Asa Relíquia Amauris gênero de borboletas por $" + price + "!";
        case "es-ES" -> "🚀 ¡Has comprado las Alas Reliquia Amauris género de borboletas por $" + price + "!";
        default -> "🚀 You bought the Amauris Wing Relic for $" + price + "!";
    };

    // 🔹 Envia mensagem ao jogador
    player.sendMessage(Component.text(message).color(NamedTextColor.GOLD));
}



public class WingRelicListener implements Listener {
    @EventHandler
    public void onPlayerFly(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        ItemStack chestplate = player.getInventory().getChestplate();

        // 🔹 Verifica se o jogador está usando a Asa Relíquia
        if (chestplate != null && chestplate.hasItemMeta() &&
            chestplate.getItemMeta().displayName().equals(Component.text("🪽 Asa Relíquia do Nether").color(NamedTextColor.GOLD))) {
            
            // 🔥 Dá efeito de levitação para imitar voo sem foguetes
            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 60, 1)); 
        }
    }
}




    // 📌 Compra de Maçã Encantada
    public void buyEnchantedApple(Player player) {
    System.out.println("DEBUG (buyEnchantedApple): Iniciado para " + player.getName());

    int price = config.getInt("store.price.apple", 5000); // 🔹 Fallback para evitar erro caso config falhe
    System.out.println("DEBUG (buyEnchantedApple): Preço da maçã lido: " + price);

    String lang = getPlayerLanguage(player);
    System.out.println("DEBUG (buyEnchantedApple): Idioma identificado: " + lang);

    // Envia uma mensagem inicial ao jogador
    player.sendMessage(Component.text("🍳. Lang= " + lang, NamedTextColor.YELLOW));

    System.out.println("DEBUG (buyEnchantedApple): Chamando processPurchase...");
    if (!processPurchase(player, price)) {
        System.out.println("DEBUG (buyEnchantedApple): Compra falhou. Interrompendo processo.");
        return;
    }

    System.out.println("DEBUG (buyEnchantedApple): Compra processada com sucesso. Adicionando item...");

    // 🔹 Adiciona a maçã encantada ao inventário do jogador dentro da região global
    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        player.getInventory().addItem(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1));
    });

    System.out.println("DEBUG (buyEnchantedApple): Item adicionado. Ajustando saldo...");
    //ajustarSaldo(player, "take", price);

    // 🔹 Mensagem para o jogador
    String message = switch (lang) {
        case "pt-BR" -> "🍎 Você comprou uma Maçã Encantada por $" + price + "!";
        case "es-ES" -> "🍎 ¡Has comprado una Manzana Encantada por $" + price + "!";
        default -> "🍎 You bought an Enchanted Apple for $" + price + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.GOLD));
    System.out.println("DEBUG (buyEnchantedApple): Mensagem final enviada.");
    System.out.println("DEBUG (buyEnchantedApple): Finalizado.");
}



    // 📌 Compra de Esmeralda
    public void buyEmerald(Player player) {
    int price = config.getInt("store.price.emerald", 1000); // 🔹 Obtém preço do config.yml, com fallback de 1000
    if (!processPurchase(player, price)) return; // 🔹 Interrompe se a compra falhar

    // 🔹 Adiciona a esmeralda ao inventário do jogador dentro da região global
    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        player.getInventory().addItem(new ItemStack(Material.EMERALD, 1));
    });

    // 🔹 Mensagem para o jogador
    String lang = getPlayerLanguage(player);
    String message = switch (lang) {
        case "pt-BR" -> "💎 Você comprou uma Esmeralda por $" + price + "!";
        case "es-ES" -> "💎 ¡Has comprado una Esmeralda por $" + price + "!";
        default -> "💎 You bought an Emerald for $" + price + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.GOLD));
}



    // 📌 Compra de Bloco de Ouro

    public void buyGoldBlock(Player player) {
    int price = config.getInt("store.price.buyGoldBlock", 10000); // 🔹 Obtém preço do config.yml, com fallback de 10000
    if (!processPurchase(player, price)) return; // 🔹 Interrompe se a compra falhar

    // 🔹 Adiciona o bloco de ouro ao inventário do jogador dentro da região global
    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        player.getInventory().addItem(new ItemStack(Material.GOLD_BLOCK, 1));
    });

    // 🔹 Mensagem para o jogador
    String lang = getPlayerLanguage(player);
    String message = switch (lang) {
        case "pt-BR" -> "🏆 Você comprou um Bloco de Ouro por $" + price + "!";
        case "es-ES" -> "🏆 ¡Has comprado un Bloque de Oro por $" + price + "!";
        default -> "🏆 You bought a Gold Block for $" + price + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.GOLD));
}



    // 📌 Compra de Bloco de Diamante

    public void buyDiamondBlock(Player player) {
    int price = config.getInt("store.price.buyDiamondBlock", 20000); // 🔹 Obtém preço do config.yml, com fallback de 20000
    if (!processPurchase(player, price)) return; // 🔹 Interrompe se a compra falhar

    // 🔹 Adiciona o bloco de diamante ao inventário do jogador dentro da região global
    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        player.getInventory().addItem(new ItemStack(Material.DIAMOND_BLOCK, 1));
    });

    // 🔹 Mensagem para o jogador
    String lang = getPlayerLanguage(player);
    String message = switch (lang) {
        case "pt-BR" -> "💎 Você comprou um Bloco de Diamante por $" + price + "!";
        case "es-ES" -> "💎 ¡Has comprado un Bloque de Diamante por $" + price + "!";
        default -> "💎 You bought a Diamond Block for $" + price + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.GOLD));
}




    // 📌 Compra de Bloco de Esmeralda
    public void buyEmeraldBlock(Player player) {
    int price = config.getInt("store.price.buyEmeraldBlock", 50000); // 🔹 Obtém preço do config.yml, com fallback de 50000
    if (!processPurchase(player, price)) return; // 🔹 Interrompe se a compra falhar

    // 🔹 Adiciona o bloco de esmeralda ao inventário do jogador dentro da região global
    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        player.getInventory().addItem(new ItemStack(Material.EMERALD_BLOCK, 1));
    });

    // 🔹 Mensagem para o jogador
    String lang = getPlayerLanguage(player);
    String message = switch (lang) {
        case "pt-BR" -> "💎 Você comprou um Bloco de Esmeralda por $" + price + "!";
        case "es-ES" -> "💎 ¡Has comprado un Bloque de Esmeralda por $" + price + "!";
        default -> "💎 You bought an Emerald Block for $" + price + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.GOLD));
}



    // 📌 Compra de Bloco de Netherite
    public void buyNetheriteBlock(Player player) {
    int price = config.getInt("store.price.buyNetheriteBlock", 100000); // 🔹 Obtém preço do config.yml, com fallback de 100000
    if (!processPurchase(player, price)) return; // 🔹 Interrompe se a compra falhar

    // 🔹 Adiciona os itens ao inventário do jogador dentro da região global
    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        player.getInventory().addItem(new ItemStack(Material.ANCIENT_DEBRIS, 10));
        player.getInventory().addItem(new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1));
    });

    // 🔹 Mensagem para o jogador
    String lang = getPlayerLanguage(player);
    String message = switch (lang) {
        case "pt-BR" -> "💎 Você comprou um Bloco de Netherite por $" + price + "!";
        case "es-ES" -> "💎 ¡Has comprado un Bloque de Netherite por $" + price + "!";
        default -> "💎 You bought a Netherite Block for $" + price + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.GOLD));
}



    // 📌 Compra de Bloco de Ferro
    public void buyIronBlock(Player player) {
    int price = config.getInt("store.price.buyIronBlock", 5000); // 🔹 Obtém preço do config.yml, com fallback de 5000
    if (!processPurchase(player, price)) return; // 🔹 Interrompe se a compra falhar

    // 🔹 Adiciona o bloco de ferro ao inventário do jogador dentro da região global
    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        player.getInventory().addItem(new ItemStack(Material.IRON_BLOCK, 1));
    });

    // 🔹 Mensagem para o jogador
    String lang = getPlayerLanguage(player);
    String message = switch (lang) {
        case "pt-BR" -> "🏆 Você comprou um Bloco de Ferro por $" + price + "!";
        case "es-ES" -> "🏆 ¡Has comprado un Bloque de Hierro por $" + price + "!";
        default -> "🏆 You bought an Iron Block for $" + price + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.GOLD));
}



    // 📌 Compra de Bloco de Lapis
    public void buyLapisBlock(Player player) {
    int price = config.getInt("store.price.lapis", 1000); // 🔹 Obtém preço do config.yml, com fallback de 1000
    if (!processPurchase(player, price)) return; // 🔹 Interrompe se a compra falhar

    // 🔹 Adiciona o bloco de lápis-lazúli ao inventário do jogador dentro da região global
    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        player.getInventory().addItem(new ItemStack(Material.LAPIS_BLOCK, 1));
    });

    // 🔹 Mensagem para o jogador
    String lang = getPlayerLanguage(player);
    String message = switch (lang) {
        case "pt-BR" -> "💎 Você comprou um Bloco de Lápis-lazúli por $" + price + "!";
        case "es-ES" -> "💎 ¡Has comprado un Bloque de Lapislázuli por $" + price + "!";
        default -> "💎 You bought a Lapis Block for $" + price + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.GOLD));
}



    // 📌 Compra de Bloco de Redstone
    public void buyRedstoneBlock(Player player) {
    int price = config.getInt("store.price.redstone", 1000); // 🔹 Obtém preço do config.yml, com fallback de 1000
    if (!processPurchase(player, price)) return; // 🔹 Interrompe se a compra falhar

    // 🔹 Adiciona o bloco de Redstone ao inventário do jogador dentro da região global
    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        player.getInventory().addItem(new ItemStack(Material.REDSTONE_BLOCK, 1));
    });

    // 🔹 Mensagem para o jogador
    String lang = getPlayerLanguage(player);
    String message = switch (lang) {
        case "pt-BR" -> "💎 Você comprou um Bloco de Redstone por $" + price + "!";
        case "es-ES" -> "💎 ¡Has comprado un Bloque de Redstone por $" + price + "!";
        default -> "💎 You bought a Redstone Block for $" + price + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.GOLD));
}



    // 📌 Compra de Bloco de Quartzo
    public void buyQuartzBlock(Player player) {
    int price = config.getInt("store.price.quartz", 1000); // 🔹 Obtém preço do config.yml, com fallback de 1000
    if (!processPurchase(player, price)) return; // 🔹 Interrompe se a compra falhar

    // 🔹 Adiciona o bloco de quartzo ao inventário do jogador dentro da região global
    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        player.getInventory().addItem(new ItemStack(Material.QUARTZ_BLOCK, 1));
    });

    // 🔹 Mensagem para o jogador
    String lang = getPlayerLanguage(player);
    String message = switch (lang) {
        case "pt-BR" -> "💎 Você comprou um Bloco de Quartzo por $" + price + "!";
        case "es-ES" -> "💎 ¡Has comprado un Bloque de Cuarzo por $" + price + "!";
        default -> "💎 You bought a Quartz Block for $" + price + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.GOLD));
}



    // 📌 Compra de Bloco de Argila
    public void buyClayBlock(Player player) {
    int price = config.getInt("store.price.clay", 1000); // 🔹 Obtém preço do config.yml, com fallback de 1000
    if (!processPurchase(player, price)) return; // 🔹 Interrompe se a compra falhar

    // 🔹 Adiciona o bloco de argila ao inventário do jogador dentro da região global
    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        player.getInventory().addItem(new ItemStack(Material.CLAY, 1));
    });

    // 🔹 Mensagem para o jogador
    String lang = getPlayerLanguage(player);
    String message = switch (lang) {
        case "pt-BR" -> "💎 Você comprou um Bloco de Argila por $" + price + "!";
        case "es-ES" -> "💎 ¡Has comprado un Bloque de Arcilla por $" + price + "!";
        default -> "💎 You bought a Clay Block for $" + price + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.GOLD));
}



    // 📌 Compra de Bloco de Areia
    public void buySandBlock(Player player) {
    int price = config.getInt("store.price.buySandBlock", 1000); // 🔹 Obtém preço do config.yml, com fallback de 1000
    if (!processPurchase(player, price)) return; // 🔹 Interrompe se a compra falhar

    // 🔹 Adiciona o bloco de areia ao inventário do jogador dentro da região global
    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        player.getInventory().addItem(new ItemStack(Material.SAND, 1));
    });

    // 🔹 Mensagem para o jogador
    String lang = getPlayerLanguage(player);
    String message = switch (lang) {
        case "pt-BR" -> "💎 Você comprou um Bloco de Areia por $" + price + "!";
        case "es-ES" -> "💎 ¡Has comprado un Bloque de Arena por $" + price + "!";
        default -> "💎 You bought a Sand Block for $" + price + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.GOLD));
}



public void buyAllTools(Player player) {
    int totalPrice = config.getInt("store.price.buyAllTools", 5000); // 🔹 Obtém preço do config.yml, com fallback de 5000
    if (!processPurchase(player, totalPrice)) return; // 🔹 Interrompe se a compra falhar

    // 🔹 Lista das ferramentas disponíveis para compra
    List<Material> tools = List.of(
        Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SHOVEL,
        Material.DIAMOND_HOE, Material.DIAMOND_SWORD
    );

    // 🔹 Adiciona as ferramentas ao inventário do jogador dentro da região global
    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        for (Material tool : tools) {
            ItemStack toolItem = new ItemStack(tool, 1); // Adiciona 1 unidade de cada ferramenta
            player.getInventory().addItem(toolItem);
        }
    });

    // 🔹 Mensagem para o jogador
    String lang = getPlayerLanguage(player);
    String message = switch (lang) {
        case "pt-BR" -> "🛠️ Você comprou todas as ferramentas por $" + totalPrice + "!";
        case "es-ES" -> "🛠️ ¡Has comprado todas las herramientas por $" + totalPrice + "!";
        default -> "🛠️ You bought all tools for $" + totalPrice + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.GOLD));
}




public void buyAllFood(Player player) {
    int totalPrice = config.getInt("store.price.buyAllFood", 2000); // 🔹 Obtém preço do config.yml, com fallback de 2000
    if (!processPurchase(player, totalPrice)) return; // 🔹 Interrompe se a compra falhar

    // 🔹 Lista de alimentos disponíveis para compra
    List<Material> foodItems = List.of(
        Material.APPLE, Material.BREAD, Material.COOKED_BEEF, Material.COOKED_CHICKEN,
        Material.COOKED_MUTTON, Material.COOKED_PORKCHOP, Material.COOKED_RABBIT,
        Material.CARROT, Material.POTATO, Material.BAKED_POTATO, Material.GOLDEN_CARROT,
        Material.BEETROOT, Material.BEETROOT_SOUP, Material.MUSHROOM_STEW,
        Material.MELON_SLICE, Material.PUMPKIN_PIE, Material.COOKIE
    );

    // 🔹 Adiciona os alimentos ao inventário do jogador dentro da região global
    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        for (Material food : foodItems) {
            ItemStack foodItem = new ItemStack(food, 5); // Adiciona 5 unidades de cada comida
            player.getInventory().addItem(foodItem);
        }
    });

    // 🔹 Mensagem para o jogador
    String lang = getPlayerLanguage(player);
    String message = switch (lang) {
        case "pt-BR" -> "🍽️ Você comprou todos os alimentos por $" + totalPrice + "!";
        case "es-ES" -> "🍽️ ¡Has comprado todos los alimentos por $" + totalPrice + "!";
        default -> "🍽️ You bought all food items for $" + totalPrice + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.GOLD));
}




public void buySimpleBook(Player player) {
    int price = config.getInt("store.price.buySimpleBook", 50); // 🔹 Obtém preço do config.yml, com fallback de 50
    if (!processPurchase(player, price)) return; // 🔹 Interrompe se a compra falhar

    // Ajusta o saldo do jogador após a compra
        //ajustarSaldo(player, "take", price);

    // 🔹 Executa o comando para dar um livro ao jogador
    String commandStr = String.format("give %s minecraft:book 1", player.getName());
    Bukkit.getGlobalRegionScheduler().execute(this.plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandStr));

    // 🔹 Mensagem para o jogador
    String lang = getPlayerLanguage(player);
    player.sendMessage(
        Component.text("📖 ").append(
            lang.equals("pt-BR") ? Component.text("Você comprou um livro simples por $" + price + "!", NamedTextColor.GRAY) :
            lang.equals("es-ES") ? Component.text("¡Has comprado un libro simple por $" + price + "!", NamedTextColor.GRAY) :
            Component.text("You bought a simple book for $" + price + "!", NamedTextColor.GRAY)
        )
    );
}

public void buySimpleMap(Player player) {
    try {
        int price = config.getInt("store.price.buySimpleMap", 100);

        if (!processPurchase(player, price)) return;

        List<String> commands = Arrays.asList(
            "minecraft:enchant " + player.getName() + " mending 1",
            "minecraft:enchant " + player.getName() + " efficiency 5",
            "minecraft:enchant " + player.getName() + " fortune 3",
            "minecraft:enchant " + player.getName() + " unbreaking 3",
            "minecraft:give " + player.getName() + " filled_map 1"
        );

        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
            for (String command : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        });

        String lang = getPlayerLanguage(player);
        String message = switch (lang) {
            case "pt-BR" -> "🗺️ Você comprou um mapa simples por $" + price + "!";
            case "es-ES" -> "🗺️ ¡Has comprado un mapa simple por $" + price + "!";
            default -> "🗺️ You bought a simple map for $" + price + "!";
        };

        player.sendMessage(Component.text(message).color(NamedTextColor.GRAY));
    } catch (Exception e) {
        Bukkit.getLogger().severe("Erro ao executar buySimpleMap para " + player.getName() + ": " + e.getMessage());
    }
}



public void buySimpleCompass(Player player) {
    int price = config.getInt("store.price.buySimpleCompass", 150);

    // Processa a compra e ajusta saldo
    if (!processPurchase(player, price)) return;

    // Lista de comandos ajustados
    List<String> commands = Arrays.asList(
        "minecraft:enchant " + player.getName() + " looting 3",
        "minecraft:enchant " + player.getName() + " mending 1",
        "minecraft:enchant " + player.getName() + " smite 5",
        "minecraft:enchant " + player.getName() + " knockback 2",
        "minecraft:enchant " + player.getName() + " fire_aspect 2",
        "minecraft:enchant " + player.getName() + " unbreaking 3",
        "minecraft:give " + player.getName() + " recovery_compass 1"
    );

    // Executa os comandos na região correta para evitar conflitos no Folia
    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        for (String command : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    });

    // Mensagem para o jogador
    String lang = getPlayerLanguage(player);
    String message = switch (lang) {
        case "pt-BR" -> "🧭 Você comprou uma bússola simples por $" + price + "!";
        case "es-ES" -> "🧭 ¡Has comprado una brújula simple por $" + price + "!";
        default -> "🧭 You bought a simple compass for $" + price + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.GRAY));
}




public void buySimpleFishingRod(Player player) {
    int price = config.getInt("store.price.buySimpleFishingRod", 200); // 🔹 Obtém preço do config.yml, com fallback de 200

    if (!processPurchase(player, price)) return; // 🔹 Interrompe se a compra falhar

    // 🔹 Lista de comandos ajustados
    List<String> commands = Arrays.asList(
        "minecraft:enchant " + player.getName() + " luck_of_the_sea 3",
        "minecraft:enchant " + player.getName() + " lure 3",
        "minecraft:enchant " + player.getName() + " unbreaking 3",
        "minecraft:enchant " + player.getName() + " mending 1",
        "minecraft:give " + player.getName() + " fishing_rod 1"
    );

    // 🔹 Executa os comandos dentro da região global para evitar conflitos no Folia
    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        for (String command : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    });

    // 🔹 Mensagem para o jogador
    String lang = getPlayerLanguage(player);
    String message = switch (lang) {
        case "pt-BR" -> "🎣 Você comprou uma Vara de Pesca encantada por $" + price + "!";
        case "es-ES" -> "🎣 ¡Has comprado una Caña de Pescar encantada por $" + price + "!";
        default -> "🎣 You bought an enchanted Fishing Rod for $" + price + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.GRAY));
}



public void buySpinningWand(Player player) {
    int price = config.getInt("store.price.buySpinningWand", 800); // 🔹 Obtém preço do config.yml, com fallback de 800
    if (!processPurchase(player, price)) return; // 🔹 Interrompe se a compra falhar

    // 🔹 Lista de comandos ajustados para dar um Debug Stick ao jogador
    List<String> commands = Arrays.asList(
        "minecraft:give " + player.getName() + " debug_stick 1"
    );

    // 🔹 Executa os comandos dentro da região global para evitar conflitos no Folia
    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        for (String command : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    });

    // 🔹 Mensagem para o jogador
    String lang = getPlayerLanguage(player);
    String message = switch (lang) {
        case "pt-BR" -> "✨ Você comprou um Debug Stick por $" + price + "!";
        case "es-ES" -> "✨ ¡Has comprado un Debug Stick por $" + price + "!";
        default -> "✨ You bought a Debug Stick for $" + price + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.LIGHT_PURPLE));
}




public void buyAxolotlBucket(Player player) {
    int price = config.getInt("store.price.axolotl_bucket", 500); // 🔹 Obtém preço do config.yml, com fallback de 500
    if (!processPurchase(player, price)) return; // 🔹 Interrompe se a compra falhar

    // 🔹 Lista de comandos ajustados para dar um balde com Axolote ao jogador
    List<String> commands = Arrays.asList(
        "minecraft:give " + player.getName() + " axolotl_bucket 1"
    );

    // 🔹 Executa os comandos dentro da região global para evitar conflitos no Folia
    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
        for (String command : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    });

    // 🔹 Mensagem para o jogador
    String lang = getPlayerLanguage(player);
    String message = switch (lang) {
        case "pt-BR" -> "🪣 Você comprou um balde com Axolote por $" + price + "!";
        case "es-ES" -> "🪣 ¡Has comprado un cubo con Axolote por $" + price + "!";
        default -> "🪣 You bought an Axolotl Bucket for $" + price + "!";
    };

    player.sendMessage(Component.text(message).color(NamedTextColor.AQUA));
}




// 📌 Método para ajustar o saldo do jogador do sql do plugin EssentialsX (nao e necessario mas tenta mater os dados iguais do sql e do mysql)
//fallback
    public void ajustarSaldo(Player player, String tipo, double valor) {
    if (this.plugin == null) {
        return;
    }

    final String playerName = player.getName();

    try {
        // No Folia, comandos do console devem ser despachados via GlobalRegionScheduler
        Bukkit.getGlobalRegionScheduler().execute(this.plugin, () -> {
            try {
                if (tipo.equalsIgnoreCase("give")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + playerName + " " + valor);
                } else if (tipo.equalsIgnoreCase("take")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco take " + playerName + " " + valor);
                } else if (tipo.equalsIgnoreCase("set")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco set " + playerName + " " + valor);
                } else {
                    player.sendMessage("Comando inválido! Use 'give' ou 'take' ou set.");
                }
            } catch (Exception e) {
                System.err.println("ERROR (ajustarSaldo): Erro ao despachar comando eco para " + playerName);
                e.printStackTrace();
            }
        });

    } catch (Exception e) {
        System.err.println("ERROR (ajustarSaldo): Exceção ao agendar tarefa no GlobalRegionScheduler!");
        e.printStackTrace();
    }
}

public void transferirtokengamer(Player player, String recipient, double amount) {
    try (PreparedStatement stmtJogador = connection.prepareStatement("UPDATE banco SET saldo = saldo - ? WHERE jogador = ?");
         PreparedStatement stmtDestinatario = connection.prepareStatement("UPDATE banco SET saldo = saldo + ? WHERE jogador = ?")) {

        stmtJogador.setDouble(1, amount);
        stmtJogador.setString(2, player.getName()); // Corrigido: Usar o nome do jogador
        stmtJogador.executeUpdate();

        stmtDestinatario.setDouble(1, amount);
        stmtDestinatario.setString(2, recipient); // Já está correto
        stmtDestinatario.executeUpdate();

        // Comandos do Bukkit para manter a sincronização com o sistema do jogo
        Bukkit.getGlobalRegionScheduler().execute(this.plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + recipient + " " + amount);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco take " + player.getName() + " " + amount); // Corrigido
        });

    } catch (SQLException e) {
        System.out.println("⚠ Erro ao atualizar o banco de dados: " + e.getMessage());
        e.printStackTrace();
    }
}



}// 🔹 Fim da classe Store