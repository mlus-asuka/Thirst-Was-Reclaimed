package cn.mlus.thirst.foundation.network.message;

import cn.mlus.thirst.Thirst;
import cn.mlus.thirst.compat.supernatural.SupernaturalHelper;
import cn.mlus.thirst.content.purity.WaterPurity;
import cn.mlus.thirst.foundation.common.capability.ModAttachment;
import cn.mlus.thirst.foundation.config.CommonConfig;
import cn.mlus.thirst.foundation.util.MathHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public record DrinkByHandMessage(Vector3f pos) implements CustomPacketPayload
{
    private static final String DRINK_BY_HAND_COOLDOWN_KEY = "thirstDrinkByHandCooldown";

    public static final CustomPacketPayload.Type<DrinkByHandMessage> TYPE = new Type<>(Thirst.asResource("drinkbyhand"));

    public static final StreamCodec<ByteBuf, DrinkByHandMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VECTOR3F,
            DrinkByHandMessage::pos,
            DrinkByHandMessage::new
    );

    public static void clientHandle(final DrinkByHandMessage data, final IPayloadContext context){

    }


    public static void serverHandle(final DrinkByHandMessage data, final IPayloadContext context) {
            context.enqueueWork(() ->
            {
                Player player = context.player();
                Level level = player.level();
                if(player.getData(ModAttachment.PLAYER_THIRST).getThirst() >= 20)
                    return;

                long gameTime = level.getGameTime();
                if(player.getPersistentData().getLong(DRINK_BY_HAND_COOLDOWN_KEY) > gameTime)
                    return;

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

                int cooldown = Math.max(0, CommonConfig.HAND_DRINKING_COOLDOWN.get().intValue());
                if(cooldown > 0)
                    player.getPersistentData().putLong(DRINK_BY_HAND_COOLDOWN_KEY, gameTime + cooldown);
                else
                    player.getPersistentData().remove(DRINK_BY_HAND_COOLDOWN_KEY);

                int purity = WaterPurity.getBlockPurity(level, blockPos);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_DRINK, SoundSource.NEUTRAL, 1.0F, 1.0F);
                if(WaterPurity.givePurityEffects(player, purity))
                        player.getData(ModAttachment.PLAYER_THIRST).drink(CommonConfig.HAND_DRINKING_HYDRATION.get().intValue(), CommonConfig.HAND_DRINKING_QUENCHED.get().intValue());
            });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
