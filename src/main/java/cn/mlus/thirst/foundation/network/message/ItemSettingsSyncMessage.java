package cn.mlus.thirst.foundation.network.message;

import cn.mlus.thirst.Thirst;
import cn.mlus.thirst.api.ThirstHelper;
import cn.mlus.thirst.content.purity.WaterPurity;
import cn.mlus.thirst.foundation.config.CommonConfig;
import cn.mlus.thirst.foundation.config.ItemSettingsConfig;
import cn.mlus.thirst.foundation.config.KeyWordConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public record ItemSettingsSyncMessage(
        List<Entry> drinks,
        List<Entry> foods,
        List<String> blacklist,
        List<String> containers,
        boolean keywordEnabled,
        String keywordBlacklist,
        String keywordDrink,
        String keywordSoup,
        String keywordFruit,
        int defaultDrinkHydration,
        int defaultDrinkQuenched,
        int defaultSoupHydration,
        int defaultSoupQuenched,
        int defaultFruitHydration,
        int defaultFruitQuenched,
        boolean purityEnabled
) implements CustomPacketPayload
{
    public record Entry(String itemId, int thirst, int quenched) {}

    public static final CustomPacketPayload.Type<ItemSettingsSyncMessage> TYPE = new Type<>(Thirst.asResource("item_settings_sync"));

    public static final StreamCodec<ByteBuf, ItemSettingsSyncMessage> STREAM_CODEC = new StreamCodec<>()
    {
        @Override
        public ItemSettingsSyncMessage decode(ByteBuf buffer)
        {
            List<Entry> drinks = readEntries(buffer);
            List<Entry> foods = readEntries(buffer);
            List<String> blacklist = readStrings(buffer);
            List<String> containers = readStrings(buffer);
            boolean keywordEnabled = buffer.readBoolean();
            String keywordBlacklist = readString(buffer);
            String keywordDrink = readString(buffer);
            String keywordSoup = readString(buffer);
            String keywordFruit = readString(buffer);
            int defaultDrinkHydration = buffer.readInt();
            int defaultDrinkQuenched = buffer.readInt();
            int defaultSoupHydration = buffer.readInt();
            int defaultSoupQuenched = buffer.readInt();
            int defaultFruitHydration = buffer.readInt();
            int defaultFruitQuenched = buffer.readInt();
            boolean purityEnabled = buffer.readBoolean();
            return new ItemSettingsSyncMessage(
                    drinks,
                    foods,
                    blacklist,
                    containers,
                    keywordEnabled,
                    keywordBlacklist,
                    keywordDrink,
                    keywordSoup,
                    keywordFruit,
                    defaultDrinkHydration,
                    defaultDrinkQuenched,
                    defaultSoupHydration,
                    defaultSoupQuenched,
                    defaultFruitHydration,
                    defaultFruitQuenched,
                    purityEnabled
            );
        }

        @Override
        public void encode(ByteBuf buffer, ItemSettingsSyncMessage message)
        {
            writeEntries(buffer, message.drinks);
            writeEntries(buffer, message.foods);
            writeStrings(buffer, message.blacklist);
            writeStrings(buffer, message.containers);
            buffer.writeBoolean(message.keywordEnabled);
            writeString(buffer, message.keywordBlacklist);
            writeString(buffer, message.keywordDrink);
            writeString(buffer, message.keywordSoup);
            writeString(buffer, message.keywordFruit);
            buffer.writeInt(message.defaultDrinkHydration);
            buffer.writeInt(message.defaultDrinkQuenched);
            buffer.writeInt(message.defaultSoupHydration);
            buffer.writeInt(message.defaultSoupQuenched);
            buffer.writeInt(message.defaultFruitHydration);
            buffer.writeInt(message.defaultFruitQuenched);
            buffer.writeBoolean(message.purityEnabled);
        }
    };

    public static ItemSettingsSyncMessage create()
    {
        return new ItemSettingsSyncMessage(
                toEntries(ThirstHelper.VALID_DRINKS),
                toEntries(ThirstHelper.VALID_FOODS),
                List.copyOf(ItemSettingsConfig.ITEMS_BLACKLIST.get()),
                toItemIds(ThirstHelper.containers),
                KeyWordConfig.ENABLE_KEYWORD_CONFIG.get(),
                KeyWordConfig.KEYWORD_BLACKLIST.get(),
                KeyWordConfig.KEYWORD_DRINK.get(),
                KeyWordConfig.KEYWORD_SOUP.get(),
                KeyWordConfig.KEYWORD_FRUIT.get(),
                KeyWordConfig.getDrinkHydration(),
                KeyWordConfig.getDrinkQuenchness(),
                KeyWordConfig.getSoupHydration(),
                KeyWordConfig.getSoupQuenchness(),
                KeyWordConfig.getFruitHydration(),
                KeyWordConfig.getFruitQuenchness(),
                CommonConfig.ENABLE_PURITY.get()
        );
    }

    public static void serverHandle(final ItemSettingsSyncMessage message, final IPayloadContext context)
    {

    }

    public static void clientHandle(final ItemSettingsSyncMessage message, final IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            List<Item> syncedContainers = toItems(message.containers);
            ThirstHelper.applySyncedItemSettings(
                    toMap(message.drinks),
                    toMap(message.foods),
                    message.blacklist,
                    message.keywordEnabled,
                    message.keywordBlacklist,
                    message.keywordDrink,
                    message.keywordSoup,
                    message.keywordFruit,
                    message.defaultDrinkHydration,
                    message.defaultDrinkQuenched,
                    message.defaultSoupHydration,
                    message.defaultSoupQuenched,
                    message.defaultFruitHydration,
                    message.defaultFruitQuenched
            );
            ThirstHelper.applySyncedContainers(syncedContainers);
            ThirstHelper.applySyncedSettingsHash(message.contentHash());
            WaterPurity.applySyncedEnabled(message.purityEnabled);
            WaterPurity.applySyncedContainers(syncedContainers);
        });
    }

    private static List<Entry> toEntries(Map<Item, Number[]> source)
    {
        List<Entry> entries = new ArrayList<>();
        source.forEach((item, values) ->
                entries.add(new Entry(BuiltInRegistries.ITEM.getKey(item).toString(), values[0].intValue(), values[1].intValue())));
        entries.sort(Comparator.comparing(Entry::itemId));
        return entries;
    }

    private static List<String> toItemIds(List<Item> items)
    {
        List<String> entries = new ArrayList<>();
        for (Item item : items)
        {
            if (item != Items.AIR)
                entries.add(BuiltInRegistries.ITEM.getKey(item).toString());
        }
        entries.sort(String::compareTo);
        return entries;
    }

    private static Map<Item, Number[]> toMap(List<Entry> source)
    {
        Map<Item, Number[]> map = new HashMap<>();
        for (Entry entry : source)
        {
            Identifier id = Identifier.tryParse(entry.itemId);
            if (id == null)
                continue;

            Item item = BuiltInRegistries.ITEM.getValue(id);
            if (item != null)
                map.put(item, new Number[]{entry.thirst, entry.quenched});
        }
        return map;
    }

    private static List<Item> toItems(List<String> source)
    {
        List<Item> items = new ArrayList<>();
        for (String itemId : source)
        {
            Identifier id = Identifier.tryParse(itemId);
            if (id == null)
                continue;

            Item item = BuiltInRegistries.ITEM.getValue(id);
            if (item != Items.AIR)
                items.add(item);
        }
        return items;
    }

    private static void writeEntries(ByteBuf buffer, List<Entry> entries)
    {
        buffer.writeInt(entries.size());
        for (Entry entry : entries)
        {
            writeString(buffer, entry.itemId);
            buffer.writeInt(entry.thirst);
            buffer.writeInt(entry.quenched);
        }
    }

    private static List<Entry> readEntries(ByteBuf buffer)
    {
        int size = buffer.readInt();
        List<Entry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
        {
            entries.add(new Entry(readString(buffer), buffer.readInt(), buffer.readInt()));
        }
        return entries;
    }

    private static void writeStrings(ByteBuf buffer, List<String> strings)
    {
        buffer.writeInt(strings.size());
        for (String string : strings)
        {
            writeString(buffer, string);
        }
    }

    private static List<String> readStrings(ByteBuf buffer)
    {
        int size = buffer.readInt();
        List<String> strings = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
        {
            strings.add(readString(buffer));
        }
        return strings;
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
        if (length < 0 || length > 1_048_576)
            throw new IllegalArgumentException("Invalid synced string length: " + length);

        byte[] bytes = new byte[length];
        buffer.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static String createHash()
    {
        return create().contentHash();
    }

    public String contentHash()
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            updateEntries(digest, drinks);
            updateEntries(digest, foods);
            updateStrings(digest, blacklist);
            updateStrings(digest, containers);
            updateBoolean(digest, keywordEnabled);
            updateString(digest, keywordBlacklist);
            updateString(digest, keywordDrink);
            updateString(digest, keywordSoup);
            updateString(digest, keywordFruit);
            updateInt(digest, defaultDrinkHydration);
            updateInt(digest, defaultDrinkQuenched);
            updateInt(digest, defaultSoupHydration);
            updateInt(digest, defaultSoupQuenched);
            updateInt(digest, defaultFruitHydration);
            updateInt(digest, defaultFruitQuenched);
            updateBoolean(digest, purityEnabled);
            return HexFormat.of().formatHex(digest.digest());
        }
        catch (NoSuchAlgorithmException exception)
        {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static void updateEntries(MessageDigest digest, List<Entry> entries)
    {
        updateInt(digest, entries.size());
        for (Entry entry : entries)
        {
            updateString(digest, entry.itemId);
            updateInt(digest, entry.thirst);
            updateInt(digest, entry.quenched);
        }
    }

    private static void updateStrings(MessageDigest digest, List<String> strings)
    {
        updateInt(digest, strings.size());
        for (String string : strings)
        {
            updateString(digest, string);
        }
    }

    private static void updateString(MessageDigest digest, String string)
    {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        updateInt(digest, bytes.length);
        digest.update(bytes);
    }

    private static void updateBoolean(MessageDigest digest, boolean value)
    {
        digest.update((byte) (value ? 1 : 0));
    }

    private static void updateInt(MessageDigest digest, int value)
    {
        digest.update((byte) (value >>> 24));
        digest.update((byte) (value >>> 16));
        digest.update((byte) (value >>> 8));
        digest.update((byte) value);
    }
}
