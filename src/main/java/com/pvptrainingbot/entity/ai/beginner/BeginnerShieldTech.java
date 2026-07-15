package com.pvptrainingbot.entity.ai.beginner;

import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class BeginnerShieldTech {

    public static void tick(PvpBotEntity bot, LivingEntity target) {
        if (!bot.isTechEnabled("beginner_shield") || target == null) {
            return;
        }

        float distance = bot.distanceTo(target);

        // If target is dive bombing or if bot is close and has low/medium health (< 12.0 HP = 6.0 hearts)
        boolean targetIsDiveBombing = target.fallDistance > 2.0F || target.getVelocity().y < -0.3D;
        if ((targetIsDiveBombing || (distance < 4.5F && bot.getHealth() < 12.0F)) && bot.isOnGround() && bot.getShieldStunTimer() <= 0) {
            if (bot.getOffHandStack().isOf(Items.SHIELD) || bot.getMainHandStack().isOf(Items.SHIELD)) {
                bot.setCurrentHand(bot.getOffHandStack().isOf(Items.SHIELD) ? Hand.OFF_HAND : Hand.MAIN_HAND);
                bot.getLookControl().lookAt(target, 45.0F, 45.0F);
            }
        } else if (bot.isUsingItem() && bot.getActiveItem().isOf(Items.SHIELD) && distance > 6.0F && !targetIsDiveBombing) {
            bot.clearActiveItem();
        }
    }
}
