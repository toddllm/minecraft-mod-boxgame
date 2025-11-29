package com.antigravity.trapplatform.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class BowlEntity extends Entity {

    private boolean isFlipping = false;
    private int flipTimer = 0;

    public BowlEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker(net.minecraft.entity.data.DataTracker.Builder builder) {
        // No data needed
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.isFlipping = nbt.getBoolean("Flipping");
        this.flipTimer = nbt.getInt("FlipTimer");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putBoolean("Flipping", this.isFlipping);
        nbt.putInt("FlipTimer", this.flipTimer);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!this.getWorld().isClient && !this.isFlipping) {
            this.isFlipping = true;
            this.flipTimer = 0;
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isFlipping) {
            this.flipTimer++;

            // Visuals: Flip Animation (Rotation handled in Renderer via age/timer)
            // Particles: Souls coming out
            if (this.flipTimer > 20 && this.flipTimer < 60) {
                if (this.getWorld().isClient) {
                    for (int i = 0; i < 5; i++) {
                        this.getWorld().addParticle(ParticleTypes.SCULK_SOUL, 
                                this.getX() + (this.random.nextDouble() - 0.5), 
                                this.getY() + 0.5, 
                                this.getZ() + (this.random.nextDouble() - 0.5), 
                                0, 0.1, 0);
                    }
                }
            }

            // Sequence Completion
            if (!this.getWorld().isClient && this.flipTimer >= 100) {
                spawnPhase2();
                this.discard();
            }
        }
    }

    private void spawnPhase2() {
        World world = this.getWorld();

        // 1. Spawn Lica
        LicaEntity lica = new LicaEntity(com.antigravity.trapplatform.TrapPlatformMod.LICA_ENTITY, world);
        lica.setPosition(this.getX() + 2, this.getY() + 5, this.getZ());
        world.spawnEntity(lica);

        // Spawn Phase 2 Boss
        com.antigravity.trapplatform.entity.VoidQueenPhase2Entity boss = com.antigravity.trapplatform.TrapPlatformMod.VOID_QUEEN_PHASE_2_ENTITY.create(this.getWorld());
        if (boss != null) {
            boss.refreshPositionAndAngles(this.getX(), this.getY() + 2, this.getZ(), 0, 0);
            this.getWorld().spawnEntity(boss);
        }

        // Update Battle State
        if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            com.antigravity.trapplatform.state.BossBattleState.getServerState(serverWorld).setPhase(3); // Phase 2
        }
        
        this.discard();
        
        // Effects
        world.playSound(null, this.getBlockPos(), net.minecraft.sound.SoundEvents.ENTITY_WITHER_SPAWN, net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
    }
    
    @Override
    public boolean canHit() {
        return true;
    }
}
