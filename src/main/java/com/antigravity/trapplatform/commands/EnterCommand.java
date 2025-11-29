package com.antigravity.trapplatform.commands;

import com.antigravity.trapplatform.items.ArenaTeleporterItem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class EnterCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("trapplatform")
                .then(CommandManager.literal("enter")
                        .executes(EnterCommand::enter)));
    }

    private static int enter(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (source.getEntity() instanceof ServerPlayerEntity player) {
            ServerWorld serverWorld = player.getServer().getWorld(ArenaTeleporterItem.ARENA_KEY);

            if (serverWorld == null) {
                source.sendError(Text.of("Arena dimension not found!"));
                return 0;
            }

            // Generate platform
            BlockPos center = new BlockPos(0, 100, 0);
            int radius = 5;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    serverWorld.setBlockState(center.add(x, 0, z), Blocks.STONE.getDefaultState());
                }
            }

            // Teleport
            player.teleport(serverWorld, 0.5, 101, 0.5, 0, 0);
            source.sendFeedback(() -> Text.of("§aTeleported to Arena via Command!"), true);
            return 1;
        }
        return 0;
    }
}
