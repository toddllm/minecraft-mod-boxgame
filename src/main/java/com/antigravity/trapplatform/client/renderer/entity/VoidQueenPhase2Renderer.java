package com.antigravity.trapplatform.client.renderer.entity;

import com.antigravity.trapplatform.entity.VoidQueenPhase2Entity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

public class VoidQueenPhase2Renderer extends VoidQueenRenderer {

    public VoidQueenPhase2Renderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(com.antigravity.trapplatform.entity.VoidQueenEntity entity) {
        // Use same texture for now, maybe tint it red in code if possible, or just rely on name/bossbar
        return net.minecraft.util.Identifier.of("trapplatform", "textures/entity/void_queen.png");
    }
}
