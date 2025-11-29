package com.antigravity.trapplatform.traps;

import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class WebTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        BlockPos center = player.getBlockPos();
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 2; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos pos = center.add(x, y, z);
                    if (world.getBlockState(pos).isAir()) {
                        world.setBlockState(pos, Blocks.COBWEB.getDefaultState());
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Web Trap";
    }
}
