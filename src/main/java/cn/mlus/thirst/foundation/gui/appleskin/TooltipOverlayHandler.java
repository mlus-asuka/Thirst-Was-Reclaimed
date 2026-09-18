package cn.mlus.thirst.foundation.gui.appleskin;

import cn.mlus.thirst.Thirst;
import cn.mlus.thirst.api.ThirstHelper;
import cn.mlus.thirst.foundation.gui.ThirstBarRenderer;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import squeek.appleskin.ModConfig;
import squeek.appleskin.helpers.KeyHelper;

public final class TooltipOverlayHandler {
    private static final Identifier MOD_ICONS = Thirst.asResource("textures/gui/appleskin_icons.png");

    private TooltipOverlayHandler() {
    }

    public static void init() {
        NeoForge.EVENT_BUS.addListener(TooltipOverlayHandler::gatherTooltips);
    }

    public static void register(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ThirstTooltip.class, ThirstTooltipRenderer::new);
    }

    private static void gatherTooltips(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (!event.isCanceled() && shouldShowTooltip(stack)) {
            ThirstTooltip tooltip = new ThirstTooltip(stack);
            if (tooltip.thirstBars > 0) event.getTooltipElements().add(Either.right(tooltip));
        }
    }

    private static boolean shouldShowTooltip(ItemStack stack) {
        boolean enabled = ModConfig.SHOW_FOOD_VALUES_IN_TOOLTIP.get() && KeyHelper.isShiftKeyDown()
                || ModConfig.ALWAYS_SHOW_FOOD_VALUES_TOOLTIP.get();
        return !stack.isEmpty() && enabled && ThirstHelper.itemRestoresThirst(stack);
    }

    static final class ThirstTooltip implements TooltipComponent {
        private final ItemStack stack;
        private final int thirstBars;
        private final int quenchedBars;
        private final String thirstText;
        private final String quenchedText;

        private ThirstTooltip(ItemStack stack) {
            this.stack = stack.copy();
            int thirst = ThirstHelper.getThirst(stack);
            float quenched = ThirstHelper.getQuenched(stack);
            int rawThirstBars = (int) Math.ceil(Math.abs(thirst) / 2.0F);
            int rawQuenchedBars = (int) Math.ceil(Math.abs(quenched) / 2.0F);
            this.thirstBars = rawThirstBars > 10 ? 1 : rawThirstBars;
            this.quenchedBars = rawQuenchedBars > 10 || rawQuenchedBars == 0 ? 1 : rawQuenchedBars;
            this.thirstText = rawThirstBars > 10 ? "x" + rawThirstBars : null;
            this.quenchedText = rawQuenchedBars > 10 || rawQuenchedBars == 0 ? "x" + rawQuenchedBars : null;
        }
    }

    static final class ThirstTooltipRenderer implements ClientTooltipComponent {
        private final ThirstTooltip tooltip;

        private ThirstTooltipRenderer(ThirstTooltip tooltip) {
            this.tooltip = tooltip;
        }

        @Override
        public int getHeight(@NotNull Font font) {
            return 20;
        }

        @Override
        public int getWidth(@NotNull Font font) {
            int thirstWidth = tooltip.thirstBars * 9 + (tooltip.thirstText == null ? 0 : font.width(tooltip.thirstText));
            int quenchedWidth = tooltip.quenchedBars * 7 + (tooltip.quenchedText == null ? 0 : font.width(tooltip.quenchedText));
            return Math.max(thirstWidth, quenchedWidth) + 2;
        }

        @Override
        public void extractImage(@NotNull Font font, int x, int y, int width, int height, @NotNull GuiGraphicsExtractor graphics) {
            if (!shouldShowTooltip(tooltip.stack)) return;

            int thirst = ThirstHelper.getThirst(tooltip.stack);
            int iconX = x + (tooltip.thirstBars - 1) * 9;
            for (int i = 0; i < tooltip.thirstBars * 2; i += 2) {
                int u = thirst == i + 1 ? 8 : 16;
                graphics.blit(RenderPipelines.GUI_TEXTURED, ThirstBarRenderer.THIRST_ICONS, iconX, y, u, 0, 9, 9, 25, 9);
                iconX -= 9;
            }
            if (tooltip.thirstText != null) graphics.text(font, tooltip.thirstText, x + 11, y + 1, 0xFFAAAAAA);

            float quenched = ThirstHelper.getQuenched(tooltip.stack);
            float absolute = Math.abs(quenched);
            iconX = x + (tooltip.quenchedBars - 1) * 7;
            for (int i = 0; i < tooltip.quenchedBars * 2; i += 2) {
                float amount = (absolute - i) / 2.0F;
                int u = amount >= 1 ? 21 : amount > .5F ? 14 : amount > .25F ? 7 : amount > 0 ? 0 : 28;
                int color = absolute <= i ? 0x80FFFFFF : 0xFFFFFFFF;
                graphics.blit(RenderPipelines.GUI_TEXTURED, MOD_ICONS, iconX, y + 10, u, quenched >= 0 ? 27 : 34, 7, 7, 256, 256, color);
                iconX -= 7;
            }
            if (tooltip.quenchedText != null) graphics.text(font, tooltip.quenchedText, x + 9, y + 10, 0xFFAAAAAA);
        }
    }
}
