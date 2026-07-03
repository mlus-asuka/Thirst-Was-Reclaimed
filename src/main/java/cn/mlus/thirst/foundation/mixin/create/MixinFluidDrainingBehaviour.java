package cn.mlus.thirst.foundation.mixin.create;

import cn.mlus.thirst.content.purity.WaterPurity;
import com.simibubi.create.content.fluids.transfer.FluidDrainingBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FluidDrainingBehaviour.class,remap = false)
public abstract class MixinFluidDrainingBehaviour
{

    @Inject(method = "getDrainableFluid", at = @At("RETURN"), remap = false, cancellable = true)
    public void getDrainableFluid(BlockPos rootPos, CallbackInfoReturnable<FluidStack> cir){
        if(!WaterPurity.isEnabled())
            return;

        FluidStack output = cir.getReturnValue();
        if (output.isEmpty() || !FluidHelper.isWater(output.getFluid()))
            return;

        FluidDrainingBehaviour behaviour = ((FluidDrainingBehaviour)(Object) this);
        FluidStack copy = output.copy();
        WaterPurity.addPurity(copy, WaterPurity.getBlockPurity(behaviour.getWorld(), rootPos));
        cir.setReturnValue(copy);
    }
}
