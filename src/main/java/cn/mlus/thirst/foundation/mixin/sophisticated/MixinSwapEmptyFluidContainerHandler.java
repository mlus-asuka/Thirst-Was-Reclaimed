package cn.mlus.thirst.foundation.mixin.sophisticated;

import cn.mlus.thirst.compat.sophisticated.SophisticatedPurity;
import cn.mlus.thirst.content.purity.WaterPurity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.upgrades.tank.TankUpgradeWrapper$SwapEmptyFluidContainerHandler", remap = false)
public abstract class MixinSwapEmptyFluidContainerHandler {
    @Shadow private ItemStack container;
    @Shadow private FluidStack contents;

    @Inject(method = "<init>", at = @At("RETURN"), require = 1)
    private void readBottlePurity(CallbackInfo ci) {
        if (!contents.isEmpty() && contents.getFluid() == Fluids.WATER && WaterPurity.hasPurity(container)) {
            WaterPurity.addPurity(contents, WaterPurity.getPurity(container));
        }
    }

    @Inject(method = "fill", at = @At("RETURN"), require = 1)
    private void writeBottlePurity(FluidStack resource, IFluidHandler.FluidAction action, CallbackInfoReturnable<Integer> cir) {
        if (action.execute() && cir.getReturnValue() > 0 && resource.getFluid() == Fluids.WATER
                && WaterPurity.hasPurity(resource)) {
            WaterPurity.addPurity(container, WaterPurity.getPurity(resource));
        }
    }

    // The comparison lives in a compiler-named drain lambda. Match its single call site.
    @Redirect(method = "*",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"), require = 1, allow = 1)
    private boolean recognizeFilledWaterBottle(ItemStack first, ItemStack second) {
        return SophisticatedPurity.canStackIgnoringPurity(first, second);
    }

    // The request describes an amount/type, not the purity of the water actually drained.
    @ModifyVariable(method = "drain(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/neoforged/neoforge/fluids/FluidStack;",
            at = @At("HEAD"), argsOnly = true, require = 1)
    private FluidStack preserveDrainedPurity(FluidStack requested) {
        if (WaterPurity.isEnabled() && !contents.isEmpty() && contents.getFluid() == Fluids.WATER
                && FluidStack.isSameFluidSameComponents(contents, requested)) {
            return contents.copyWithAmount(requested.getAmount());
        }
        return requested;
    }
}
