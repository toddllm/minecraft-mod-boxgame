package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class DrownedTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        for (int i = 0; i < 5; i++) {
            DrownedEntity drowned = EntityType.DROWNED.create(world);
            drowned.setPosition(player.getX() + (world.random.nextDouble() - 0.5) * 10, player.getY(),
                    player.getZ() + (world.random.nextDouble() - 0.5) * 10);
            drowned.equipStack(net.minecraft.entity.EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
            world.spawnEntity(drowned);
        }
    }

    @Override
    public String getName() {
        return "Drowned";
    }
}
