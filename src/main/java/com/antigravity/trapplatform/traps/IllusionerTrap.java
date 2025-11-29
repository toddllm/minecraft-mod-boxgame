package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class IllusionerTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        IllusionerEntity illusioner = EntityType.ILLUSIONER.create(world);
        illusioner.setPosition(player.getX() + (world.random.nextDouble() - 0.5) * 10, player.getY(),
                player.getZ() + (world.random.nextDouble() - 0.5) * 10);
        world.spawnEntity(illusioner);
    }

    @Override
    public String getName() {
        return "Illusioner";
    }
}
