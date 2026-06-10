package com.SolanaDevMinecraft;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SolanaForge.MODID);

    public static final RegistryObject<Item> NETHER_RELIC = ITEMS.register("nether_relic",
            () -> new ArmorItem(ArmorMaterials.GOLD, Type.HELMET, new Item.Properties().fireResistant()));

    public static final RegistryObject<Item> THOR_AXE = ITEMS.register("thor_axe",
            () -> new Item(new Item.Properties().stacksTo(1).durability(2031)));
}
