package com.pvptrainingbot.client;

import com.pvptrainingbot.client.render.PvpBotEntityRenderer;
import com.pvptrainingbot.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class PvpTrainingBotClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.PVP_BOT, PvpBotEntityRenderer::new);
    }
}
