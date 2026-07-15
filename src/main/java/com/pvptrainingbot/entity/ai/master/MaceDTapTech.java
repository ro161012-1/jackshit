package com.pvptrainingbot.entity.ai.master;

import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;

public class MaceDTapTech {

    public static void tick(PvpBotEntity bot, LivingEntity target) {
        if (!bot.isTechEnabled("mace_dtap") || target == null) {
            return;
        }

        // Check if target is at low health (< 8.0 HP = 4.0 hearts) OR drill active
        boolean lowHp = target.getHealth() < 8.0F;
        boolean drill = bot.getActiveDrillTech().equalsIgnoreCase("mace_dtap");

        if ((lowHp || drill) && bot.isElytraGliding() && bot.getVelocity().y < -0.1D) {
            float distance = bot.distanceTo(target);
            if (distance <= 5.0F) {
                executeFirstTapTotemPop(bot, target);
                if (drill) {
                    bot.setActiveDrillTech("");
                }
            }
        }
    }

    public static void executeFirstTapTotemPop(PvpBotEntity bot, LivingEntity target) {
        bot.toggleHotbarElytra(false);
        int optimalSlot = bot.getOptimalMaceSlot(target);
        bot.equipHotbarSlot(optimalSlot);

        // Deliver hit to pop totem immediately
        target.damage(bot.getDamageSources().mobAttack(bot), 20.0F);
        bot.fallDistance = 0.0F;

        bot.setVelocity(0.0D, 0.85D, 0.0D);
        bot.velocityDirty = true;
        bot.toggleHotbarElytra(true);

        if (!bot.getWorld().isClient && target instanceof net.minecraft.entity.player.PlayerEntity p) {
            String maceUsed = optimalSlot == 0 ? "Density V + Wind Burst I" : "Breach IV (No Wind Burst)";
            p.sendMessage(Text.literal("§5[PvP Bot] D-Tap 1st Tap: " + maceUsed + " popped totem! Preparing 2nd Dive Bomb!"), true);
        }
    }
}
