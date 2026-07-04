package cn.mlus.thirst.foundation.mixin;

import cn.mlus.thirst.content.purity.WaterPurity;
import cn.mlus.thirst.foundation.util.FluidHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;



/**
 * Makes FluidStack.isFluidEqual() ignore the "Purity" NBT tag for water,
 * so water with different purity levels is treated as the same fluid type
 * for Create pipe routing, tank fill checks, and all mod compatibility.
 *
 * In Forge 1.20.1, isFluidEqual() checks both fluid type AND NBT tags,
 * so this fix is required for water with purity NBT to flow through pipes.
 */
@Mixin(value = FluidStack.class, remap = false)
public class MixinFluidStack
{
    @Inject(
        method = "isFluidEqual(Lnet/minecraftforge/fluids/FluidStack;)Z",
        at = @At("HEAD"), cancellable = true, remap = false
    )
    private void ignorePurityForWaterComparison(FluidStack other, CallbackInfoReturnable<Boolean> cir)
    {
        if (!WaterPurity.isEnabled()) return;
        FluidStack self = (FluidStack) (Object) this;
        if (FluidHelper.convertToStill(self.getFluid()) != Fluids.WATER || FluidHelper.convertToStill(other.getFluid()) != Fluids.WATER) return;

        // For water, compare fluid type and all NBT except "Purity"
        if (self.getFluid() != other.getFluid())
        {
            cir.setReturnValue(false);
            return;
        }
        CompoundTag selfTag = self.hasTag() ? self.getTag().copy() : new CompoundTag();
        CompoundTag otherTag = other.hasTag() ? other.getTag().copy() : new CompoundTag();
        selfTag.remove("Purity");
        otherTag.remove("Purity");
        cir.setReturnValue(selfTag.equals(otherTag));
    }
}
