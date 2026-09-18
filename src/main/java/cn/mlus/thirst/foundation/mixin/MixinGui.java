package cn.mlus.thirst.foundation.mixin;

import cn.mlus.thirst.foundation.gui.RenderGuiEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class MixinGui
{

    @Unique
    private DeltaTracker deltaTracker;
    @Shadow
    public int rightHeight;

    @Inject(method="extractRenderState", at=@At(value="HEAD"))
    public void onRender(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci)
    {
        this.deltaTracker = deltaTracker;
    }

    @Inject(method="extractCameraOverlays", at=@At(value="HEAD"))
    private void onBeginRenderFrozenOverlay(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci)
    {
        NeoForge.EVENT_BUS.post(new RenderGuiEvent.Pre(RenderGuiEvent.Type.FROSTBITE, (Gui)(Object)this, guiGraphics, this.deltaTracker, guiGraphics.guiWidth(), guiGraphics.guiHeight()));
    }

    @Inject(method="extractFoodLevel", at=@At(value="HEAD"))
    private void onRenderPlayerHealth(GuiGraphicsExtractor guiGraphics, CallbackInfo ci)
    {
        NeoForge.EVENT_BUS.post(new RenderGuiEvent.Pre(RenderGuiEvent.Type.FOOD, (Gui)(Object)this, guiGraphics, this.deltaTracker, guiGraphics.guiWidth(), guiGraphics.guiHeight()));
    }

    @Inject(method="extractAirLevel", at=@At(value="HEAD"))
    private void onBeginRenderAir(GuiGraphicsExtractor guiGraphics, CallbackInfo ci)
    {
        NeoForge.EVENT_BUS.post(new RenderGuiEvent.Pre(RenderGuiEvent.Type.AIR, (Gui)(Object)this, guiGraphics, this.deltaTracker, guiGraphics.guiWidth(), guiGraphics.guiHeight(),rightHeight - 10));
    }
}
