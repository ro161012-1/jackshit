package com.pvptrainingbot.entity.ai.advanced;

import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;

public class SpearDoublePopTech {

    public static void tick(PvpBotEntity bot, LivingEntity target) {
        if (!bot.isTechEnabled("spear_double_pop") || target == null) {
            return;
        }

        float fall = bot.fallDistance;
        double vy = bot.getVelocity().y;
        float distance = bot.distanceTo(target);

        // If falling down rapidly right above target
        if ((fall > 2.0F || vy < -0.35D) && distance < 6.0F) {
            // 1. Equip Spear in offhand to charge high relative motion damage
            if (!bot.getOffHandStack().isOf(Items.TRIDENT)) {
                bot.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TRIDENT));
                bot.setCurrentHand(Hand.OFF_HAND); // Hold right click to charge
            }

            // 2. Right as we enter striking distance (< 4.2F), execute Double Pop!
            if (distance <= 4.2F) {
                executeSpearDoublePop(bot, target);
            }
        } else if (bot.isOnGround() && bot.getOffHandStack().isOf(Items.TRIDENT)) {
            bot.clearActiveItem();
            bot.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
        }
    }

    public static void executeSpearDoublePop(PvpBotEntity bot, LivingEntity target) {
        // 1. Calculate relative motion spear impact damage (falling fast = +12 to +18 dmg)
        double relativeSpeed = Math.abs(bot.getVelocity().y - target.getVelocity().y);
        float spearDamage = (float) (10.0D + relativeSpeed * 12.0D);
        target.damage(bot.getDamageSources().mobAttack(bot), spearDamage);
        bot.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ITEM_TRIDENT_HIT, bot.getSoundCategory(), 1.0F, 0.8F);

        // 2. Simultaneously left-click with optimal mace (Density + WB 1 if high up, Breach IV if lower on ground)
        bot.equipHotbarSlot(bot.getOptimalMaceSlot(target));
        bot.swingHand(Hand.MAIN_HAND);
        bot.tryAttack(target);

        bot.clearActiveItem();
        bot.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));

        if (bot.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 15, 0.3, 0.4, 0.3, 0.2);
        }
    }
}
