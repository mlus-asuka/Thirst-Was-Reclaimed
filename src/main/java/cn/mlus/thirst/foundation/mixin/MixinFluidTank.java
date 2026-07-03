package cn.mlus.thirst.foundation.mixin;

import cn.mlus.thirst.content.purity.WaterPurity;
import com.simibubi.create.foundation.fluid.FluidHelper;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * When two water FluidStacks with different purity levels merge in a tank,
 * the resulting fluid takes the lower (dirtier) purity value.
 * Dirty water contaminates clean water.
 */
@Mixin(value = FluidTank.class, remap = false)
public class MixinFluidTank
{
    @Shadow protected FluidStack fluid;

    @Inject(method = "fill", at = @At("RETURN"), remap = false)
    private void mergeToLowestPurity(FluidStack resource, IFluidHandler.FluidAction action, CallbackInfoReturnable<Integer> cir)
    {
        if (!WaterPurity.isEnabled()) return;
        if (action.simulate()) return;
        if (cir.getReturnValue() == 0) return;
        if (fluid.isEmpty()) return;
        if (!FluidHelper.isWater(fluid.getFluid()) || !FluidHelper.isWater(resource.getFluid())) return;

        int storedPurity = WaterPurity.getPurity(fluid);
        int incomingPurity = WaterPurity.getPurity(resource);
        int minPurity = Math.min(storedPurity, incomingPurity);

        if (minPurity != storedPurity)
            WaterPurity.addPurity(fluid, minPurity);
    }
}
