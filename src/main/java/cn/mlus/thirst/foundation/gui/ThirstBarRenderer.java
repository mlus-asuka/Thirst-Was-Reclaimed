package cn.mlus.thirst.foundation.gui;

import cn.mlus.thirst.Thirst;
import cn.mlus.thirst.foundation.common.capability.IThirst;
import cn.mlus.thirst.foundation.common.capability.ModAttachment;
import cn.mlus.thirst.foundation.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class ThirstBarRenderer
{
    public static IThirst PLAYER_THIRST = null;
    public static Identifier THIRST_ICONS = Thirst.asResource("textures/gui/thirst_icons.png");
    public static Boolean cancelRender = false;
    private static int lastBarRight;
    private static int lastBarTop;
    private static boolean renderedThisFrame;
    protected final static RandomSource random = RandomSource.create();

    @SubscribeEvent
    public static void onBeginRenderAir(RenderGuiEvent.Pre event)
    {
        if (event.getType() != RenderGuiEvent.Type.AIR)
            return;

        Minecraft minecraft = Minecraft.getInstance();
        renderedThisFrame = false;
        cancelRender = !shouldRender(minecraft);
        if (cancelRender)
            return;

        render(event.getScreenWidth(),event.getScreenHeight(),event.getGuiGraphics());
    }

    public static boolean shouldRender(Minecraft minecraft)
    {
        if (minecraft == null || minecraft.player == null)
            return false;

        Entity vehicle = minecraft.player.getVehicle();
        boolean isMounted = vehicle != null && vehicle.showVehicleHealth();
        if (isMounted || minecraft.options.hideGui || minecraft.gameMode == null || !minecraft.gameMode.canHurtPlayer())
            return false;

        return minecraft.player.isAlive() && minecraft.player.getData(ModAttachment.PLAYER_THIRST).getShouldTickThirst();
    }

    public static int getBarRight(int width)
    {
        return renderedThisFrame ? lastBarRight : width / 2 + 91 + ClientConfig.THIRST_BAR_X_OFFSET.get();
    }

    public static int getBarTop(int height)
    {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.gui == null)
            return height - 39 + ClientConfig.THIRST_BAR_Y_OFFSET.get();

        return renderedThisFrame ? lastBarTop : height - minecraft.gui.rightHeight + ClientConfig.THIRST_BAR_Y_OFFSET.get();
    }

    public static void render(int width, int height, GuiGraphicsExtractor guiGraphics)
    {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null || minecraft.gui == null)
            return;

        PLAYER_THIRST = minecraft.player.getData(ModAttachment.PLAYER_THIRST);

        Identifier thirst_icons = THIRST_ICONS;

        int left = width / 2 + 91 + ClientConfig.THIRST_BAR_X_OFFSET.get();
        int top = height - minecraft.gui.rightHeight + ClientConfig.THIRST_BAR_Y_OFFSET.get();
        minecraft.gui.rightHeight += 10;
        lastBarRight = left;
        lastBarTop = top;
        renderedThisFrame = true;

        int level = PLAYER_THIRST.getThirst();

        for (int i = 0; i < 10; ++i)
        {
            int idx = i * 2 + 1;
            int x = left - i * 8 - 9;
            int y = top;

            if (PLAYER_THIRST.getQuenched() <= 0.0F && minecraft.gui.getGuiTicks() % (level * 3 + 1) == 0)
            {
                y = top + (random.nextInt(3) - 1);
            }

            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, thirst_icons, x, y, 0, 0, 9, 9, 25, 9);

            if (idx < level)
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, thirst_icons, x, y, 16, 0, 9, 9, 25, 9);
            else if (idx == level)
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, thirst_icons, x, y, 8, 0, 9, 9, 25, 9);
        }
    }
}
