package com.pvptrainingbot.entity.ai.master;

import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class ElytraStunSlamTech {

    public static void tick(PvpBotEntity bot, LivingEntity target) {
        if (!bot.isTechEnabled("elytra_stun_slam") || target == null) {
            return;
        }

        // Check if dive bombing high speed and target is blocking with shield
        if (bot.isElytraGliding() && target instanceof PlayerEntity player && player.isUsingItem() && player.getActiveItem().isOf(Items.SHIELD)) {
            float distance = bot.distanceTo(target);
            if (distance <= 5.2F && bot.getVelocity().y < -0.15D) {
                executeElytraStunSlam(bot, target);
            }
        }
    }

    public static void executeElytraStunSlam(PvpBotEntity bot, LivingEntity target) {
        // 1. Unequip Elytra instantly mid-flight (switch to Netherite Chestplate)
        bot.toggleHotbarElytra(false);

        // 2. Switch to Shield Stun Axe (Slot 3) to shatter shield at dive speed
        bot.equipHotbarSlot(3);
        bot.swingHand(Hand.MAIN_HAND);
        bot.tryAttack(target);

        // 3. 1 tick later attribute-swap to Wind Burst III Mace (Slot 0) to land the aerial stun slam!
        bot.equipHotbarSlot(0);
        bot.setAttributeSwapped(true);
        bot.setAttributeSwapTickTimer(2);
    }
}
