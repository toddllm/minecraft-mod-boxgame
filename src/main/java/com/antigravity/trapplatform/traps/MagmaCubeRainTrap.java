package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class MagmaCubeRainTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        for (int i = 0; i < 5; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 8;
            double offsetZ = (world.random.nextDouble() - 0.5) * 8;
            Vec3d pos = player.getPos().add(offsetX, 15, offsetZ);

            MagmaCubeEntity magma = EntityType.MAGMA_CUBE.create(world);
            if (magma != null) {
                magma.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
                magma.setSize(4, true); // Big cubes
                world.spawnEntity(magma);
            }
        }
    }

    @Override
    public String getName() {
        return "Magma Rain";
    }
}
