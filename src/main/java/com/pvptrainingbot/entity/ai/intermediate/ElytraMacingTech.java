package com.pvptrainingbot.entity.ai.intermediate;

import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;

public class ElytraMacingTech {

    public static void tick(PvpBotEntity bot, LivingEntity target) {
        if (!bot.isTechEnabled("elytra_macing") || target == null) {
            return;
        }

        double verticalDiff = bot.getY() - target.getY();
        float distance = bot.distanceTo(target);

        // If bot is high up and dive bombing towards target
        if (bot.isElytraGliding() && verticalDiff > 2.0D && distance < 6.5F && bot.getVelocity().y < -0.1D) {
            // Mojang coded 1.21 so you can't mace while gliding! Must unequip elytra right before striking
            if (distance <= 4.8F) {
                bot.toggleHotbarElytra(false); // Hotbar switch chestplate
                bot.equipHotbarSlot(0);        // Switch to Wind Burst III Mace
                if (bot.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.CLOUD, bot.getX(), bot.getY(), bot.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
                }
            }
        }
    }
}
