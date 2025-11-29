package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class TntRainTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        for (int i = 0; i < 10; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 8;
            double offsetZ = (world.random.nextDouble() - 0.5) * 8;
            Vec3d pos = player.getPos().add(offsetX, 15, offsetZ);

            TntEntity tnt = EntityType.TNT.create(world);
            if (tnt != null) {
                tnt.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
                tnt.setFuse(40); // 2 seconds
                world.spawnEntity(tnt);
            }
        }
    }

    @Override
    public String getName() {
        return "TNT Rain";
    }
}
