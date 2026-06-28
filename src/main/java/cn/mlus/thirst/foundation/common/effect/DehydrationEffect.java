package cn.mlus.thirst.foundation.common.effect;

import cn.mlus.thirst.foundation.common.capability.ModCapabilities;
import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class DehydrationEffect extends InstantenousMobEffect {
    public DehydrationEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity p_295892_, int p_296026_) {
        if (!p_295892_.level().isClientSide && p_295892_ instanceof Player player) {
            player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(cap -> cap.drink(player, -1, -1));
        }
    }
}
