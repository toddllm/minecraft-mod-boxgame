package com.antigravity.trapplatform.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.BreezeWindChargeEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WindChargeTurretEntity extends Entity {

    private int shootCooldown = 0;

    public WindChargeTurretEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker(net.minecraft.entity.data.DataTracker.Builder builder) {
        // No data to track
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        // No custom data
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        // No custom data
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!this.getWorld().isClient && !player.hasVehicle()) {
            player.startRiding(this);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.shootCooldown > 0) {
            this.shootCooldown--;
        }

        if (!this.getWorld().isClient) {
            Entity passenger = this.getFirstPassenger();
            if (passenger instanceof PlayerEntity player) {
                // Rotate turret with player
                this.setYaw(player.getYaw());
                this.setPitch(player.getPitch());
                this.setHeadYaw(player.getHeadYaw());

                // Shoot if player swings hand (Left Click)
                if (player.handSwinging && this.shootCooldown == 0) {
                    shoot(player);
                    this.shootCooldown = 10; // 0.5s cooldown
                }
            }
        }
    }

    private void shoot(PlayerEntity player) {
        Vec3d look = player.getRotationVector();
        BreezeWindChargeEntity charge = new BreezeWindChargeEntity(net.minecraft.entity.EntityType.BREEZE_WIND_CHARGE,
                this.getWorld());
        charge.setPosition(player.getX(), player.getEyeY(), player.getZ());
        charge.setVelocity(look.x, look.y, look.z, 1.5f, 0.0f);
        charge.setOwner(player); // Player is owner so they get credit for damage
        this.getWorld().spawnEntity(charge);

        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                net.minecraft.sound.SoundEvents.ENTITY_BREEZE_WIND_BURST, net.minecraft.sound.SoundCategory.PLAYERS,
                1.0f, 1.0f);
    }

    @Override
    public boolean canHit() {
        return true; // Can be broken
    }
}
