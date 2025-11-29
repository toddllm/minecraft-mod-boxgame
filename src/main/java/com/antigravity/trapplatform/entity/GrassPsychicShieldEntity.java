package com.antigravity.trapplatform.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * Grass-Psychic Shield that surrounds and protects its owner
 */
public class GrassPsychicShieldEntity extends Entity {

    private LivingEntity owner;
    private int tickCounter = 0;

    public GrassPsychicShieldEntity(EntityType<?> type, World world) {
        super(type, world);
        this.noClip = true;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient) {
            spawnShieldParticles();
            return;
        }

        // Follow owner
        if (owner != null && owner.isAlive()) {
            this.setPosition(owner.getPos());
        } else {
            this.discard();
            return;
        }

        tickCounter++;

        // Pull entities every tick
        if (tickCounter % 2 == 0) {
            pullEntities();
        }
    }

    private void pullEntities() {
        double pullRadius = 15.0;
        Box pullBox = new Box(
                this.getX() - pullRadius, this.getY() - pullRadius, this.getZ() - pullRadius,
                this.getX() + pullRadius, this.getY() + pullRadius, this.getZ() + pullRadius
        );

        List<Entity> entities = this.getWorld().getOtherEntities(this, pullBox, 
            e -> e instanceof LivingEntity 
                && e != owner 
                && !(e instanceof com.antigravity.trapplatform.entity.LicaEntity)); // Don't pull Lica!

        for (Entity entity : entities) {
            // Calculate pull vector
            double dx = this.getX() - entity.getX();
            double dy = this.getY() - entity.getY();
            double dz = this.getZ() - entity.getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distance > 0.5 && distance < pullRadius) {
                double pullStrength = (pullRadius - distance) / pullRadius * 0.1;
                entity.addVelocity(
                        (dx / distance) * pullStrength,
                        (dy / distance) * pullStrength,
                        (dz / distance) * pullStrength
                );
                entity.velocityModified = true;
            }
        }
    }

    private void spawnShieldParticles() {
        double radius = 3.0;
        int particleCount = 5;

        for (int i = 0; i < particleCount; i++) {
            double angle = (tickCounter + i * 72) * Math.PI / 180.0;
            double yAngle = Math.sin(tickCounter * 0.05) * 0.5;

            double x = this.getX() + Math.cos(angle) * radius;
            double y = this.getY() + 1.0 + yAngle;
            double z = this.getZ() + Math.sin(angle) * radius;

            // Only green particles, NO PURPLE!
            this.getWorld().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, x, y, z, 0, 0.02, 0);
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        // No data to track
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        // Nothing to save
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        // Nothing to save
    }

    @Override
    public boolean canHit() {
        return false;
    }

    @Override
    public boolean isCollidable() {
        return false;
    }
}
