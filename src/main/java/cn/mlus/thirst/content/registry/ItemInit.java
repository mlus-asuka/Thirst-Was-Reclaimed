package cn.mlus.thirst.content.registry;

import cn.mlus.thirst.foundation.common.item.DrinkableItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ItemInit {
    public static final DeferredRegister.Items ITEMS;
    public static final DeferredItem<Item> CLAY_BOWL;
    public static final DeferredItem<Item>  TERRACOTTA_BOWL;
    public static final DeferredItem<Item>  TERRACOTTA_WATER_BOWL;

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    static {
        ITEMS = DeferredRegister.createItems("thirst");
        CLAY_BOWL = ITEMS.registerSimpleItem("clay_bowl", properties -> properties.stacksTo(64));
        TERRACOTTA_BOWL = ITEMS.registerSimpleItem("terracotta_bowl", properties -> properties.stacksTo(64));
        TERRACOTTA_WATER_BOWL = ITEMS.registerItem("terracotta_water_bowl",
                properties -> new DrinkableItem(properties).setContainer(TERRACOTTA_BOWL));
    }
}
