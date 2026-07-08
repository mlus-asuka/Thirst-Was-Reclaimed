package cn.mlus.thirst.content.thirst;

import cn.mlus.thirst.api.ThirstHelper;
import cn.mlus.thirst.content.purity.WaterPurity;
import cn.mlus.thirst.foundation.common.capability.ModAttachment;
import cn.mlus.thirst.foundation.common.item.DrinkableItem;
import cn.mlus.thirst.foundation.config.CommonConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber
public class PlayerThirstManager {

    @SubscribeEvent
    public static void drinkByHand(PlayerInteractEvent.RightClickBlock event) {
        if (CommonConfig.CAN_DRINK_BY_HAND.get() && event.getEntity().level().isClientSide)
            DrinkByHandClient.drinkByHand();
    }

    @SubscribeEvent
    public static void drinkByHand(PlayerInteractEvent.RightClickEmpty event) {
        if (CommonConfig.CAN_DRINK_BY_HAND.get() && event.getEntity().level().isClientSide)
            DrinkByHandClient.drinkByHand();
    }

    @SubscribeEvent
    public static void drink(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player && ThirstHelper.itemRestoresThirst(event.getItem())) {
            ItemStack item = event.getItem();
            if(event.getItem().getItem() instanceof PotionItem)
                return;

            if (WaterPurity.givePurityEffects((Player) event.getEntity(), item)){
                if(event.getItem().getFoodProperties(null) != null)
                    return;
                if(event.getItem().getItem() instanceof DrinkableItem)
                    return;
                event.getEntity().getData(ModAttachment.PLAYER_THIRST).drink(ThirstHelper.getThirst(item), ThirstHelper.getQuenched(item));
            }

        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerThirst thirstData = serverPlayer.getData(ModAttachment.PLAYER_THIRST);
            thirstData.tick(serverPlayer);
            thirstData.updatePersistentData(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event){
        if(event.getEntity() instanceof ServerPlayer player){
            PlayerThirst thirstData = player.getData(ModAttachment.PLAYER_THIRST);
            thirstData.setThirst(20);
            thirstData.setQuenched(5);
            thirstData.updateThirstData(player);
        }
    }

    @SubscribeEvent
    public static void initDrinks(ServerStartedEvent event){
        ThirstHelper.init();
    }
}


