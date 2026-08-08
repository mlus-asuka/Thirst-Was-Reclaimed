package cn.mlus.thirst.foundation.mixin.create;


import cn.mlus.thirst.content.purity.WaterPurity;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = IHaveGoggleInformation.class,remap = false)
public interface MixinIHaveGoggleInformation {
    /**
     * @author mlus
     * @reason add purity information
     */
    @Inject(method = "containedFluidTooltip", at = @At("HEAD"), cancellable = true)
    private void containedFluidTooltip(List<Component> tooltip, boolean isPlayerSneaking, IFluidHandler handler, CallbackInfoReturnable<Boolean> cir) {
        if (handler == null){
            cir.setReturnValue(false);
            return;
        }


        if (handler.getTanks() == 0){
            cir.setReturnValue(false);
            return;
        }


        LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
        CreateLang.translate("gui.goggles.fluid_container")
                .forGoggles(tooltip);

        boolean isEmpty = true;
        for (int i = 0; i < handler.getTanks(); i++) {
            FluidStack fluidStack = handler.getFluidInTank(i);
            if (fluidStack.isEmpty())
                continue;

            if(WaterPurity.isEnabled() && (WaterPurity.hasPurity(fluidStack) || fluidStack.getFluid().equals(Fluids.WATER)) && WaterPurity.getPurity(fluidStack) != -1){
                int purity = WaterPurity.getPurity(fluidStack);
                ChatFormatting color = getPurityColor(purity);
                CreateLang.builder()
                        .text(WaterPurity.getPurityText(purity)+" ")
                        .add(fluidStack.getHoverName().copy())
                        .style(color)
                        .forGoggles(tooltip, 1);
            }else {
                CreateLang.fluidName(fluidStack)
                        .style(ChatFormatting.GRAY)
                        .forGoggles(tooltip, 1);
            }



            CreateLang.builder()
                    .add(CreateLang.number(fluidStack.getAmount())
                            .add(mb)
                            .style(ChatFormatting.GOLD))
                    .text(ChatFormatting.GRAY, " / ")
                    .add(CreateLang.number(handler.getTankCapacity(i))
                            .add(mb)
                            .style(ChatFormatting.DARK_GRAY))
                    .forGoggles(tooltip, 1);

            isEmpty = false;
        }

        if (handler.getTanks() > 1) {
            if (isEmpty)
                tooltip.removeLast();
            cir.setReturnValue(true);
            return;
        }

        if (!isEmpty){
            cir.setReturnValue(true);
            return;
        }


        CreateLang.translate("gui.goggles.fluid_container.capacity")
                .add(CreateLang.number(handler.getTankCapacity(0))
                        .add(mb)
                        .style(ChatFormatting.GOLD))
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);

        cir.setReturnValue(true);
    }

    default ChatFormatting getPurityColor(int purity){
        if(purity == 3){
            return ChatFormatting.AQUA;
        }else if(purity == 2){
            return ChatFormatting.BLUE;
        }else if(purity == 1){
            return ChatFormatting.GRAY;
        }else if(purity == 0){
            return ChatFormatting.DARK_GRAY;
        }else{
            return ChatFormatting.GRAY;
        }
    }

}
