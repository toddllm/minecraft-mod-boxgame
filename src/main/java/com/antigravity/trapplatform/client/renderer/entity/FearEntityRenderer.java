package com.antigravity.trapplatform.client.renderer.entity;

import com.antigravity.trapplatform.entity.FearEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

public class FearEntityRenderer extends BipedEntityRenderer<FearEntity, PlayerEntityModel<FearEntity>> {
    private static final Identifier TEXTURE = Identifier.of("trapplatform", "textures/entity/fear.png");

    public FearEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 0.5f);
        this.addFeature(new net.minecraft.client.render.entity.feature.ArmorFeatureRenderer<>(
                this,
                new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER_INNER_ARMOR)),
                new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public Identifier getTexture(FearEntity entity) {
        return TEXTURE;
    }
}
