package com.antigravity.trapplatform.client.renderer.entity;

import com.antigravity.trapplatform.entity.WindChargeTurretEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class WindChargeTurretRenderer extends EntityRenderer<WindChargeTurretEntity> {

    public WindChargeTurretRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(WindChargeTurretEntity entity) {
        return net.minecraft.util.Identifier.of("minecraft", "textures/block/dispenser_front.png"); // Not used for
                                                                                                    // block render
    }

    @Override
    public void render(WindChargeTurretEntity entity, float yaw, float tickDelta, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - yaw));
        matrices.translate(-0.5D, 0.0D, -0.5D);

        // Render a Dispenser Block
        net.minecraft.client.MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(
                net.minecraft.block.Blocks.DISPENSER.getDefaultState(), matrices, vertexConsumers, light,
                OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }
}
