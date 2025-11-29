package com.antigravity.trapplatform.items;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ArenaTeleporterItem extends Item {
    public static final RegistryKey<World> ARENA_KEY = RegistryKey.of(RegistryKeys.WORLD,
            Identifier.of("trapplatform", "arena"));

    public ArenaTeleporterItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            System.out.println("Item used by " + user.getName().getString());
            if (user instanceof ServerPlayerEntity player) {
                ServerWorld serverWorld = player.getServer().getWorld(ARENA_KEY);

                if (serverWorld == null) {
                    player.sendMessage(Text.of("§cError: Arena dimension not found!"), true);
                    return TypedActionResult.fail(user.getStackInHand(hand));
                }

                // Always generate platform
                BlockPos center = new BlockPos(0, 100, 0);
                generatePlatform(serverWorld, center);

                // Teleport
                player.teleport(serverWorld, 0.5, 101, 0.5, 0, 0);
                player.sendMessage(Text.of("§aTeleported to Arena!"), true);

                return TypedActionResult.success(user.getStackInHand(hand));
            }
        }
        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    @Override
    public net.minecraft.util.ActionResult useOnBlock(net.minecraft.item.ItemUsageContext context) {
        this.use(context.getWorld(), context.getPlayer(), context.getHand());
        return net.minecraft.util.ActionResult.SUCCESS;
    }

    private void generatePlatform(ServerWorld world, BlockPos center) {
        int radius = 5;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                world.setBlockState(center.add(x, 0, z), Blocks.STONE.getDefaultState());
            }
        }
    }
}
