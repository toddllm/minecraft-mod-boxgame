package com.antigravity.trapplatform;

import com.antigravity.trapplatform.traps.Trap;
import com.antigravity.trapplatform.traps.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TrapManager {
    private static final List<Trap> TRAPS = new ArrayList<>();
    private static final Random RANDOM = new Random();
    private static int timer = 0;
    private static final int TRAP_INTERVAL = 300; // 15 seconds

    public static void registerTrap(Trap trap) {
        TRAPS.add(trap);
    }

    public static void init() {
        // Original Traps (Enhanced)
        TRAPS.add(new ArrowTrap());
        TRAPS.add(new FangTrap());
        TRAPS.add(new LightningTrap());
        TRAPS.add(new MobTrap());
        TRAPS.add(new FireballTrap());
        TRAPS.add(new WindChargeTrap());
        TRAPS.add(new AnvilTrap());

        // New Traps
        TRAPS.add(new TntRainTrap());
        TRAPS.add(new CreeperSwarmTrap());
        TRAPS.add(new BlazeInfernoTrap());
        TRAPS.add(new GhastScreamTrap());
        TRAPS.add(new PhantomSwoopTrap());
        TRAPS.add(new SlimeRainTrap());
        TRAPS.add(new MagmaCubeRainTrap());
        TRAPS.add(new VexSwarmTrap());
        TRAPS.add(new WardenTrap());
        TRAPS.add(new PotionRainTrap());
        TRAPS.add(new EvokerTrap());
        TRAPS.add(new WebTrap());

        // 19 New Traps (Expansion)
        TRAPS.add(new ZombieHordeTrap());
        TRAPS.add(new SkeletonSniperTrap());
        TRAPS.add(new WitherSkeletonTrap());
        TRAPS.add(new StrayTrap());
        TRAPS.add(new HuskTrap());
        TRAPS.add(new DrownedTrap());
        TRAPS.add(new PiglinBruteTrap());
        TRAPS.add(new HoglinTrap());
        TRAPS.add(new ZoglinTrap());
        TRAPS.add(new RavagerTrap());
        TRAPS.add(new VindicatorTrap());
        TRAPS.add(new PillagerTrap());
        TRAPS.add(new WitchCovenTrap());
        TRAPS.add(new CaveSpiderSwarmTrap());
        TRAPS.add(new SilverfishInfestationTrap());
        TRAPS.add(new EndermiteTrap());
        TRAPS.add(new ShulkerTrap());
        TRAPS.add(new IllusionerTrap());
        TRAPS.add(new GiantTrap());

        ServerTickEvents.END_SERVER_TICK.register(TrapManager::tick);
    }

    private static java.util.UUID boxUuid = null;
    private static int attackTimer = 0;

    private static void tick(MinecraftServer server) {
        timer++;
        if (timer >= TRAP_INTERVAL) {
            timer = 0;
            triggerRandomTrap(server);
        }

        // Trap Box Logic
        ServerWorld arenaWorld = server.getWorld(com.antigravity.trapplatform.items.ArenaTeleporterItem.ARENA_KEY);
        if (arenaWorld != null) {
            tickBox(arenaWorld);
        }
    }

    private static void tickBox(ServerWorld world) {
        // 1. Ensure Box Exists
        if (boxUuid == null || !(world.getEntity(boxUuid) instanceof net.minecraft.entity.FallingBlockEntity)) {
            // Spawn new box (FallingBlockEntity)
            net.minecraft.entity.FallingBlockEntity box = net.minecraft.entity.FallingBlockEntity.spawnFromBlock(world,
                    new net.minecraft.util.math.BlockPos(0, 105, 0),
                    net.minecraft.block.Blocks.CRYING_OBSIDIAN.getDefaultState());
            box.setPosition(0.5, 105.5, 0.5);
            box.setNoGravity(true);
            box.setGlowing(true);
            box.setInvulnerable(true);
            // Make it persistent so it doesn't despawn or turn into a block
            box.timeFalling = -2147483648; // Keep it alive
            box.dropItem = false;

            world.spawnEntity(box);
            boxUuid = box.getUuid();
        }

        net.minecraft.entity.Entity entity = world.getEntity(boxUuid);
        if (entity instanceof net.minecraft.entity.FallingBlockEntity box) {
            // 2. Rotate (FallingBlockEntity doesn't rotate visually in vanilla without
            // resource pack/mod, but we can spin the entity itself)
            box.setYaw(box.getYaw() + 5f);
            // Ensure it stays alive
            box.timeFalling = -2147483648;

            // 3. Attack
            attackTimer++;
            if (attackTimer >= 120) { // Every 6 seconds
                attackTimer = 0;
                attackRandomPlayer(world, box);
            }
        }
    }

    private static void attackRandomPlayer(ServerWorld world, net.minecraft.entity.Entity source) {
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty())
            return;

        ServerPlayerEntity target = players.get(RANDOM.nextInt(players.size()));

        // Ring of Fangs around player
        spawnFangRing(world, target.getPos(), 3.0, 0);
    }

    private static void spawnFangRing(ServerWorld world, net.minecraft.util.math.Vec3d center, double radius,
            int warmupDelay) {
        int fangs = 8;
        for (int i = 0; i < fangs; i++) {
            double angle = 2 * Math.PI * i / fangs;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            double y = center.getY();

            // Adjust Y to ground
            net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos((int) x, (int) y, (int) z);
            while (world.getBlockState(pos).isAir() && pos.getY() > 0) {
                pos = pos.down();
            }
            y = pos.getY() + 1;

            world.spawnEntity(new net.minecraft.entity.mob.EvokerFangsEntity(world, x, y, z,
                    (float) Math.toDegrees(angle), warmupDelay, null));
        }
    }

    private static void spawnShrinkingCircle(ServerWorld world) {
        net.minecraft.util.math.Vec3d center = new net.minecraft.util.math.Vec3d(0.5, 100, 0.5); // Arena center

        // Radii from 15 down to 5
        for (int r = 15; r >= 5; r -= 2) {
            int warmup = (15 - r) * 5; // 0, 10, 20... ticks delay
            spawnFangRing(world, center, r, warmup);
        }
    }

    private static void triggerRandomTrap(MinecraftServer server) {
        if (TRAPS.isEmpty())
            return;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            // Check if player is in the arena dimension
            if (player.getWorld().getRegistryKey().getValue().equals(Identifier.of("trapplatform", "arena"))) {
                ServerWorld world = player.getServerWorld();

                // Cleanup previous wave
                cleanup(world, player);

                // Shrinking Circle of Fangs
                spawnShrinkingCircle(world);

                // Trigger new trap
                Trap trap = TRAPS.get(RANDOM.nextInt(TRAPS.size()));
                trap.trigger(world, player);

                // Announce
                player.networkHandler.sendPacket(
                        new net.minecraft.network.packet.s2c.play.TitleS2CPacket(Text.of("§c" + trap.getName())));
                player.sendMessage(Text.of("§7Wave Started: " + trap.getName()), true);

                // Progression
                waveCount++;
                checkProgression(player.getServer().getOverworld(), player);
            }
        }
    }

    private static int waveCount = 0;

    private static void checkProgression(ServerWorld overworld, ServerPlayerEntity player) {
        if (waveCount == 5) {
            // E
            int[][] shape = {
                    { 1, 1, 1, 1, 1 },
                    { 1, 0, 0, 0, 0 },
                    { 1, 1, 1, 1, 1 },
                    { 1, 0, 0, 0, 0 },
                    { 1, 1, 1, 1, 1 }
            };
            String[] text = { "§4The Shadow", "§cApproaches...", "§0E...", "§8(The True Boss)" };
            com.antigravity.trapplatform.TrapPlatformMod.generateStructure(overworld, 40, shape, text);
            player.sendMessage(Text.of("§4§lA tremor is felt in the Overworld... The name is revealing itself..."),
                    false);
        } else if (waveCount == 10) {
            // A
            int[][] shape = {
                    { 0, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 1 },
                    { 1, 1, 1, 1, 1 },
                    { 1, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 1 }
            };
            String[] text = { "§4The Shadow", "§cIs Near...", "§0A...", "§8(The True Boss)" };
            com.antigravity.trapplatform.TrapPlatformMod.generateStructure(overworld, 60, shape, text);
            player.sendMessage(Text.of("§4§lThe ground shakes violently... Another letter burns..."), false);
        } else if (waveCount == 15) {
            // R
            int[][] shape = {
                    { 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 1 },
                    { 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 1 }
            };
            String[] text = { "§4The Shadow", "§cIs Here...", "§0R...", "§8(FEAR)" };
            com.antigravity.trapplatform.TrapPlatformMod.generateStructure(overworld, 80, shape, text);
            net.minecraft.util.math.BlockPos rPos = overworld.getSpawnPos().add(80, 0, 20);
            player.sendMessage(Text
                    .of("§4§lTHE NAME IS COMPLETE. FEAR HIM. (Look at X=" + rPos.getX() + ", Z=" + rPos.getZ() + ")"),
                    false);

            // Spawn Boss
            spawnFearBoss(overworld, 100, 20);

            // Give Lore Book
            giveLoreBook(player);
        }
    }

    private static void spawnFearBoss(ServerWorld world, int x, int z) {
        net.minecraft.util.math.BlockPos pos = world.getTopPosition(net.minecraft.world.Heightmap.Type.WORLD_SURFACE,
                world.getSpawnPos().add(x, 0, z));

        // The Beast (Ravager)
        net.minecraft.entity.mob.RavagerEntity beast = net.minecraft.entity.EntityType.RAVAGER.create(world);
        beast.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        beast.setCustomName(Text.of("§4§lThe Beast of Fear"));
        beast.setCustomNameVisible(true);
        beast.setPersistent();

        // The Master (Evoker)
        net.minecraft.entity.mob.EvokerEntity master = net.minecraft.entity.EntityType.EVOKER.create(world);
        master.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        master.setCustomName(Text.of("§4§lFEAR"));
        master.setCustomNameVisible(true);
        master.setPersistent();

        // Buffs
        master.setAbsorptionAmount(50.0f); // Extra health for the master

        // Spawn and Mount
        world.spawnEntity(beast);
        world.spawnEntity(master);
        master.startRiding(beast);

        fearBoss = master;
    }

    private static net.minecraft.entity.mob.EvokerEntity fearBoss;
    private static int fearTimer = 0;

    private static void tickFearBoss(ServerWorld world) {
        if (fearBoss == null || !fearBoss.isAlive())
            return;

        fearTimer++;

        // 1. Ominous Magic (Night + Storm)
        if (world.getTimeOfDay() % 24000 != 18000)
            world.setTimeOfDay(18000); // Midnight
        if (!world.isThundering())
            world.setWeather(0, 6000, true, true);

        // 2. Lightning (Visual/Danger)
        if (RANDOM.nextFloat() < 0.05f) {
            net.minecraft.entity.LightningEntity bolt = net.minecraft.entity.EntityType.LIGHTNING_BOLT.create(world);
            bolt.setPosition(fearBoss.getX() + (RANDOM.nextDouble() - 0.5) * 20, fearBoss.getY(),
                    fearBoss.getZ() + (RANDOM.nextDouble() - 0.5) * 20);
            world.spawnEntity(bolt);
        }

        // 3. Fireballs (Every 2s)
        if (fearTimer % 40 == 0 && fearBoss.getTarget() != null) {
            net.minecraft.entity.LivingEntity target = fearBoss.getTarget();
            double dX = target.getX() - fearBoss.getX();
            double dY = target.getBodyY(0.5) - fearBoss.getBodyY(0.5);
            double dZ = target.getZ() - fearBoss.getZ();

            net.minecraft.util.math.Vec3d velocity = new net.minecraft.util.math.Vec3d(dX, dY, dZ).normalize()
                    .multiply(0.1);
            // Use FireballEntity (Large Fireball)
            net.minecraft.entity.projectile.FireballEntity fireball = new net.minecraft.entity.projectile.FireballEntity(
                    world, fearBoss, velocity, 1);
            fireball.setPosition(fearBoss.getX(), fearBoss.getBodyY(0.5) + 0.5, fearBoss.getZ());
            world.spawnEntity(fireball);
        }

        // 4. Lava (Every 5s)
        if (fearTimer % 100 == 0) {
            net.minecraft.util.math.BlockPos pos = fearBoss.getBlockPos().add(RANDOM.nextInt(10) - 5, 0,
                    RANDOM.nextInt(10) - 5);
            pos = world.getTopPosition(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, pos);
            if (world.getBlockState(pos).isAir()) {
                world.setBlockState(pos, net.minecraft.block.Blocks.LAVA.getDefaultState());
            }
        }

        // 5. Ride Passive Mobs (if dismounted)
        if (!fearBoss.hasVehicle() && fearTimer % 20 == 0) {
            java.util.List<net.minecraft.entity.passive.PassiveEntity> animals = world.getEntitiesByClass(
                    net.minecraft.entity.passive.PassiveEntity.class, fearBoss.getBoundingBox().expand(10), e -> true);
            if (!animals.isEmpty()) {
                net.minecraft.entity.passive.PassiveEntity mount = animals.get(RANDOM.nextInt(animals.size()));
                fearBoss.startRiding(mount);
            }
        }
    }

    private static void giveLoreBook(ServerPlayerEntity player) {
        net.minecraft.item.ItemStack book = new net.minecraft.item.ItemStack(net.minecraft.item.Items.WRITTEN_BOOK);

        // NBT for book
        net.minecraft.component.type.WrittenBookContentComponent content = new net.minecraft.component.type.WrittenBookContentComponent(
                net.minecraft.text.RawFilteredPair.of("The Legend of Fear"),
                "The Shadow",
                0,
                java.util.List.of(
                        net.minecraft.text.RawFilteredPair.of(net.minecraft.text.Text.of(
                                "§0Fear is the First Vindicator, banished for his cruelty.\n\nHe has returned to claim this world.\n\nHe waits for you at X="
                                        + (player.getWorld().getSpawnPos().getX() + 100) + ", Z="
                                        + (player.getWorld().getSpawnPos().getZ() + 20) + ".\n\nPrepare yourself.")),
                        net.minecraft.text.RawFilteredPair.of(net.minecraft.text.Text.of("§4He is watching."))),
                true);
        book.set(net.minecraft.component.DataComponentTypes.WRITTEN_BOOK_CONTENT, content);

        if (!player.getInventory().insertStack(book)) {
            player.dropItem(book, false);
        }
    }

    private static void cleanup(ServerWorld world, ServerPlayerEntity player) {
        // Kill all non-player entities in a large radius
        net.minecraft.util.math.Box box = new net.minecraft.util.math.Box(player.getPos().add(-100, -100, -100),
                player.getPos().add(100, 100, 100));
        List<net.minecraft.entity.Entity> entities = world.getOtherEntities(player, box);

        for (net.minecraft.entity.Entity entity : entities) {
            if (!(entity instanceof ServerPlayerEntity) && !entity.getUuid().equals(boxUuid)) {
                entity.discard();
            }
        }

        // Clear blocks above platform (y=101 to 110)
        net.minecraft.util.math.BlockPos center = new net.minecraft.util.math.BlockPos(0, 100, 0);
        int radius = 10;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = 1; y <= 10; y++) {
                    net.minecraft.util.math.BlockPos pos = center.add(x, y, z);
                    if (!world.getBlockState(pos).isAir()) {
                        world.setBlockState(pos, net.minecraft.block.Blocks.AIR.getDefaultState());
                    }
                }
            }
        }
    }
}
