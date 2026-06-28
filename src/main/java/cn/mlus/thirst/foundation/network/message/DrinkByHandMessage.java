package cn.mlus.thirst.foundation.network.message;

import cn.mlus.thirst.compat.supernatural.SupernaturalHelper;
import cn.mlus.thirst.content.purity.WaterPurity;
import cn.mlus.thirst.foundation.common.capability.ModCapabilities;
import cn.mlus.thirst.foundation.config.CommonConfig;
import cn.mlus.thirst.foundation.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DrinkByHandMessage
{
    public BlockPos pos;

    public DrinkByHandMessage(BlockPos pos)
    {
        this.pos = pos;
    }

    public static void encode(DrinkByHandMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(message.pos);
    }

    public static DrinkByHandMessage decode(FriendlyByteBuf buffer)
    {
        return new DrinkByHandMessage(buffer.readBlockPos());
    }

    public static void handle(DrinkByHandMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        if (context.getDirection().getReceptionSide().isServer())
        {
            context.enqueueWork(() ->
            {
                Player player = context.getSender();
                Level level = player.level();

                if(!player.isCrouching() || player.isInvulnerable())
                    return;

                if(!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
                        || !player.getItemInHand(InteractionHand.OFF_HAND).isEmpty())
                    return;

                if (ModList.get().isLoaded("supernatural")) {
                    if (SupernaturalHelper.isVampireCheck(player)) {
                        return;
                    }
                }

                BlockPos blockPos = MathHelper.getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY).getBlockPos();
                if(!level.getFluidState(blockPos).is(FluidTags.WATER))
                    return;

                if (player.distanceToSqr(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5) > 25)
                    return;

                player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(cap ->
                {
                    if(cap.getThirst()==20)
                        return;

                    int purity = WaterPurity.getBlockPurity(level, blockPos);
                    level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_DRINK, SoundSource.NEUTRAL, 1.0F, 1.0F);

                    if(WaterPurity.givePurityEffects(player, purity))
                        cap.drink(player, CommonConfig.HAND_DRINKING_HYDRATION.get().intValue(), CommonConfig.HAND_DRINKING_QUENCHED.get().intValue());
                });
            });
        }

        context.setPacketHandled(true);
    }
}
