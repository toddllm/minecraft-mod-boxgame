package com.antigravity.trapplatform.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GrassMassBlock extends Block {

    public GrassMassBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        super.onEntityCollision(state, world, pos, entity);
        
        if (!world.isClient && entity instanceof LivingEntity livingEntity) {
            // Apply slowness
            livingEntity.addStatusEffect(new StatusEffectInstance(
                net.minecraft.entity.effect.StatusEffects.SLOWNESS,
                40, // 2 seconds
                2, // Level 3
                false,
                false
            ));
            
            // 20% chance to tangle
            if (world.random.nextFloat() < 0.2f) {
                livingEntity.addStatusEffect(new StatusEffectInstance(
                    net.minecraft.registry.Registries.STATUS_EFFECT.getEntry(com.antigravity.trapplatform.TrapPlatformMod.TANGLED_EFFECT),
                    200, // 10 seconds
                    0,
                    false,
                    true
                ));
            }
        }
    }

    @Override
    public float getHardness() {
        return -1.0f; // Unbreakable
    }

    @Override
    public float getBlastResistance() {
        return 3600000.0f; // Like bedrock
    }
}
