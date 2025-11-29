package com.antigravity.trapplatform.client.renderer.entity;

import com.antigravity.trapplatform.entity.VoidQueenEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class VoidQueenRenderer extends BipedEntityRenderer<VoidQueenEntity, PlayerEntityModel<VoidQueenEntity>> {
    private static final Identifier TEXTURE = Identifier.of("trapplatform", "textures/entity/void_queen.png");

    public VoidQueenRenderer(EntityRendererFactory.Context context) {
        super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 0.5f);
        this.addFeature(new LicaCageFeatureRenderer(this));
    }

    @Override
    public Identifier getTexture(VoidQueenEntity entity) {
        return TEXTURE;
    }

    static class MeteorHeadFeatureRenderer
            extends FeatureRenderer<VoidQueenEntity, PlayerEntityModel<VoidQueenEntity>> {
        public MeteorHeadFeatureRenderer(
                FeatureRendererContext<VoidQueenEntity, PlayerEntityModel<VoidQueenEntity>> context) {
            super(context);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                VoidQueenEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress,
                float headYaw, float headPitch) {
            matrices.push();
            // Attach to Head
            this.getContextModel().head.rotate(matrices);

            // Scale up slightly
            matrices.scale(1.5f, 1.5f, 1.5f);
            matrices.translate(-0.5, -0.75, -0.5); // Center on head

            // Render Magma Block
            net.minecraft.client.MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(
                    net.minecraft.block.Blocks.MAGMA_BLOCK.getDefaultState(), matrices, vertexConsumers, light,
                    net.minecraft.client.render.OverlayTexture.DEFAULT_UV);

            matrices.pop();
        }
    }

    static class LicaCageFeatureRenderer extends FeatureRenderer<VoidQueenEntity, PlayerEntityModel<VoidQueenEntity>> {
        public LicaCageFeatureRenderer(
                FeatureRendererContext<VoidQueenEntity, PlayerEntityModel<VoidQueenEntity>> context) {
            super(context);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                VoidQueenEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress,
                float headYaw, float headPitch) {
            matrices.push();
            // Attach to Body
            this.getContextModel().body.rotate(matrices);
            matrices.translate(0, -0.5, 0.4); // Position on back
            matrices.scale(0.5f, 0.5f, 0.5f);

            // Render Glass Cage
            net.minecraft.client.MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(
                    net.minecraft.block.Blocks.GLASS.getDefaultState(), matrices, vertexConsumers, light,
                    net.minecraft.client.render.OverlayTexture.DEFAULT_UV);

            // Render Lica (Tiny Ghost/Fairy inside)
            matrices.scale(0.5f, 0.5f, 0.5f);
            matrices.translate(0, 0, 0);
            // Use Beacon Beam or End Rod for "Ghost" effect? Or just a small white
            // block/item?
            // Let's use a Sea Lantern for a glowing soul effect
            net.minecraft.client.MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(
                    net.minecraft.block.Blocks.SEA_LANTERN.getDefaultState(), matrices, vertexConsumers, 15728880,
                    net.minecraft.client.render.OverlayTexture.DEFAULT_UV);

            matrices.pop();
        }
    }
}
