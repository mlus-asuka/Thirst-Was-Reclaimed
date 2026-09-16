package cn.mlus.thirst.foundation.mixin;

import cn.mlus.thirst.content.purity.WaterPurity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CauldronBlock.class)
public abstract class MixinCauldronBlock
{
    @Inject(method = "receiveStalactiteDrip", at = @At("TAIL"))
    private void markDripstoneWaterDirty(BlockState state, Level level, BlockPos pos, Fluid fluid, CallbackInfo ci)
    {
        if (WaterPurity.isEnabled() && fluid == Fluids.WATER)
        {
            BlockState filledState = level.getBlockState(pos);
            level.setBlockAndUpdate(pos, filledState.setValue(WaterPurity.BLOCK_PURITY, WaterPurity.MIN_PURITY + 1));
        }
    }
}
