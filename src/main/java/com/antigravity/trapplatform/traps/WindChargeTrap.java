package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class WindChargeTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        // 8 Wind Charges from a circle
        double radius = 5.0;
        int count = 8;

        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = player.getX() + radius * Math.cos(angle);
            double z = player.getZ() + radius * Math.sin(angle);
            double y = player.getY() + 2;

            Vec3d pos = new Vec3d(x, y, z);
            Vec3d direction = player.getPos().add(0, 1, 0).subtract(pos).normalize();

            WindChargeEntity charge = new WindChargeEntity(EntityType.WIND_CHARGE, world);
            charge.setPosition(x, y, z);
            charge.setVelocity(direction);
            world.spawnEntity(charge);
        }
    }

    @Override
    public String getName() {
        return "Wind Storm";
    }
}
