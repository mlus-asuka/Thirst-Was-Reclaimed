package dev.ghen.thirst.content.purity;

import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * @deprecated Compatibility bridge for addons compiled against the old package.
 * Use {@link cn.mlus.thirst.content.purity.ContainerWithPurity} instead.
 */
@Deprecated()
@SuppressWarnings("unused")
public class ContainerWithPurity extends cn.mlus.thirst.content.purity.ContainerWithPurity
{
    public ContainerWithPurity(ItemStack emptyItem, ItemStack filledItem)
    {
        super(emptyItem, filledItem);
    }

    public ContainerWithPurity(ItemStack emptyItem, ItemStack filledItem, boolean isDrinkable)
    {
        super(emptyItem, filledItem, isDrinkable);
    }

    public ContainerWithPurity(ItemStack filledItem)
    {
        super(filledItem);
    }

    @Override
    public ContainerWithPurity canHarvestRunningWater(boolean canHarvestRunningWater)
    {
        super.canHarvestRunningWater(canHarvestRunningWater);
        return this;
    }

    @Override
    public ContainerWithPurity setEqualsEmpty(Predicate<ItemStack> predicate)
    {
        super.setEqualsEmpty(predicate);
        return this;
    }

    @Override
    public ContainerWithPurity setEqualsFilled(Predicate<ItemStack> predicate)
    {
        super.setEqualsFilled(predicate);
        return this;
    }
}
