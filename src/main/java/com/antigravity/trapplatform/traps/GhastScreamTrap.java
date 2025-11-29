package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class GhastScreamTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        for (int i = 0; i < 3; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 15;
            double offsetZ = (world.random.nextDouble() - 0.5) * 15;
            Vec3d pos = player.getPos().add(offsetX, 10, offsetZ);

            GhastEntity ghast = EntityType.GHAST.create(world);
            if (ghast != null) {
                ghast.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
                world.spawnEntity(ghast);
            }
        }
    }

    @Override
    public String getName() {
        return "Ghast Scream";
    }
}
