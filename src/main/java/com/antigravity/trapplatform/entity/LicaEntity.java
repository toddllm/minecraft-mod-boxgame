package com.antigravity.trapplatform.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LicaEntity extends FlyingEntity {

    private boolean ascended = false;
    private boolean enraged = false;
    private com.antigravity.trapplatform.entity.GrassPsychicPysroEntity pysroReference = null;
    private int dialogueCooldown = 0;

    private static final net.minecraft.entity.data.TrackedData<Boolean> ABSORBED_PYSRO = net.minecraft.entity.data.DataTracker
            .registerData(LicaEntity.class, net.minecraft.entity.data.TrackedDataHandlerRegistry.BOOLEAN);

    private static final String[] DIALOGUE_LINES = {
            "§5§lYou dare challenge Mass Lica?",
            "§d§oThe evoker fangs... they hunger...",
            "§5§lI am the ascended one!",
            "§d§oFear and Jewis were just the beginning...",
            "§5§lYour end approaches with every fang!",
            "§d§oThe mass... it consumes all!",
            "§5§lBow before the Grass-Psychic power!",
            "§d§oYou cannot escape my fangs!",
            "§5§lI transcended death itself!"
    };

    public LicaEntity(EntityType<? extends FlyingEntity> entityType, World world) {
        super(entityType, world);
        this.setPersistent();
        com.antigravity.trapplatform.TrapPlatformMod.LOGGER
                .info("LicaEntity CONSTRUCTOR called! " + this.getUuidAsString());
    }

    @Override
    protected void initDataTracker(net.minecraft.entity.data.DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ABSORBED_PYSRO, false);
    }

    public static DefaultAttributeContainer.Builder createLicaAttributes() {
        return FlyingEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 50.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 1024.0); // Infinite sight/range
    }

    @Override
    protected void initGoals() {
        com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("LicaEntity initGoals called!");
        this.goalSelector.add(0, new SwimGoal(this));
        // Unified Combat Goal handles everything now
        this.goalSelector.add(1, new MassLicaCombatGoal(this));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(8, new LookAroundGoal(this));

        // Target priority: Players > All Mobs (except Pysro)
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(2,
                new ActiveTargetGoal<>(this, net.minecraft.entity.mob.MobEntity.class, 10, true, false,
                        entity -> !(entity instanceof com.antigravity.trapplatform.entity.GrassPsychicPysroEntity)));

        // VISUALS: Equip Grimoire (Force equip on init)
        this.equipStack(net.minecraft.entity.EquipmentSlot.MAINHAND,
                new net.minecraft.item.ItemStack(net.minecraft.item.Items.ENCHANTED_BOOK));
        this.setEquipmentDropChance(net.minecraft.entity.EquipmentSlot.MAINHAND, 0.0f);

        // VISUALS: Equip Elytra (Wings)
        this.equipStack(net.minecraft.entity.EquipmentSlot.CHEST,
                new net.minecraft.item.ItemStack(net.minecraft.item.Items.ELYTRA));
        this.setEquipmentDropChance(net.minecraft.entity.EquipmentSlot.CHEST, 0.0f);
    }

    @Override
    protected void initEquipment(net.minecraft.util.math.random.Random random,
            net.minecraft.world.LocalDifficulty localDifficulty) {
        super.initEquipment(random, localDifficulty);
        this.equipStack(net.minecraft.entity.EquipmentSlot.MAINHAND,
                new net.minecraft.item.ItemStack(net.minecraft.item.Items.ENCHANTED_BOOK));
        this.setEquipmentDropChance(net.minecraft.entity.EquipmentSlot.MAINHAND, 0.0f);
        this.equipStack(net.minecraft.entity.EquipmentSlot.CHEST,
                new net.minecraft.item.ItemStack(net.minecraft.item.Items.ELYTRA));
        this.setEquipmentDropChance(net.minecraft.entity.EquipmentSlot.CHEST, 0.0f);
    }

    // Helper for Dynamic Equipment
    public void equipMainHand(net.minecraft.item.Item item) {
        if (!this.getMainHandStack().isOf(item)) {
            this.equipStack(net.minecraft.entity.EquipmentSlot.MAINHAND, new net.minecraft.item.ItemStack(item));
        }
    }

    public void setPysroReference(com.antigravity.trapplatform.entity.GrassPsychicPysroEntity pysro) {
        this.pysroReference = pysro;
    }

    // Boss Bar
    private final net.minecraft.entity.boss.ServerBossBar bossBar = (net.minecraft.entity.boss.ServerBossBar) new net.minecraft.entity.boss.ServerBossBar(
            net.minecraft.text.Text.of("Mass Lica"), // Use explicit text to avoid issues
            net.minecraft.entity.boss.BossBar.Color.PURPLE,
            net.minecraft.entity.boss.BossBar.Style.PROGRESS).setDarkenSky(true);

    @Override
    public void onStartedTrackingBy(net.minecraft.server.network.ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(net.minecraft.server.network.ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }

    @Override
    public void tick() {
        super.tick();

        // Ensure persistence
        if (!this.isPersistent()) {
            this.setPersistent();
        }

        // Make Lica GLOW BRIGHTLY
        if (!this.hasStatusEffect(StatusEffects.GLOWING)) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 999999, 0, false, false));
        }

        // FORCE VISIBILITY (Debug Fix)
        this.setInvisible(false);

        // AERIAL POSTURE: Never touch the ground!
        this.setNoGravity(true);

        // HIGHLANDER RULE: There can be only one! (Prevent Duplicate Boss Bars)
        // SINGLETON V2: Check every 5 ticks (0.25s) for faster cleanup
        if (!this.getWorld().isClient && this.age % 5 == 0) {
            java.util.List<LicaEntity> others = this.getWorld().getEntitiesByClass(LicaEntity.class,
                    this.getBoundingBox().expand(500.0), e -> e != this);
            for (LicaEntity other : others) {
                // If I am younger than another Lica, I am the impostor. Die.
                if (other.age > this.age || (other.age == this.age && other.getId() < this.getId())) {
                    com.antigravity.trapplatform.TrapPlatformMod.LOGGER
                            .warn("Duplicate Lica detected! Removing younger/duplicate entity (ID: " + this.getId()
                                    + ", Age: " + this.age + ")");
                    this.discard();
                    return;
                }
            }
        }

        // GLOBAL BOSS BAR FIX (Force add all players)
        if (!this.getWorld().isClient && this.age % 20 == 0) {
            for (net.minecraft.entity.player.PlayerEntity player : this.getWorld().getPlayers()) {
                if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                    if (!this.bossBar.getPlayers().contains(serverPlayer)) {
                        this.bossBar.addPlayer(serverPlayer);
                    }
                }
            }
        }

        // Dialogue system
        if (!this.getWorld().isClient && ascended) {
            if (dialogueCooldown > 0) {
                dialogueCooldown--;
            } else if (this.getTarget() != null && this.random.nextFloat() < 0.03f) {
                String dialogue = DIALOGUE_LINES[this.random.nextInt(DIALOGUE_LINES.length)];
                this.getWorld().getPlayers().forEach(p -> {
                    if (p.squaredDistanceTo(this) < 1000) { // 50 block range
                        p.sendMessage(net.minecraft.text.Text.of("§5§o" + dialogue), true);
                    }
                });
                dialogueCooldown = 1; // NO COOLDOWN!
            }
        }

        // Bright purple particle aura (ALWAYS)
        if (this.getWorld().isClient && ascended) {
            for (int i = 0; i < 3; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 2;
                double offsetY = this.random.nextDouble() * 2;
                double offsetZ = (this.random.nextDouble() - 0.5) * 2;
                this.getWorld().addParticle(ParticleTypes.WITCH,
                        this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0, 0.1, 0);
                this.getWorld().addParticle(ParticleTypes.PORTAL,
                        this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0, 0.05, 0);
            }
        }

        // Debug: Announce location periodically
        if (!this.getWorld().isClient && this.age % 100 == 0) {
            com.antigravity.trapplatform.TrapPlatformMod.LOGGER
                    .info("Lica is at: " + this.getX() + ", " + this.getY() + ", " + this.getZ());
        }

        // Check if Pysro died (trigger enraged mode)
        if (ascended && !enraged && pysroReference != null && !pysroReference.isAlive()) {
            triggerEnragedMode();
        }

        // SAFETY LEASH (Force return if too far)
        // Tightened to 30 blocks (900 squared) to prevent her from getting lost
        if (!this.getWorld().isClient && this.getTarget() != null) {
            double distSq = this.squaredDistanceTo(this.getTarget());
            if (distSq > 900) { // 30 blocks
                this.teleportToFlank(this.getTarget());
                com.antigravity.trapplatform.TrapPlatformMod.LOGGER
                        .warn("Lica Safety Leash Triggered! Too far from target (" + Math.sqrt(distSq)
                                + " blocks). Teleporting back.");
            }
        }

        // VORTEX PULL LOGIC
        if (!this.getWorld().isClient) {
            java.util.List<net.minecraft.entity.AreaEffectCloudEntity> clouds = this.getWorld().getEntitiesByClass(
                    net.minecraft.entity.AreaEffectCloudEntity.class,
                    this.getBoundingBox().expand(50.0),
                    e -> e.getParticleType() == ParticleTypes.PORTAL // Identify our vortexes
            );

            for (net.minecraft.entity.AreaEffectCloudEntity cloud : clouds) {
                for (net.minecraft.entity.player.PlayerEntity player : this.getWorld().getPlayers()) {
                    if (player.squaredDistanceTo(cloud) < 100.0) { // 10 block radius
                        Vec3d dir = cloud.getPos().subtract(player.getPos()).normalize().multiply(0.2); // Pull strength
                        player.addVelocity(dir.x, dir.y, dir.z);
                        player.velocityModified = true;
                    }
                }
            }
        }

        // SPELLBOOK LOGIC (Random Spells)
        if (!this.getWorld().isClient && ascended && this.getTarget() != null && this.random.nextFloat() < 0.02f) { // 2%
                                                                                                                    // chance
                                                                                                                    // per
                                                                                                                    // tick
                                                                                                                    // (~once
                                                                                                                    // every
                                                                                                                    // 2.5s)
            castRandomSpell();
        }
    }

    // === LICA'S SPELLBOOK ===

    private void castRandomSpell() {
        // VISUALS: Equip Book
        this.equipMainHand(net.minecraft.item.Items.ENCHANTED_BOOK);

        int spell = this.random.nextInt(6);
        switch (spell) {
            case 0:
                castPotionMastery();
                break;
            case 1:
                castFriendEmy();
                break;
            case 2:
                castMockery();
                break;
            case 3:
                castDevourer();
                break;
            case 4:
                castAlchemy();
                break;
            case 5:
                castVanish();
                break;
        }
    }

    private void castPotionMastery() {
        // Buffs: Strength, Speed, Regen, Resistance
        net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.effect.StatusEffect>[] effects = new net.minecraft.registry.entry.RegistryEntry[] {
                StatusEffects.STRENGTH, StatusEffects.SPEED, StatusEffects.REGENERATION, StatusEffects.RESISTANCE
        };
        net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.effect.StatusEffect> effect = effects[this.random
                .nextInt(effects.length)];
        this.addStatusEffect(new StatusEffectInstance(effect, 200, 1)); // 10 seconds, Amp 1
        this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_WITCH_DRINK,
                net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
        com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("Lica cast Potion Mastery: " + effect.getIdAsString());
    }

    private void castFriendEmy() {
        // Passive -> Hostile
        java.util.List<net.minecraft.entity.passive.PassiveEntity> passives = this.getWorld().getEntitiesByClass(
                net.minecraft.entity.passive.PassiveEntity.class, this.getBoundingBox().expand(20.0), e -> true);

        for (net.minecraft.entity.passive.PassiveEntity passive : passives) {
            EntityType<?> newType = null;
            if (passive instanceof net.minecraft.entity.passive.PigEntity)
                newType = EntityType.PIGLIN_BRUTE;
            else if (passive instanceof net.minecraft.entity.passive.CowEntity)
                newType = EntityType.RAVAGER;
            else if (passive instanceof net.minecraft.entity.passive.ChickenEntity)
                newType = EntityType.VEX;
            else if (passive instanceof net.minecraft.entity.passive.SheepEntity)
                newType = EntityType.EVOKER;
            else if (passive instanceof net.minecraft.entity.passive.VillagerEntity)
                newType = EntityType.WITCH;

            if (newType != null)
                transformMob(passive, newType);
        }
        if (!passives.isEmpty()) {
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,
                    net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
        }
    }

    private void castMockery() {
        // Hostile -> Passive
        java.util.List<net.minecraft.entity.mob.HostileEntity> hostiles = this.getWorld().getEntitiesByClass(
                net.minecraft.entity.mob.HostileEntity.class, this.getBoundingBox().expand(20.0),
                e -> !e.getUuid().equals(this.getUuid())
                        && (pysroReference == null || !e.getUuid().equals(pysroReference.getUuid()))
                        && !(e instanceof net.minecraft.entity.boss.WitherEntity)); // Don't mock bosses

        for (net.minecraft.entity.mob.HostileEntity hostile : hostiles) {
            EntityType<?> newType = null;
            if (hostile instanceof net.minecraft.entity.mob.CreeperEntity)
                newType = EntityType.CHICKEN;
            else if (hostile instanceof net.minecraft.entity.mob.ZombieEntity)
                newType = EntityType.SHEEP;
            else if (hostile instanceof net.minecraft.entity.mob.SkeletonEntity)
                newType = EntityType.COW;
            else if (hostile instanceof net.minecraft.entity.mob.SpiderEntity)
                newType = EntityType.PIG;

            if (newType != null)
                transformMob(hostile, newType);
        }
        if (!hostiles.isEmpty()) {
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_WITCH_CELEBRATE,
                    net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
        }
    }

    private void transformMob(net.minecraft.entity.Entity target, EntityType<?> newType) {
        net.minecraft.entity.Entity newMob = newType.create(this.getWorld());
        if (newMob != null) {
            newMob.refreshPositionAndAngles(target.getX(), target.getY(), target.getZ(), target.getYaw(),
                    target.getPitch());
            this.getWorld().spawnEntity(newMob);
            target.discard();
            this.getWorld().addParticle(ParticleTypes.POOF, target.getX(), target.getY() + 1, target.getZ(), 0, 0, 0);
        }
    }

    private void castDevourer() {
        // Giant Mouth: Tunnel of Fangs
        LivingEntity target = this.getTarget();
        if (target == null)
            return;

        Vec3d dir = target.getPos().subtract(this.getPos()).normalize();
        Vec3d start = this.getPos().add(dir.multiply(2.0));

        for (int i = 0; i < 15; i++) { // 15 blocks long
            Vec3d center = start.add(dir.multiply(i));
            // 3x3 tunnel
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    // Perpendicular offset logic simplified (just box around center)
                    double px = center.x + x;
                    double py = center.y + y;
                    double pz = center.z + x; // Simple offset

                    this.spawnFang(px, py, pz);

                    // Break blocks
                    BlockPos pos = new BlockPos((int) px, (int) py, (int) pz);
                    if (!this.getWorld().isAir(pos)
                            && this.getWorld().getBlockState(pos).getHardness(this.getWorld(), pos) != -1.0f) {
                        this.getWorld().breakBlock(pos, false);
                    }
                }
            }
        }
        this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_EVOKER_FANGS_ATTACK,
                net.minecraft.sound.SoundCategory.HOSTILE, 2.0f, 0.5f);
    }

    private void castAlchemy() {
        int type = this.random.nextInt(3);
        BlockPos center = this.getBlockPos();
        int radius = 5;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.add(x, y, z);
                    net.minecraft.block.BlockState state = this.getWorld().getBlockState(pos);

                    if (type == 0) { // Solidify: Water/Lava -> Grass
                        if (state.isOf(net.minecraft.block.Blocks.WATER)
                                || state.isOf(net.minecraft.block.Blocks.LAVA)) {
                            this.getWorld().setBlockState(pos,
                                    net.minecraft.block.Blocks.GRASS_BLOCK.getDefaultState());
                        }
                    } else if (type == 1) { // Liquify: Solid -> Lava
                        if (!state.isAir() && state.isSolidBlock(this.getWorld(), pos)
                                && state.getHardness(this.getWorld(), pos) != -1.0f) {
                            if (this.random.nextFloat() < 0.1f) // Don't melt everything, just 10%
                                this.getWorld().setBlockState(pos, net.minecraft.block.Blocks.LAVA.getDefaultState());
                        }
                    } else { // Harden: Sand/Dirt -> Obsidian
                        if (state.isOf(net.minecraft.block.Blocks.SAND)
                                || state.isOf(net.minecraft.block.Blocks.DIRT)) {
                            this.getWorld().setBlockState(pos, net.minecraft.block.Blocks.OBSIDIAN.getDefaultState());
                        } else if (state.isOf(net.minecraft.block.Blocks.GRAVEL)) {
                            this.getWorld().setBlockState(pos, net.minecraft.block.Blocks.BEDROCK.getDefaultState());
                        }
                    }
                }
            }
        }
        this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
    }

    private void castVanish() {
        // Delete blocks/mobs in radius
        BlockPos center = this.getBlockPos();
        int radius = 4;

        // Blocks
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.add(x, y, z);
                    if (!this.getWorld().isAir(pos)
                            && this.getWorld().getBlockState(pos).getHardness(this.getWorld(), pos) != -1.0f) {
                        this.getWorld().setBlockState(pos, net.minecraft.block.Blocks.AIR.getDefaultState());
                        this.getWorld().addParticle(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.5,
                                pos.getZ() + 0.5, 0, 0, 0);
                    }
                }
            }
        }

        // Mobs
        java.util.List<net.minecraft.entity.Entity> entities = this.getWorld().getEntitiesByClass(
                net.minecraft.entity.Entity.class, this.getBoundingBox().expand(radius),
                e -> e != this && e != pysroReference && !(e instanceof PlayerEntity));

        for (net.minecraft.entity.Entity e : entities) {
            e.discard();
            this.getWorld().addParticle(ParticleTypes.POOF, e.getX(), e.getY() + 1, e.getZ(), 0, 0, 0);
        }

        this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 2.0f);
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());

        // Sand Chain Heal Logic (Every 20 ticks)
        if (this.age % 20 == 0 && this.hasAbsorbedPysro()) {
            pullSandChain();
        }

        // VORTEX SUMMONING (Low Health) - Replaces Vexes
        if (!this.getWorld().isClient && this.getHealth() < this.getMaxHealth() * 0.4 && this.age % 100 == 0
                && this.random.nextFloat() < 0.3f) {
            summonVortex();
        }
    }

    private void summonVortex() {
        this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_FLAP,
                net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 0.5f);

        // Spawn 3 Vortexes around Lica
        for (int i = 0; i < 3; i++) {
            double angle = (i / 3.0) * Math.PI * 2;
            double x = this.getX() + Math.cos(angle) * 5;
            double z = this.getZ() + Math.sin(angle) * 5;

            net.minecraft.entity.AreaEffectCloudEntity cloud = new net.minecraft.entity.AreaEffectCloudEntity(
                    this.getWorld(), x, this.getY(), z);
            cloud.setRadius(3.0f);
            cloud.setDuration(200); // 10 seconds
            cloud.setParticleType(ParticleTypes.PORTAL);
            cloud.setWaitTime(0);

            // Custom Name to identify it as a Vortex for logic (if needed, or just use
            // particle type check)
            cloud.setCustomName(net.minecraft.text.Text.of("Gravity Vortex"));

            this.getWorld().spawnEntity(cloud);
        }
    }

    // VOID SHIELD & PYSRO SHIELD
    @Override
    public boolean damage(net.minecraft.entity.damage.DamageSource source, float amount) {
        if (this.getWorld().isClient)
            return super.damage(source, amount);

        // PYSRO SHIELD (50% Damage Reduction)
        if (this.pysroReference != null && this.pysroReference.isAlive()) {
            amount *= 0.5f;
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.BLOCK_BEACON_AMBIENT,
                    net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 2.0f);
        }

        // VOID SHIELD (Block 20% of attacks)
        if (this.random.nextFloat() < 0.2f && source.getAttacker() instanceof LivingEntity) {
            // BLOCK!
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ITEM_SHIELD_BLOCK,
                    net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 0.8f);
            this.getWorld().addParticle(ParticleTypes.FLASH, this.getX(), this.getY() + 1, this.getZ(), 0, 0, 0);
            if (source.getAttacker() instanceof PlayerEntity player) {
                player.sendMessage(net.minecraft.text.Text.of("§5§l[VOID SHIELD] Attack Blocked!"), true);
            }
            return false;
        }
        return super.damage(source, amount);
    }

    // SAND MECHANICS (With Predictive Aiming & 5 SECOND RULE)
    public void throwSandAt(LivingEntity target) {
        if (target == null)
            return;
        net.minecraft.entity.FallingBlockEntity sand = net.minecraft.entity.FallingBlockEntity.spawnFromBlock(
                this.getWorld(),
                this.getBlockPos().up(2),
                net.minecraft.block.Blocks.SAND.getDefaultState());

        // Predictive Aiming
        Vec3d targetPos = getInterceptPos(target, 1.5); // Sand speed ~1.5
        Vec3d dir = targetPos.subtract(this.getPos()).normalize().multiply(1.5);
        sand.setVelocity(dir);
        this.getWorld().spawnEntity(sand);
    }

    // PREDICTIVE AIMING HELPER
    private Vec3d getInterceptPos(LivingEntity target, double projectileSpeed) {
        if (target == null)
            return this.getPos(); // Null safety
        double dist = this.distanceTo(target);
        double time = dist / projectileSpeed;
        return target.getPos().add(target.getVelocity().multiply(time)); // Predict future position
    }

    // Updated Beam Logic to use Predictive Aiming
    // (Note: This requires updating MassLicaCombatGoal.fireBeam, which is an inner
    // class.
    // I will update the inner class in a separate edit or assume the user wants me
    // to do it now.
    // Since I can't edit non-contiguous blocks easily, I'll update the inner class
    // in the next step.)

    private void pullSandChain() {
        // Find nearby FallingBlock entities (Sand)
        java.util.List<net.minecraft.entity.FallingBlockEntity> sands = this.getWorld().getEntitiesByClass(
                net.minecraft.entity.FallingBlockEntity.class,
                this.getBoundingBox().expand(20.0),
                e -> e.getBlockState().getBlock() == net.minecraft.block.Blocks.SAND);

        for (net.minecraft.entity.FallingBlockEntity sand : sands) {
            // 5 SECOND RULE: Only eat fresh sand (< 100 ticks)
            if (sand.age > 100)
                continue;

            // Pull towards Lica
            Vec3d dir = this.getPos().subtract(sand.getPos()).normalize().multiply(0.5);
            sand.setVelocity(dir);

            // If close, absorb and heal
            if (this.squaredDistanceTo(sand) < 4.0) {
                this.heal(5.0f);
                sand.discard();
                this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.BLOCK_SAND_BREAK,
                        net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
                this.getWorld().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY(), this.getZ(), 0, 0,
                        0);
            }
        }
    }

    @Override
    public boolean isFlappingWings() {
        return true; // Visual flight
    }

    private void triggerEnragedMode() {
        enraged = true;
        com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("Mass Lica entering ENRAGED MODE!");

        if (!this.getWorld().isClient) {
            // Light explosion
            this.getWorld().addParticle(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 0, 0,
                    0);
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(),
                    net.minecraft.sound.SoundCategory.HOSTILE, 5.0f, 0.5f);

            // Massive fang grid (20x20)
            for (int x = -10; x <= 10; x++) {
                for (int z = -10; z <= 10; z++) {
                    spawnFang(this.getX() + x, this.getY(), this.getZ() + z);
                }
            }

            // Announce rage
            this.getWorld().getPlayers().forEach(p -> {
                p.sendMessage(net.minecraft.text.Text.of("§4§l⚠ PYSRO HAS FALLEN! MASS LICA IS ENRAGED! ⚠"), false);
            });

            // Enable flying
            this.setNoGravity(true);
            this.getAttributeInstance(EntityAttributes.GENERIC_FLYING_SPEED).setBaseValue(0.8);

            // Enable flying
            this.setNoGravity(true);
            this.getAttributeInstance(EntityAttributes.GENERIC_FLYING_SPEED).setBaseValue(0.8);

            // Goals are already initialized in initGoals() or setAscended()
            // Just ensure state is set
            this.enraged = true;

            this.getWorld().getPlayers().forEach(p -> {
                p.sendMessage(net.minecraft.text.Text.of("§c§l§kXXX§r §c§lMASS LICA IS ENRAGED!§r §c§l§kXXX"), false);
                p.sendMessage(net.minecraft.text.Text.of("§4§oThe Pysro's death has unleashed her fury..."), true);
            });
        }
    }

    public void setAscended(boolean ascended) {
        this.ascended = ascended;
        if (ascended) {
            this.setCustomName(net.minecraft.text.Text.of("§5§lMass Lica"));

            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(99999.0);
            this.setHealth(99999.0f);
            this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.5);
            this.getAttributeInstance(EntityAttributes.GENERIC_FLYING_SPEED).setBaseValue(0.5);
            this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(20.0);

            this.getWorld().sendEntityStatus(this, (byte) 20);

            // Add evoker fang attack goals
            // Removed individual fang goals to use the centralized Decision System
            // The EnragedFangBarrageGoal now handles all these patterns and more.
            // Force enraged state for logic
            this.enraged = true;
        }
    }

    public boolean isAscended() {
        return ascended;
    }

    public void absorbPysro() {
        this.dataTracker.set(ABSORBED_PYSRO, true);
        this.enraged = true; // Ensure enraged

        // Massive effects
        if (!this.getWorld().isClient) {
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM,
                    net.minecraft.sound.SoundCategory.HOSTILE, 10.0f, 0.5f);
            this.getWorld().getPlayers().forEach(p -> {
                p.sendMessage(
                        net.minecraft.text.Text.of("§4§l§k|||§r §4§lMASS LICA HAS CONSUMED THE PYSRO!§r §4§l§k|||"),
                        false);
                p.sendMessage(net.minecraft.text.Text.of("§c§oUnlimited power flows through her fangs..."), true);
            });
        }
    }

    public boolean hasAbsorbedPysro() {
        return this.dataTracker.get(ABSORBED_PYSRO);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("Ascended", ascended);
        nbt.putBoolean("Enraged", enraged);
        nbt.putBoolean("AbsorbedPysro", this.hasAbsorbedPysro());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setAscended(nbt.getBoolean("Ascended"));
        this.enraged = nbt.getBoolean("Enraged");
        if (nbt.contains("AbsorbedPysro")) {
            this.dataTracker.set(ABSORBED_PYSRO, nbt.getBoolean("AbsorbedPysro"));
        }
    }

    @Override
    public void onDeath(net.minecraft.entity.damage.DamageSource damageSource) {
        com.antigravity.trapplatform.TrapPlatformMod.LOGGER.warn("LicaEntity DIED! Source: " + damageSource.getName());
        super.onDeath(damageSource);
    }

    @Override
    public void remove(RemovalReason reason) {
        com.antigravity.trapplatform.TrapPlatformMod.LOGGER
                .error("LicaEntity REMOVED! Reason: " + reason + " at " + this.getBlockPos());
        super.remove(reason);
    }

    // Helper method to spawn fang
    private void spawnFang(double x, double y, double z) {
        if (!this.getWorld().isClient) {
            // Spawn warning particles to give players time to dodge!
            if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.ENCHANTED_HIT, x, y, z, 5, 0.2, 0.5, 0.2, 0.1);
                serverWorld.spawnParticles(ParticleTypes.SMOKE, x, y, z, 3, 0.2, 0.1, 0.2, 0.0);
            }

            // Spawn fang with delay (60 ticks = 3.0 seconds)
            // Using constructor: EvokerFangsEntity(World world, double x, double y, double
            // z, float yaw, int warmup, LivingEntity owner)
            EvokerFangsEntity fang = new EvokerFangsEntity(this.getWorld(), x, y, z, 0, 60, this);
            this.getWorld().spawnEntity(fang);
        }
    }

    // ===== EVOKER FANG ATTACK GOALS =====

    private static class FangRingGoal extends Goal {
        private final LicaEntity lica;
        private int cooldown = 0;

        public FangRingGoal(LicaEntity lica) {
            this.lica = lica;
        }

        @Override
        public boolean canStart() {
            return lica.ascended && cooldown <= 0 && lica.getTarget() != null && lica.random.nextFloat() < 0.15f;
        }

        @Override
        public void start() {
            LivingEntity target = lica.getTarget();
            if (target == null)
                return;

            double radius = 5.0;
            for (int i = 0; i < 16; i++) {
                double angle = (i / 16.0) * Math.PI * 2;
                double x = target.getX() + Math.cos(angle) * radius;
                double z = target.getZ() + Math.sin(angle) * radius;
                lica.spawnFang(x, target.getY(), z);
            }

            cooldown = 1;
        }

        @Override
        public void tick() {
            if (cooldown > 0)
                cooldown--;
        }
    }

    private static class FangLineGoal extends Goal {
        private final LicaEntity lica;
        private int cooldown = 0;

        public FangLineGoal(LicaEntity lica) {
            this.lica = lica;
        }

        @Override
        public boolean canStart() {
            return lica.ascended && cooldown <= 0 && lica.getTarget() != null && lica.random.nextFloat() < 0.15f;
        }

        @Override
        public void start() {
            LivingEntity target = lica.getTarget();
            if (target == null)
                return;

            Vec3d dir = target.getPos().subtract(lica.getPos()).normalize();
            for (int i = 1; i <= 10; i++) {
                double x = lica.getX() + dir.x * i;
                double z = lica.getZ() + dir.z * i;
                lica.spawnFang(x, lica.getY(), z);
            }

            cooldown = 1;
        }

        @Override
        public void tick() {
            if (cooldown > 0)
                cooldown--;
        }
    }

    private static class FangGridGoal extends Goal {
        private final LicaEntity lica;
        private int cooldown = 0;

        public FangGridGoal(LicaEntity lica) {
            this.lica = lica;
        }

        @Override
        public boolean canStart() {
            return lica.ascended && cooldown <= 0 && lica.getTarget() != null;
        }

        @Override
        public void start() {
            LivingEntity target = lica.getTarget();
            if (target != null) {
                // Spawn 5x5 grid AT the player!
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        lica.spawnFang(target.getX() + x * 2, target.getY(), target.getZ() + z * 2);
                    }
                }
            }
            cooldown = 1;
        }

        @Override
        public void tick() {
            if (cooldown > 0)
                cooldown--;
        }
    }

    private static class FangSpiralGoal extends Goal {
        private final LicaEntity lica;
        private int cooldown = 0;

        public FangSpiralGoal(LicaEntity lica) {
            this.lica = lica;
        }

        @Override
        public boolean canStart() {
            return lica.ascended && cooldown <= 0 && lica.random.nextFloat() < 0.1f;
        }

        @Override
        public void start() {
            for (int i = 0; i < 30; i++) {
                double angle = (i / 30.0) * Math.PI * 4;
                double radius = i * 0.3;
                double x = lica.getX() + Math.cos(angle) * radius;
                double z = lica.getZ() + Math.sin(angle) * radius;
                lica.spawnFang(x, lica.getY(), z);
            }

            cooldown = 1;
        }

        @Override
        public void tick() {
            if (cooldown > 0)
                cooldown--;
        }
    }

    private static class FangCrossGoal extends Goal {
        private final LicaEntity lica;
        private int cooldown = 0;

        public FangCrossGoal(LicaEntity lica) {
            this.lica = lica;
        }

        @Override
        public boolean canStart() {
            return lica.ascended && cooldown <= 0 && lica.getTarget() != null && lica.random.nextFloat() < 0.12f;
        }

        @Override
        public void start() {
            LivingEntity target = lica.getTarget();
            if (target == null)
                return;

            // Horizontal line
            for (int x = -5; x <= 5; x++) {
                lica.spawnFang(target.getX() + x, target.getY(), target.getZ());
            }
            // Vertical line
            for (int z = -5; z <= 5; z++) {
                lica.spawnFang(target.getX(), target.getY(), target.getZ() + z);
            }

            cooldown = 1;
        }

        @Override
        public void tick() {
            if (cooldown > 0)
                cooldown--;
        }
    }

    private static class FangStarGoal extends Goal {
        private final LicaEntity lica;
        private int cooldown = 0;

        public FangStarGoal(LicaEntity lica) {
            this.lica = lica;
        }

        @Override
        public boolean canStart() {
            return lica.ascended && cooldown <= 0 && lica.random.nextFloat() < 0.08f;
        }

        @Override
        public void start() {
            // 5-pointed star
            for (int i = 0; i < 5; i++) {
                double angle = (i / 5.0) * Math.PI * 2 - Math.PI / 2;
                for (int r = 1; r <= 5; r++) {
                    double x = lica.getX() + Math.cos(angle) * r;
                    double z = lica.getZ() + Math.sin(angle) * r;
                    lica.spawnFang(x, lica.getY(), z);
                }
            }

            cooldown = 1;
        }

        @Override
        public void tick() {
            if (cooldown > 0)
                cooldown--;
        }
    }

    private static class FangExplosionGoal extends Goal {
        private final LicaEntity lica;
        private int cooldown = 0;

        public FangExplosionGoal(LicaEntity lica) {
            this.lica = lica;
        }

        @Override
        public boolean canStart() {
            return lica.ascended && cooldown <= 0 && lica.random.nextFloat() < 0.12f;
        }

        @Override
        public void start() {
            for (int i = 0; i < 20; i++) {
                double angle = (i / 20.0) * Math.PI * 2;
                double radius = 3.0;
                double x = lica.getX() + Math.cos(angle) * radius;
                double z = lica.getZ() + Math.sin(angle) * radius;
                lica.spawnFang(x, lica.getY(), z);
            }

            cooldown = 1;
        }

        @Override
        public void tick() {
            if (cooldown > 0)
                cooldown--;
        }
    }

    private static class FangPatchGoal extends Goal {
        private final LicaEntity lica;
        private int cooldown = 0;

        public FangPatchGoal(LicaEntity lica) {
            this.lica = lica;
        }

        @Override
        public boolean canStart() {
            return lica.ascended && cooldown <= 0 && lica.getTarget() != null && lica.random.nextFloat() < 0.15f;
        }

        @Override
        public void start() {
            LivingEntity target = lica.getTarget();
            if (target == null)
                return;

            // Random scattered fangs near target
            for (int i = 0; i < 15; i++) {
                double x = target.getX() + (lica.random.nextDouble() - 0.5) * 10;
                double z = target.getZ() + (lica.random.nextDouble() - 0.5) * 10;
                lica.spawnFang(x, target.getY(), z);
            }

            cooldown = 1;
        }

        @Override
        public void tick() {
            if (cooldown > 0)
                cooldown--;
        }
    }

    // ===== ENRAGED MODE GOALS =====

    // EnragedFangBarrageGoal removed - replaced by MassLicaCombatGoal

    // ===== UNIFIED COMBAT AI =====

    // ===== UNIFIED COMBAT AI (SMARTER LICA) =====

    private static class MassLicaCombatGoal extends Goal {
        private final LicaEntity lica;
        private int stateTimer = 0;
        private CombatState currentState = CombatState.CHASING;
        private int attackCooldown = 0;
        private java.util.Queue<Runnable> comboQueue = new java.util.LinkedList<>();

        private enum CombatState {
            CHASING,
            ATTACKING,
            RECOVERING
        }

        public MassLicaCombatGoal(LicaEntity lica) {
            this.lica = lica;
            this.setControls(java.util.EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return lica.enraged && lica.getTarget() != null;
        }

        @Override
        public void tick() {
            LivingEntity target = lica.getTarget();
            if (target == null) {
                if (lica.age % 40 == 0)
                    com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("Lica AI: No Target!");
                return;
            }

            // Always look at target
            lica.getLookControl().lookAt(target, 30.0f, 30.0f);

            // Dynamic Aggression: Lower health = Faster attacks
            int baseCooldown = 20; // 1 second
            float healthPct = lica.getHealth() / lica.getMaxHealth();
            if (healthPct < 0.5f)
                baseCooldown = 10; // 0.5s
            if (healthPct < 0.2f)
                baseCooldown = 5; // 0.25s

            if (attackCooldown > 0) {
                attackCooldown--;
                // CONSTANT MOTION: Even during cooldowns, keep moving!
                performIdleMovement(target);
                return;
            }

            switch (currentState) {
                case CHASING:
                    // If far away, teleport close
                    if (lica.squaredDistanceTo(target) > 100) { // > 10 blocks
                        lica.teleportToFlank(target); // Flank them!
                        currentState = CombatState.ATTACKING;
                    } else {
                        currentState = CombatState.ATTACKING;
                    }
                    break;

                case ATTACKING:
                    if (!comboQueue.isEmpty()) {
                        // Execute next step in combo
                        comboQueue.poll().run();
                        attackCooldown = 10; // Short delay between combo steps
                    } else {
                        // Start new combo
                        startNewCombo(target);
                        currentState = CombatState.RECOVERING;
                        stateTimer = baseCooldown;
                    }
                    break;

                case RECOVERING:
                    // CONSTANT MOTION: Strafe while recovering
                    performIdleMovement(target);
                    if (stateTimer-- <= 0) {
                        currentState = CombatState.CHASING;
                    }
                    break;
            }
        }

        private void startNewCombo(LivingEntity target) {
            float chance = lica.random.nextFloat();
            boolean superMode = lica.hasAbsorbedPysro();

            // RULE OF 3: Everything happens in triplets!

            if (superMode && chance < 0.4f) {
                // COMBO: ORBITAL BOMBARDMENT (Super Only)
                // 1. Teleport Up -> 3x (Super Beam + Dodge) -> 3x Ring of Fangs
                comboQueue.add(() -> lica.teleportToAmbush(target));
                for (int i = 0; i < 3; i++)
                    comboQueue.add(() -> {
                        fireBeam(target);
                        performDodge(target);
                    });
                for (int i = 0; i < 3; i++)
                    comboQueue.add(() -> spawnFangPattern(target, 0)); // Ring
            } else if (chance < 0.7f) {
                // COMBO: JUMPSCARE
                // 1. Teleport Behind -> 3x Dash Attack
                comboQueue.add(() -> lica.teleportToFlank(target));
                for (int i = 0; i < 3; i++)
                    comboQueue.add(() -> performDash(target));
            } else if (chance < 0.85f) {
                // COMBO: VOID INHALE (New)
                // 1. Teleport Front -> 3x Inhale
                comboQueue.add(() -> lica.teleportToPos(target.getX(), target.getY(), target.getZ() + 5, target));
                for (int i = 0; i < 3; i++)
                    comboQueue.add(() -> performInhale(target));
            } else {
                // COMBO: CLASSIC BARRAGE
                // 1. Teleport Random -> 3x (Beam + Dodge) -> 3x Fangs
                comboQueue.add(() -> lica.teleportToVantagePoint(target, 8.0));
                for (int i = 0; i < 3; i++)
                    comboQueue.add(() -> {
                        fireBeam(target);
                        performDodge(target);
                    });
                for (int i = 0; i < 3; i++)
                    comboQueue.add(() -> spawnFangPattern(target, 2)); // Random Cluster

                // Also throw sand in 3s
                for (int i = 0; i < 3; i++)
                    comboQueue.add(() -> lica.throwSandAt(target));
            }
        }

        private void performInhale(LivingEntity target) {
            // VISUALS: Equip Trident
            lica.equipMainHand(net.minecraft.item.Items.TRIDENT);

            // Suction Logic
            Vec3d pullOrigin = lica.getEyePos();
            java.util.List<LivingEntity> entities = lica.getWorld().getEntitiesByClass(LivingEntity.class,
                    lica.getBoundingBox().expand(15.0), e -> e != lica && e != lica.pysroReference);

            for (LivingEntity entity : entities) {
                Vec3d dir = pullOrigin.subtract(entity.getEyePos()).normalize().multiply(0.5);
                entity.addVelocity(dir.x, dir.y, dir.z);
                entity.velocityModified = true;

                if (entity.squaredDistanceTo(lica) < 9.0) { // 3 blocks
                    entity.damage(lica.getDamageSources().magic(), 6.0f); // 3 hearts
                }
            }

            // Visuals
            lica.getWorld().playSound(null, lica.getBlockPos(), SoundEvents.ITEM_TRIDENT_RIPTIDE_3.value(),
                    net.minecraft.sound.SoundCategory.HOSTILE, 2.0f, 0.5f);
        }

        private void fireBeam(LivingEntity target) {
            // VISUALS: Equip Book
            lica.equipMainHand(net.minecraft.item.Items.ENCHANTED_BOOK);

            try {
                // Visuals
                lica.getWorld().playSound(null, lica.getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM,
                        net.minecraft.sound.SoundCategory.HOSTILE, 5.0f, 0.5f);
                if (lica.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                    // ETERNAL SHADOWS (Particles)
                    for (int i = 0; i < 20; i++) {
                        double x = lica.getX() + (lica.random.nextDouble() - 0.5) * 2.0;
                        double y = lica.getY() + lica.random.nextDouble() * 2.0;
                        double z = lica.getZ() + (lica.random.nextDouble() - 0.5) * 2.0;
                        serverWorld.spawnParticles(ParticleTypes.SQUID_INK, x, y, z, 1, 0, 0, 0, 0.1);
                        serverWorld.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 1, 0, 0, 0, 0.05);
                    }

                    // Beam particles
                    Vec3d start = lica.getEyePos();

                    // PREDICTIVE AIMING
                    Vec3d targetPos = lica.getInterceptPos(target, 2.0); // Beam "speed" approx 2.0 for calculation
                    Vec3d dir = targetPos.subtract(start).normalize();

                    boolean superBeam = lica.hasAbsorbedPysro();

                    for (double d = 0; d < 30; d += 0.5) {
                        Vec3d pos = start.add(dir.multiply(d));
                        serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);

                        // Drowned Transformation Logic (ALWAYS ACTIVE)
                        java.util.List<LivingEntity> hits = serverWorld.getEntitiesByClass(LivingEntity.class,
                                new net.minecraft.util.math.Box(pos.subtract(1.5, 1.5, 1.5), pos.add(1.5, 1.5, 1.5)),
                                e -> e != lica && e != lica.pysroReference
                                        && !(e instanceof net.minecraft.entity.mob.DrownedEntity));

                        for (LivingEntity hit : hits) {
                            // PLAYER SAFETY: Do not transform players!
                            if (hit instanceof PlayerEntity)
                                continue;

                            net.minecraft.entity.mob.DrownedEntity drowned = net.minecraft.entity.EntityType.DROWNED
                                    .create(serverWorld);
                            if (drowned != null) {
                                drowned.refreshPositionAndAngles(hit.getX(), hit.getY(), hit.getZ(), hit.getYaw(),
                                        hit.getPitch());
                                serverWorld.spawnEntity(drowned);
                                hit.discard();
                                serverWorld.spawnParticles(ParticleTypes.SPLASH, hit.getX(), hit.getY(), hit.getZ(), 10,
                                        0.5, 0.5, 0.5, 0.1);
                            }
                        }
                    }

                    // Damage
                    float damage = superBeam ? 100000.0f : 50.0f;

                    // NERF: Players only take 5 damage (2.5 hearts)
                    if (target instanceof PlayerEntity) {
                        damage = 5.0f;
                    }

                    target.damage(lica.getDamageSources().magic(), damage);
                }
            } catch (Exception e) {
                com.antigravity.trapplatform.TrapPlatformMod.LOGGER.error("Error in fireBeam: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void spawnFangPattern(LivingEntity target, int forcedPattern) {
            boolean superMode = lica.hasAbsorbedPysro();
            int pattern = (forcedPattern != -1) ? forcedPattern : lica.random.nextInt(superMode ? 5 : 3);

            if (pattern == 0) {
                // Ring
                for (int i = 0; i < 12; i++) {
                    double angle = (i / 12.0) * Math.PI * 2;
                    lica.spawnFang(target.getX() + Math.cos(angle) * 3, target.getY(),
                            target.getZ() + Math.sin(angle) * 3);
                }
            } else if (pattern == 1) {
                // Cross
                for (int i = -4; i <= 4; i++) {
                    lica.spawnFang(target.getX() + i, target.getY(), target.getZ());
                    lica.spawnFang(target.getX(), target.getY(), target.getZ() + i);
                }
            } else if (pattern == 2) {
                // Random Cluster
                for (int i = 0; i < 10; i++) {
                    lica.spawnFang(target.getX() + (lica.random.nextDouble() - 0.5) * 8, target.getY(),
                            target.getZ() + (lica.random.nextDouble() - 0.5) * 8);
                }
            } else if (pattern == 3) {
                // [SUPER] Spiral
                for (int i = 0; i < 30; i++) {
                    double angle = (i / 10.0) * Math.PI * 2;
                    double dist = i * 0.5;
                    lica.spawnFang(target.getX() + Math.cos(angle) * dist, target.getY(),
                            target.getZ() + Math.sin(angle) * dist);
                }
            } else if (pattern == 4) {
                // [SUPER] Chaos Grid
                for (int x = -8; x <= 8; x += 2) {
                    for (int z = -8; z <= 8; z += 2) {
                        if (lica.random.nextBoolean()) {
                            lica.spawnFang(target.getX() + x, target.getY(), target.getZ() + z);
                        }
                    }
                }
            }
        }

        private void performIdleMovement(LivingEntity target) {
            // Circle/Strafe Logic
            // Maintain distance of ~10-15 blocks and height of ~5-8 blocks above target

            double time = lica.age * 0.05; // Slow rotation
            double radius = 12.0;

            double targetX = target.getX() + Math.cos(time) * radius;
            double targetZ = target.getZ() + Math.sin(time) * radius;
            double targetY = target.getY() + 6.0 + Math.sin(time * 0.5) * 2.0; // Bobbing up and down

            lica.getMoveControl().moveTo(targetX, targetY, targetZ, 1.0);
            lica.getLookControl().lookAt(target, 30.0f, 30.0f);
        }

        private void performDash(LivingEntity target) {
            // VISUALS: Equip Sword
            lica.equipMainHand(net.minecraft.item.Items.NETHERITE_SWORD);

            Vec3d dir = target.getPos().subtract(lica.getPos()).normalize().multiply(2.0);
            lica.setVelocity(dir);
            lica.velocityModified = true;
            lica.getWorld().playSound(null, lica.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                    net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 0.5f);
        }

        private void performDodge(LivingEntity target) {
            if (target == null)
                return;
            Vec3d dir = target.getPos().subtract(lica.getPos()).normalize();
            Vec3d right = dir.crossProduct(new Vec3d(0, 1, 0)).normalize();

            // Randomly left or right
            if (lica.random.nextBoolean())
                right = right.multiply(-1);

            Vec3d dodgePos = lica.getPos().add(right.multiply(5.0));

            // Try to teleport, if fail, just dash
            if (!lica.teleportToPos(dodgePos.x, lica.getY(), dodgePos.z, target)) {
                lica.setVelocity(right.multiply(1.5));
                lica.velocityModified = true;
            }
        }

    }

    // STRUCTURE SYNERGY

    public void teleportToStructure() {
        if (this.getWorld().isClient)
            return;

        // Find nearby End Crystals
        java.util.List<net.minecraft.entity.decoration.EndCrystalEntity> crystals = this.getWorld().getEntitiesByClass(
                net.minecraft.entity.decoration.EndCrystalEntity.class,
                this.getBoundingBox().expand(50.0),
                e -> true);

        if (!crystals.isEmpty()) {
            net.minecraft.entity.decoration.EndCrystalEntity crystal = crystals
                    .get(this.random.nextInt(crystals.size()));
            this.teleportToPos(crystal.getX(), crystal.getY() + 2, crystal.getZ(), this.getTarget());
            com.antigravity.trapplatform.TrapPlatformMod.LOGGER.info("Lica teleported to End Crystal!");
            return;
        }

        // If no crystals, try to find the Altar (0, 100, 0 is a good guess based on
        // previous context)
        // For now, just teleport high up if no crystals
        if (this.random.nextBoolean()) {
            this.teleportToAmbush(this.getTarget());
        }
    }

    public void triggerTraps() {
        // Trigger nearby TNT or Traps
        // This is a placeholder for "using the altar traps"
        // We can simulate this by spawning a few primed TNTs around the player
        LivingEntity target = this.getTarget();
        if (target != null) {
            for (int i = 0; i < 3; i++) {
                double x = target.getX() + (this.random.nextDouble() - 0.5) * 10;
                double z = target.getZ() + (this.random.nextDouble() - 0.5) * 10;
                net.minecraft.entity.TntEntity tnt = new net.minecraft.entity.TntEntity(this.getWorld(), x,
                        target.getY() + 5, z, this);
                tnt.setFuse(40); // 2 seconds
                this.getWorld().spawnEntity(tnt);
            }
            this.getWorld().playSound(null, target.getBlockPos(), SoundEvents.ENTITY_TNT_PRIMED,
                    net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
        }
    }

    // TACTICAL TELEPORT: FLANK (Behind Target)
    public void teleportToFlank(LivingEntity target) {
        if (target == null)
            return;
        Vec3d look = target.getRotationVec(1.0f);
        Vec3d behind = target.getPos().subtract(look.multiply(3.0)); // 3 blocks behind
        teleportToPos(behind.x, target.getY(), behind.z, target);
    }

    // TACTICAL TELEPORT: AMBUSH (Above Target)
    public void teleportToAmbush(LivingEntity target) {
        if (target == null)
            return;
        teleportToPos(target.getX(), target.getY() + 8, target.getZ(), target); // 8 blocks up
    }

    // Helper method for teleporting to a vantage point (Random)
    public void teleportToVantagePoint(LivingEntity target, double preferredDistance) {
        if (target == null)
            return;

        for (int i = 0; i < 10; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            double x = target.getX() + Math.cos(angle) * preferredDistance;
            double z = target.getZ() + Math.sin(angle) * preferredDistance;
            double y = target.getY() + 2 + this.random.nextInt(3);

            if (teleportToPos(x, y, z, target))
                return;
        }
        // Fallback
        teleportToPos(target.getX(), target.getY() + 5, target.getZ(), target);
    }

    // Core Teleport Logic with Safety Checks
    private boolean teleportToPos(double x, double y, double z, LivingEntity target) {
        // Clamp Y
        if (y < -60)
            y = -60;
        if (y > 315)
            y = 315;

        // Check Air
        BlockPos pos = new BlockPos((int) x, (int) y, (int) z);

        // Check Chunk Loaded (Prevent teleporting into void/unloaded chunks)
        if (!this.getWorld().isChunkLoaded(pos)) {
            return false;
        }

        if (this.getWorld().getBlockState(pos).isAir() && this.getWorld().getBlockState(pos.up()).isAir()) {
            Vec3d start = this.getPos();
            Vec3d end = new Vec3d(x, y, z);

            this.refreshPositionAndAngles(x, y, z, this.getYaw(), this.getPitch());
            this.setVelocity(0, 0, 0);

            // Sound at NEW location (Loud)
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                    net.minecraft.sound.SoundCategory.HOSTILE, 2.0f, 0.5f);
            this.lookAt(net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor.EYES, target.getPos());

            if (!this.getWorld().isClient) {
                this.getWorld().getPlayers()
                        .forEach(p -> p.sendMessage(net.minecraft.text.Text.of("§5[Lica] Teleported!"), true));

                // Particle Trail (Server-side spawn)
                if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                    Vec3d dir = end.subtract(start);
                    double dist = dir.length();
                    Vec3d norm = dir.normalize();

                    for (double d = 0; d < dist; d += 0.5) {
                        Vec3d p = start.add(norm.multiply(d));
                        serverWorld.spawnParticles(ParticleTypes.DRAGON_BREATH, p.x, p.y + 1.0, p.z, 1, 0, 0, 0, 0);
                    }
                }
            }
            return true;
        }
        return false;
    }
}
