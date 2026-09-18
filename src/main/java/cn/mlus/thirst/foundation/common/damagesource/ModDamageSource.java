package cn.mlus.thirst.foundation.common.damagesource;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

public class ModDamageSource
{

    public static final ResourceKey<DamageType> DIE_OF_THIRST_KEY = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("thirst", "dehydrate"));

    public static DamageSource getDamageSource(Level level, ResourceKey<DamageType> type) {
        return new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(type), null, null);
    }

}
