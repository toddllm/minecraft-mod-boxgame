package com.antigravity.trapplatform.traps;

import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class FireballTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        // 5 Fireballs spread out
        for (int i = 0; i < 5; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 10;
            double offsetZ = (world.random.nextDouble() - 0.5) * 10;
            Vec3d pos = player.getPos().add(offsetX, 15, offsetZ);

            // Aim at player
            Vec3d direction = player.getPos().subtract(pos).normalize();

            FireballEntity fireball = new FireballEntity(world, player, direction, 1);
            fireball.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
            world.spawnEntity(fireball);
        }
    }

    @Override
    public String getName() {
        return "Meteor Shower";
    }
}
