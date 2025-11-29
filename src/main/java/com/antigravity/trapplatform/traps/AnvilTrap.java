package com.antigravity.trapplatform.traps;

import net.minecraft.block.Blocks;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class AnvilTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        // Rain 12 Anvils
        for (int i = 0; i < 12; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 7;
            double offsetZ = (world.random.nextDouble() - 0.5) * 7;
            Vec3d pos = player.getPos().add(offsetX, 15 + world.random.nextDouble() * 5, offsetZ);

            FallingBlockEntity anvil = FallingBlockEntity.spawnFromBlock(world,
                    new net.minecraft.util.math.BlockPos((int) pos.x, (int) pos.y, (int) pos.z),
                    Blocks.ANVIL.getDefaultState());
            anvil.setHurtEntities(2.0f, 40);
        }
    }

    @Override
    public String getName() {
        return "Anvil Rain";
    }
}
