package cn.mlus.thirst.foundation.config;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

public record LootConfigCondition() implements ICondition, LootItemCondition
{
    public static final LootConfigCondition INSTANCE = new LootConfigCondition();
    public static final MapCodec<LootConfigCondition> CODEC = MapCodec.unit(INSTANCE).stable();

    @Override
    public boolean test(ICondition.@NotNull IContext context) {
        return CommonConfig.ENABLE_LOOT.get();
    }

    @Override
    public boolean test(@NotNull LootContext context) {
        return CommonConfig.ENABLE_LOOT.get();
    }

    @Override
    public @NotNull MapCodec<LootConfigCondition> codec() {
        return CODEC;
    }
}
