package com.antigravity.trapplatform.client.renderer.entity;

import com.antigravity.trapplatform.entity.LicaEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

// Now using BipedEntityRenderer (Humanoid) so she has hands/legs and can hold items!
public class LicaRenderer extends BipedEntityRenderer<LicaEntity, BipedEntityModel<LicaEntity>> {

    public LicaRenderer(EntityRendererFactory.Context context) {
        super(context, new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER)), 0.5f);
        // WINGS: Add Elytra layer
        this.addFeature(
                new net.minecraft.client.render.entity.feature.ElytraFeatureRenderer<>(this, context.getModelLoader()));
    }

    @Override
    public Identifier getTexture(LicaEntity entity) {
        // VISUAL FIX: Use Wither Skeleton texture (Solid/Dark) to prevent "half
        // invisible" Vex glitch
        return Identifier.of("minecraft", "textures/entity/skeleton/wither_skeleton.png");
    }
}
