package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class VexSwarmTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        for (int i = 0; i < 8; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 6;
            double offsetZ = (world.random.nextDouble() - 0.5) * 6;
            Vec3d pos = player.getPos().add(offsetX, 5, offsetZ);

            VexEntity vex = EntityType.VEX.create(world);
            if (vex != null) {
                vex.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
                vex.setTarget(player);
                world.spawnEntity(vex);
            }
        }
    }

    @Override
    public String getName() {
        return "Vex Swarm";
    }
}
