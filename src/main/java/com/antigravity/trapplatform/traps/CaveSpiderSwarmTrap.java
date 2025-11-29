package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class CaveSpiderSwarmTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        for (int i = 0; i < 10; i++) {
            CaveSpiderEntity spider = EntityType.CAVE_SPIDER.create(world);
            spider.setPosition(player.getX() + (world.random.nextDouble() - 0.5) * 10, player.getY(),
                    player.getZ() + (world.random.nextDouble() - 0.5) * 10);
            world.spawnEntity(spider);
        }
    }

    @Override
    public String getName() {
        return "Cave Spider Swarm";
    }
}
