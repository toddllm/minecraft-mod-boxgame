package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class SilverfishInfestationTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        for (int i = 0; i < 15; i++) {
            SilverfishEntity silverfish = EntityType.SILVERFISH.create(world);
            silverfish.setPosition(player.getX() + (world.random.nextDouble() - 0.5) * 10, player.getY(),
                    player.getZ() + (world.random.nextDouble() - 0.5) * 10);
            world.spawnEntity(silverfish);
        }
    }

    @Override
    public String getName() {
        return "Silverfish Infestation";
    }
}
