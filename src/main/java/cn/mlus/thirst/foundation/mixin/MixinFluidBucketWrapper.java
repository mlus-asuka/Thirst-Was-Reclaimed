package cn.mlus.thirst.foundation.mixin;

import cn.mlus.thirst.content.purity.WaterPurity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = FluidBucketWrapper.class,remap = false)
public class MixinFluidBucketWrapper {
    @Shadow @NotNull protected ItemStack container;

    /**
     * @author mlus
     * @reason add purity to fluid stack in bucket
     */
    @Overwrite
    public @NotNull FluidStack getFluid() {
        Item item = container.getItem();
        if (item instanceof BucketItem) {
            FluidStack stack = new FluidStack(((BucketItem) item).content, FluidType.BUCKET_VOLUME);
            if(WaterPurity.hasPurity(container)){
                WaterPurity.addPurity(stack,WaterPurity.getPurity(container));
            }
            return stack;
        } else {
            return FluidStack.EMPTY;
        }
    }
}
