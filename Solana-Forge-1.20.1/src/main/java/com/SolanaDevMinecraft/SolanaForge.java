package com.SolanaDevMinecraft;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SolanaForge.MODID)
public class SolanaForge {
    public static final String MODID = "solanaforge";
    private static final Logger LOGGER = LogManager.getLogger();

    private static DatabaseManager databaseManager;
    private static SolanaManager solanaManager;
    private static StoreManager storeManager;
    private static HomeManager homeManager;
    private static TeleportManager teleportManager;
    private static ChestLockManager chestLockManager;
    private static SolanaCommands solanaCommands;

    public SolanaForge() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigManager.SPEC);
        ItemInit.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Solana Forge 1.20.1 iniciado!");
        
        databaseManager = new DatabaseManager(
                ConfigManager.DB_URL.get(),
                ConfigManager.DB_USER.get(),
                ConfigManager.DB_PASSWORD.get()
        );
        databaseManager.setupTables();
        
        solanaManager = new SolanaManager(databaseManager);
        storeManager = new StoreManager(databaseManager);
        homeManager = new HomeManager(databaseManager);
        teleportManager = new TeleportManager();
        chestLockManager = new ChestLockManager(databaseManager);
        solanaCommands = new SolanaCommands(solanaManager, storeManager, homeManager, teleportManager, chestLockManager);
        
        PlayerJoinHandler.init(storeManager);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        if (solanaCommands != null) {
            solanaCommands.register(event.getDispatcher());
        }
    }
}
