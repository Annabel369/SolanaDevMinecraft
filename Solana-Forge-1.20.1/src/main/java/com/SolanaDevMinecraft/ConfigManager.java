package com.SolanaDevMinecraft;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigManager {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<String> DB_URL;
    public static final ForgeConfigSpec.ConfigValue<String> DB_USER;
    public static final ForgeConfigSpec.ConfigValue<String> DB_PASSWORD;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DB_USE_SSL;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DB_VERIFY_CERT;

    public static final ForgeConfigSpec.ConfigValue<String> DOCKER_HOST;
    public static final ForgeConfigSpec.ConfigValue<String> API_WEB_KEY;
    public static final ForgeConfigSpec.ConfigValue<String> SOLANA_COMMAND;
    public static final ForgeConfigSpec.ConfigValue<String> BASE_PATH;
    public static final ForgeConfigSpec.ConfigValue<String> WALLET_BANK;

    public static final ForgeConfigSpec.ConfigValue<Integer> CONVERSION_RATE;
    
    // Prices
    public static final ForgeConfigSpec.ConfigValue<Integer> PRICE_APPLE;
    public static final ForgeConfigSpec.ConfigValue<Integer> PRICE_EMERALD;
    public static final ForgeConfigSpec.ConfigValue<Integer> PRICE_NETHER_RELIC;
    public static final ForgeConfigSpec.ConfigValue<Integer> PRICE_THOR_AXE;

    static {
        BUILDER.push("Database");
        DB_URL = BUILDER.define("url", "jdbc:mysql://localhost:3306/solanadev");
        DB_USER = BUILDER.define("user", "root");
        DB_PASSWORD = BUILDER.define("password", "0073007");
        DB_USE_SSL = BUILDER.define("use_ssl", false);
        DB_VERIFY_CERT = BUILDER.define("verify_server_certificate", true);
        BUILDER.pop();

        BUILDER.push("Docker");
        DOCKER_HOST = BUILDER.define("host", "localhost");
        API_WEB_KEY = BUILDER.define("api_web_key", "b493d48364afe44d");
        SOLANA_COMMAND = BUILDER.define("solana_command", "heysolana");
        BASE_PATH = BUILDER.define("base_path", "/home/astral/astralcoin");
        WALLET_BANK = BUILDER.define("wallet_bank_store_admin", "dadhcDXHiHDrWkT2Z4pSZyF6HWmHwQMG3HtGciwccVP");
        BUILDER.pop();

        BUILDER.push("Store");
        CONVERSION_RATE = BUILDER.define("value_of_in_game_currency", 1000);
        PRICE_APPLE = BUILDER.define("price_apple", 500);
        PRICE_EMERALD = BUILDER.define("price_emerald", 250);
        PRICE_NETHER_RELIC = BUILDER.define("price_nether_relic", 1000);
        PRICE_THOR_AXE = BUILDER.define("price_thor_axe", 60000);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
