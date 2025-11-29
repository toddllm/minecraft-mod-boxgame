package com.antigravity.trapplatform.traps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class SkeletonSniperTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        for (int i = 0; i < 5; i++) {
            SkeletonEntity skeleton = EntityType.SKELETON.create(world);
            skeleton.setPosition(player.getX() + (world.random.nextDouble() - 0.5) * 10, player.getY(),
                    player.getZ() + (world.random.nextDouble() - 0.5) * 10);
            ItemStack bow = new ItemStack(Items.BOW);
            bow.addEnchantment(
                    world.getRegistryManager().getWrapperOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                            .getOrThrow(net.minecraft.enchantment.Enchantments.POWER),
                    3);
            skeleton.equipStack(net.minecraft.entity.EquipmentSlot.MAINHAND, bow);
            world.spawnEntity(skeleton);
        }
    }

    @Override
    public String getName() {
        return "Skeleton Snipers";
    }
}
