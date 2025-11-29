package com.antigravity.trapplatform.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class VoidQueenPhase2Entity extends VoidQueenEntity {

    private final ServerBossBar bossBar = (ServerBossBar) (new ServerBossBar(Text.of("Jewis (Ascended)"),
            BossBar.Color.RED, BossBar.Style.NOTCHED_10)).setDarkenSky(true);
    private int attackTimer = 0;

    public VoidQueenPhase2Entity(EntityType<? extends VoidQueenEntity> entityType, World world) {
        super(entityType, world);
        this.setCustomName(Text.of("Jewis (Ascended)"));
        if (!world.isClient && world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            net.minecraft.server.world.ServerWorld overworld = serverWorld.getServer().getWorld(net.minecraft.world.World.OVERWORLD);
            if (overworld != null) {
                com.antigravity.trapplatform.state.BossBattleState.getServerState(overworld).setJewisActive(true);
                com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("Jewis spawned - state set to active");
            }
        }
    }

    @Override
    public void onDeath(net.minecraft.entity.damage.DamageSource damageSource) {
        super.onDeath(damageSource);
        if (!this.getWorld().isClient && this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            net.minecraft.server.world.ServerWorld overworld = serverWorld.getServer().getWorld(net.minecraft.world.World.OVERWORLD);
            if (overworld != null) {
                com.antigravity.trapplatform.state.BossBattleState.getServerState(overworld).setJewisActive(false);
                com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("Jewis died - state set to inactive");
            }
        }
    }

    public static DefaultAttributeContainer.Builder createPhase2Attributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 100.0) // Stronger (50 Hearts)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1) // Faster
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.1)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 100.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new VoidQueenFlyGoal(this));
        this.goalSelector.add(2, new Phase2AttackGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    static class Phase2AttackGoal extends net.minecraft.entity.ai.goal.Goal {
        private final VoidQueenPhase2Entity queen;

        public Phase2AttackGoal(VoidQueenPhase2Entity queen) {
            this.queen = queen;
        }

        @Override
        public boolean canStart() {
            return queen.getTarget() != null;
        }

        @Override
        public void tick() {
            LivingEntity target = queen.getTarget();
            if (target == null) return;

            queen.attackTimer++;

            // 1. Void Lightning (Frequent)
            if (queen.attackTimer % 40 == 0) {
                net.minecraft.entity.LightningEntity lightning = net.minecraft.entity.EntityType.LIGHTNING_BOLT.create(queen.getWorld());
                lightning.setPosition(target.getX(), target.getY(), target.getZ());
                queen.getWorld().spawnEntity(lightning);
            }

            // 2. Sonic Boom (Occasional)
            if (queen.attackTimer % 100 == 0) {
                queen.getWorld().playSound(null, queen.getX(), queen.getY(), queen.getZ(), 
                        net.minecraft.sound.SoundEvents.ENTITY_WARDEN_SONIC_BOOM, 
                        net.minecraft.sound.SoundCategory.HOSTILE, 3.0f, 1.0f);
                
                // Damage and Knockback (Sonic Boom logic)
                if (queen.distanceTo(target) < 20) {
                    target.damage(queen.getDamageSources().sonicBoom(queen), 10.0f);
                    double d = target.getX() - queen.getX();
                    double e = target.getZ() - queen.getZ();
                    double f = Math.max(d * d + e * e, 0.001);
                    target.addVelocity(d / f * 5.0, 0.2, e / f * 5.0);
                }
            }
        }
    }




    private int phaseLevel = 1;

    @Override
    public void writeCustomDataToNbt(net.minecraft.nbt.NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("PhaseLevel", this.phaseLevel);
    }

    @Override
    public void readCustomDataFromNbt(net.minecraft.nbt.NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.phaseLevel = nbt.getInt("PhaseLevel");
        this.setCustomName(Text.of("Jewis (Phase " + this.phaseLevel + ")"));
    }

    @Override
    public boolean damage(net.minecraft.entity.damage.DamageSource source, float amount) {
        boolean damaged = super.damage(source, amount);
        
        if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
             net.minecraft.server.world.ServerWorld overworld = serverWorld.getServer().getWorld(net.minecraft.world.World.OVERWORLD);
             if (overworld != null && com.antigravity.trapplatform.state.BossBattleState.getServerState(overworld).isJewisPhasesDisabled()) {
                 return damaged; // Die normally if phases disabled
             }
        }

        if (damaged && this.getHealth() <= 0 && this.phaseLevel < 1000) {
            // Rebirth Logic
            this.setHealth(this.getMaxHealth());
            this.phaseLevel++;
            
            // Update Name
            this.setCustomName(Text.of("Jewis (Phase " + this.phaseLevel + ")"));
            
            // Buff Stats (Example: +10 HP per phase, max 5000)
            double newMaxHealth = 100.0 + (this.phaseLevel * 10.0);
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(newMaxHealth);
            this.setHealth((float)newMaxHealth); // Heal to new max

            // Visual/Audio Effects
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), 
                    net.minecraft.sound.SoundEvents.ITEM_TOTEM_USE, 
                    net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
            this.getWorld().sendEntityStatus(this, (byte) 35); // Totem particles

            return false; // Prevent actual death
        }
        return damaged;
    }


}
