package cn.mlus.thirst.compat.sophisticated;

import cn.mlus.thirst.content.purity.WaterPurity;
import cn.mlus.thirst.content.registry.ThirstComponent;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.Optional;

@GameTestHolder("thirst")
@PrefixGameTestTemplate(false)
public class SophisticatedPurityGameTests {
    @SuppressWarnings("unchecked")
    private static IFluidHandlerItem handler(ItemStack stack) throws Exception {
        var method = Class.forName("net.p3pp3rf1y.sophisticatedcore.upgrades.tank.TankUpgradeWrapper")
                .getDeclaredMethod("getCustomFluidHandler", ItemStack.class);
        method.setAccessible(true);
        return ((Optional<IFluidHandlerItem>) method.invoke(null, stack)).orElseThrow();
    }

    @GameTest(template = "empty", templateNamespace = "thirst")
    public static void bottlesPreservePurity(GameTestHelper test) throws Exception {
        for (int purity = 0; purity <= 3; purity++) {
            FluidStack water = new FluidStack(Fluids.WATER, 1000);
            WaterPurity.addPurity(water, purity);
            IFluidHandlerItem empty = handler(new ItemStack(Items.GLASS_BOTTLE));
            test.assertTrue(empty.fill(water, IFluidHandler.FluidAction.SIMULATE) == 250, "Simulated bottle capacity");
            test.assertTrue(empty.getContainer().is(Items.GLASS_BOTTLE), "Simulation changed bottle");
            test.assertTrue(empty.fill(water, IFluidHandler.FluidAction.EXECUTE) == 250, "Bottle fill amount");
            test.assertTrue(WaterPurity.getPurity(empty.getContainer()) == purity, "Filled bottle purity " + purity);
            IFluidHandlerItem full = handler(empty.getContainer());
            test.assertTrue(WaterPurity.getPurity(full.getFluidInTank(0)) == purity, "Read bottle purity " + purity);
            FluidStack simulated = full.drain(250, IFluidHandler.FluidAction.SIMULATE);
            test.assertTrue(simulated.getAmount() == 250 && WaterPurity.getPurity(simulated) == purity, "Simulated drain purity");
            test.assertTrue(full.getContainer().is(Items.POTION), "Simulated drain changed bottle");
            FluidStack drained = full.drain(new FluidStack(Fluids.WATER, 250), IFluidHandler.FluidAction.EXECUTE);
            test.assertTrue(drained.getAmount() == 250 && WaterPurity.getPurity(drained) == purity, "Drained purity " + purity);
            test.assertTrue(full.getContainer().is(Items.GLASS_BOTTLE), "Drained bottle not empty");
            test.assertTrue(!full.getContainer().has(ThirstComponent.PURITY), "Empty bottle retained purity");
            test.assertTrue(water.getAmount() == 1000 && WaterPurity.getPurity(water) == purity, "Input fluid mutated");
        }
        IFluidHandlerItem empty = handler(new ItemStack(Items.GLASS_BOTTLE));
        test.assertTrue(empty.fill(new FluidStack(Fluids.WATER, 249), IFluidHandler.FluidAction.EXECUTE) == 0, "Partial bottle filled");
        test.assertTrue(empty.fill(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.EXECUTE) == 0, "Lava accepted");
        IFluidHandlerItem xp = handler(new ItemStack(Items.EXPERIENCE_BOTTLE));
        test.assertTrue(!xp.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE).isEmpty(), "XP bottle broken");
        WaterPurity.applySyncedEnabled(false);
        try {
            IFluidHandlerItem disabled = handler(new ItemStack(Items.GLASS_BOTTLE));
            test.assertTrue(disabled.fill(new FluidStack(Fluids.WATER, 250), IFluidHandler.FluidAction.EXECUTE) == 250, "Disabled purity broke fill");
            test.assertTrue(!disabled.getContainer().has(ThirstComponent.PURITY), "Disabled purity wrote component");
        } finally {
            WaterPurity.applySyncedEnabled(true);
        }
        test.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "thirst")
    public static void tankPreservesAndMergesPurity(GameTestHelper test) throws Exception {
        var registry = net.minecraft.core.registries.BuiltInRegistries.ITEM;
        ItemStack backpack = new ItemStack(registry.get(net.minecraft.resources.ResourceLocation.parse("sophisticatedbackpacks:backpack")));
        ItemStack upgrade = new ItemStack(registry.get(net.minecraft.resources.ResourceLocation.parse("sophisticatedbackpacks:tank_upgrade")));
        Class<?> wrapperClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper");
        Object wrapper = wrapperClass.getMethod("fromStack", ItemStack.class).invoke(null, backpack);
        Class<?> storageClass = Class.forName("net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper");
        Class<?> tankClass = Class.forName("net.p3pp3rf1y.sophisticatedcore.upgrades.tank.TankUpgradeWrapper");
        var constructor = tankClass.getDeclaredConstructor(storageClass, ItemStack.class, java.util.function.Consumer.class);
        constructor.setAccessible(true);
        java.util.function.Consumer<ItemStack> save = ignored -> {};
        Object tank = constructor.newInstance(wrapper, upgrade, save);
        java.util.function.Consumer<Object> render = ignored -> {};
        tankClass.getMethod("setTankRenderInfoUpdateCallback", java.util.function.Consumer.class).invoke(tank, render);
        var fill = tankClass.getMethod("fill", FluidStack.class, IFluidHandler.FluidAction.class, boolean.class);
        var contents = tankClass.getMethod("getContents");
        var drain = tankClass.getMethod("drain", int.class, IFluidHandler.FluidAction.class, boolean.class);
        FluidStack clean = new FluidStack(Fluids.WATER, 1000);
        FluidStack dirty = new FluidStack(Fluids.WATER, 1000);
        WaterPurity.addPurity(dirty, 0);
        test.assertTrue((int)fill.invoke(tank, clean, IFluidHandler.FluidAction.EXECUTE, true) == 1000, "Initial tank fill");
        fill.invoke(tank, dirty, IFluidHandler.FluidAction.SIMULATE, true);
        test.assertTrue(WaterPurity.getPurity((FluidStack)contents.invoke(tank)) == 3, "Simulation contaminated tank");
        fill.invoke(tank, dirty.copyWithAmount(0), IFluidHandler.FluidAction.EXECUTE, true);
        test.assertTrue(WaterPurity.getPurity((FluidStack)contents.invoke(tank)) == 3, "Zero fill contaminated tank");
        fill.invoke(tank, dirty, IFluidHandler.FluidAction.EXECUTE, true);
        test.assertTrue(WaterPurity.getPurity((FluidStack)contents.invoke(tank)) == 0, "Mixed water not dirty");
        Object reloaded = constructor.newInstance(wrapper, upgrade.copy(), save);
        tankClass.getMethod("setTankRenderInfoUpdateCallback", java.util.function.Consumer.class).invoke(reloaded, render);
        FluidStack saved = (FluidStack)contents.invoke(reloaded);
        test.assertTrue(saved.getAmount() == 2000 && WaterPurity.getPurity(saved) == 0, "Tank lost saved purity");
        FluidStack drained = (FluidStack)drain.invoke(reloaded, 250, IFluidHandler.FluidAction.EXECUTE, true);
        test.assertTrue(drained.getAmount() == 250 && WaterPurity.getPurity(drained) == 0, "Tank drain lost purity");
        IFluidHandlerItem bottle = handler(new ItemStack(Items.GLASS_BOTTLE));
        bottle.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        test.assertTrue(WaterPurity.getPurity(bottle.getContainer()) == 0, "Tank to bottle lost purity");
        fill.invoke(reloaded, clean, IFluidHandler.FluidAction.EXECUTE, true);
        test.assertTrue(WaterPurity.getPurity((FluidStack)contents.invoke(reloaded)) == 0, "Clean water purified dirty tank");
        test.succeed();
    }
}
