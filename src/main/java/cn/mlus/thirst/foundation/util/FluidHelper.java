package cn.mlus.thirst.foundation.util;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public class FluidHelper {
    public static Fluid convertToStill(Fluid fluid) {
        if (fluid == Fluids.FLOWING_WATER)
            return Fluids.WATER;
        if (fluid == Fluids.FLOWING_LAVA)
            return Fluids.LAVA;
        if (fluid instanceof BaseFlowingFluid)
            return ((BaseFlowingFluid) fluid).getSource();
        return fluid;
    }
}
