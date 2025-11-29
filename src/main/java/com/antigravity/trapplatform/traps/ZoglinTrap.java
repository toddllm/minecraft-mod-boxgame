package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZoglinEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class ZoglinTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        ZoglinEntity zoglin = EntityType.ZOGLIN.create(world);
        zoglin.setPosition(player.getX() + (world.random.nextDouble() - 0.5) * 10, player.getY(),
                player.getZ() + (world.random.nextDouble() - 0.5) * 10);
        world.spawnEntity(zoglin);
    }

    @Override
    public String getName() {
        return "Zoglin";
    }
}
