package com.antigravity.trapplatform.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.world.World;

public class FearEntity extends EvokerEntity {

    public FearEntity(EntityType<? extends EvokerEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        // Vulnerable to Fireballs (so player can deflect them back)
        if (damageSource.getSource() instanceof net.minecraft.entity.projectile.FireballEntity) {
            return false;
        }

        // Immune to Fire, Lava, Lightning, Arrows, Explosions
        if (damageSource.isIn(DamageTypeTags.IS_FIRE) ||
                damageSource.isIn(DamageTypeTags.IS_LIGHTNING) ||
                damageSource.isIn(DamageTypeTags.IS_PROJECTILE) ||
                damageSource.isIn(DamageTypeTags.IS_EXPLOSION)) {
            return true;
        }
        return super.isInvulnerableTo(damageSource);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean damaged = super.damage(source, amount);

        // Check if hit by a reflected fireball (Attacker is usually the player who hit
        // it)
        if (source.getSource() instanceof net.minecraft.entity.projectile.FireballEntity) {
            clearMinions();
            // Optional: Play sound or effect
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.minecraft.sound.SoundEvents.ENTITY_GENERIC_EXPLODE, net.minecraft.sound.SoundCategory.HOSTILE,
                    1.0f, 0.5f);
        }

        return damaged;
    }

    private void clearMinions() {
        if (this.getWorld().isClient)
            return;

        // Kill Vexes, Ravagers, and other minions in range
        java.util.List<net.minecraft.entity.Entity> minions = this.getWorld().getOtherEntities(this,
                this.getBoundingBox().expand(50), entity -> {
                    return entity instanceof net.minecraft.entity.mob.VexEntity ||
                            entity instanceof net.minecraft.entity.mob.RavagerEntity ||
                            entity instanceof net.minecraft.entity.mob.ZombieEntity ||
                            entity instanceof net.minecraft.entity.mob.SkeletonEntity;
                });

        for (net.minecraft.entity.Entity minion : minions) {
            // Don't kill the boss itself (already filtered by getOtherEntities usually, but
            // good to be safe)
            if (minion != this) {
                minion.kill();
                // Spawn particles/smoke where they died
                ((net.minecraft.server.world.ServerWorld) this.getWorld()).spawnParticles(
                        net.minecraft.particle.ParticleTypes.POOF, minion.getX(), minion.getY(), minion.getZ(), 10, 0.5,
                        0.5, 0.5, 0.1);
            }
        }

        // Announce
        if (!minions.isEmpty()) {
            // Find nearest player to announce? Or just broadcast?
            // Simple way:
            this.getWorld().getPlayers().forEach(
                    p -> p.sendMessage(net.minecraft.text.Text.of("§b§lThe Fear Boss's minions vanish!"), true));
        }
    }

    private boolean stateSet = false;

    @Override
    public void tick() {
        super.tick();
        
        // Set state once on server
        if (!this.getWorld().isClient && !stateSet) {
            if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                net.minecraft.server.world.ServerWorld overworld = serverWorld.getServer().getWorld(net.minecraft.world.World.OVERWORLD);
                if (overworld != null) {
                    com.antigravity.trapplatform.state.BossBattleState.getServerState(overworld).setFearActive(true);
                    stateSet = true;
                    com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("Fear spawned - state set to active");
                }
            }
        }
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        if (!this.getWorld().isClient && this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            net.minecraft.server.world.ServerWorld overworld = serverWorld.getServer().getWorld(net.minecraft.world.World.OVERWORLD);
            if (overworld != null) {
                com.antigravity.trapplatform.state.BossBattleState.getServerState(overworld).setFearActive(false);
                com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("Fear died - state set to inactive");
            }
        }

        if (!this.getWorld().isClient) {
            // Give Amulet to all players in range (e.g. 100 blocks)
            net.minecraft.item.Item amuletItem = net.minecraft.registry.Registries.ITEM
                    .get(net.minecraft.util.Identifier.of("trapplatform", "fear_amulet"));

            this.getWorld().getPlayers().forEach(player -> {
                if (player.squaredDistanceTo(this) < 100 * 100) {
                    net.minecraft.item.ItemStack stack = new net.minecraft.item.ItemStack(amuletItem);
                    if (!player.getInventory().insertStack(stack)) {
                        player.dropItem(stack, false);
                    }
                    player.sendMessage(net.minecraft.text.Text.of("§5§lThe Amulet of Fear manifests in your hands..."),
                            true);
                }
            });
        }
    }
}
