package com.pvptrainingbot.entity.ai.intermediate;

import com.pvptrainingbot.config.BotConfig;
import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class WindChargeCancelTech {

    public static void tick(PvpBotEntity bot, LivingEntity target) {
        if (!bot.isTechEnabled("windcharge_cancel") || target == null) {
            return;
        }

        float distance = bot.distanceTo(target);

        // Check if target is dive bombing or falling rapidly towards bot
        if ((target.getVelocity().y < -0.35D || target.fallDistance > 3.0F) && distance < 8.0F && distance > 2.5F && bot.getWindChargeCooldown() <= 0) {
            boolean shouldCancel = bot.getRandom().nextFloat() < BotConfig.getTechAccuracy(bot.getBotDifficulty());
            if (shouldCancel || bot.getActiveDrillTech().equalsIgnoreCase("windcharge_cancel")) {
                executeWindChargeCancel(bot, target);
                if (bot.getActiveDrillTech().equalsIgnoreCase("windcharge_cancel")) {
                    bot.setActiveDrillTech("");
                }
            }
        }
    }

    public static void executeWindChargeCancel(PvpBotEntity bot, LivingEntity target) {
        // Aim slightly below their feet to intercept falling velocity
        Vec3d interceptPos = new Vec3d(target.getX(), target.getY() - 0.5D, target.getZ());
        Vec3d dir = interceptPos.subtract(bot.getPos()).normalize();

        // Apply knockback / burst effect directly intercepting target
        target.addVelocity(dir.x * 0.8D, 0.6D, dir.z * 0.8D);
        target.velocityDirty = true;
        target.fallDistance = 0.0F; // Resets fall/dive bomb

        bot.setWindChargeCooldown(BotConfig.getCooldownMultiplierTicks(bot.getBotDifficulty(), 50));
        bot.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_WIND_CHARGE_WIND_BURST.value(), bot.getSoundCategory(), 1.0F, 1.1F);
        if (bot.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.GUST_EMITTER_LARGE, target.getX(), target.getY(), target.getZ(), 3, 0.3, 0.3, 0.3, 0.05);
        }
    }
}
