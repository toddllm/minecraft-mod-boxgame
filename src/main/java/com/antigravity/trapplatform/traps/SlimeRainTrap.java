package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class SlimeRainTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        for (int i = 0; i < 5; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 8;
            double offsetZ = (world.random.nextDouble() - 0.5) * 8;
            Vec3d pos = player.getPos().add(offsetX, 15, offsetZ);

            SlimeEntity slime = EntityType.SLIME.create(world);
            if (slime != null) {
                slime.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
                slime.setSize(4, true); // Big slimes
                world.spawnEntity(slime);
            }
        }
    }

    @Override
    public String getName() {
        return "Slime Rain";
    }
}
