package com.pvptrainingbot.entity.ai.bonus;

import com.pvptrainingbot.config.BotConfig;
import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class WallPearlingTech {

    public static void tick(PvpBotEntity bot, LivingEntity target) {
        if (!bot.isTechEnabled("wall_pearling") || target == null) {
            return;
        }

        // Check if target is near or under an obstacle/wall (or if horizontal distance is ~6-12 blocks)
        float distance = bot.distanceTo(target);
        if (bot.isOnGround() && distance >= 6.0F && distance <= 14.0F && bot.getPearlCooldown() <= 0) {
            boolean shouldWallPearl = bot.getRandom().nextFloat() < 0.10F || bot.getActiveDrillTech().equalsIgnoreCase("wall_pearling");
            if (shouldWallPearl) {
                executeWallPearl(bot, target);
                if (bot.getActiveDrillTech().equalsIgnoreCase("wall_pearling")) {
                    bot.setActiveDrillTech("");
                }
            }
        }
    }

    public static void executeWallPearl(PvpBotEntity bot, LivingEntity target) {
        // Throw pearl directly above wall / target's head (+6 blocks vertical offset)
        bot.equipHotbarSlot(6); // Pearl slot
        bot.getWorld().playSound(null, bot.getX(), bot.getY(), bot.getZ(), SoundEvents.ENTITY_ENDER_PEARL_THROW, bot.getSoundCategory(), 1.0F, 1.0F);

        // Teleport right above wall/obstacle directly over target's head
        bot.requestTeleport(target.getX(), target.getY() + 7.0D, target.getZ());
        bot.setPearlCooldown(BotConfig.getCooldownMultiplierTicks(bot.getBotDifficulty(), 100));

        // Switch to Wind Burst III Mace (Slot 0) or Breach IV Mace (Slot 1) as we plummet straight down on their head!
        bot.equipHotbarSlot(0);
        bot.setVelocity(0.0D, -0.4D, 0.0D);
        bot.velocityDirty = true;

        if (bot.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 7.0D, target.getZ(), 20, 0.4, 0.4, 0.4, 0.1);
        }
    }
}
