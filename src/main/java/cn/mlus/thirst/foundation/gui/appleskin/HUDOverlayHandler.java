package cn.mlus.thirst.foundation.gui.appleskin;

import cn.mlus.thirst.Thirst;
import cn.mlus.thirst.api.ThirstHelper;
import cn.mlus.thirst.foundation.common.capability.IThirst;
import cn.mlus.thirst.foundation.common.capability.ModAttachment;
import cn.mlus.thirst.foundation.gui.ThirstBarRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import squeek.appleskin.ModConfig;
import squeek.appleskin.util.IntPoint;

import java.util.Vector;

public final class HUDOverlayHandler {
    private static final Identifier MOD_ICONS = Thirst.asResource("textures/gui/appleskin_icons.png");
    private static final Vector<IntPoint> BAR_OFFSETS = new Vector<>();
    private static final RandomSource RANDOM = RandomSource.create();
    private static float unclampedFlashAlpha;
    private static float flashAlpha;
    private static byte alphaDirection = 1;

    private HUDOverlayHandler() {
    }

    public static void init() {
        NeoForge.EVENT_BUS.addListener(HUDOverlayHandler::onClientTick);
    }

    public static class ExhaustionOverlay extends squeek.appleskin.client.HUDOverlayHandler.Overlay {
        public static final Identifier ID = Thirst.asResource("exhaustion_overlay");

        @Override
        public void render(Minecraft minecraft, Player player, GuiGraphicsExtractor graphics, int left, int right, int top, int guiTicks) {
            if (ThirstBarRenderer.shouldRender(minecraft) && ModConfig.SHOW_FOOD_EXHAUSTION_UNDERLAY.get()) {
                int barRight = ThirstBarRenderer.getBarRight(graphics.guiWidth());
                int barTop = ThirstBarRenderer.getBarTop(graphics.guiHeight());
                drawExhaustionOverlay(player.getData(ModAttachment.PLAYER_THIRST).getExhaustion(), graphics, barRight, barTop);
            }
        }
    }

    public static class SaturationOverlay extends squeek.appleskin.client.HUDOverlayHandler.Overlay {
        public static final Identifier ID = Thirst.asResource("saturation_overlay");

        @Override
        public void render(Minecraft minecraft, Player player, GuiGraphicsExtractor graphics, int left, int right, int top, int guiTicks) {
            if (ThirstBarRenderer.shouldRender(minecraft)) {
                renderThirstOverlay(player, graphics, guiTicks);
            }
        }
    }

    private static void renderThirstOverlay(Player player, GuiGraphicsExtractor graphics, int guiTicks) {
        IThirst thirst = player.getData(ModAttachment.PLAYER_THIRST);
        int right = ThirstBarRenderer.getBarRight(graphics.guiWidth());
        int top = ThirstBarRenderer.getBarTop(graphics.guiHeight());
        generateBarOffsets(guiTicks, thirst);

        if (ModConfig.SHOW_SATURATION_OVERLAY.get()) {
            drawSaturationOverlay(0, thirst.getQuenched(), graphics, right, top, 1.0F);
        }

        ItemStack heldItem = player.getMainHandItem();
        if (ModConfig.SHOW_FOOD_VALUES_OVERLAY_WHEN_OFFHAND.get() && !ThirstHelper.itemRestoresThirst(heldItem)) {
            heldItem = player.getOffhandItem();
        }

        if (heldItem.isEmpty() || !ThirstHelper.itemRestoresThirst(heldItem)) {
            resetFlash();
            return;
        }

        int restoredThirst = ThirstHelper.getThirst(heldItem);
        float restoredQuenched = ThirstHelper.getQuenched(heldItem);
        if (thirst.getThirst() < 20) {
            drawThirstOverlay(restoredThirst, thirst.getThirst(), graphics, right, top, flashAlpha);
        }
        if (!ThirstHelper.isFood(heldItem) || player.getFoodData().getFoodLevel() < 20) {
            drawSaturationOverlay(restoredQuenched, thirst.getQuenched(), graphics, right, top, flashAlpha);
        }
    }

    private static void drawSaturationOverlay(float gained, float current, GuiGraphicsExtractor graphics, int right, int top, float alpha) {
        float value = Math.max(0, Math.min(current + gained, 20));
        int start = gained == 0 ? 0 : Math.max((int) current / 2, 0);
        int end = (int) Math.ceil(value / 2.0F);
        int color = color(alpha);

        for (int i = start; i < end && i < BAR_OFFSETS.size(); i++) {
            IntPoint offset = BAR_OFFSETS.get(i);
            if (offset == null) continue;
            float amount = value / 2.0F - i;
            int u = amount >= 1 ? 27 : amount > .5F ? 18 : amount > .25F ? 9 : 0;
            graphics.blit(RenderPipelines.GUI_TEXTURED, MOD_ICONS, right + offset.x, top + offset.y, u, 0, 9, 9, 256, 256, color);
        }
    }

    private static void drawThirstOverlay(int restored, int current, GuiGraphicsExtractor graphics, int right, int top, float alpha) {
        if (restored <= 0) return;
        int value = Math.max(0, Math.min(20, current + restored));
        int start = Math.max(0, current / 2);
        int end = (int) Math.ceil(value / 2.0F);
        int color = color(alpha);

        for (int i = start; i < end && i < BAR_OFFSETS.size(); i++) {
            IntPoint offset = BAR_OFFSETS.get(i);
            if (offset == null) continue;
            int u = i * 2 + 1 == value ? 8 : 16;
            graphics.blit(RenderPipelines.GUI_TEXTURED, ThirstBarRenderer.THIRST_ICONS, right + offset.x, top + offset.y, u, 0, 9, 9, 25, 9, color);
        }
    }

    private static void drawExhaustionOverlay(float exhaustion, GuiGraphicsExtractor graphics, int right, int top) {
        int width = (int) (Math.clamp(exhaustion / 4.0F, 0, 1) * 81);
        if (width > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, MOD_ICONS, right - width, top, 81 - width, 18, width, 9, 256, 256, color(.75F));
        }
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        unclampedFlashAlpha += alphaDirection * 0.125F;
        if (unclampedFlashAlpha >= 1.5F) alphaDirection = -1;
        else if (unclampedFlashAlpha <= -0.5F) alphaDirection = 1;
        flashAlpha = Math.clamp(unclampedFlashAlpha, 0, 1) * ModConfig.MAX_HUD_OVERLAY_FLASH_ALPHA.get().floatValue();
    }

    private static void resetFlash() {
        unclampedFlashAlpha = flashAlpha = 0;
        alphaDirection = 1;
    }

    private static void generateBarOffsets(int ticks, IThirst thirst) {
        if (BAR_OFFSETS.size() != 10) BAR_OFFSETS.setSize(10);
        boolean animate = thirst.getQuenched() <= 0 && ticks % (thirst.getThirst() * 3 + 1) == 0;
        for (int i = 0; i < 10; i++) {
            IntPoint point = BAR_OFFSETS.get(i);
            if (point == null) {
                point = new IntPoint();
                BAR_OFFSETS.set(i, point);
            }
            point.x = -i * 8 - 9;
            point.y = animate ? RANDOM.nextInt(3) - 1 : 0;
        }
    }

    private static int color(float alpha) {
        return Math.round(Math.clamp(alpha, 0, 1) * 255) << 24 | 0xFFFFFF;
    }
}
