package cn.mlus.thirst.compat.supernatural;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.salju.supernatural.SupernaturalMod;
import net.salju.supernatural.events.SupernaturalManager;
import net.salju.supernatural.init.SupernaturalEffects;
import net.salju.supernatural.init.SupernaturalItems;

public class SupernaturalHelper {
    public static ResourceLocation VAMPIRE_THIRST_ICONS = ResourceLocation.fromNamespaceAndPath(SupernaturalMod.MODID, "textures/gui/thirst_icons.png");
    private static final ResourceLocation appleskinIcons = ResourceLocation.fromNamespaceAndPath(SupernaturalMod.MODID, "textures/gui/appleskin_icons.png");

    public static boolean canDrinkItem(ItemStack stack, Player player) {
        if (isVampireCheck(player)) {
            return isBloodCheck(stack);
        } else {
            return true;
        }
    }

    //Checks for Persistent NBT Player Data for Vampirism. Serverside only.
    public static boolean isVampireCheck(Player player) {
        return SupernaturalManager.isVampire(player);
    }

    //Checks if the Player has the Vampirism MobEffect. Works on Clientside & Serverside.
    public static boolean hasVampirismCheck(Player player) {
        return player.hasEffect(SupernaturalEffects.SUPERNATURAL.get());
    }

    //Checks if the item is tagged as Blood. By default, it is just the Blood o' Blood item.
    public static boolean isBloodCheck(ItemStack stack) {
        return stack.is(SupernaturalItems.BLOOD.get());
    }

    //Grabs the Bottle o' Blood item.
    public static Item getBloodBottle() {
        return SupernaturalItems.BLOOD.get();
    }

    //Grabs new Vampire Thirst Icons for Player.
    public static ResourceLocation getVampireIcons(ResourceLocation original, Player player) {
        if (hasVampirismCheck(player)) {
            return VAMPIRE_THIRST_ICONS;
        }
        return original;
    }

    //Grabs new Vampire Thirst Icons for ItemStack.
    public static ResourceLocation getVampireIcons(ResourceLocation original, ItemStack stack) {
        if (isBloodCheck(stack)) {
            return VAMPIRE_THIRST_ICONS;
        }
        return original;
    }

    //Grabs new Vampire Thirst Appleskin Icons for Player.
    public static ResourceLocation getVampireAppleskinIcons(ResourceLocation original, Player player) {
        if (hasVampirismCheck(player)) {
            return appleskinIcons;
        }
        return original;
    }

    //Grabs new Vampire Thirst Appleskin Icons for ItemStack.
    public static ResourceLocation getVampireAppleskinIcons(ResourceLocation original, ItemStack stack) {
        if (isBloodCheck(stack)) {
            return appleskinIcons;
        }
        return original;
    }
}
