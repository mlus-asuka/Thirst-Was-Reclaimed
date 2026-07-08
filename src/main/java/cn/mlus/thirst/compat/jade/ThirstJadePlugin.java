package cn.mlus.thirst.compat.jade;

import cn.mlus.thirst.Thirst;
import cn.mlus.thirst.content.purity.WaterPurity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import snownee.jade.api.*;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.*;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.JadeForgeUtils;

import java.util.List;
import java.util.Objects;

@WailaPlugin(Thirst.ID)
public class ThirstJadePlugin implements IWailaPlugin
{
    private static final ResourceLocation WATER_CAULDRON_FLUID = Thirst.asResource("water_cauldron_fluid");
    private static final ResourceLocation FLUID_STORAGE = Thirst.asResource("fluid_storage");

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

    private enum WaterCauldronFluidProvider implements IServerExtensionProvider<CompoundTag>
    {
        INSTANCE;

        @Override
        public List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor)
        {
            if (!WaterPurity.isEnabled() || !(accessor instanceof BlockAccessor blockAccessor))
                return null;

            BlockState blockState = blockAccessor.getBlockState();
            if (!blockState.is(Blocks.WATER_CAULDRON))
                return null;

            int level = blockState.getValue(LayeredCauldronBlock.LEVEL);
            FluidStack fluid = new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME * level / 3);
            WaterPurity.addPurity(fluid, WaterPurity.getBlockPurity(blockState));

            return List.of(new ViewGroup<>(List.of(FluidView.writeDefault(
                    JadeForgeUtils.fromFluidStack(fluid),
                    FluidType.BUCKET_VOLUME))));
        }

        @Override
        public ResourceLocation getUid()
        {
            return WATER_CAULDRON_FLUID;
        }

        @Override
        public int getDefaultPriority()
        {
            return 900;
        }
    }

    private enum DefaultFluidStorageProvider implements IServerExtensionProvider<CompoundTag>
    {
        INSTANCE;

        @Override
        public List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor)
        {
            return CommonProxy.wrapFluidStorage(accessor);
        }

        @Override
        public boolean shouldRequestData(Accessor<?> accessor)
        {
            return CommonProxy.hasDefaultFluidStorage(accessor);
        }

        @Override
        public ResourceLocation getUid()
        {
            return FLUID_STORAGE;
        }

        @Override
        public int getDefaultPriority()
        {
            return 9500;
        }
    }

    private enum PurityFluidClientProvider implements IClientExtensionProvider<CompoundTag, FluidView>
    {
        WATER_CAULDRON(WATER_CAULDRON_FLUID),
        DEFAULT_STORAGE(FLUID_STORAGE);

        private final ResourceLocation uid;

        PurityFluidClientProvider(ResourceLocation uid)
        {
            this.uid = uid;
        }

        @Override
        public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> groups)
        {
            return ClientViewGroup.map(groups, PurityFluidClientProvider::readDefault, (serverGroup, clientGroup) -> {});
        }

        private static FluidView readDefault(CompoundTag tag)
        {
            FluidView view = FluidView.readDefault(tag);
            if (view == null)
                return null;

            if (!tag.contains("fluid"))
                return view;

            JadeFluidObject fluid = JadeFluidObject.CODEC
                    .parse(NbtOps.INSTANCE, tag.get("fluid"))
                    .result()
                    .orElse(null);
            if (fluid != null)
                view.fluidName = getFluidName(fluid);

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
        public ResourceLocation getUid()
        {
            return uid;
        }
    }
}
