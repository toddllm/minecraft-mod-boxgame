package com.antigravity.trapplatform.traps;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public interface Trap {
    void trigger(ServerWorld world, ServerPlayerEntity player);

    String getName();
}
