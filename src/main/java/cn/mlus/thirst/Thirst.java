package cn.mlus.thirst;

import cn.mlus.thirst.content.purity.WaterPurity;
import cn.mlus.thirst.content.registry.ConditionInit;
import cn.mlus.thirst.content.registry.EffectInit;
import cn.mlus.thirst.content.registry.ItemInit;
import cn.mlus.thirst.content.registry.ThirstComponent;
import cn.mlus.thirst.content.thirst.PlayerThirst;
import cn.mlus.thirst.foundation.common.capability.ModAttachment;
import cn.mlus.thirst.foundation.config.*;
import cn.mlus.thirst.foundation.gui.appleskin.HUDOverlayHandler;
import cn.mlus.thirst.foundation.gui.appleskin.OverlayRegister;
import cn.mlus.thirst.foundation.gui.appleskin.TooltipOverlayHandler;
import cn.mlus.thirst.foundation.tab.ThirstTab;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;


@Mod(Thirst.ID)
public class Thirst
{
    public static final String ID = "thirst";

    public Thirst(IEventBus modBus, ModContainer modContainer)
    {

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);
        ModAttachment.ATTACHMENT_TYPES.register(modBus);
        ThirstComponent.DR.register(modBus);

        ItemInit.register(modBus);
        EffectInit.register(modBus);
        ConditionInit.CONDITION_CODECS.register(modBus);
        ConditionInit.LOOT_CONDITION_TYPES.register(modBus);

        if (FMLEnvironment.getDist().isClient() && ModList.get().isLoaded("appleskin")) {
            HUDOverlayHandler.init();
            TooltipOverlayHandler.init();
            modBus.addListener(this::registerTooltipComponents);
            modBus.addListener(OverlayRegister::onRenderGuiOverlayPost);
        }

        ThirstTab.register(modBus);
        //configs
        ItemSettingsConfig.setup(modContainer);
        CommonConfig.setup(modContainer);
        ClientConfig.setup(modContainer);
        KeyWordConfig.setup(modContainer);
        ContainerConfig.setup(modContainer);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(WaterPurity::init);

        if(ModList.get().isLoaded("tombstone"))
            PlayerThirst.checkTombstoneEffects = true;

        if(ModList.get().isLoaded("farmersdelight"))
            PlayerThirst.checkFDEffects = true;

        if(ModList.get().isLoaded("bakery"))
            PlayerThirst.checkLetsDoBakeryEffects = true;

        if(ModList.get().isLoaded("brewery"))
            PlayerThirst.checkLetsDoBreweryEffects = true;
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {
    }

    private void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event)
    {
        TooltipOverlayHandler.register(event);
    }

    public static Identifier asResource(String path)
    {
        return Identifier.fromNamespaceAndPath(ID, path);
    }
}
