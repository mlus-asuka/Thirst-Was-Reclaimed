package cn.mlus.thirst.foundation.network.message;

import cn.mlus.thirst.Thirst;
import cn.mlus.thirst.api.ThirstHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public record ItemSettingsHashMessage(String settingsHash) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<ItemSettingsHashMessage> TYPE = new Type<>(Thirst.asResource("item_settings_hash"));

    public static final StreamCodec<ByteBuf, ItemSettingsHashMessage> STREAM_CODEC = new StreamCodec<>()
    {
        @Override
        public ItemSettingsHashMessage decode(ByteBuf buffer)
        {
            return new ItemSettingsHashMessage(readString(buffer));
        }

        @Override
        public void encode(ByteBuf buffer, ItemSettingsHashMessage message)
        {
            writeString(buffer, message.settingsHash);
        }
    };

    public static ItemSettingsHashMessage create()
    {
        return new ItemSettingsHashMessage(ItemSettingsSyncMessage.createHash());
    }

    public static void serverHandle(final ItemSettingsHashMessage message, final IPayloadContext context)
    {

    }

    public static void clientHandle(final ItemSettingsHashMessage message, final IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            if (!ThirstHelper.hasSyncedSettingsHash(message.settingsHash))
                ClientPacketDistributor.sendToServer(new ItemSettingsSyncRequestMessage());
        });
    }

    private static void writeString(ByteBuf buffer, String string)
    {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        buffer.writeInt(bytes.length);
        buffer.writeBytes(bytes);
    }

    private static String readString(ByteBuf buffer)
    {
        int length = buffer.readInt();
        if (length < 0 || length > 1_024)
            throw new IllegalArgumentException("Invalid settings hash length: " + length);

        byte[] bytes = new byte[length];
        buffer.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
