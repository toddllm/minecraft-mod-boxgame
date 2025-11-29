package com.antigravity.trapplatform.traps;

import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class ArrowTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        // Rain 20 arrows in a 5x5 area
        for (int i = 0; i < 20; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 5;
            double offsetZ = (world.random.nextDouble() - 0.5) * 5;
            Vec3d pos = player.getPos().add(offsetX, 10 + world.random.nextDouble() * 5, offsetZ);

            ArrowEntity arrow = new ArrowEntity(world, pos.x, pos.y, pos.z,
                    new net.minecraft.item.ItemStack(net.minecraft.item.Items.ARROW), null);
            arrow.setVelocity(0, -1.5, 0); // Faster drop
            world.spawnEntity(arrow);
        }
    }

    @Override
    public String getName() {
        return "Arrow Rain";
    }
}
