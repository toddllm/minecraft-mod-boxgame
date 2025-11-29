package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class GiantTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        GiantEntity giant = EntityType.GIANT.create(world);
        giant.setPosition(player.getX() + (world.random.nextDouble() - 0.5) * 10, player.getY(),
                player.getZ() + (world.random.nextDouble() - 0.5) * 10);
        world.spawnEntity(giant);
    }

    @Override
    public String getName() {
        return "Giant";
    }
}
