package cn.mlus.thirst.compat.sophisticated;

import cn.mlus.thirst.content.purity.WaterPurity;
import cn.mlus.thirst.content.registry.ThirstComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

public final class SophisticatedPurity {
    private SophisticatedPurity() {}

    public static boolean canStackIgnoringPurity(ItemStack first, ItemStack second) {
        if (!WaterPurity.isEnabled() || !isWaterBottle(first) || !isWaterBottle(second)) {
            return ItemStack.isSameItemSameComponents(first, second);
        }

        ItemStack firstCopy = first.copy();
        ItemStack secondCopy = second.copy();
        firstCopy.remove(ThirstComponent.PURITY);
        secondCopy.remove(ThirstComponent.PURITY);
        return ItemStack.isSameItemSameComponents(firstCopy, secondCopy);
    }

    private static boolean isWaterBottle(ItemStack stack) {
        return stack.is(Items.POTION)
                && stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER);
    }
}
