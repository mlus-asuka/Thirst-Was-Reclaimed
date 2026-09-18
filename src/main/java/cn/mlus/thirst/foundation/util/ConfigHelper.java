package cn.mlus.thirst.foundation.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.*;

public class ConfigHelper
{
    /**
     * This class was taken from <a href="https://github.com/Momo-Studios/Cold-Sweat/blob/1.18.x-FG/src/main/java/dev/momostudios/coldsweat/util/config/ConfigHelper.java">Cold Sweat</a>
     */

    public static Map<Item, Number[]> getItemsWithValues(List<? extends List<?>> source)
    {
        Map<Item, Number[]> map = new HashMap<>();
        for (List<?> entry : source)
        {
            String itemID = (String) entry.get(0);

            if (itemID.startsWith("#"))
            {
                final String tagID = itemID.replace("#", "");
                Optional<HolderSet.Named<Item>> optionalTag = BuiltInRegistries.ITEM.getTags().filter(tag ->
                        tag.key().location().toString().equals(tagID)).findFirst();
                optionalTag.ifPresent(itemITag ->
                {
                    for (Holder<Item> item : itemITag.stream().toList())
                    {
                        map.put(item.value(), new Number[]{(Number) entry.get(1), (Number) entry.get(2)});
                    }
                });
            }
            else
            {
                Item newItem = BuiltInRegistries.ITEM.getValue(Identifier.tryParse(itemID));

                if (newItem != null) map.put(newItem, new Number[]{(Number) entry.get(1), (Number) entry.get(2)});
            }
        }
        return map;
    }

    public static List<Item> getItems(List<? extends String> source){
        List<Item> list = new ArrayList<>();
        for(String itemID : source){
            if (itemID.startsWith("#"))
            {
                final String tagID = itemID.replace("#", "");
                Optional<HolderSet.Named<Item>> optionalTag = BuiltInRegistries.ITEM.getTags().filter(tag ->
                        tag.key().location().toString().equals(tagID)).findFirst();
                optionalTag.ifPresent(itemITag ->
                        {
                            for (Holder<Item> item : itemITag.stream().toList()) {
                                list.add(item.value());
                            }
                        });
            }
            else
            {
                Item newItem = BuiltInRegistries.ITEM.getValue(Identifier.tryParse(itemID));

                if (newItem != null) list.add(newItem);
            }
        }
        return list;
    }
}
