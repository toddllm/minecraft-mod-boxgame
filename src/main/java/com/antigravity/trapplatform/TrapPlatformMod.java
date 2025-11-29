package com.antigravity.trapplatform;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.antigravity.trapplatform.TrapManager; // Added import for TrapManager

public class TrapPlatformMod implements ModInitializer {
    public static final String MOD_ID = "trapplatform";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

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
                });

        // Give item on Join
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            giveTeleporter(handler.player);
        });

        // Give item on Respawn
        net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.AFTER_RESPAWN
                .register((oldPlayer, newPlayer, alive) -> {
                    giveTeleporter(newPlayer);
                });

        // Generate Ominous F on Server Start (or first join)
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            net.minecraft.server.world.ServerWorld world = server.getOverworld();
            generateOminousF(world);
        });
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
}
