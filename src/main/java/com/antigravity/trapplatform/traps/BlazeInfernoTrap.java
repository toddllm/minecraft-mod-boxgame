package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class BlazeInfernoTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        for (int i = 0; i < 5; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 8;
            double offsetZ = (world.random.nextDouble() - 0.5) * 8;
            Vec3d pos = player.getPos().add(offsetX, 2, offsetZ);

            BlazeEntity blaze = EntityType.BLAZE.create(world);
            if (blaze != null) {
                blaze.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
                world.spawnEntity(blaze);
            }
        }
    }

    @Override
    public String getName() {
        return "Blaze Inferno";
    }
}
