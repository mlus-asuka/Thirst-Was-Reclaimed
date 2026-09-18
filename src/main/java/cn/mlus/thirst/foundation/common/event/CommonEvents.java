package cn.mlus.thirst.foundation.common.event;

import cn.mlus.thirst.foundation.common.capability.ModAttachment;
import cn.mlus.thirst.foundation.network.message.ItemSettingsHashMessage;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public class CommonEvents {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(event.getEntity() instanceof ServerPlayer player) {
            player.getData(ModAttachment.PLAYER_THIRST).updateThirstData(player);
            PacketDistributor.sendToPlayer(player, ItemSettingsHashMessage.create());
        }
    }
}
