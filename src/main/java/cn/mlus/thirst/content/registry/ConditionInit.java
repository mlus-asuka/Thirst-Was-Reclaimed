package cn.mlus.thirst.content.registry;

import cn.mlus.thirst.Thirst;
import cn.mlus.thirst.foundation.config.LootConfigCondition;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ConditionInit {
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, Thirst.ID);
    public static final DeferredRegister<MapCodec<? extends LootItemCondition>> LOOT_CONDITION_TYPES = DeferredRegister.create(BuiltInRegistries.LOOT_CONDITION_TYPE, Thirst.ID);

    public static final Supplier<MapCodec<LootConfigCondition>> LOOT_CONFIG_CONDITION = CONDITION_CODECS.register("loot_config", () -> LootConfigCondition.CODEC);
    public static final Supplier<MapCodec<? extends LootItemCondition>> LOOT_CONFIG_LOOT_CONDITION = LOOT_CONDITION_TYPES.register("loot_config", () -> LootConfigCondition.CODEC);
}
