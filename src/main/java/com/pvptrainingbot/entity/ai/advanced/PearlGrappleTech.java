package com.pvptrainingbot.entity.ai.advanced;

import com.pvptrainingbot.config.BotConfig;
import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;

public class PearlGrappleTech {

    public static void tick(PvpBotEntity bot, LivingEntity target) {
        if (!bot.isTechEnabled("pearl_grapple") || target == null) {
            return;
        }

        double verticalDiff = target.getY() - bot.getY();
        double distanceSq = bot.squaredDistanceTo(target);
        boolean drill = bot.getActiveDrillTech().equalsIgnoreCase("pearl_grapple");

        if ((drill || (verticalDiff > 5.0D && distanceSq < 256.0D)) && bot.getPearlCooldown() <= 0) {
            boolean shouldGrapple = drill || bot.getRandom().nextFloat() < 0.20F;
            if (shouldGrapple) {
                executePearlGrapple(bot, target);
                if (drill) {
                    bot.setActiveDrillTech("");
                }
            }
        }
    }

    public static void executePearlGrapple(PvpBotEntity bot, LivingEntity target) {
        // 1. Throw pearl directly at/slightly above airborne target
        bot.equipHotbarSlot(6); // Pearl slot
        bot.getWorld().playSound(null, bot.getX(), bot.getY(), bot.getZ(), SoundEvents.ENTITY_ENDER_PEARL_THROW, bot.getSoundCategory(), 1.0F, 1.0F);

        // Simulate instant mid-air grapple teleport right beside target
        bot.requestTeleport(target.getX(), target.getY() + 0.5D, target.getZ());
        bot.setPearlCooldown(BotConfig.getCooldownMultiplierTicks(bot.getBotDifficulty(), 100));

        // 2. Immediately swing with optimal aerial Mace (Slot 0: Density + WB 1) to pop even higher!
        bot.equipHotbarSlot(0);
        bot.tryAttack(target);
        
        bot.setVelocity(0.0D, 0.95D, 0.0D);
        bot.velocityDirty = true;

        if (bot.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.PORTAL, target.getX(), target.getY(), target.getZ(), 15, 0.3, 0.5, 0.3, 0.1);
        }
    }
}
