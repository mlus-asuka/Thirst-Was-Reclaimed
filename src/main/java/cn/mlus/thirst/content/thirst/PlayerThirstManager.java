package cn.mlus.thirst.content.thirst;

import cn.mlus.thirst.Thirst;
import cn.mlus.thirst.api.ThirstHelper;
import cn.mlus.thirst.foundation.common.capability.IThirst;
import cn.mlus.thirst.foundation.common.capability.ModCapabilities;
import cn.mlus.thirst.foundation.common.item.DrinkableItem;
import cn.mlus.thirst.foundation.config.CommonConfig;
import cn.mlus.thirst.foundation.network.ThirstModPacketHandler;
import cn.mlus.thirst.foundation.network.message.ItemSettingsHashMessage;
import cn.mlus.thirst.foundation.network.message.ItemSettingsSyncMessage;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.PotionItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class PlayerThirstManager
{

    @SubscribeEvent
    public static void attachCapabilityToEntityHandler(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player)
        {
            Player player = (Player) event.getObject();
            PlayerThirst playerThirstCap = new PlayerThirst();
            LazyOptional<IThirst> capOptional = LazyOptional.of(() -> playerThirstCap);
            Capability<IThirst> capability = ModCapabilities.PLAYER_THIRST;

            ICapabilityProvider provider = new ICapabilitySerializable<CompoundTag>()
            {
                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction direction)
                {
                    if (cap == capability)
                    {
                        return capOptional.cast();
                    }
                    return LazyOptional.empty();
                }

                @Override
                public CompoundTag serializeNBT()
                {
                    return playerThirstCap.serializeNBT();
                }

                @Override
                public void deserializeNBT(CompoundTag nbt)
                {
                    playerThirstCap.deserializeNBT(nbt);
                    playerThirstCap.updatePersistentData(player);
                }
            };

            event.addCapability(Thirst.asResource("thirst"), provider);
        }
    }

    @SubscribeEvent
    public static void drinkByHand(PlayerInteractEvent.RightClickBlock event)
    {
        if(CommonConfig.CAN_DRINK_BY_HAND.get() && event.getEntity().level().isClientSide)
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> DrinkByHandClient::drinkByHand);
    }

    @SubscribeEvent
    public static void drinkByHand(PlayerInteractEvent.RightClickEmpty event)
    {
        if(CommonConfig.CAN_DRINK_BY_HAND.get() && event.getEntity().level().isClientSide)
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> DrinkByHandClient::drinkByHand);
    }

    @SubscribeEvent
    public static void drink(LivingEntityUseItemEvent.Finish event)
    {
        if(event.getEntity() instanceof Player && ThirstHelper.itemRestoresThirst(event.getItem()))
        {
            if(event.getItem().getItem() instanceof PotionItem)
                return;
            if(event.getItem().getItem().isEdible())
                return;
            if(event.getItem().getItem() instanceof DrinkableItem)
                return;
            PlayerThirst.drink(event.getItem(), (Player) event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START && event.player instanceof ServerPlayer serverPlayer)
        {
            serverPlayer.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(cap -> {
                cap.tick(serverPlayer);
                if(cap instanceof PlayerThirst playerThirst)
                    playerThirst.updatePersistentData(serverPlayer);
            });
        }
    }

    /**
     * Adds the thirst capability to the player if they returned from the end
     * without dying.
     */
    @SubscribeEvent
    public static void endFix(PlayerEvent.Clone event)
    {
        if (!event.getEntity().level().isClientSide)
        {
            Player oldPlayer = event.getOriginal();
            oldPlayer.reviveCaps();

            if(!event.isWasDeath()) {
                event.getEntity().getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(cap ->
                        oldPlayer.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(oldCap -> {
                            cap.copy(oldCap);
                            cap.updateThirstData(event.getEntity());
                        }));
            }
            else {
                event.getEntity().getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(cap ->
                        oldPlayer.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(oldCap -> {
                            cap.setShouldTickThirst(oldCap.getShouldTickThirst());
                            cap.updateThirstData(event.getEntity());
                        }));
            }
            oldPlayer.invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void initDrinks(ServerStartedEvent event){
        ThirstHelper.init();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(cap -> cap.updateThirstData(player));
            ThirstModPacketHandler.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new ItemSettingsHashMessage(ItemSettingsSyncMessage.createHash())
            );
        }
    }
}
