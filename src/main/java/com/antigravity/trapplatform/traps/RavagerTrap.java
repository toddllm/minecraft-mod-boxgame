package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class RavagerTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        RavagerEntity ravager = EntityType.RAVAGER.create(world);
        ravager.setPosition(player.getX() + (world.random.nextDouble() - 0.5) * 10, player.getY(),
                player.getZ() + (world.random.nextDouble() - 0.5) * 10);
        world.spawnEntity(ravager);
    }

    @Override
    public String getName() {
        return "Ravager";
    }
}
