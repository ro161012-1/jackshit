package com.pvptrainingbot.client.render;

import com.mojang.authlib.GameProfile;
import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PvpBotEntityRenderer extends LivingEntityRenderer<PvpBotEntity, PlayerEntityModel<PvpBotEntity>> {
    private static final Map<String, Identifier> skinCache = new HashMap<>();
    private static final Map<String, Boolean> isSlimCache = new HashMap<>();
    
    private final PlayerEntityModel<PvpBotEntity> defaultModel;
    private final PlayerEntityModel<PvpBotEntity> slimModel;

    public PvpBotEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new PlayerEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER), false), 0.5f);
        this.defaultModel = this.getModel();
        this.slimModel = new PlayerEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER_SLIM), true);
        this.addFeature(new HeldItemFeatureRenderer<>(this, ctx.getHeldItemRenderer()));
    }

    @Override
    protected boolean hasLabel(PvpBotEntity entity) {
        // Always render the custom HP nametag label area above the bot's head (`[PvP Bot] username ❤ HP [MODE]`)
        return entity.isCustomNameVisible() || super.hasLabel(entity);
    }

    @Override
    public void render(PvpBotEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        String username = entity.getBotUsername();
        if (isSlimCache.getOrDefault(username, false)) {
            this.model = this.slimModel;
        } else {
            this.model = this.defaultModel;
        }

        this.model.sneaking = entity.isSneaking();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(PvpBotEntity entity) {
        String username = entity.getBotUsername();
        if (username == null || username.trim().isEmpty()) {
            return DefaultSkinHelper.getTexture(entity.getUuid());
        }

        if (skinCache.containsKey(username) && skinCache.get(username) != null) {
            return skinCache.get(username);
        }

        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes());
        GameProfile profile = new GameProfile(uuid, username);
        
        try {
            SkinTextures textures = MinecraftClient.getInstance().getSkinProvider().getSkinTextures(profile);
            if (textures != null && textures.texture() != null) {
                skinCache.put(username, textures.texture());
                isSlimCache.put(username, textures.model() == SkinTextures.Model.SLIM);
                return textures.texture();
            }
        } catch (Exception ignored) {
        }

        Identifier fallback = DefaultSkinHelper.getTexture(uuid);
        skinCache.putIfAbsent(username, fallback);
        return fallback;
    }
}
