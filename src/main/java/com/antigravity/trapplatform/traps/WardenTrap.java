package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class WardenTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        Vec3d pos = player.getPos().add(2, 0, 2);

        WardenEntity warden = EntityType.WARDEN.create(world);
        if (warden != null) {
            warden.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
            world.spawnEntity(warden);
        }
    }

    @Override
    public String getName() {
        return "The Warden";
    }
}
