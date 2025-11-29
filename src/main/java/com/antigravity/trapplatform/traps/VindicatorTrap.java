package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class VindicatorTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        for (int i = 0; i < 3; i++) {
            VindicatorEntity vindicator = EntityType.VINDICATOR.create(world);
            vindicator.setPosition(player.getX() + (world.random.nextDouble() - 0.5) * 10, player.getY(),
                    player.getZ() + (world.random.nextDouble() - 0.5) * 10);
            world.spawnEntity(vindicator);
        }
    }

    @Override
    public String getName() {
        return "Vindicators";
    }
}
