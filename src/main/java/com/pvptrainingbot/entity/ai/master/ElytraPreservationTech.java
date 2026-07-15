package com.pvptrainingbot.entity.ai.master;

import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.entity.LivingEntity;

public class ElytraPreservationTech {

    public static void tick(PvpBotEntity bot, LivingEntity target) {
        if (!bot.isTechEnabled("elytra_preservation") || target == null) {
            return;
        }

        // If bot is currently gliding in the air
        if (bot.isElytraGliding() && !bot.isOnGround() && bot.getVelocity().y < 0.2D) {
            int currentTick = bot.getElytraPreservationTick() + 1;
            bot.setElytraPreservationTick(currentTick);

            // Elytra durability ticks down once every 1 second (20 ticks).
            // Unequip just before 20 ticks (e.g. at tick 16) and re-equip to reset durability timer!
            if (currentTick == 16) {
                bot.toggleHotbarElytra(false); // Unequip (reset flight timer)
            } else if (currentTick >= 17) {
                bot.toggleHotbarElytra(true);  // Instantly re-equip and resume glide
                bot.setElytraPreservationTick(0);
            }
        } else {
            bot.setElytraPreservationTick(0);
        }
    }
}
