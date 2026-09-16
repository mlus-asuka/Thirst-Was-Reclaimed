package cn.mlus.thirst.foundation.mixin.sophisticated;

import cn.mlus.thirst.compat.sophisticated.SophisticatedPurity;
import cn.mlus.thirst.content.purity.WaterPurity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Pseudo
@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.upgrades.tank.TankUpgradeWrapper", remap = false)
public abstract class MixinTankUpgradeWrapper {
    @Shadow @Final
    private static Map<ItemStack, Function<ItemStack, IFluidHandlerItem>> CUSTOM_FLUIDHANDLER_FACTORIES;

    @Inject(method = "getCustomFluidHandler", at = @At("RETURN"), cancellable = true, require = 1)
    private static void recognizeWaterPurity(ItemStack stack, CallbackInfoReturnable<Optional<IFluidHandlerItem>> cir) {
        if (!WaterPurity.isEnabled() || cir.getReturnValue().isPresent()) {
            return;
        }

        CUSTOM_FLUIDHANDLER_FACTORIES.entrySet().stream()
                .filter(entry -> SophisticatedPurity.canStackIgnoringPurity(stack, entry.getKey()))
                .findFirst()
                .ifPresent(entry -> cir.setReturnValue(Optional.of(entry.getValue().apply(stack))));
    }

    // Update purity before the tank saves and synchronizes its contents.
    @Redirect(method = "fill", at = @At(value = "INVOKE",
            target = "Lnet/neoforged/neoforge/fluids/FluidStack;setAmount(I)V"), require = 1)
    private void mergeWaterPurity(FluidStack contents, int amount, FluidStack resource,
                                 IFluidHandler.FluidAction action, boolean ignoreInOutLimit) {
        if (WaterPurity.isEnabled() && action.execute() && amount > contents.getAmount()
                && resource.getFluid() == Fluids.WATER && contents.getFluid() == Fluids.WATER) {
            int purity = Math.min(WaterPurity.getPurity(contents), WaterPurity.getPurity(resource));
            if (purity != WaterPurity.getPurity(contents)) {
                WaterPurity.addPurity(contents, purity);
            }
        }
        contents.setAmount(amount);
    }
}
