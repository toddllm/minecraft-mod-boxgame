package com.antigravity.trapplatform.traps;

import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class FangTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        // Ring of 16 fangs
        Vec3d center = player.getPos();
        double radius = 3.0;
        int count = 16;

        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);
            double y = player.getY();

            // Adjust y to ground
            y = world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, (int) x, (int) z);
            if (y < player.getY() - 5)
                y = player.getY(); // Fallback if over void

            world.spawnEntity(new EvokerFangsEntity(world, x, y, z, 0, 0, player));
        }
    }

    @Override
    public String getName() {
        return "Fang Ring";
    }
}
