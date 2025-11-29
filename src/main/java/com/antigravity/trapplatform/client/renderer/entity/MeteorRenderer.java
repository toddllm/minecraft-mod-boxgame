package com.antigravity.trapplatform.client.renderer.entity;

import com.antigravity.trapplatform.entity.MeteorEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class MeteorRenderer extends EntityRenderer<MeteorEntity> {
    private static final Identifier TEXTURE = Identifier.of("trapplatform", "textures/entity/meteor.png");

    public MeteorRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(MeteorEntity entity, float yaw, float tickDelta, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.scale(2.0f, 2.0f, 2.0f);
        matrices.multiply(this.dispatcher.getRotation());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));

        MatrixStack.Entry entry = matrices.peek();
        VertexConsumer vertexConsumer = vertexConsumers
                .getBuffer(net.minecraft.client.render.RenderLayer.getEntityCutout(this.getTexture(entity)));

        produceVertex(vertexConsumer, entry, light, 0.0F, 0, 0, 1);
        produceVertex(vertexConsumer, entry, light, 1.0F, 0, 1, 1);
        produceVertex(vertexConsumer, entry, light, 1.0F, 1, 1, 0);
        produceVertex(vertexConsumer, entry, light, 0.0F, 1, 0, 0);

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private static void produceVertex(VertexConsumer vertexConsumer, MatrixStack.Entry entry, int light, float x, int y,
            int u, int v) {
        vertexConsumer.vertex(entry, x - 0.5F, (float) y - 0.25F, 0.0F).color(255, 255, 255, 255)
                .texture((float) u, (float) v).overlay(OverlayTexture.DEFAULT_UV).light(light)
                .normal(entry, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public Identifier getTexture(MeteorEntity entity) {
        return TEXTURE;
    }
}
