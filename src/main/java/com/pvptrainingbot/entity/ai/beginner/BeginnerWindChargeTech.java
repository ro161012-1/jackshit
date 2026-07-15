package com.pvptrainingbot.entity.ai.beginner;

import com.pvptrainingbot.config.BotConfig;
import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class BeginnerWindChargeTech {

    public static void tick(PvpBotEntity bot, LivingEntity target) {
        if (!bot.isTechEnabled("beginner_windcharge") || target == null) {
            return;
        }

        double distanceSq = bot.squaredDistanceTo(target);
        
        // Check if on ground and ready to execute windcharge jump
        if (bot.isOnGround() && bot.getWindChargeCooldown() <= 0 && distanceSq <= 120.0D && distanceSq >= 9.0D) {
            // Respect Mace 1.67s (33 ticks) cooldown! Do not jump if mace is cooling down
            if (bot.getMaceAttackCooldown() <= 0) {
                boolean shouldJump = bot.getRandom().nextFloat() < (bot.getBotBehavior().equals("aggressive") ? 0.35F : 0.20F);
                if (shouldJump || bot.getActiveDrillTech().equalsIgnoreCase("beginner_windcharge")) {
                    executeWindChargeJump(bot);
                    if (bot.getActiveDrillTech().equalsIgnoreCase("beginner_windcharge")) {
                        bot.setActiveDrillTech("");
                    }
                }
            }
        }
    }

    public static void executeWindChargeJump(PvpBotEntity bot) {
        // Right click wind charge and press jump at exact same time
        bot.jump();
        bot.setPitch(90.0F); // Look right down at feet

        Vec3d vel = bot.getVelocity();
        // High vertical jump impulse
        bot.setVelocity(vel.x * 1.5D, 1.45D, vel.z * 1.5D);
        bot.velocityDirty = true;
        bot.setWindChargeCooldown(BotConfig.getCooldownMultiplierTicks(bot.getBotDifficulty(), 60));

        bot.getWorld().playSound(null, bot.getX(), bot.getY(), bot.getZ(), SoundEvents.ENTITY_WIND_CHARGE_WIND_BURST.value(), bot.getSoundCategory(), 1.0F, 1.1F);
        if (bot.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.GUST_EMITTER_LARGE, bot.getX(), bot.getY(), bot.getZ(), 2, 0.2, 0.1, 0.2, 0.05);
        }
    }
}
