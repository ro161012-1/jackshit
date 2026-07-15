package com.pvptrainingbot.entity.ai.advanced;

import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class StunSlammingTech {

    public static void tick(PvpBotEntity bot, LivingEntity target) {
        if (!bot.isTechEnabled("stun_slamming") || target == null) {
            return;
        }

        float distance = bot.distanceTo(target);

        // 1. Check if target is holding Shield and blocking
        if (target instanceof PlayerEntity player && player.isUsingItem() && player.getActiveItem().isOf(Items.SHIELD)) {
            if (distance <= 4.2F && !bot.getMainHandStack().isOf(Items.NETHERITE_AXE)) {
                // Optimal 2-tick stun slam transition: switch to Shield Stun Axe (Slot 3)
                bot.equipHotbarSlot(3);
                bot.swingHand(Hand.MAIN_HAND);
                bot.tryAttack(target);
                
                // Immediately 1 tick later attribute-swap to optimal mace (Density + WB 1 if high up, Breach IV if low/grounded)
                int optimalSlot = bot.getOptimalMaceSlot(target);
                bot.equipHotbarSlot(optimalSlot);
                bot.setAttributeSwapped(true);
                bot.setAttributeSwapTickTimer(2);
            }
        }
    }
}
