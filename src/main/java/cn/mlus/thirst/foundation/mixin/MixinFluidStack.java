package cn.mlus.thirst.foundation.mixin;

import cn.mlus.thirst.content.purity.WaterPurity;
import cn.mlus.thirst.content.registry.ThirstComponent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
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
    private static final ThreadLocal<Boolean> COMPARING = ThreadLocal.withInitial(() -> false);

    @Inject(
        method = "isSameFluidSameComponents(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/FluidStack;)Z",
        at = @At("HEAD"), cancellable = true, remap = false
    )
    private static void ignorePurityForWaterComparison(FluidStack a, FluidStack b, CallbackInfoReturnable<Boolean> cir)
    {
        if (!WaterPurity.isEnabled()) return;
        if (a == null || b == null) return;
        if (convertToStill(a.getFluid()) != Fluids.WATER || convertToStill(b.getFluid()) != Fluids.WATER) return;
        if (COMPARING.get()) return;

        // Compare water ignoring only PURITY — all other components still matter
        COMPARING.set(true);
        try {
            FluidStack aCopy = a.copy();
            FluidStack bCopy = b.copy();
            aCopy.remove(ThirstComponent.PURITY);
            bCopy.remove(ThirstComponent.PURITY);
            cir.setReturnValue(FluidStack.isSameFluidSameComponents(aCopy, bCopy));
        } finally {
            COMPARING.set(false);
        }
    }

    private static Fluid convertToStill(Fluid fluid) {
        if (fluid == Fluids.FLOWING_WATER)
            return Fluids.WATER;
        if (fluid == Fluids.FLOWING_LAVA)
            return Fluids.LAVA;
        if (fluid instanceof BaseFlowingFluid)
            return ((BaseFlowingFluid) fluid).getSource();
        return fluid;
    }
}
