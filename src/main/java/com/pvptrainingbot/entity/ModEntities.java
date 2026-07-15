package com.pvptrainingbot.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<PvpBotEntity> PVP_BOT = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of("pvptrainingbot", "pvp_bot"),
            EntityType.Builder.create(PvpBotEntity::new, SpawnGroup.CREATURE)
                    .dimensions(0.6f, 1.8f)
                    .maxTrackingRange(64)
                    .trackingTickInterval(3)
                    .build()
    );

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(PVP_BOT, PvpBotEntity.createBotAttributes());
    }
}
