package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class LightningTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        // 5 Lightning strikes nearby
        for (int i = 0; i < 5; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 6;
            double offsetZ = (world.random.nextDouble() - 0.5) * 6;
            Vec3d pos = player.getPos().add(offsetX, 0, offsetZ);

            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
            if (lightning != null) {
                lightning.refreshPositionAfterTeleport(pos.x, pos.y, pos.z);
                world.spawnEntity(lightning);
            }
        }
    }

    @Override
    public String getName() {
        return "Thunderstorm";
    }
}
