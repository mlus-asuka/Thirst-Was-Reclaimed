package cn.mlus.thirst.foundation.mixin;

import cn.mlus.thirst.content.purity.WaterPurity;
import com.simibubi.create.foundation.fluid.FluidHelper;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes FluidStack comparison ignore ThirstComponent.PURITY for water,
 * so water with different purity levels are treated as the same fluid type
 * for tank merging, pipe routing, and all mod compatibility.
 *
 * This fixes Create:Pipes'n Physics compatibility where comparison happens
 * in Pipes'n Physics's own code (not in paths we can easily intercept).
 */
@Mixin(value = FluidStack.class, remap = false)
public class MixinFluidStack
{
    @Inject(
        method = "isSameFluidSameComponents(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/FluidStack;)Z",
        at = @At("HEAD"), cancellable = true, remap = false
    )
    private static void ignorePurityForWaterComparison(FluidStack a, FluidStack b, CallbackInfoReturnable<Boolean> cir)
    {
        if (!WaterPurity.isEnabled()) return;
        if (a == null || b == null) return;
        if (!FluidHelper.isWater(a.getFluid()) || !FluidHelper.isWater(b.getFluid())) return;

        // For water, only compare fluid type — ignore all components including PURITY
        // This allows water with purity=0/1/2/3 to coexist in the same tank/pipe
        cir.setReturnValue(a.getFluid() == b.getFluid());
    }
}
