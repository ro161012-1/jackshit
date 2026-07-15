package com.pvptrainingbot.entity.ai.advanced;

import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class SwordAttributeSwapTech {

    public static void tick(PvpBotEntity bot, LivingEntity target) {
        if (!bot.isTechEnabled("sword_attribute_swap") || target == null) {
            return;
        }

        float distance = bot.distanceTo(target);

        // If in close combat range and Mace is cooling down or wanting fast sword speed
        if (distance <= 3.8F && bot.getMaceAttackCooldown() > 0 && bot.getSwordAttackCooldown() <= 0) {
            boolean shouldSwap = bot.getRandom().nextFloat() < 0.25F || bot.getActiveDrillTech().equalsIgnoreCase("sword_attribute_swap");
            if (shouldSwap) {
                executeSwordToMaceSwap(bot, target);
                if (bot.getActiveDrillTech().equalsIgnoreCase("sword_attribute_swap")) {
                    bot.setActiveDrillTech("");
                }
            }
        }
    }

    public static void executeSwordToMaceSwap(PvpBotEntity bot, LivingEntity target) {
        // 1. Swing with Netherite Sword (Slot 2) for 0.625s cooldown + extra base damage
        bot.equipHotbarSlot(2);
        bot.swingHand(Hand.MAIN_HAND);
        bot.tryAttack(target);
        bot.setSwordAttackCooldown(12); // 0.625s cooldown

        // 2. 1 tick window: immediately attribute-swap to the optimal mace!
        // Use Density + Wind Burst 1 (Slot 0) when high up, or Breach IV without windburst (Slot 1) when low/grounded
        int optimalSlot = bot.getOptimalMaceSlot(target);
        bot.equipHotbarSlot(optimalSlot);
        bot.setAttributeSwapped(true);
        bot.setAttributeSwapTickTimer(3);
    }
}
