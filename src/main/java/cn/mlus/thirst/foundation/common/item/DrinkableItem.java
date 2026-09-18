package cn.mlus.thirst.foundation.common.item;

import cn.mlus.thirst.content.purity.WaterPurity;
import cn.mlus.thirst.content.thirst.PlayerThirst;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class DrinkableItem extends Item
{
    private Supplier<? extends Item> container;

    public DrinkableItem()
    {
        super(new Properties().stacksTo(64));
    }

    public DrinkableItem(Properties p_41383_)
    {
        super(p_41383_);
    }

    public DrinkableItem setContainer(Supplier<? extends Item> item)
    {
        this.container = item;
        return this;
    }

    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack item, @NotNull Level level, @NotNull LivingEntity entity)
    {
        Player player = entity instanceof Player ? (Player)entity : null;

        if (player instanceof ServerPlayer)
        {
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)player, item);
        }
        if(player != null)
        {
            if(WaterPurity.givePurityEffects(player, item))
                PlayerThirst.drink(item, player);
        }

        if (player != null)
        {
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild)
            {
                item.shrink(1);
            }
        }

        if (player == null || !player.getAbilities().instabuild)
        {
            if (item.isEmpty())
            {
                return new ItemStack(container.get());
            }

            if (player != null)
            {
                ItemStack container = new ItemStack(this.container.get());
                if (!player.getInventory().add(container)) {
                    player.drop(container, false);
                }
            }
        }

        level.gameEvent(entity, GameEvent.ITEM_INTERACT_FINISH, entity.getEyePosition());
        return item;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity user) {
        return 32;
    }

    public @NotNull ItemUseAnimation getUseAnimation(@NotNull ItemStack p_42997_) {
        return ItemUseAnimation.DRINK;
    }

    public @NotNull InteractionResult use(@NotNull Level p_42993_, @NotNull Player p_42994_, @NotNull InteractionHand p_42995_)
    {
        return ItemUtils.startUsingInstantly(p_42993_, p_42994_, p_42995_);
    }
}
