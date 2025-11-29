package com.antigravity.trapplatform.state;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import java.util.UUID;

public class BossBattleState extends PersistentState {

    private boolean isBattleActive = false;
    private int currentPhase = 0; // 0=None, 1=Queen, 2=Bowl, 3=Phase2
    private UUID bossUuid = null;

    public static BossBattleState getServerState(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                new net.minecraft.world.PersistentState.Type<>(
                        BossBattleState::new,
                        BossBattleState::fromNbt,
                        null // DataFixTypes
                ),
                "void_queen_battle"
        );
    }



    private boolean fearActive = false;
    private boolean jewisActive = false;
    private int absorptionTimer = 0;
    private boolean licaAscended = false;
    private boolean jewisPhasesDisabled = false;

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, net.minecraft.registry.RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putBoolean("isBattleActive", isBattleActive);
        nbt.putInt("currentPhase", currentPhase);
        if (bossUuid != null) {
            nbt.putUuid("bossUuid", bossUuid);
        }
        nbt.putBoolean("fearActive", fearActive);
        nbt.putBoolean("jewisActive", jewisActive);
        nbt.putInt("absorptionTimer", absorptionTimer);
        nbt.putBoolean("licaAscended", licaAscended);
        nbt.putBoolean("jewisPhasesDisabled", jewisPhasesDisabled);
        return nbt;
    }

    public static BossBattleState fromNbt(NbtCompound nbt, net.minecraft.registry.RegistryWrapper.WrapperLookup registryLookup) {
        BossBattleState state = new BossBattleState();
        state.isBattleActive = nbt.getBoolean("isBattleActive");
        state.currentPhase = nbt.getInt("currentPhase");
        if (nbt.contains("bossUuid")) {
            state.bossUuid = nbt.getUuid("bossUuid");
        }
        state.fearActive = nbt.getBoolean("fearActive");
        state.jewisActive = nbt.getBoolean("jewisActive");
        state.absorptionTimer = nbt.getInt("absorptionTimer");
        state.licaAscended = nbt.getBoolean("licaAscended");
        state.jewisPhasesDisabled = nbt.getBoolean("jewisPhasesDisabled");
        return state;
    }

    public void tick(ServerWorld world) {
        if (fearActive && jewisActive && !licaAscended) {
            absorptionTimer++;
            if (absorptionTimer % 20 == 0) { // Log every second
                com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("Absorption timer: " + absorptionTimer + "/100 (Fear: " + fearActive + ", Jewis: " + jewisActive + ")");
            }
            if (absorptionTimer >= 100) { // 5 seconds
                com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("TRIGGERING ABSORPTION EVENT!");
                triggerAssumption(); // Set the flag!
                triggerAbsorption(world);
            }
            this.markDirty();
        }
        
        // Pysro Absorption Timer (15 seconds after spawn)
        if (licaSpawned && !pysroAbsorbed) {
            pysroAbsorptionTimer++;
            if (pysroAbsorptionTimer >= 300) { // 15 seconds
                consumePysro(world);
            }
            this.markDirty();
        }
    }
    
    private void consumePysro(ServerWorld world) {
        // Find Lica
        com.antigravity.trapplatform.entity.LicaEntity lica = null;
        java.util.List<? extends com.antigravity.trapplatform.entity.LicaEntity> licas = world.getEntitiesByType(
            com.antigravity.trapplatform.TrapPlatformMod.LICA_ENTITY, e -> true
        );
        if (!licas.isEmpty()) lica = licas.get(0);
        
        // Find Pysro
        com.antigravity.trapplatform.entity.GrassPsychicPysroEntity pysro = null;
        java.util.List<? extends com.antigravity.trapplatform.entity.GrassPsychicPysroEntity> pysros = world.getEntitiesByType(
            com.antigravity.trapplatform.TrapPlatformMod.GRASS_PSYCHIC_PYSRO_ENTITY, e -> true
        );
        if (!pysros.isEmpty()) pysro = pysros.get(0);
        
        if (lica != null && pysro != null && pysro.isAlive()) {
            com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("EXECUTING PYSRO ABSORPTION!");
            
            // Effects at Pysro
            world.spawnParticles(net.minecraft.particle.ParticleTypes.EXPLOSION_EMITTER, 
                pysro.getX(), pysro.getY(), pysro.getZ(), 1, 0, 0, 0, 0);
            
            // Kill Pysro
            pysro.discard();
            
            // Power up Lica
            lica.absorbPysro();
            
            this.pysroAbsorbed = true;
            this.markDirty();
        }
    }

    private void triggerAbsorption(ServerWorld world) {
        this.licaAscended = true;
        this.jewisPhasesDisabled = true;
        this.fearActive = false;
        this.jewisActive = false;
        this.markDirty();

        com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("=== MASS ABSORPTION EVENT TRIGGERED ===");

        // Find spawn location (near first player)
        net.minecraft.server.network.ServerPlayerEntity nearestPlayer = null;
        for (net.minecraft.server.network.ServerPlayerEntity player : world.getPlayers()) {
            nearestPlayer = player;
            break;
        }

        double centerX = nearestPlayer != null ? nearestPlayer.getX() : 0;
        double centerY = nearestPlayer != null ? nearestPlayer.getY() + 10 : 110;
        double centerZ = nearestPlayer != null ? nearestPlayer.getZ() : 0;

        com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("Absorption center: " + centerX + ", " + centerY + ", " + centerZ);

        // === CONSUME ALL ENTITIES (100 block radius) ===
        // Only run absorption logic if triggered
        if (!this.assumptionTriggered) return;

        net.minecraft.util.math.Box absorptionBox = new net.minecraft.util.math.Box(
            centerX - 100, centerY - 100, centerZ - 100,
            centerX + 100, centerY + 100, centerZ + 100
        );

        // Kill ALL entities except players and Lica
        java.util.List<net.minecraft.entity.Entity> allEntities = world.getEntitiesByClass(
            net.minecraft.entity.Entity.class, 
            absorptionBox, 
            e -> !(e instanceof net.minecraft.server.network.ServerPlayerEntity) && 
                 !(e instanceof com.antigravity.trapplatform.entity.LicaEntity)
        );
        
        com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("Consuming " + allEntities.size() + " entities");
        for (net.minecraft.entity.Entity entity : allEntities) {
            // Particles at entity location before death
            world.spawnParticles(
                net.minecraft.particle.ParticleTypes.PORTAL,
                entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ(),
                20, 0.5, 0.5, 0.5, 0.1
            );
            entity.discard();
        }

        // === BREAK ALL PULLABLE BLOCKS (50 block radius) ===
        com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("Breaking pullable blocks");
        int radius = 50;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double dist = Math.sqrt(x*x + y*y + z*z);
                    if (dist <= radius) {
                        net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos(
                            (int)(centerX + x), 
                            (int)(centerY + y), 
                            (int)(centerZ + z)
                        );
                        net.minecraft.block.BlockState state = world.getBlockState(pos);
                        
                        // Check if pullable
                        if (state.getBlock() instanceof net.minecraft.block.FallingBlock ||
                            state.isOf(net.minecraft.block.Blocks.SAND) ||
                            state.isOf(net.minecraft.block.Blocks.RED_SAND) ||
                            state.isOf(net.minecraft.block.Blocks.GRAVEL) ||
                            state.isOf(net.minecraft.block.Blocks.WHITE_CONCRETE_POWDER) ||
                            state.isOf(net.minecraft.block.Blocks.ORANGE_CONCRETE_POWDER) ||
                            state.isOf(net.minecraft.block.Blocks.MAGENTA_CONCRETE_POWDER) ||
                            state.isOf(net.minecraft.block.Blocks.LIGHT_BLUE_CONCRETE_POWDER) ||
                            state.isOf(net.minecraft.block.Blocks.YELLOW_CONCRETE_POWDER) ||
                            state.isOf(net.minecraft.block.Blocks.LIME_CONCRETE_POWDER) ||
                            state.isOf(net.minecraft.block.Blocks.PINK_CONCRETE_POWDER) ||
                            state.isOf(net.minecraft.block.Blocks.GRAY_CONCRETE_POWDER) ||
                            state.isOf(net.minecraft.block.Blocks.LIGHT_GRAY_CONCRETE_POWDER) ||
                            state.isOf(net.minecraft.block.Blocks.CYAN_CONCRETE_POWDER) ||
                            state.isOf(net.minecraft.block.Blocks.PURPLE_CONCRETE_POWDER) ||
                            state.isOf(net.minecraft.block.Blocks.BLUE_CONCRETE_POWDER) ||
                            state.isOf(net.minecraft.block.Blocks.BROWN_CONCRETE_POWDER) ||
                            state.isOf(net.minecraft.block.Blocks.GREEN_CONCRETE_POWDER) ||
                            state.isOf(net.minecraft.block.Blocks.RED_CONCRETE_POWDER) ||
                            state.isOf(net.minecraft.block.Blocks.BLACK_CONCRETE_POWDER)) {
                            
                            world.breakBlock(pos, false);
                            world.spawnParticles(
                                net.minecraft.particle.ParticleTypes.CLOUD,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                5, 0.3, 0.3, 0.3, 0.0
                            );
                        }
                    }
                }
            }
        }

        // === PULL PLAYERS & DAMAGE EQUIPMENT ===
        for (net.minecraft.server.network.ServerPlayerEntity player : world.getPlayers()) {
            com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("Pulling player: " + player.getName().getString());
            
            // Calculate pull vector
            double dx = centerX - player.getX();
            double dy = centerY - player.getY();
            double dz = centerZ - player.getZ();
            double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);
            
            if (distance > 0 && distance < 100) {
                // Pull strength decreases with distance
                double pullStrength = (100 - distance) / 100.0 * 2.0;
                player.addVelocity(
                    (dx / distance) * pullStrength,
                    (dy / distance) * pullStrength,
                    (dz / distance) * pullStrength
                );
                player.velocityModified = true;
            }
            
            // Damage equipment (50% chance per piece)
            for (net.minecraft.entity.EquipmentSlot slot : net.minecraft.entity.EquipmentSlot.values()) {
                if (slot.getType() == net.minecraft.entity.EquipmentSlot.Type.HUMANOID_ARMOR || 
                    slot.getType() == net.minecraft.entity.EquipmentSlot.Type.HAND) {
                    
                    net.minecraft.item.ItemStack stack = player.getEquippedStack(slot);
                    if (!stack.isEmpty() && world.random.nextFloat() < 0.5f) {
                        int currentDamage = stack.getDamage();
                        stack.setDamage(Math.min(currentDamage + stack.getMaxDamage() / 2, stack.getMaxDamage()));
                        player.sendMessage(net.minecraft.text.Text.of("§c§lYour " + stack.getName().getString() + " was damaged by the absorption!"), true);
                    }
                }
            }
        }

        // === SPAWN MASS LICA AND GRASS-PSYCHIC PYSRO ===
        // Only spawn ONCE when assumption is triggered
        if (this.assumptionTriggered && !this.licaSpawned) {
            // Force remove any existing Licas to ensure a fresh, working boss
            java.util.List<? extends net.minecraft.entity.Entity> existingLicas = world.getEntitiesByType(
                com.antigravity.trapplatform.TrapPlatformMod.LICA_ENTITY, e -> true
            );
            for (net.minecraft.entity.Entity e : existingLicas) {
                e.discard(); // Kill existing Lica
            }
            
            com.antigravity.trapplatform.entity.LicaEntity lica = com.antigravity.trapplatform.TrapPlatformMod.LICA_ENTITY.create(world);
            if (lica != null) {
                // Try spawning at center first
                lica.refreshPositionAndAngles(centerX, centerY, centerZ, 0, 0);
                if (!world.spawnEntity(lica)) {
                    // Fallback: Spawn 5 blocks higher
                    lica.refreshPositionAndAngles(centerX, centerY + 5, centerZ, 0, 0);
                    world.spawnEntity(lica);
                    com.antigravity.trapplatform.TrapPlatformMod.LOGGER.warn("Initial spawn failed, spawned Lica higher at " + centerX + ", " + (centerY + 5) + ", " + centerZ);
                } else {
                    com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("Spawned FRESH Mass Lica at " + centerX + ", " + centerY + ", " + centerZ);
                }
                
                // Tell players Lica spawned
                world.getPlayers().forEach(p -> {
                    p.sendMessage(net.minecraft.text.Text.of("§5§l✦ MASS LICA HAS BEEN BORN! ✦"), false);
                });
                
                lica.setAscended(true);
                world.getPlayers().forEach(p -> {
                    p.sendMessage(net.minecraft.text.Text.of("§5Mass Lica has ASCENDED!"), false);
                });
                
                this.licaSpawned = true; // Mark as spawned so we don't loop!
                
                // Spawn Grass-Psychic Pysro
                com.antigravity.trapplatform.entity.GrassPsychicPysroEntity pysro = 
                    com.antigravity.trapplatform.TrapPlatformMod.GRASS_PSYCHIC_PYSRO_ENTITY.create(world);
                
                if (pysro != null) {
                    // Find nearest player and spawn Pysro on OPPOSITE side from Lica
                    net.minecraft.entity.player.PlayerEntity closestPlayer = world.getClosestPlayer(centerX, centerY, centerZ, 100, false);
                    if (closestPlayer != null) {
                        // Spawn Pysro 15 blocks away in opposite direction from Lica
                        double dx = lica.getX() - closestPlayer.getX();
                        double dz = lica.getZ() - closestPlayer.getZ();
                        double distance = Math.sqrt(dx * dx + dz * dz);
                        if (distance > 0) {
                            // Normalize and spawn 15 blocks away on opposite side
                            double pysroX = closestPlayer.getX() - (dx / distance) * 15;
                            double pysroZ = closestPlayer.getZ() - (dz / distance) * 15;
                            pysro.refreshPositionAndAngles(pysroX, closestPlayer.getY(), pysroZ, 0, 0);
                        } else {
                            pysro.refreshPositionAndAngles(closestPlayer.getX() + 15, closestPlayer.getY(), closestPlayer.getZ(), 0, 0);
                        }
                    } else {
                        pysro.refreshPositionAndAngles(centerX + 15, centerY, centerZ, 0, 0);
                    }
                    world.spawnEntity(pysro);
                    
                    // Link Pysro to Mass Lica so she knows when it dies
                    lica.setPysroReference(pysro);
                    
                    com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("Spawned Grass-Psychic Pysro and linked to Mass Lica");
                    
                    world.getPlayers().forEach(p -> {
                        p.sendMessage(net.minecraft.text.Text.of("§2§l✦ GRASS-PSYCHIC PYSRO HAS BEEN BORN! ✦"), false);
                    });
                    
                    // MASSIVE effects
                    world.playSound(null, centerX, centerY, centerZ, 
                        net.minecraft.sound.SoundEvents.ENTITY_ENDER_DRAGON_GROWL, 
                        net.minecraft.sound.SoundCategory.HOSTILE, 10.0f, 0.5f);
                    world.playSound(null, centerX, centerY, centerZ, 
                        net.minecraft.sound.SoundEvents.ENTITY_WITHER_SPAWN, 
                        net.minecraft.sound.SoundCategory.HOSTILE, 5.0f, 1.0f);
                    
                    world.getPlayers().forEach(p -> {
                        p.sendMessage(net.minecraft.text.Text.of("§5§l§k|||§r §5§lMASS LICA§r §d§land§r §2§lTHE GRASS-PSYCHIC PYSRO§r §5§l§k|||"), false);
                        p.sendMessage(net.minecraft.text.Text.of("§d§oThey have been born from the absorbed power!"), true);
                    });
                    
                    com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("=== MASS LICA AND PYSRO SUMMONED ===");
                } else {
                    com.antigravity.trapplatform.TrapPlatformMod.LOGGER.error("CRITICAL: Failed to spawn Pysro!");
                }
            } else {
                com.antigravity.trapplatform.TrapPlatformMod.LOGGER.error("Failed to create Mass Lica entity!");
            }
        }
    }

    // jewisActive removed (already defined)
    private boolean assumptionTriggered = false;
    private boolean licaSpawned = false;
    private int pysroAbsorptionTimer = 0;
    private boolean pysroAbsorbed = false;

    public void setFearActive(boolean active) {
        this.fearActive = active;
        this.markDirty();
    }

    public void setJewisActive(boolean active) {
        this.jewisActive = active;
        this.markDirty();
    }
    
    public void triggerAssumption() {
        this.assumptionTriggered = true;
        this.licaSpawned = false; // Allow respawn if triggered again
        this.pysroAbsorptionTimer = 0;
        this.pysroAbsorbed = false;
        com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("Assumption Triggered! Mass Lica will spawn.");
        this.markDirty();
    }

    public boolean isJewisPhasesDisabled() {
        return jewisPhasesDisabled;
    }

    public boolean isBattleActive() {
        return isBattleActive;
    }

    public void startBattle(UUID bossUuid) {
        this.isBattleActive = true;
        this.currentPhase = 1;
        this.bossUuid = bossUuid;
        this.markDirty();
    }

    public void setPhase(int phase) {
        this.currentPhase = phase;
        this.markDirty();
    }

    public void endBattle() {
        this.isBattleActive = false;
        this.currentPhase = 0;
        this.bossUuid = null;
        this.fearActive = false;
        this.jewisActive = false;
        this.absorptionTimer = 0;
        this.licaAscended = false;
        this.jewisPhasesDisabled = false;
        this.markDirty();
    }
}
