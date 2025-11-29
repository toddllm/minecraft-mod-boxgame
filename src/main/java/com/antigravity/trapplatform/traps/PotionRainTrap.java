package com.antigravity.trapplatform.traps;

import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class PotionRainTrap implements Trap {
    @Override
    public void trigger(ServerWorld world, ServerPlayerEntity player) {
        for (int i = 0; i < 5; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 5;
            double offsetZ = (world.random.nextDouble() - 0.5) * 5;
            Vec3d pos = player.getPos().add(offsetX, 10, offsetZ);

            PotionEntity potion = new PotionEntity(world, pos.x, pos.y, pos.z);
            ItemStack stack = new ItemStack(Items.SPLASH_POTION);
            stack.set(net.minecraft.component.DataComponentTypes.POTION_CONTENTS,
                    new net.minecraft.component.type.PotionContentsComponent(
                            world.random.nextBoolean() ? Potions.HARMING : Potions.POISON));
            potion.setItem(stack);
            potion.setVelocity(0, -0.5, 0);
            world.spawnEntity(potion);
        }
    }

    @Override
    public String getName() {
        return "Potion Rain";
    }
}
