package cn.mlus.thirst.foundation.mixin;

import cn.mlus.thirst.content.purity.WaterPurity;
import cn.mlus.thirst.foundation.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public class MixinBucketItem
{
    private static final ThreadLocal<Integer> BUCKET_PURITY = new ThreadLocal<>();

    @Inject(method = "use", at = @At("HEAD"))
    public void setPurity(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir)
    {
        if(!WaterPurity.isEnabled())
        {
            BUCKET_PURITY.set(null);
            return;
        }

        BlockPos blockPos = MathHelper.getPlayerPOVHitResult(player.level(), player, ClipContext.Fluid.SOURCE_ONLY).getBlockPos();
        boolean shouldModify = level.getFluidState(blockPos).is(FluidTags.WATER) && level.getFluidState(blockPos).isSource();
        BUCKET_PURITY.set(shouldModify ? WaterPurity.getBlockPurity(level, blockPos) : null);
    }

    @ModifyArg(method = "use", index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemUtils;createFilledResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack addPurity(ItemStack result)
    {
        Integer purity = BUCKET_PURITY.get();
        if (purity != null && WaterPurity.isWaterFilledContainer(result))
        {
            WaterPurity.addPurity(result, purity);
        }
        return result;
    }

    @Inject(method = "use", at = @At("RETURN"))
    public void cleanup(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir)
    {
        BUCKET_PURITY.remove();
    }
}
