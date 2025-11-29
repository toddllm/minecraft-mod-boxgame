package com.antigravity.trapplatform.client.renderer.entity;

import com.antigravity.trapplatform.entity.BowlEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class BowlRenderer extends EntityRenderer<BowlEntity> {

    public BowlRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(BowlEntity entity) {
        return net.minecraft.util.Identifier.of("minecraft", "textures/item/bowl.png"); // Not used for item render
    }

    @Override
    public void render(BowlEntity entity, float yaw, float tickDelta, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);

        matrices.push();
        matrices.translate(0.0D, 0.5D, 0.0D);
        
        // Flip Animation
        // We can't easily access the entity's timer here without casting or public fields
        // For now, just render it upright. If flipping, maybe rotate it?
        // Since we don't have easy access to 'isFlipping' state in renderer without syncing data,
        // we'll keep it static for now or use a simple wobble.
        
        // Render a Bowl Item
        net.minecraft.client.MinecraftClient.getInstance().getItemRenderer().renderItem(
                new net.minecraft.item.ItemStack(net.minecraft.item.Items.BOWL),
                net.minecraft.client.render.model.json.ModelTransformationMode.GROUND,
                light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), 0);
        
        matrices.pop();
    }
}
