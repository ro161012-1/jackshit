package com.pvptrainingbot.entity.ai.intermediate;

import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class LungeSwappingTech {

    public static void tick(PvpBotEntity bot, LivingEntity target) {
        if (!bot.isTechEnabled("lunge_swapping") || target == null) {
            return;
        }

        float distance = bot.distanceTo(target);

        // If target is far away on the ground or midair and bot wants to close the gap instantly
        if (distance > 7.0F && distance < 18.0F && bot.isOnGround() && bot.getMaceAttackCooldown() <= 0) {
            boolean shouldLunge = bot.getRandom().nextFloat() < 0.10F || bot.getActiveDrillTech().equalsIgnoreCase("lunge_swapping");
            if (shouldLunge) {
                executeLungeSwap(bot, target);
                if (bot.getActiveDrillTech().equalsIgnoreCase("lunge_swapping")) {
                    bot.setActiveDrillTech("");
                }
            }
        }
    }

    public static void executeLungeSwap(PvpBotEntity bot, LivingEntity target) {
        // 1. Equip short cooldown item (Wind Charge slot 5) on tick 0
        bot.equipHotbarSlot(5);
        // 2. Exact same tick: switch to Lunge Spear (slot 4) and click to inherit short cooldown + Lunge attribute!
        bot.equipHotbarSlot(4);
        bot.setAttributeSwapped(true);
        bot.setAttributeSwapTickTimer(3);

        // Apply Lunge forward dash
        Vec3d dir = new Vec3d(target.getX() - bot.getX(), (target.getY() - bot.getY()) * 0.2D, target.getZ() - bot.getZ()).normalize();
        bot.addVelocity(dir.x * 1.35D, 0.35D, dir.z * 1.35D);
        bot.velocityDirty = true;

        bot.getWorld().playSound(null, bot.getX(), bot.getY(), bot.getZ(), SoundEvents.ITEM_TRIDENT_RIPTIDE_1.value(), bot.getSoundCategory(), 1.0F, 1.2F);
        if (bot.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.CLOUD, bot.getX(), bot.getY() + 0.5D, bot.getZ(), 8, 0.3, 0.2, 0.3, 0.1);
        }
    }
}
