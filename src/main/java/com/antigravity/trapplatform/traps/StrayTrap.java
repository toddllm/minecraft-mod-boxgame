package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.StrayEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class StrayTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        for (int i = 0; i < 5; i++) {
            StrayEntity stray = EntityType.STRAY.create(world);
            stray.setPosition(player.getX() + (world.random.nextDouble() - 0.5) * 10, player.getY(),
                    player.getZ() + (world.random.nextDouble() - 0.5) * 10);
            world.spawnEntity(stray);
        }
    }

    @Override
    public String getName() {
        return "Strays";
    }
}
