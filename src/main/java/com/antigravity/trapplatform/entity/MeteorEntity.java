package com.antigravity.trapplatform.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class MeteorEntity extends ExplosiveProjectileEntity {

    public MeteorEntity(EntityType<? extends MeteorEntity> entityType, World world) {
        super(entityType, world);
    }

    public MeteorEntity(World world, LivingEntity owner, double directionX, double directionY, double directionZ) {
        super(com.antigravity.trapplatform.TrapPlatformMod.METEOR_ENTITY, owner,
                new net.minecraft.util.math.Vec3d(directionX, directionY, directionZ), world);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.getWorld().isClient) {
            // Visual Explosion Only (Rare - 5% chance)
            if (this.random.nextFloat() < 0.05f) {
                this.getWorld().addParticle(net.minecraft.particle.ParticleTypes.EXPLOSION_EMITTER, this.getX(),
                        this.getY(), this.getZ(), 0, 0, 0);
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        net.minecraft.sound.SoundEvents.ENTITY_GENERIC_EXPLODE,
                        net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
            } else {
                // Just a thud
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        net.minecraft.sound.SoundEvents.BLOCK_STONE_BREAK,
                        net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
            }
            this.discard();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (!this.getWorld().isClient) {
            entityHitResult.getEntity().damage(this.getDamageSources().explosion(this, this.getOwner()), 10.0f);
        }
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }
}
