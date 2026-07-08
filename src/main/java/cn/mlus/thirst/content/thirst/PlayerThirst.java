package cn.mlus.thirst.content.thirst;

import cn.mlus.thirst.api.ThirstHelper;
import cn.mlus.thirst.content.purity.WaterPurity;
import cn.mlus.thirst.foundation.common.capability.IThirst;
import cn.mlus.thirst.foundation.common.capability.ModCapabilities;
import cn.mlus.thirst.foundation.common.damagesource.ModDamageSource;
import cn.mlus.thirst.foundation.config.CommonConfig;
import cn.mlus.thirst.foundation.network.ThirstModPacketHandler;
import cn.mlus.thirst.foundation.network.message.PlayerThirstSyncMessage;
import de.teamlapen.vampirism.util.Helper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import vectorwing.farmersdelight.common.registry.ModEffects;

public class PlayerThirst implements IThirst
{
    public static final String PERSISTENT_THIRST_KEY = "thirst";
    public static final String PERSISTENT_QUENCHED_KEY = "thirst_quenched";
    public static final String PERSISTENT_EXHAUSTION_KEY = "thirst_exhaustion";
    public static final String PERSISTENT_ENABLED_KEY = "thirst_enabled";
    public static final String PERSISTENT_DATA_KEY = "thirst:player_thirst";

    public static boolean checkTombstoneEffects = false;
    public static boolean checkFDEffects = false;
    public static boolean checkLetsDoBakeryEffects = false;
    public static boolean checkLetsDoBreweryEffects = false;
    public static boolean checkVampirismEffects = false;

    int thirst = 20;
    int quenched = 5;
    float exhaustion = 0;
    int damageTimer = 0;
    int syncTimer = 0;
    float prevTickExhaustion = 0.0F;
    boolean justHealed = false;
    boolean shouldTickThirst = true;
    boolean exhaustionRecalculate = false;
    boolean init = true;

    /**
     * Attempts to give hydration to player if item restores thirst.
     * @param item
     * @param player
     */
    public static void drink(ItemStack item, Player player)
    {
        if(ThirstHelper.itemRestoresThirst(item) && ThirstHelper.playerRestoresThirst(item, player))
        {
            player.getCapability(ModCapabilities.PLAYER_THIRST,null).ifPresent(cap ->
            {
                if(WaterPurity.givePurityEffects(player, item))
                    cap.drink(player, ThirstHelper.getThirst(item), ThirstHelper.getQuenched(item));
            });
        }
    }

    public int getThirst()
    {
        return thirst;
    }

    public void setThirst(int value)
    {
        thirst = value;
    }

    public int getQuenched()
    {
        return quenched;
    }

    public void setQuenched(int value)
    {
        quenched = value;
    }

    public float getExhaustion()
    {
        return exhaustion;
    }

    public void setExhaustion(float value)
    {
        exhaustion = value;
    }

    @Override
    public void setShouldTickThirst(boolean value){shouldTickThirst = value;}
    @Override
    public boolean getShouldTickThirst(){return shouldTickThirst;}

    public void drink(Player player, int thirst, int quenched)
    {
        int extra_quenched = Math.max(this.thirst + thirst - 20, 0);
        if(!CommonConfig.EXTRA_HYDRATION_CONVERT_TO_QUENCHED.get())
            extra_quenched = 0;
        this.thirst = Math.min(this.thirst + thirst, 20);
        this.quenched = Math.min(this.quenched + quenched + extra_quenched, this.thirst);
        updatePersistentData(player);
    }

    /**
    * Method adapted from minecraft's Food Data class equivalent for hunger.
    */
    public void tick(Player player)
    {
        Difficulty difficulty = player.level().getDifficulty();

        if(player.getAbilities().invulnerable)
            return;

        if(!shouldTickThirst) {
            if (init) {
                init = false;
                updateThirstData(player);
            }
            return;
        }

        if(checkTombstoneEffects && player.getActiveEffects().stream().anyMatch(e -> e.getDescriptionId().contains("ghostly_shape")))
            return;

        if(checkVampirismEffects && Helper.isVampire(player))
            return;

        boolean isNourished = checkFDEffects && player.hasEffect(ModEffects.NOURISHMENT.get());
        boolean isHunger = player.hasEffect(MobEffects.HUNGER);
        boolean isStuffed = checkLetsDoBakeryEffects &&
                player.getActiveEffects().stream().anyMatch(e -> e.getDescriptionId().contains("stuffed"));
        boolean isSaturated = checkLetsDoBreweryEffects &&
                player.getActiveEffects().stream().anyMatch(e -> e.getDescriptionId().contains("saturated"));
        boolean isSitting = player.isPassenger();

        if(CommonConfig.DEPLETES_WHEN_NAUSEA.get() && player.getActiveEffects().stream().anyMatch(e->e.getEffect().equals(MobEffects.CONFUSION))){
            addExhaustion(player,0.06F);
        }

        if(isHunger){
            exhaustion -= 0.005F * (float)(player.getEffect(MobEffects.HUNGER).getAmplifier() + 1) *
                    ThirstHelper.getExhaustionBiomeModifier(player) *
                    ThirstHelper.getExhaustionFireProtModifier(player)*
                    ThirstHelper.getExhaustionFireResistanceModifier(player);
        }

        if (!isSitting && !isNourished && !isStuffed && !isSaturated)
        {
            updateExhaustion(player);
        }

        if (exhaustion > 4)
        {
            exhaustion -= 4;
            if (quenched > 0)
            {
                quenched--;
            }
            else if (difficulty != Difficulty.PEACEFUL || CommonConfig.THIRST_DEPLETION_IN_PEACEFUL.get())
            {
                thirst = Math.max(thirst - 1, 0);
            }
        }

        ++syncTimer;
        if(syncTimer > 10 && !player.level().isClientSide())
        {
            if(difficulty == Difficulty.PEACEFUL && !CommonConfig.THIRST_DEPLETION_IN_PEACEFUL.get()){
                thirst = Math.min(thirst + 1,20);
            }

            final float angle = Mth.wrapDegrees(player.getXRot());
            if (angle <= -80  && player.level().isRainingAt(player.blockPosition().above()) && CommonConfig.CAN_DRINK_RAIN_WATETR.get())
            {
                thirst = Math.min(thirst + 1,20);
                quenched = Math.min(quenched +1,20);
            }

            updateThirstData(player);
            syncTimer = 0;
        }

        if (thirst <= 0)
        {
            ++damageTimer;
            if (damageTimer >= 40)
            {
                if (player.getHealth() > 10.0F || difficulty == Difficulty.HARD || player.getHealth() > 0 && difficulty == Difficulty.NORMAL)
                {
                    player.hurt(ModDamageSource.getDamageSource(player.level(),ModDamageSource.DIE_OF_THIRST_KEY), 1.0F);
                }

                damageTimer = 0;
            }
        }
    }

    void updateExhaustion(Player player)
    {
        float hungerExhaustion = player.getFoodData().getExhaustionLevel();
        float normalizedHungerExhaustion = hungerExhaustion < this.prevTickExhaustion ? (exhaustionRecalculate ? hungerExhaustion + 4.0F : hungerExhaustion) : hungerExhaustion;
        if(exhaustionRecalculate){
            exhaustionRecalculate = false;
        }
        float deltaExhaustion = normalizedHungerExhaustion - this.prevTickExhaustion;
        this.addExhaustion(player, deltaExhaustion);
        this.prevTickExhaustion = hungerExhaustion;
    }

    public void updateThirstData(Player player)
    {
        updatePersistentData(player);
        ThirstModPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                new PlayerThirstSyncMessage(thirst, quenched, exhaustion,shouldTickThirst));
    }

    public void updatePersistentData(Player player)
    {
        CompoundTag persistentData = player.getPersistentData();
        persistentData.putInt(PERSISTENT_THIRST_KEY, thirst);
        persistentData.putInt(PERSISTENT_QUENCHED_KEY, quenched);
        persistentData.putFloat(PERSISTENT_EXHAUSTION_KEY, exhaustion);
        persistentData.putBoolean(PERSISTENT_ENABLED_KEY, shouldTickThirst);

        CompoundTag thirstData = new CompoundTag();
        thirstData.putInt("thirst", thirst);
        thirstData.putInt("quenched", quenched);
        thirstData.putFloat("exhaustion", exhaustion);
        thirstData.putBoolean("enable", shouldTickThirst);
        persistentData.put(PERSISTENT_DATA_KEY, thirstData);
    }

    @Override
    public void setJustHealed()
    {
        justHealed = true;
    }

    @Override
    public void ExhaustionRecalculate(){exhaustionRecalculate = true;}

    @Override
    public void copy(IThirst cap)
    {
        thirst = cap.getThirst();
        quenched = cap.getQuenched();
        exhaustion = cap.getExhaustion();
        shouldTickThirst = cap.getShouldTickThirst();
    }

    public void addExhaustion(Player player, float amount)
    {
        if(!CommonConfig.HEALTH_REGEN_DEPLETES_HYDRATION.get() && justHealed)
            amount = 0;

        if(!CommonConfig.HEALTH_REGEN_DEHYDRATION_IS_BIOME_DEPENDENT.get() && justHealed)
            exhaustion += amount;
        else
            exhaustion += (amount *
                    ThirstHelper.getExhaustionBiomeModifier(player) *
                    ThirstHelper.getExhaustionFireProtModifier(player)*
                    ThirstHelper.getExhaustionFireResistanceModifier(player)
            );

        if(justHealed)
            justHealed = false;

        updateThirstData(player);
    }

    public CompoundTag serializeNBT()
    {
        CompoundTag nbt = new CompoundTag();

        nbt.putInt("thirst", thirst);
        nbt.putInt("quenched", quenched);
        nbt.putFloat("exhaustion", exhaustion);
        nbt.putBoolean("enable",shouldTickThirst);

        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt)
    {
        thirst = nbt.getInt("thirst");
        quenched = nbt.getInt("quenched");
        exhaustion = nbt.getFloat("exhaustion");
        shouldTickThirst = !nbt.contains("enable") || nbt.getBoolean("enable");
    }
}
