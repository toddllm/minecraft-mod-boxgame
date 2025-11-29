package com.antigravity.trapplatform.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class TangledStatusEffect extends StatusEffect {

    public TangledStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0x228B22); // Forest green color
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true; // Apply every tick
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!entity.getWorld().isClient) {
            // Damage if entity is moving
            double speed = Math.sqrt(
                entity.getVelocity().x * entity.getVelocity().x + 
                entity.getVelocity().z * entity.getVelocity().z
            );
            
            if (speed > 0.01) { // Moving
                entity.damage(entity.getDamageSources().magic(), 0.5f + amplifier * 0.5f);
                
                // Spawn vine particles
                entity.getWorld().addParticle(
                    net.minecraft.particle.ParticleTypes.SPORE_BLOSSOM_AIR,
                    entity.getX(),
                    entity.getY() + entity.getHeight() / 2,
                    entity.getZ(),
                    0, 0, 0
                );
            }
        }
        return true;
    }
}
