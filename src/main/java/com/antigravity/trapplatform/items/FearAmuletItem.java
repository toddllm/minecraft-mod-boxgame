package com.antigravity.trapplatform.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;

public class FearAmuletItem extends Item {
    public FearAmuletItem(Settings settings) {
        super(settings.rarity(Rarity.EPIC).maxCount(1));
    }

    @Override
    public net.minecraft.util.TypedActionResult<ItemStack> use(net.minecraft.world.World world,
            net.minecraft.entity.player.PlayerEntity user, net.minecraft.util.Hand hand) {
        if (!world.isClient && user instanceof net.minecraft.server.network.ServerPlayerEntity player) {
            net.minecraft.server.MinecraftServer server = player.getServer();
            if (server != null) {
                net.minecraft.server.world.ServerWorld secretDim = server
                        .getWorld(com.antigravity.trapplatform.TrapPlatformMod.SECRET_DIMENSION);
                if (secretDim != null && !player.getWorld().getRegistryKey()
                        .equals(com.antigravity.trapplatform.TrapPlatformMod.SECRET_DIMENSION)) {
                    // Disable other teleporters
                    com.antigravity.trapplatform.TrapPlatformMod.ARE_TELEPORTERS_DISABLED = true;

                    // Create Platform at 0, 100, 0
                    net.minecraft.util.math.BlockPos center = new net.minecraft.util.math.BlockPos(0, 100, 0);
                    // Smart Generation: Only generate if the platform is missing
                    if (secretDim.getBlockState(center).isAir()) {
                        // Generate Giant Breakable Platform (Black Concrete)
                        // Radius 50 (100x100)
                        for (int x = -50; x <= 50; x++) {
                            for (int z = -50; z <= 50; z++) {
                                secretDim.setBlockState(center.add(x, -1, z),
                                        net.minecraft.block.Blocks.BLACK_CONCRETE.getDefaultState(), 2);
                            }
                        }

                        // Generate Elemental Floor (Water/Lava Rays) at y=95
                        // Radius 50 (100x100) - Matches platform size
                        for (int x = -50; x <= 50; x++) {
                            for (int z = -50; z <= 50; z++) {
                                net.minecraft.util.math.BlockPos pos = center.add(x, -5, z); // y=95

                                // 1. Place Base Layer FIRST (Obsidian)
                                secretDim.setBlockState(pos.down(),
                                        net.minecraft.block.Blocks.OBSIDIAN.getDefaultState(), 2);

                                // 2. Place Liquid Layer
                                // Rock Separator Lines (Axis)
                                if (Math.abs(x) < 3 || Math.abs(z) < 3) {
                                    secretDim.setBlockState(pos, net.minecraft.block.Blocks.STONE.getDefaultState(), 2);
                                }
                                // Quadrants
                                else if (x > 0 && z > 0) { // Q1
                                    secretDim.setBlockState(pos, net.minecraft.block.Blocks.WATER.getDefaultState(), 2);
                                } else if (x < 0 && z > 0) { // Q2
                                    secretDim.setBlockState(pos, net.minecraft.block.Blocks.LAVA.getDefaultState(), 2);
                                } else if (x < 0 && z < 0) { // Q3
                                    secretDim.setBlockState(pos, net.minecraft.block.Blocks.WATER.getDefaultState(), 2);
                                } else { // Q4
                                    secretDim.setBlockState(pos, net.minecraft.block.Blocks.LAVA.getDefaultState(), 2);
                                }
                            }
                        }

                        // Generate Void Portals around the platform
                        for (int x = -55; x <= 55; x += 16) {
                            for (int z = -55; z <= 55; z += 16) {
                                secretDim.setBlockState(center.add(x, 0, z),
                                        net.minecraft.block.Blocks.END_GATEWAY.getDefaultState(), 2);
                            }
                        }

                        // Spawn Wind Charge Turrets (Corners)
                        int[][] turretPositions = { { 45, 45 }, { -45, 45 }, { 45, -45 }, { -45, -45 } };
                        for (int[] pos : turretPositions) {
                            com.antigravity.trapplatform.entity.WindChargeTurretEntity turret = new com.antigravity.trapplatform.entity.WindChargeTurretEntity(
                                    com.antigravity.trapplatform.TrapPlatformMod.WIND_CHARGE_TURRET_ENTITY, secretDim);
                            turret.setPosition(center.getX() + pos[0] + 0.5, center.getY(),
                                    center.getZ() + pos[1] + 0.5);
                            secretDim.spawnEntity(turret);
                        }
                    }

                    // Teleport
                    player.teleport(secretDim, 0.5, 100, 0.5, player.getYaw(), player.getPitch());
                    player.sendMessage(Text.of("§5§lYou have entered the Void Queen's Realm..."), true);

                    // Spawn Boss if not present (Global State Check)
                    com.antigravity.trapplatform.state.BossBattleState battleState = com.antigravity.trapplatform.state.BossBattleState.getServerState(secretDim);
                    
                    if (!battleState.isBattleActive()) {
                        com.antigravity.trapplatform.entity.VoidQueenEntity boss = com.antigravity.trapplatform.TrapPlatformMod.VOID_QUEEN_ENTITY
                                .create(secretDim);
                        if (boss != null) {
                            boss.refreshPositionAndAngles(0.5, 110, 0.5, 0, 0);
                            secretDim.spawnEntity(boss);
                            battleState.startBattle(boss.getUuid());
                            player.sendMessage(Text.of("§4§lGewinus Meteoritoligous has awoken!"), true);
                        }
                    } else {
                        player.sendMessage(Text.of("§4§lThe battle continues..."), true);
                    }

                    return net.minecraft.util.TypedActionResult.success(user.getStackInHand(hand));
                }
            }
        }
        return net.minecraft.util.TypedActionResult.pass(user.getStackInHand(hand));
    }
}
