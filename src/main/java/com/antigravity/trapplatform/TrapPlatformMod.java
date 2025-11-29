package com.antigravity.trapplatform;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.antigravity.trapplatform.TrapManager; // Added import for TrapManager

public class TrapPlatformMod implements ModInitializer {
    public static final String MOD_ID = "trapplatform";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final net.minecraft.entity.EntityType<com.antigravity.trapplatform.entity.FearEntity> FEAR_ENTITY = net.minecraft.registry.Registry
            .register(
                    net.minecraft.registry.Registries.ENTITY_TYPE,
                    net.minecraft.util.Identifier.of(MOD_ID, "fear_entity"),
                    net.minecraft.entity.EntityType.Builder
                            .create(com.antigravity.trapplatform.entity.FearEntity::new,
                                    net.minecraft.entity.SpawnGroup.MONSTER)
                            .dimensions(0.6f, 1.95f).build("fear_entity"));

    public static final net.minecraft.entity.EntityType<com.antigravity.trapplatform.entity.VoidQueenEntity> VOID_QUEEN_ENTITY = net.minecraft.registry.Registry
            .register(
                    net.minecraft.registry.Registries.ENTITY_TYPE,
                    net.minecraft.util.Identifier.of(MOD_ID, "void_queen_entity"),
                    net.minecraft.entity.EntityType.Builder
                            .create(com.antigravity.trapplatform.entity.VoidQueenEntity::new,
                                    net.minecraft.entity.SpawnGroup.MONSTER)
                            .dimensions(1.5f, 5.0f).build("void_queen_entity"));

    public static final net.minecraft.entity.EntityType<com.antigravity.trapplatform.entity.MeteorEntity> METEOR_ENTITY = net.minecraft.registry.Registry
            .register(
                    net.minecraft.registry.Registries.ENTITY_TYPE,
                    net.minecraft.util.Identifier.of(MOD_ID, "meteor_entity"),
                    net.minecraft.entity.EntityType.Builder.<com.antigravity.trapplatform.entity.MeteorEntity>create(
                            com.antigravity.trapplatform.entity.MeteorEntity::new,
                            net.minecraft.entity.SpawnGroup.MISC)
                            .dimensions(1.0f, 1.0f).build("meteor_entity"));

    public static final net.minecraft.entity.EntityType<com.antigravity.trapplatform.entity.WindChargeTurretEntity> WIND_CHARGE_TURRET_ENTITY = net.minecraft.registry.Registry
            .register(
                    net.minecraft.registry.Registries.ENTITY_TYPE,
                    net.minecraft.util.Identifier.of(MOD_ID, "wind_charge_turret"),
                    net.minecraft.entity.EntityType.Builder.<com.antigravity.trapplatform.entity.WindChargeTurretEntity>create(
                            com.antigravity.trapplatform.entity.WindChargeTurretEntity::new,
                            net.minecraft.entity.SpawnGroup.MISC)
                            .dimensions(1.0f, 1.0f).build("wind_charge_turret"));

    public static final net.minecraft.entity.EntityType<com.antigravity.trapplatform.entity.BowlEntity> BOWL_ENTITY = net.minecraft.registry.Registry
            .register(
                    net.minecraft.registry.Registries.ENTITY_TYPE,
                    net.minecraft.util.Identifier.of(MOD_ID, "bowl_entity"),
                    net.minecraft.entity.EntityType.Builder.<com.antigravity.trapplatform.entity.BowlEntity>create(
                            com.antigravity.trapplatform.entity.BowlEntity::new,
                            net.minecraft.entity.SpawnGroup.MISC)
                            .dimensions(0.5f, 0.5f).build("bowl_entity"));

    public static final net.minecraft.entity.EntityType<com.antigravity.trapplatform.entity.LicaEntity> LICA_ENTITY = net.minecraft.registry.Registry
            .register(
                    net.minecraft.registry.Registries.ENTITY_TYPE,
                    net.minecraft.util.Identifier.of(MOD_ID, "lica_entity"),
                    net.minecraft.entity.EntityType.Builder.<com.antigravity.trapplatform.entity.LicaEntity>create(
                            com.antigravity.trapplatform.entity.LicaEntity::new,
                            net.minecraft.entity.SpawnGroup.MONSTER)
                            .dimensions(0.6f, 1.8f).build("lica_entity"));


    public static final net.minecraft.entity.EntityType<com.antigravity.trapplatform.entity.VoidQueenPhase2Entity> VOID_QUEEN_PHASE_2_ENTITY = net.minecraft.registry.Registry
            .register(
                    net.minecraft.registry.Registries.ENTITY_TYPE,
                    net.minecraft.util.Identifier.of(MOD_ID, "void_queen_phase_2"),
                    net.minecraft.entity.EntityType.Builder.<com.antigravity.trapplatform.entity.VoidQueenPhase2Entity>create(
                            com.antigravity.trapplatform.entity.VoidQueenPhase2Entity::new,
                            net.minecraft.entity.SpawnGroup.MONSTER)
                            .dimensions(0.6f, 1.8f).build("void_queen_phase_2"));


    public static final net.minecraft.entity.EntityType<com.antigravity.trapplatform.entity.GrassPsychicShieldEntity> GRASS_PSYCHIC_SHIELD_ENTITY = net.minecraft.registry.Registry
            .register(
                    net.minecraft.registry.Registries.ENTITY_TYPE,
                    net.minecraft.util.Identifier.of(MOD_ID, "grass_psychic_shield"),
                    net.minecraft.entity.EntityType.Builder.<com.antigravity.trapplatform.entity.GrassPsychicShieldEntity>create(
                            com.antigravity.trapplatform.entity.GrassPsychicShieldEntity::new,
                            net.minecraft.entity.SpawnGroup.MISC)
                            .dimensions(6.0f, 6.0f).build("grass_psychic_shield"));

    public static final net.minecraft.entity.EntityType<com.antigravity.trapplatform.entity.GrassPsychicPysroEntity> GRASS_PSYCHIC_PYSRO_ENTITY = net.minecraft.registry.Registry
            .register(
                    net.minecraft.registry.Registries.ENTITY_TYPE,
                    net.minecraft.util.Identifier.of(MOD_ID, "grass_psychic_pysro"),
                    net.minecraft.entity.EntityType.Builder.<com.antigravity.trapplatform.entity.GrassPsychicPysroEntity>create(
                            com.antigravity.trapplatform.entity.GrassPsychicPysroEntity::new,
                            net.minecraft.entity.SpawnGroup.MONSTER)
                            .dimensions(0.8f, 2.0f).build("grass_psychic_pysro"));


    public static final net.minecraft.registry.RegistryKey<net.minecraft.world.World> SECRET_DIMENSION = net.minecraft.registry.RegistryKey
            .of(
                    net.minecraft.registry.RegistryKeys.WORLD,
                    net.minecraft.util.Identifier.of(MOD_ID, "secret_dimension"));

    // Grass Mass Block
    public static final net.minecraft.block.Block GRASS_MASS_BLOCK = net.minecraft.registry.Registry.register(
            net.minecraft.registry.Registries.BLOCK,
            net.minecraft.util.Identifier.of("trapplatform", "grass_mass"),
            new com.antigravity.trapplatform.block.GrassMassBlock(
                    net.minecraft.block.AbstractBlock.Settings.create()
                            .strength(-1.0f, 3600000.0f)
                            .mapColor(net.minecraft.util.DyeColor.GREEN)
                            .sounds(net.minecraft.sound.BlockSoundGroup.GRASS)
                            .luminance(state -> 7)
            )
    );

    // Tangled Status Effect
    public static final net.minecraft.entity.effect.StatusEffect TANGLED_EFFECT = net.minecraft.registry.Registry.register(
            net.minecraft.registry.Registries.STATUS_EFFECT,
            net.minecraft.util.Identifier.of(MOD_ID, "tangled"),
            new com.antigravity.trapplatform.effect.TangledStatusEffect()
    );

    public static boolean ARE_TELEPORTERS_DISABLED = false;


    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Trap Platform Mod!");

        // Register Traps
        TrapManager.registerTrap(new com.antigravity.trapplatform.traps.ArrowTrap());
        TrapManager.registerTrap(new com.antigravity.trapplatform.traps.FangTrap());
        TrapManager.registerTrap(new com.antigravity.trapplatform.traps.LightningTrap());
        TrapManager.registerTrap(new com.antigravity.trapplatform.traps.MobTrap());
        TrapManager.registerTrap(new com.antigravity.trapplatform.traps.FireballTrap());
        TrapManager.registerTrap(new com.antigravity.trapplatform.traps.WindChargeTrap());
        TrapManager.registerTrap(new com.antigravity.trapplatform.traps.AnvilTrap());

        // Initialize Manager
        TrapManager.init();

        // Register Tick Handler for Boss Battle State (in Overworld)
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getRegistryKey() == net.minecraft.world.World.OVERWORLD) {
                com.antigravity.trapplatform.state.BossBattleState.getServerState(world).tick(world);
            }
        });

        // Debug: List dimensions on server start
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("Server Started. Listing Dimensions:");
            for (net.minecraft.registry.RegistryKey<net.minecraft.world.World> key : server.getWorldRegistryKeys()) {
                LOGGER.info(" - " + key.getValue().toString());
            }
        });

        // Register Item
        net.minecraft.registry.Registry.register(
                net.minecraft.registry.Registries.ITEM,
                net.minecraft.util.Identifier.of("trapplatform", "teleporter"),
                new com.antigravity.trapplatform.items.ArenaTeleporterItem(new net.minecraft.item.Item.Settings()));

        net.minecraft.registry.Registry.register(
                net.minecraft.registry.Registries.ITEM,
                net.minecraft.util.Identifier.of("trapplatform", "fear_amulet"),
                new com.antigravity.trapplatform.items.FearAmuletItem(new net.minecraft.item.Item.Settings()));

        // Register Command
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT
                .register((dispatcher, registryAccess, environment) -> {
                    com.antigravity.trapplatform.commands.EnterCommand.register(dispatcher);
                });

        // Add to Creative Tab
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.item.ItemGroups.TOOLS)
                .register(content -> {
                    content.add(net.minecraft.registry.Registries.ITEM
                            .get(net.minecraft.util.Identifier.of("trapplatform", "teleporter")));
                    content.add(net.minecraft.registry.Registries.ITEM
                            .get(net.minecraft.util.Identifier.of("trapplatform", "fear_amulet")));
                });

        // Give item on Join
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            giveTeleporter(handler.player);
            // Don't give amulet on join, only on respawn if they had it? Or maybe just give
            // it if they unlocked it?
            // For now, let's just give it on respawn if they are in the secret dimension or
            // if they died fighting the boss.
            // Actually, user said "amulet should respawn itself", implying it's a permanent
            // item once obtained.
            // But we don't track "obtained" state easily without capability/advancement.
            // Let's just give it on respawn for now to be safe, or maybe we can check if
            // they are in the dimension.
        });

        // Give item on Respawn
        net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.AFTER_RESPAWN
                .register((oldPlayer, newPlayer, alive) -> {
                    giveTeleporter(newPlayer);
                    giveAmulet(newPlayer);
                });

        // Generate Ominous F on Server Start (or first join)
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            net.minecraft.server.world.ServerWorld world = server.getOverworld();
            generateOminousF(world);
        });

        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(FEAR_ENTITY,
                net.minecraft.entity.mob.EvokerEntity.createEvokerAttributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(VOID_QUEEN_ENTITY,
                com.antigravity.trapplatform.entity.VoidQueenEntity.createVoidQueenAttributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(LICA_ENTITY,
                com.antigravity.trapplatform.entity.LicaEntity.createLicaAttributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(VOID_QUEEN_PHASE_2_ENTITY,
                com.antigravity.trapplatform.entity.VoidQueenPhase2Entity.createPhase2Attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(GRASS_PSYCHIC_PYSRO_ENTITY,
                com.antigravity.trapplatform.entity.GrassPsychicPysroEntity.createPysroAttributes());
    }

    public static void generateStructure(net.minecraft.server.world.ServerWorld world, int xOffset, int[][] shape,
            String[] signText) {
        net.minecraft.util.math.BlockPos start = world.getSpawnPos().add(xOffset, 0, 20);

        // Force load chunk to ensure Heightmap is correct
        int chunkX = net.minecraft.util.math.ChunkSectionPos.getSectionCoord(start.getX());
        int chunkZ = net.minecraft.util.math.ChunkSectionPos.getSectionCoord(start.getZ());
        world.getChunk(chunkX, chunkZ, net.minecraft.world.chunk.ChunkStatus.FULL, true);

        // Find ground
        start = world.getTopPosition(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, start);
        System.out.println("Generating structure at: " + start.toShortString());

        for (int x = 0; x < shape.length; x++) {
            for (int z = 0; z < shape[x].length; z++) {
                if (shape[x][z] == 1) {
                    net.minecraft.util.math.BlockPos pos = start.add(x, 0, z);
                    // Dig hole
                    for (int y = 0; y < 5; y++) {
                        world.setBlockState(pos.down(y), net.minecraft.block.Blocks.AIR.getDefaultState());
                    }
                    // Lava at bottom
                    world.setBlockState(pos.down(5), net.minecraft.block.Blocks.LAVA.getDefaultState());
                }
            }
        }

        // Fire around
        for (int x = -1; x <= 5; x++) {
            for (int z = -1; z <= 5; z++) {
                // If border
                if (x == -1 || x == 5 || z == -1 || z == 5) {
                    net.minecraft.util.math.BlockPos pos = start.add(x, 0, z);
                    if (world.getBlockState(pos).isAir()) {
                        world.setBlockState(pos, net.minecraft.block.Blocks.FIRE.getDefaultState());
                    }
                }
            }
        }

        // Sign
        if (signText != null) {
            net.minecraft.util.math.BlockPos signPos = start.add(0, 1, -2);
            world.setBlockState(signPos, net.minecraft.block.Blocks.CRIMSON_SIGN.getDefaultState());
            net.minecraft.block.entity.BlockEntity be = world.getBlockEntity(signPos);
            if (be instanceof net.minecraft.block.entity.SignBlockEntity sign) {
                net.minecraft.text.Text[] texts = new net.minecraft.text.Text[4];
                for (int i = 0; i < 4; i++)
                    texts[i] = (i < signText.length) ? net.minecraft.text.Text.of(signText[i])
                            : net.minecraft.text.Text.empty();

                sign.setText(new net.minecraft.block.entity.SignText(
                        texts,
                        new net.minecraft.text.Text[] { net.minecraft.text.Text.empty(),
                                net.minecraft.text.Text.empty(), net.minecraft.text.Text.empty(),
                                net.minecraft.text.Text.empty() },
                        net.minecraft.util.DyeColor.BLACK,
                        true), true);
            }
        }
    }

    private void generateOminousF(net.minecraft.server.world.ServerWorld world) {
        int[][] shape = {
                { 1, 1, 1, 1, 1 },
                { 1, 0, 0, 0, 0 },
                { 1, 1, 1, 0, 0 },
                { 1, 0, 0, 0, 0 },
                { 1, 0, 0, 0, 0 }
        };
        String[] text = { "§4The Shadow", "§cLurks...", "§0F...", "§8(The True Boss)" };
        generateStructure(world, 20, shape, text);
    }

    private void giveTeleporter(net.minecraft.server.network.ServerPlayerEntity player) {
        net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM
                .get(net.minecraft.util.Identifier.of("trapplatform", "teleporter"));
        if (!player.getInventory().contains(new net.minecraft.item.ItemStack(item))) {
            player.getInventory().insertStack(new net.minecraft.item.ItemStack(item));
        }
    }

    private void giveAmulet(net.minecraft.server.network.ServerPlayerEntity player) {
        // Only give if they are in the secret dimension or if we want to be generous.
        // User said "respawn itself", so we'll just give it.
        net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM
                .get(net.minecraft.util.Identifier.of("trapplatform", "fear_amulet"));
        if (!player.getInventory().contains(new net.minecraft.item.ItemStack(item))) {
            player.getInventory().insertStack(new net.minecraft.item.ItemStack(item));
        }
    }
}
