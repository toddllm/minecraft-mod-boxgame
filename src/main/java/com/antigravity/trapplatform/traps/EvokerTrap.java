package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class EvokerTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        for (int i = 0; i < 2; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 10;
            double offsetZ = (world.random.nextDouble() - 0.5) * 10;
            Vec3d pos = player.getPos().add(offsetX, 0, offsetZ);

            EvokerEntity evoker = EntityType.EVOKER.create(world);
            if (evoker != null) {
                evoker.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
                world.spawnEntity(evoker);
            }
        }
    }

    @Override
    public String getName() {
        return "Evoker Duo";
    }
}
