package com.pvptrainingbot.entity.ai.master;

import com.pvptrainingbot.config.BotConfig;
import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class DiagonalPearlCatchTech {

    public static void tick(PvpBotEntity bot, LivingEntity target) {
        if (!bot.isTechEnabled("diagonal_pearl_catch") || target == null) {
            return;
        }

        double distanceSq = bot.squaredDistanceTo(target);

        // If target is far horizontally (> 14 blocks) and bot wants to traverse both horizontally & vertically
        if (bot.isOnGround() && distanceSq > 196.0D && distanceSq < 600.0D && bot.getPearlCooldown() <= 0 && bot.getWindChargeCooldown() <= 0) {
            boolean shouldDiag = bot.getRandom().nextFloat() < 0.10F || bot.getActiveDrillTech().equalsIgnoreCase("diagonal_pearl_catch");
            if (shouldDiag) {
                executeDiagonalPearlCatch(bot, target);
                if (bot.getActiveDrillTech().equalsIgnoreCase("diagonal_pearl_catch")) {
                    bot.setActiveDrillTech("");
                }
            }
        }
    }

    public static void executeDiagonalPearlCatch(PvpBotEntity bot, LivingEntity target) {
        // 1. Throw pearl diagonally towards target
        Vec3d toTarget = target.getPos().subtract(bot.getPos()).normalize();
        bot.getLookControl().lookAt(target.getX(), target.getY() + 12.0D, target.getZ(), 90.0F, 90.0F);

        // 2. Account for gravity: aim wind charge slightly BELOW the pearl arc to hit it accurately diagonally
        bot.getWorld().playSound(null, bot.getX(), bot.getY(), bot.getZ(), SoundEvents.ENTITY_ENDER_PEARL_THROW, bot.getSoundCategory(), 1.0F, 1.0F);
        bot.getWorld().playSound(null, bot.getX(), bot.getY(), bot.getZ(), SoundEvents.ENTITY_WIND_CHARGE_WIND_BURST.value(), bot.getSoundCategory(), 1.0F, 1.3F);

        // Simulate successful diagonal teleport: halfway to target horizontally + 16 blocks vertically
        Vec3d destination = bot.getPos().add(toTarget.multiply(8.0D)).add(0.0D, 16.0D, 0.0D);
        bot.requestTeleport(destination.x, destination.y, destination.z);
        bot.setVelocity(toTarget.x * 0.8D, 0.45D, toTarget.z * 0.8D);
        bot.velocityDirty = true;

        bot.setPearlCooldown(BotConfig.getCooldownMultiplierTicks(bot.getBotDifficulty(), 120));
        bot.setWindChargeCooldown(BotConfig.getCooldownMultiplierTicks(bot.getBotDifficulty(), 80));

        if (bot.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.PORTAL, destination.x, destination.y, destination.z, 25, 0.5, 0.5, 0.5, 0.1);
        }
    }
}
