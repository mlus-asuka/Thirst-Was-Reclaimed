package cn.mlus.thirst.compat.jade;

import cn.mlus.thirst.Thirst;
import cn.mlus.thirst.content.purity.WaterPurity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import snownee.jade.api.*;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.*;
import snownee.jade.util.CommonProxy;

import java.util.List;
import java.util.Objects;

@WailaPlugin(Thirst.ID)
public class ThirstJadePlugin implements IWailaPlugin
{
    private static final Identifier WATER_CAULDRON_FLUID = Thirst.asResource("water_cauldron_fluid");
    private static final Identifier FLUID_STORAGE = Thirst.asResource("fluid_storage");

    @Override
    public void register(IWailaCommonRegistration registration)
    {
        registration.registerFluidStorage(WaterCauldronFluidProvider.INSTANCE, LayeredCauldronBlock.class);
        registration.registerFluidStorage(DefaultFluidStorageProvider.INSTANCE, Object.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration)
    {
        registration.registerFluidStorageClient(PurityFluidClientProvider.WATER_CAULDRON);
        registration.registerFluidStorageClient(PurityFluidClientProvider.DEFAULT_STORAGE);
    }

    private enum WaterCauldronFluidProvider implements IServerExtensionProvider<FluidView.Data>
    {
        INSTANCE;

        @Override
        public List<ViewGroup<FluidView.Data>> getGroups(Accessor<?> accessor)
        {
            if (!WaterPurity.isEnabled() || !(accessor instanceof BlockAccessor blockAccessor))
                return null;

            BlockState blockState = blockAccessor.getBlockState();
            if (!blockState.is(Blocks.WATER_CAULDRON))
                return null;

            int level = blockState.getValue(LayeredCauldronBlock.LEVEL);
            long bucketVolume = JadeFluidObject.bucketVolume();
            int amount = (int) (bucketVolume * level / 3);
            FluidStack fluid = new FluidStack(Fluids.WATER, amount);
            WaterPurity.addPurity(fluid, WaterPurity.getBlockPurity(blockState));

            JadeFluidObject jadeFluid = JadeFluidObject.of(fluid.getFluid(), fluid.getAmount(), fluid.getComponentsPatch());
            return List.of(new ViewGroup<>(List.of(new FluidView.Data(jadeFluid, bucketVolume))));
        }

        @Override
        public Identifier getUid()
        {
            return WATER_CAULDRON_FLUID;
        }

        @Override
        public int getDefaultPriority()
        {
            return 900;
        }
    }

    private enum DefaultFluidStorageProvider implements IServerExtensionProvider<FluidView.Data>
    {
        INSTANCE;

        @Override
        public List<ViewGroup<FluidView.Data>> getGroups(Accessor<?> accessor)
        {
            return CommonProxy.wrapFluidStorage(accessor);
        }

        @Override
        public boolean shouldRequestData(Accessor<?> accessor)
        {
            return CommonProxy.hasDefaultFluidStorage(accessor);
        }

        @Override
        public Identifier getUid()
        {
            return FLUID_STORAGE;
        }

        @Override
        public int getDefaultPriority()
        {
            return 9500;
        }
    }

    private enum PurityFluidClientProvider implements IClientExtensionProvider<FluidView.Data, FluidView>
    {
        WATER_CAULDRON(WATER_CAULDRON_FLUID),
        DEFAULT_STORAGE(FLUID_STORAGE);

        private final Identifier uid;

        PurityFluidClientProvider(Identifier uid)
        {
            this.uid = uid;
        }

        @Override
        public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<FluidView.Data>> groups)
        {
            return ClientViewGroup.map(groups, PurityFluidClientProvider::readDefault, (serverGroup, clientGroup) -> {});
        }

        private static FluidView readDefault(FluidView.Data data)
        {
            FluidView view = FluidView.readDefault(data);
            if (view == null)
                return null;

            if (data.fluids().isEmpty())
                return view;

            view.fluidName = getFluidName(data.fluids().getFirst());

            return view;
        }

        private static Component getFluidName(JadeFluidObject fluid)
        {
            FluidStack stack = CommonProxy.toFluidStack(fluid);
            if (stack.isEmpty())
                return stack.getHoverName();

            if (WaterPurity.isEnabled() && (WaterPurity.hasPurity(stack) || stack.getFluid().equals(Fluids.WATER)))
            {
                return Component.literal(Objects.requireNonNull(WaterPurity.getPurityText(WaterPurity.getPurity(stack))))
                        .append(" ")
                        .append(stack.getHoverName());
            }

            return CommonProxy.getFluidName(fluid);
        }

        @Override
        public Identifier getUid()
        {
            return uid;
        }
    }
}
