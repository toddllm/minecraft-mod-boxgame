package com.antigravity.trapplatform.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.FlyGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;

public class VoidQueenEntity extends FlyingEntity {

    private final ServerBossBar bossBar = (ServerBossBar) (new ServerBossBar(this.getDisplayName(),
            BossBar.Color.PURPLE, BossBar.Style.PROGRESS)).setDarkenSky(true);
    private int attackTimer = 0;

    public VoidQueenEntity(EntityType<? extends FlyingEntity> entityType, World world) {
        super(entityType, world);
        this.moveControl = new FlightMoveControl(this, 20, true);
        this.setNoGravity(true);
        this.setCustomName(Text.of("Gewinus Meteoritoligous"));
        this.setPersistent();
        
        // Track state for absorption event
        if (!world.isClient && world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            net.minecraft.server.world.ServerWorld overworld = serverWorld.getServer().getWorld(net.minecraft.world.World.OVERWORLD);
            if (overworld != null) {
                com.antigravity.trapplatform.state.BossBattleState.getServerState(overworld).setJewisActive(true);
                com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("Void Queen Phase 1 spawned - state set to active");
            }
        }
    }

    @Override
    protected void initEquipment(net.minecraft.util.math.random.Random random,
            net.minecraft.world.LocalDifficulty localDifficulty) {
        this.equipStack(net.minecraft.entity.EquipmentSlot.MAINHAND,
                new net.minecraft.item.ItemStack(net.minecraft.item.Items.TRIDENT));
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new VoidQueenFlyGoal(this));
        this.goalSelector.add(2, new VoidQueenAttackGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    public static DefaultAttributeContainer.Builder createVoidQueenAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 50.0) // Reduced to 50
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.05) // Super Slow
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.05) // Super Slow
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 100.0);
    }

    @Override
    protected void dropLoot(DamageSource source, boolean causedByPlayer) {
        // No drops
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        if (!this.getWorld().isClient) {
            // Spawn Bowl Entity
            com.antigravity.trapplatform.entity.BowlEntity bowl = new com.antigravity.trapplatform.entity.BowlEntity(
                    com.antigravity.trapplatform.TrapPlatformMod.BOWL_ENTITY, this.getWorld());
            bowl.setPosition(this.getX(), this.getY(), this.getZ());
            this.getWorld().spawnEntity(bowl);

            // Update Battle State
            if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                com.antigravity.trapplatform.state.BossBattleState.getServerState(serverWorld).setPhase(2); // Bowl Phase
            }
        }
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation navigation = new BirdNavigation(this, world);
        navigation.setCanPathThroughDoors(false);
        navigation.setCanSwim(true);
        return navigation;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient) {
            this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());

            // Robust Singleton Check
            if (this.age % 20 == 0) { // Check every second
                this.getWorld()
                        .getEntitiesByClass(VoidQueenEntity.class, this.getBoundingBox().expand(500), e -> e != this)
                        .forEach(other -> {
                            if (other.age > this.age) {
                                this.discard(); // I am younger, so I die
                            } else {
                                other.discard(); // They are younger (or same), they die
                            }
                        });
            }
        }
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }

    // --- Custom AI Goals ---

    static class VoidQueenFlyGoal extends Goal {
        private final VoidQueenEntity queen;

        public VoidQueenFlyGoal(VoidQueenEntity queen) {
            this.queen = queen;
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            return true;
        }

        @Override
        public void tick() {
            // Move less frequently and stay lower
            if (queen.getRandom().nextInt(200) == 0 || !queen.moveControl.isMoving()) {
                double x = queen.getX() + (queen.getRandom().nextDouble() - 0.5) * 20; // Shorter range
                double y = queen.getY() + (queen.getRandom().nextDouble() - 0.5) * 10;
                double z = queen.getZ() + (queen.getRandom().nextDouble() - 0.5) * 20;

                // Try to stay near platform height (100)
                if (y > 110)
                    y = 105;
                if (y < 95)
                    y = 100;

                queen.moveControl.moveTo(x, y, z, 0.5); // Slow movement
            }
        }
    }

    static class VoidQueenAttackGoal extends Goal {
        private final VoidQueenEntity queen;

        public VoidQueenAttackGoal(VoidQueenEntity queen) {
            this.queen = queen;
        }

        @Override
        public boolean canStart() {
            return queen.getTarget() != null;
        }

        @Override
        public void tick() {
            LivingEntity target = queen.getTarget();
            if (target == null)
                return;

            queen.attackTimer++;

            // Chaos Mode: All attacks possible at any time
            int randomAttack = queen.getRandom().nextInt(100);

            // 1. Black Hole Beam (Most Frequent - 40%)
            if (randomAttack < 40) {
                // Beam Visual (Particles)
                Vec3d start = queen.getEyePos();
                Vec3d end = target.getEyePos();
                Vec3d beamDir = end.subtract(start).normalize();
                for (int i = 0; i < start.distanceTo(end); i++) {
                    Vec3d p = start.add(beamDir.multiply(i));
                    queen.getWorld().addParticle(net.minecraft.particle.ParticleTypes.SONIC_BOOM, p.x, p.y, p.z, 0, 0,
                            0);
                }

                // Pull EVERYTHING
                queen.getWorld().getEntitiesByClass(net.minecraft.entity.Entity.class,
                        queen.getBoundingBox().expand(100), e -> e != queen).forEach(e -> {
                            Vec3d pull = queen.getPos().subtract(e.getPos()).normalize().multiply(2.0); // Strong Pull
                            e.addVelocity(pull.x, pull.y, pull.z);

                            // Break Gear (Players)
                            if (e instanceof PlayerEntity player) {
                                // Damage Armor
                                for (net.minecraft.item.ItemStack stack : player.getArmorItems()) {
                                    if (!stack.isEmpty() && stack.isDamageable()) {
                                        stack.setDamage(stack.getMaxDamage()); // Break instantly
                                    }
                                }
                                // Damage Main Hand
                                net.minecraft.item.ItemStack mainHand = player.getMainHandStack();
                                if (!mainHand.isEmpty() && mainHand.isDamageable()) {
                                    mainHand.setDamage(mainHand.getMaxDamage());
                                }
                            }
                        });

                // Explosion Finale (Removed for "Almost Never" request)
                // if (queen.attackTimer % 20 == 0) { ... }
            }

            // 2. Meteor Rain (Frequent - 30%)
            if (randomAttack >= 40 && randomAttack < 70) {
                com.antigravity.trapplatform.entity.MeteorEntity meteor = new com.antigravity.trapplatform.entity.MeteorEntity(
                        queen.getWorld(), queen, 0, -1, 0);
                meteor.setPosition(target.getX() + (queen.getRandom().nextDouble() - 0.5) * 10, target.getY() + 20,
                        target.getZ() + (queen.getRandom().nextDouble() - 0.5) * 10);
                queen.getWorld().spawnEntity(meteor);
            }

            // 3. Void Swipe (Melee Dash - 15%)
            if (randomAttack >= 70 && randomAttack < 85) {
                Vec3d dir = target.getPos().subtract(queen.getPos()).normalize();
                queen.setVelocity(dir.multiply(1.0)); // Slower dash
                if (queen.distanceTo(target) < 5.0) {
                    target.damage(queen.getDamageSources().mobAttack(queen), 1.0f); // Weaker Damage (1.0)
                }
            }

            // 4. Void Summoning (Occasional - 10%)
            if (randomAttack >= 85 && randomAttack < 95
                    && queen.getWorld().getEntitiesByClass(net.minecraft.entity.mob.VexEntity.class,
                            queen.getBoundingBox().expand(50), e -> true).size() < 5) {
                net.minecraft.entity.mob.VexEntity vex = net.minecraft.entity.EntityType.VEX.create(queen.getWorld());
                vex.setPosition(queen.getPos());
                vex.setOwner(queen);
                queen.getWorld().spawnEntity(vex);
            }

            // 5. Platform Breaker (Rare - 5%)
            if (randomAttack >= 95) {
                net.minecraft.util.math.BlockPos pos = target.getBlockPos().down();
                if (queen.getWorld().getBlockState(pos).isOf(net.minecraft.block.Blocks.BLACK_CONCRETE)) {
                    queen.getWorld().breakBlock(pos, false);
                }
            }

            // 5. Portal Siphon (Constant Passive Effect)
            if (queen.attackTimer % 20 == 0) {
                queen.getWorld().getEntitiesByClass(LivingEntity.class, queen.getBoundingBox().expand(50),
                        e -> e != queen && !(e instanceof PlayerEntity)).forEach(e -> {
                            Vec3d pull = queen.getPos().subtract(e.getPos()).normalize().multiply(0.5);
                            e.addVelocity(pull.x, pull.y, pull.z);
                            if (queen.distanceTo(e) < 5.0) { // Increased range
                                e.damage(queen.getDamageSources().magic(), 1000.0f);
                                queen.heal(5.0f);
                                queen.getWorld().addParticle(net.minecraft.particle.ParticleTypes.SCULK_SOUL,
                                        queen.getX(), queen.getY(), queen.getZ(), 0, 0.5, 0);
                            }
                        });
            }

            // 6. Spawn Fodder (for Siphon)
            if (queen.attackTimer % 60 == 0) {
                double angle = queen.getRandom().nextDouble() * Math.PI * 2;
                double x = Math.cos(angle) * 16;
                double z = Math.sin(angle) * 16;
                net.minecraft.entity.mob.ZombieEntity fodder = net.minecraft.entity.EntityType.ZOMBIE
                        .create(queen.getWorld());
                if (fodder != null) {
                    fodder.refreshPositionAndAngles(x, 100, z, 0, 0);
                    queen.getWorld().spawnEntity(fodder);
                }
            }
        }
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        // Immune to Fire, Lava, Drowning, Falling, Cactus
        if (damageSource.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_FIRE) ||
                damageSource.isOf(net.minecraft.entity.damage.DamageTypes.LAVA) ||
                damageSource.isOf(net.minecraft.entity.damage.DamageTypes.DROWN) ||
                damageSource.isOf(net.minecraft.entity.damage.DamageTypes.FALL) ||
                damageSource.isOf(net.minecraft.entity.damage.DamageTypes.CACTUS)) {
            return true;
        }

        // Immune to Projectiles (Arrows, Tridents)
        if (damageSource.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_PROJECTILE)) {
            // Exception: Wind Charges (Breeze) are projectiles but should hurt her
            // We can check if the source entity is a Wind Charge
            if (damageSource.getSource() instanceof net.minecraft.entity.projectile.BreezeWindChargeEntity ||
                    damageSource.getSource() instanceof net.minecraft.entity.projectile.WindChargeEntity) {
                return false;
            }
            return true;
        }

        // Immune to Melee (Player/Mob Attacks)
        if (damageSource.isOf(net.minecraft.entity.damage.DamageTypes.PLAYER_ATTACK) ||
                damageSource.isOf(net.minecraft.entity.damage.DamageTypes.MOB_ATTACK)) {
            return true;
        }

        return super.isInvulnerableTo(damageSource);
    }
}
