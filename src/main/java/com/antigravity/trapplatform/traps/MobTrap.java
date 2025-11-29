package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class MobTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        // Spawn squad of 5
        EntityType<?>[] types = { EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.WITCH };
        EntityType<?> type = types[world.random.nextInt(types.length)];

        for (int i = 0; i < 5; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 6;
            double offsetZ = (world.random.nextDouble() - 0.5) * 6;
            Vec3d pos = player.getPos().add(offsetX, 1, offsetZ);

            if (type.create(world) instanceof MobEntity mob) {
                mob.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);

                // Gear up
                if (world.random.nextBoolean()) {
                    mob.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                    mob.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                }

                world.spawnEntity(mob);
            }
        }
    }

    @Override
    public String getName() {
        return "Mob Squad";
    }
}
