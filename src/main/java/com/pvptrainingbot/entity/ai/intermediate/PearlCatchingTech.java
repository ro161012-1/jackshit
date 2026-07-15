package com.pvptrainingbot.entity.ai.intermediate;

import com.pvptrainingbot.config.BotConfig;
import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;

public class PearlCatchingTech {

    public static void tick(PvpBotEntity bot, LivingEntity target) {
        if (!bot.isTechEnabled("pearl_catching") || target == null) {
            return;
        }

        if (bot.isOnGround() && bot.getPearlCooldown() <= 0 && bot.getWindChargeCooldown() <= 0) {
            boolean shouldPearlCatch = bot.getRandom().nextFloat() < 0.12F || bot.getActiveDrillTech().equalsIgnoreCase("pearl_catching");
            if (shouldPearlCatch) {
                executePearlCatch(bot);
                if (bot.getActiveDrillTech().equalsIgnoreCase("pearl_catching")) {
                    bot.setActiveDrillTech("");
                }
            }
        }
    }

    public static void executePearlCatch(PvpBotEntity bot) {
        // Stand still to ensure pearl momentum and wind charge align perfectly
        bot.getNavigation().stop();
        bot.setPitch(-85.0F); // Aim straight up

        // Simulate instant vertical pearl catch teleport high into the sky (~18 blocks)
        bot.requestTeleport(bot.getX(), bot.getY() + 18.0D, bot.getZ());
        bot.setVelocity(0.0D, 0.4D, 0.0D);
        bot.velocityDirty = true;
        bot.setPearlCooldown(BotConfig.getCooldownMultiplierTicks(bot.getBotDifficulty(), 100));
        bot.setWindChargeCooldown(BotConfig.getCooldownMultiplierTicks(bot.getBotDifficulty(), 60));

        bot.getWorld().playSound(null, bot.getX(), bot.getY(), bot.getZ(), SoundEvents.ENTITY_ENDER_PEARL_THROW, bot.getSoundCategory(), 1.0F, 1.0F);
        bot.getWorld().playSound(null, bot.getX(), bot.getY(), bot.getZ(), SoundEvents.ENTITY_WIND_CHARGE_WIND_BURST.value(), bot.getSoundCategory(), 1.0F, 1.3F);
        if (bot.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.PORTAL, bot.getX(), bot.getY(), bot.getZ(), 20, 0.5, 1.0, 0.5, 0.1);
            serverWorld.spawnParticles(ParticleTypes.GUST_EMITTER_LARGE, bot.getX(), bot.getY() + 18.0D, bot.getZ(), 3, 0.3, 0.3, 0.3, 0.05);
        }
    }
}
