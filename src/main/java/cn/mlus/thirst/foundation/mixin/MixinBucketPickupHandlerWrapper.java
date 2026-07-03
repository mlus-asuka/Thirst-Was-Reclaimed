package cn.mlus.thirst.foundation.mixin;

import cn.mlus.thirst.content.purity.WaterPurity;
import com.simibubi.create.foundation.fluid.FluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.wrappers.BucketPickupHandlerWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Injects water purity into all FluidStack results from BucketPickupHandlerWrapper.
 * This covers Create:Pipes'n Physics and any other mod that queries
 * Capabilities.FluidHandler.BLOCK directly, bypassing FluidDrainingBehaviour.
 *
 * Also fixes the internal isSameFluidSameComponents check in drain(FluidStack, FluidAction),
 * which would otherwise fail when resource carries purity but the bucket-extracted
 * FluidStack does not.
 */
@Mixin(value = BucketPickupHandlerWrapper.class, remap = false)
public class MixinBucketPickupHandlerWrapper
{
    @Final
    @Shadow protected Level world;
    @Final
    @Shadow protected BlockPos blockPos;

    // ── getFluidInTank ────────────────────────────────────────────────────────

    @Inject(
        method = "getFluidInTank",
        at = @At("RETURN"), cancellable = true, remap = false
    )
    private void addPurityToGetFluidInTank(int tank, CallbackInfoReturnable<FluidStack> cir)
    {
        if (!WaterPurity.isEnabled()) return;
        FluidStack result = cir.getReturnValue();
        if (result.isEmpty() || !FluidHelper.isWater(result.getFluid())) return;
        FluidStack copy = result.copy();
        WaterPurity.addPurity(copy, WaterPurity.getBlockPurity(world, blockPos));
        cir.setReturnValue(copy);
    }

    // ── drain(int, FluidAction) ───────────────────────────────────────────────

    @Inject(
        method = "drain(ILnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/neoforged/neoforge/fluids/FluidStack;",
        at = @At("RETURN"), cancellable = true, remap = false
    )
    private void addPurityToDrainByAmount(int maxDrain, IFluidHandler.FluidAction action, CallbackInfoReturnable<FluidStack> cir)
    {
        if (!WaterPurity.isEnabled()) return;
        FluidStack result = cir.getReturnValue();
        if (result.isEmpty() || !FluidHelper.isWater(result.getFluid())) return;
        FluidStack copy = result.copy();
        WaterPurity.addPurity(copy, WaterPurity.getBlockPurity(world, blockPos));
        cir.setReturnValue(copy);
    }

    // ── drain(FluidStack, FluidAction) ────────────────────────────────────────
    //
    // Problem: drain(FluidStack resource, FluidAction) calls
    //   isSameFluidSameComponents(resource, extracted)
    // where resource has PURITY (added by getFluidInTank above) but extracted
    // (from BucketItem.pickupBlock) does not — so the check fails and the method
    // returns EMPTY, causing "pump can't pull its supply".
    //
    // Fix: redirect that specific call to ignore PURITY for water.

    @Redirect(
        method = "drain(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/neoforged/neoforge/fluids/FluidStack;",
        at = @At(value = "INVOKE",
                 target = "Lnet/neoforged/neoforge/fluids/FluidStack;isSameFluidSameComponents(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/FluidStack;)Z"),
        remap = false
    )
    private boolean compareDrainFluidsIgnoringPurity(FluidStack a, FluidStack b)
    {
        if (WaterPurity.isEnabled() && FluidHelper.isWater(a.getFluid()) && FluidHelper.isWater(b.getFluid()))
            return a.getFluid() == b.getFluid();
        return FluidStack.isSameFluidSameComponents(a, b);
    }

    @Inject(
        method = "drain(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/neoforged/neoforge/fluids/FluidStack;",
        at = @At("RETURN"), cancellable = true, remap = false
    )
    private void addPurityToDrainByStack(FluidStack resource, IFluidHandler.FluidAction action, CallbackInfoReturnable<FluidStack> cir)
    {
        if (!WaterPurity.isEnabled()) return;
        FluidStack result = cir.getReturnValue();
        if (result.isEmpty() || !FluidHelper.isWater(result.getFluid())) return;
        FluidStack copy = result.copy();
        WaterPurity.addPurity(copy, WaterPurity.getBlockPurity(world, blockPos));
        cir.setReturnValue(copy);
    }
}
