package com.antigravity.trapplatform.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * The Grass-Psychic Pysro - Now a visible FlyingEntity covered by shield
 */
public class GrassPsychicPysroEntity extends FlyingEntity {

    private GrassPsychicShieldEntity shield = null;
    private int dialogueCooldown = 0;
    
    private static final String[] DIALOGUE_LINES = {
        "§5§lThe Grass-Psychic flows through me...",
        "§2§oYou cannot escape the mass...",
        "§5§lI am nature's wrath incarnate!",
        "§d§oFear and Jewis were merely... fuel.",
        "§2§lThe grass consumes all in time...",
        "§5§oYour weapons mean nothing to the Pysro.",
        "§d§lI transcend all mortal bounds!",
        "§2§oThe psychic energy... it pulses...",
        "§5§lDare you challenge the Grass-Psychic?"
    };

    public GrassPsychicPysroEntity(EntityType<? extends FlyingEntity> entityType, World world) {
        super(entityType, world);
        // Don't set NoGravity - Grass-types can't fly!
        this.setPersistent();
    }

    public static DefaultAttributeContainer.Builder createPysroAttributes() {
        return FlyingEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 70.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.4)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 25.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new DisintegrationBeamGoal(this));
        this.goalSelector.add(2, new GrassMassBlastGoal(this));
        this.goalSelector.add(3, new FollowPlayerGoal(this)); // Follow instead of teleport
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        
        // Make Pysro glow (less bright than Lica)
        if (!this.hasStatusEffect(StatusEffects.GLOWING)) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 999999, 0, false, false));
        }
        
        // LOTS of green particles to make it VERY visible!
        if (this.getWorld().isClient) {
            for (int i = 0; i < 10; i++) { // 10 particles per tick!
                double offsetX = (this.random.nextDouble() - 0.5) * 2;
                double offsetY = this.random.nextDouble() * 2;
                double offsetZ = (this.random.nextDouble() - 0.5) * 2;
                this.getWorld().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, 
                    this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0, 0.05, 0);
                // Add happy villager particles too
                this.getWorld().addParticle(ParticleTypes.HAPPY_VILLAGER, 
                    this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0, 0.1, 0);
            }
        }
        
        // Show hitbox corners so you can see where to hit!
        if (this.getWorld().isClient && this.age % 5 == 0) {
            // Draw bounding box corners
            double w = this.getWidth() / 2;
            double h = this.getHeight();
            for (int x = -1; x <= 1; x += 2) {
                for (int y = 0; y <= 1; y++) {
                    for (int z = -1; z <= 1; z += 2) {
                        this.getWorld().addParticle(ParticleTypes.END_ROD,
                            this.getX() + x * w, this.getY() + y * h, this.getZ() + z * w,
                            0, 0, 0);
                    }
                }
            }
        }
        
        // Make sure we have a visible name
        if (!this.hasCustomName()) {
            this.setCustomName(net.minecraft.text.Text.of("§2§lGrass-Psychic Pysro"));
            this.setCustomNameVisible(true);
        }
        
        // Ensure shield exists
        if (!this.getWorld().isClient && shield == null) {
            spawnShield();
        }
        
        // Dialogue system
        if (!this.getWorld().isClient) {
            if (dialogueCooldown > 0) {
                dialogueCooldown--;
            } else if (this.getTarget() != null && this.random.nextFloat() < 0.02f) {
                String dialogue = DIALOGUE_LINES[this.random.nextInt(DIALOGUE_LINES.length)];
                this.getWorld().getPlayers().forEach(p -> {
                    if (p.squaredDistanceTo(this) < 1600) {
                        p.sendMessage(net.minecraft.text.Text.of("§7[§2Grass-Psychic Pysro§7] " + dialogue), false);
                    }
                });
                dialogueCooldown = 200;
            }
        }
    }

    private void spawnShield() {
        shield = com.antigravity.trapplatform.TrapPlatformMod.GRASS_PSYCHIC_SHIELD_ENTITY.create(this.getWorld());
        if (shield != null) {
            shield.setOwner(this);
            shield.setPosition(this.getPos());
            this.getWorld().spawnEntity(shield);
            com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("Spawned Grass-Psychic Shield for Pysro");
        }
    }

    @Override
    public void onDeath(net.minecraft.entity.damage.DamageSource damageSource) {
        super.onDeath(damageSource);
        if (shield != null) {
            shield.discard();
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
    }

    @Override
    public void travel(Vec3d movementInput) {
        // Override FlyingEntity's travel to make Pysro fall to ground
        if (this.isLogicalSideForUpdatingMovement()) {
            double gravity = 0.08; // Normal gravity
            
            // Apply gravity
            this.setVelocity(this.getVelocity().add(0, -gravity, 0));
            
            // Move like a normal entity
            this.move(net.minecraft.entity.MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.91)); // Friction
        }
    }

    // ===== AI GOALS =====
    
    private static class DisintegrationBeamGoal extends net.minecraft.entity.ai.goal.Goal {
        private final GrassPsychicPysroEntity pysro;
        private int cooldown = 0;

        public DisintegrationBeamGoal(GrassPsychicPysroEntity pysro) {
            this.pysro = pysro;
        }

        @Override
        public boolean canStart() {
            return cooldown <= 0 && pysro.getTarget() != null;
        }

        @Override
        public void start() {
            fireDisintegrationBeam();
            cooldown = 100;
        }

        @Override
        public void tick() {
            if (cooldown > 0) cooldown--;
        }

        private void fireDisintegrationBeam() {
            net.minecraft.util.math.Vec3d look = pysro.getRotationVec(1.0f);
            net.minecraft.world.World world = pysro.getWorld();

            for (int i = 1; i <= 50; i++) {
                double x = pysro.getX() + look.x * i;
                double y = pysro.getEyeY() + look.y * i;
                double z = pysro.getZ() + look.z * i;

                net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos((int)x, (int)y, (int)z);

                if (!world.getBlockState(pos).isAir()) {
                    world.breakBlock(pos, false);
                }

                world.addParticle(net.minecraft.particle.ParticleTypes.END_ROD, x, y, z, 0, 0, 0);
            }

            world.playSound(null, pysro.getBlockPos(), net.minecraft.sound.SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, net.minecraft.sound.SoundCategory.HOSTILE, 2.0f, 0.5f);
        }
    }

    private static class GrassMassBlastGoal extends net.minecraft.entity.ai.goal.Goal {
        private final GrassPsychicPysroEntity pysro;
        private int cooldown = 0;

        public GrassMassBlastGoal(GrassPsychicPysroEntity pysro) {
            this.pysro = pysro;
        }

        @Override
        public boolean canStart() {
            return cooldown <= 0;
        }

        @Override
        public void start() {
            placeGrassMassWall();
            cooldown = 60;
        }

        @Override
        public void tick() {
            if (cooldown > 0) cooldown--;
        }

        private void placeGrassMassWall() {
            net.minecraft.util.math.Vec3d look = pysro.getRotationVec(1.0f);
            net.minecraft.world.World world = pysro.getWorld();

            for (int i = 2; i <= 6; i++) {
                for (int j = -1; j <= 1; j++) {
                    double x = pysro.getX() + look.x * i + j * look.z;
                    double y = pysro.getY();
                    double z = pysro.getZ() + look.z * i - j * look.x;

                    net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos((int)x, (int)y, (int)z);

                    if (world.getBlockState(pos).isReplaceable()) {
                        world.setBlockState(pos, com.antigravity.trapplatform.TrapPlatformMod.GRASS_MASS_BLOCK.getDefaultState());
                    }
                }
            }
        }
    }

    private static class FollowPlayerGoal extends net.minecraft.entity.ai.goal.Goal {
        private final GrassPsychicPysroEntity pysro;

        public FollowPlayerGoal(GrassPsychicPysroEntity pysro) {
            this.pysro = pysro;
        }

        @Override
        public boolean canStart() {
            return pysro.getTarget() != null;
        }

        @Override
        public void tick() {
            net.minecraft.entity.LivingEntity target = pysro.getTarget();
            if (target == null) return;

            // Walk towards the player (on ground)
            double distance = pysro.squaredDistanceTo(target);
            if (distance > 64.0) { // More than 8 blocks
                Vec3d targetPos = target.getPos();
                pysro.getNavigation().startMovingTo(target, 1.0);
            } else if (distance < 25.0) { // Less than 5 blocks - back away
                Vec3d awayDirection = pysro.getPos().subtract(target.getPos()).normalize();
                pysro.getNavigation().startMovingTo(
                    pysro.getX() + awayDirection.x * 3,
                    pysro.getY(),
                    pysro.getZ() + awayDirection.z * 3,
                    1.0
                );
            }
        }

        @Override
        public boolean shouldContinue() {
            return pysro.getTarget() != null;
        }
    }
}
