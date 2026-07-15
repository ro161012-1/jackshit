package com.pvptrainingbot.entity.ai.advanced;

import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class BreachSwappingTech {

    public static void tick(PvpBotEntity bot, LivingEntity target) {
        if (!bot.isTechEnabled("breach_swapping") || target == null) {
            return;
        }

        float distance = bot.distanceTo(target);

        // When grounded/1v1 sword combat and opponent has armor (> 0)
        if (bot.isOnGround() && distance <= 3.8F && target.getArmor() > 0 && bot.getSwordAttackCooldown() <= 0) {
            boolean shouldBreachSwap = bot.getRandom().nextFloat() < 0.30F || bot.getActiveDrillTech().equalsIgnoreCase("breach_swapping");
            if (shouldBreachSwap) {
                executeBreach4Swap(bot, target);
                if (bot.getActiveDrillTech().equalsIgnoreCase("breach_swapping")) {
                    bot.setActiveDrillTech("");
                }
            }
        }
    }

    public static void executeBreach4Swap(PvpBotEntity bot, LivingEntity target) {
        // 1. Swing with Netherite Sword (Slot 2) for fast swing and crit check
        bot.equipHotbarSlot(2);
        bot.swingHand(Hand.MAIN_HAND);
        bot.tryAttack(target);
        bot.setSwordAttackCooldown(12); // 0.625s cooldown

        // 2. Exact 1-tick window: Attribute Swap to Breach IV (Breach 4 is MAX!) Mace (Slot 1)
        bot.equipHotbarSlot(1);
        bot.setAttributeSwapped(true);
        bot.setAttributeSwapTickTimer(3);
    }
}
