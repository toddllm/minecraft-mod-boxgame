package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class PiglinBruteTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        for (int i = 0; i < 2; i++) {
            PiglinBruteEntity brute = EntityType.PIGLIN_BRUTE.create(world);
            brute.setPosition(player.getX() + (world.random.nextDouble() - 0.5) * 10, player.getY(),
                    player.getZ() + (world.random.nextDouble() - 0.5) * 10);
            brute.equipStack(net.minecraft.entity.EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_AXE));
            world.spawnEntity(brute);
        }
    }

    @Override
    public String getName() {
        return "Piglin Brutes";
    }
}
