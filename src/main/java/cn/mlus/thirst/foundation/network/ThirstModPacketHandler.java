package cn.mlus.thirst.foundation.network;

import cn.mlus.thirst.foundation.network.message.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class ThirstModPacketHandler
{
    private static final String PROTOCOL_VERSION = "0.1.5";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playBidirectional(
                DrinkByHandMessage.TYPE,
                DrinkByHandMessage.STREAM_CODEC,
                DrinkByHandMessage::clientHandle,
                DrinkByHandMessage::serverHandle
        );
        registrar.playBidirectional(
                PlayerThirstSyncMessage.TYPE,
                PlayerThirstSyncMessage.STREAM_CODEC,
                PlayerThirstSyncMessage::clientHandle,
                PlayerThirstSyncMessage::serverHandle
        );
        registrar.playBidirectional(
                ItemSettingsHashMessage.TYPE,
                ItemSettingsHashMessage.STREAM_CODEC,
                ItemSettingsHashMessage::clientHandle,
                ItemSettingsHashMessage::serverHandle
        );
        registrar.playBidirectional(
                ItemSettingsSyncMessage.TYPE,
                ItemSettingsSyncMessage.STREAM_CODEC,
                ItemSettingsSyncMessage::clientHandle,
                ItemSettingsSyncMessage::serverHandle
        );
        registrar.playBidirectional(
                ItemSettingsSyncRequestMessage.TYPE,
                ItemSettingsSyncRequestMessage.STREAM_CODEC,
                ItemSettingsSyncRequestMessage::clientHandle,
                ItemSettingsSyncRequestMessage::serverHandle
        );
    }
}
