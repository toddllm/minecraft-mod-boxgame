package com.antigravity.trapplatform.client;

import com.antigravity.trapplatform.TrapPlatformMod;
import com.antigravity.trapplatform.client.renderer.entity.FearEntityRenderer;
import com.antigravity.trapplatform.client.renderer.entity.MeteorRenderer;
import com.antigravity.trapplatform.client.renderer.entity.VoidQueenRenderer;
import com.antigravity.trapplatform.client.renderer.entity.WindChargeTurretRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class TrapPlatformClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(TrapPlatformMod.FEAR_ENTITY, FearEntityRenderer::new);
        EntityRendererRegistry.register(TrapPlatformMod.VOID_QUEEN_ENTITY, VoidQueenRenderer::new);
        EntityRendererRegistry.register(TrapPlatformMod.METEOR_ENTITY, MeteorRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                com.antigravity.trapplatform.TrapPlatformMod.WIND_CHARGE_TURRET_ENTITY,
                com.antigravity.trapplatform.client.renderer.entity.WindChargeTurretRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                com.antigravity.trapplatform.TrapPlatformMod.BOWL_ENTITY,
                com.antigravity.trapplatform.client.renderer.entity.BowlRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                com.antigravity.trapplatform.TrapPlatformMod.LICA_ENTITY,
                com.antigravity.trapplatform.client.renderer.entity.LicaRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                com.antigravity.trapplatform.TrapPlatformMod.VOID_QUEEN_PHASE_2_ENTITY,
                com.antigravity.trapplatform.client.renderer.entity.VoidQueenPhase2Renderer::new);
        // Shield entity (invisible, particle-only)
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                com.antigravity.trapplatform.TrapPlatformMod.GRASS_PSYCHIC_SHIELD_ENTITY,
                context -> new net.minecraft.client.render.entity.EmptyEntityRenderer<>(context));
        // Grass-Psychic Pysro (invisible but VERY visible via particles and name)
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                com.antigravity.trapplatform.TrapPlatformMod.GRASS_PSYCHIC_PYSRO_ENTITY,
                context -> new net.minecraft.client.render.entity.EmptyEntityRenderer<>(context));
    }
}
