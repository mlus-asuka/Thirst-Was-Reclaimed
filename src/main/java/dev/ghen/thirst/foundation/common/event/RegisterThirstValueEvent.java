package dev.ghen.thirst.foundation.common.event;

import net.minecraft.world.item.Item;

/**
 * @deprecated Compatibility bridge for addons compiled against the old package.
 * Use {@link cn.mlus.thirst.foundation.common.event.RegisterThirstValueEvent} instead.
 */
@Deprecated()
@SuppressWarnings({"unused"})
public class RegisterThirstValueEvent extends cn.mlus.thirst.foundation.common.event.RegisterThirstValueEvent
{
    public RegisterThirstValueEvent()
    {
        super();
    }

    public void addContainer(dev.ghen.thirst.content.purity.ContainerWithPurity container)
    {
        super.addContainer(container);
    }

    @Override
    public void addContainer(Item item)
    {
        super.addContainer(item);
    }
}
