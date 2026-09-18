package cn.mlus.thirst.foundation.mixin;

import cn.mlus.thirst.foundation.common.capability.ModAttachment;
import cn.mlus.thirst.foundation.config.CommonConfig;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class MixinLocalPlayer{

    /**
     * @reason prevent sprinting when thirst
     * @return food level or thirst level
     */

    @Inject(method = "isSprintingPossible", at = @At("HEAD"), cancellable = true)
    private void preventSprintingWhenThirsty(boolean allowedInShallowWater, CallbackInfoReturnable<Boolean> cir)
    {
        LocalPlayer player = (LocalPlayer) (Object) this;
        if (CommonConfig.MOVE_SLOW_WHEN_THIRSTY.get()
                && player.getData(ModAttachment.PLAYER_THIRST).getThirst() < 6)
            cir.setReturnValue(false);
    }
}
