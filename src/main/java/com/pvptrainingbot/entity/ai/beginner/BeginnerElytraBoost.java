package com.pvptrainingbot.entity.ai.beginner;

import com.pvptrainingbot.config.BotConfig;
import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;

public class BeginnerElytraBoost {

    public static void tick(PvpBotEntity bot, LivingEntity target) {
        if (!bot.isTechEnabled("beginner_elytra_boost") || target == null) {
            return;
        }

        // If on ground and wanting to boost into air
        if (bot.isOnGround() && bot.getWindChargeCooldown() <= 0 && bot.distanceTo(target) > 8.0F) {
            boolean shouldBoost = bot.getRandom().nextFloat() < 0.15F || bot.getActiveDrillTech().equalsIgnoreCase("beginner_elytra_boost");
            if (shouldBoost) {
                executeElytraWindChargeBoost(bot);
                if (bot.getActiveDrillTech().equalsIgnoreCase("beginner_elytra_boost")) {
                    bot.setActiveDrillTech("");
                }
            }
        }
    }

    public static void executeElytraWindChargeBoost(PvpBotEntity bot) {
        // 1. Equip Elytra (hotbar slot 7)
        bot.toggleHotbarElytra(true);
        // 2. Double jump / launch
        bot.jump();
        bot.addVelocity(0.0D, 0.45D, 0.0D);
        // 3. Fire wind charge directly beneath
        bot.addVelocity(bot.getVelocity().x * 1.6D, 1.35D, bot.getVelocity().z * 1.6D);
        bot.velocityDirty = true;
        bot.setWindChargeCooldown(BotConfig.getCooldownMultiplierTicks(bot.getBotDifficulty(), 80));

        bot.getWorld().playSound(null, bot.getX(), bot.getY(), bot.getZ(), SoundEvents.ENTITY_WIND_CHARGE_WIND_BURST.value(), bot.getSoundCategory(), 1.0F, 1.2F);
        if (bot.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.GUST_EMITTER_LARGE, bot.getX(), bot.getY(), bot.getZ(), 3, 0.3, 0.2, 0.3, 0.05);
        }
    }
}
