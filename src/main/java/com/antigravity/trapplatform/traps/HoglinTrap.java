package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class HoglinTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        for (int i = 0; i < 2; i++) {
            HoglinEntity hoglin = EntityType.HOGLIN.create(world);
            hoglin.setPosition(player.getX() + (world.random.nextDouble() - 0.5) * 10, player.getY(),
                    player.getZ() + (world.random.nextDouble() - 0.5) * 10);
            world.spawnEntity(hoglin);
        }
    }

    @Override
    public String getName() {
        return "Hoglins";
    }
}
