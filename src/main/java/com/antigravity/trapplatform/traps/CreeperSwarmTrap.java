package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class CreeperSwarmTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        for (int i = 0; i < 5; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 6;
            double offsetZ = (world.random.nextDouble() - 0.5) * 6;
            Vec3d pos = player.getPos().add(offsetX, 1, offsetZ);

            CreeperEntity creeper = EntityType.CREEPER.create(world);
            if (creeper != null) {
                creeper.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);

                // Charge them up!
                net.minecraft.nbt.NbtCompound nbt = new net.minecraft.nbt.NbtCompound();
                creeper.writeCustomDataToNbt(nbt);
                nbt.putBoolean("powered", true);
                creeper.readCustomDataFromNbt(nbt);

                world.spawnEntity(creeper);
            }
        }
    }

    @Override
    public String getName() {
        return "Charged Creeper Swarm";
    }
}
