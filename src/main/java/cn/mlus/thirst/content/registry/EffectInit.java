package cn.mlus.thirst.content.registry;

import cn.mlus.thirst.Thirst;
import cn.mlus.thirst.foundation.common.effect.DehydrationEffect;
import cn.mlus.thirst.foundation.common.effect.QuenchnessEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class EffectInit {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Thirst.ID);
    public static final RegistryObject<MobEffect> QUENCHNESS = MOB_EFFECTS.register("quenchness", () -> new QuenchnessEffect(MobEffectCategory.BENEFICIAL, 0xDC143C));
    public static final RegistryObject<MobEffect> DEHYDRATION = MOB_EFFECTS.register("dehydration", () -> new DehydrationEffect(MobEffectCategory.HARMFUL, 0x1E90FF));
}
