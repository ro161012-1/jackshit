package com.pvptrainingbot.entity.ai.bonus;

import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class RocketMacingTech {

    public static void tick(PvpBotEntity bot, LivingEntity target) {
        if (!bot.isTechEnabled("rocket_macing") || target == null) {
            return;
        }

        float distance = bot.distanceTo(target);

        // If bot is on ground or mid-air and wants to execute high-speed Rocket Macing
        if (bot.isOnGround() && bot.getMaceAttackCooldown() <= 0 && distance > 8.0F && distance < 32.0F) {
            boolean shouldRocket = bot.getRandom().nextFloat() < 0.12F || bot.getActiveDrillTech().equalsIgnoreCase("rocket_macing");
            if (shouldRocket) {
                // Cycle through Type 1 to Type 4 Rocket Macing
                int type = bot.getRocketType();
                executeRocketMacing(bot, target, type);
                bot.setRocketType(type >= 4 ? 1 : type + 1);
                if (bot.getActiveDrillTech().equalsIgnoreCase("rocket_macing")) {
                    bot.setActiveDrillTech("");
                }
            }
        }
    }

    public static void executeRocketMacing(PvpBotEntity bot, LivingEntity target, int type) {
        // 1. Equip Elytra (Slot 7) and Duration 3 Rocket (Slot 8)
        bot.toggleHotbarElytra(true);
        bot.equipHotbarSlot(8); // Flight Duration 3 Rockets
        bot.setRocketBoostTimer(25);

        Vec3d toTarget = target.getPos().subtract(bot.getPos()).normalize();

        switch (type) {
            case 1 -> {
                // Type 1: Basic Rocket Drop -> fly up -> unequip -> drop straight down
                bot.setVelocity(0.0D, 1.6D, 0.0D);
            }
            case 2 -> {
                // Type 2: Turnaround Rocket Boost -> rocket up -> instantly turn 180 towards opponent -> unequip and strike
                bot.setVelocity(toTarget.x * 1.5D, 1.2D, toTarget.z * 1.5D);
            }
            case 3 -> {
                // Type 3: Unequip Turnaround Boost -> unequip elytra while turning to eliminate horizontal drag, then re-equip
                bot.toggleHotbarElytra(false);
                bot.getLookControl().lookAt(target, 90.0F, 90.0F);
                bot.toggleHotbarElytra(true);
                bot.setVelocity(toTarget.x * 1.6D, 1.1D, toTarget.z * 1.6D);
            }
            case 4 -> {
                // Type 4: Continuous Wind Burst 3 Rocket Pogo -> high speed rocket dive straight into Wind Burst III pogo
                bot.setVelocity(toTarget.x * 1.8D, 0.6D, toTarget.z * 1.8D);
            }
        }
        bot.velocityDirty = true;
        bot.equipHotbarSlot(0); // Switch right back to Wind Burst III Mace to prepare impact!

        bot.getWorld().playSound(null, bot.getX(), bot.getY(), bot.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, bot.getSoundCategory(), 1.0F, 1.0F);
        if (bot.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.FIREWORK, bot.getX(), bot.getY() + 1.0D, bot.getZ(), 15, 0.3, 0.3, 0.3, 0.1);
        }
    }
}
