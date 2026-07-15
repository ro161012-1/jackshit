package com.pvptrainingbot.entity.ai.master;

import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public class ShieldDrainingTech {

    public static void tick(PvpBotEntity bot, LivingEntity target) {
        if (!bot.isTechEnabled("shield_draining") || target == null) {
            return;
        }

        float distance = bot.distanceTo(target);

        // If target is holding shield and bot is close and in God/Insane difficulty or drill active
        if (target instanceof PlayerEntity player && player.isUsingItem() && player.getActiveItem().isOf(Items.SHIELD)) {
            if (distance <= 3.6F && (bot.getBotDifficulty().equalsIgnoreCase("god") || bot.getBotDifficulty().equalsIgnoreCase("insane") || bot.getActiveDrillTech().equalsIgnoreCase("shield_draining"))) {
                executeShieldDrain(bot, player);
            }
        }
    }

    public static void executeShieldDrain(PvpBotEntity bot, PlayerEntity targetPlayer) {
        // Simulate drag clicking / 30 CPS rapid multi-hit durability drain!
        ItemStack shield = targetPlayer.getActiveItem();
        if (shield != null && shield.isOf(Items.SHIELD)) {
            // Drain 15-25 durability per tick to simulate extreme CPS drag clicking
            int drainAmount = 20;
            shield.damage(drainAmount, targetPlayer, EquipmentSlot.OFFHAND);

            bot.swingHand(Hand.MAIN_HAND);
            bot.getWorld().playSound(null, targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(), SoundEvents.ITEM_SHIELD_BLOCK, bot.getSoundCategory(), 0.8F, 1.4F);

            if (shield.isEmpty() || shield.getDamage() >= shield.getMaxDamage()) {
                // Shield shattered completely!
                targetPlayer.disableShield(true);
                bot.getWorld().playSound(null, targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(), SoundEvents.ITEM_SHIELD_BREAK, bot.getSoundCategory(), 1.0F, 1.0F);
                if (!bot.getWorld().isClient) {
                    targetPlayer.sendMessage(Text.literal("§c[PvP Bot] Shield Drained & Shattered via Drag Clicking!"), true);
                }
                if (bot.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.ITEM_SLIME, targetPlayer.getX(), targetPlayer.getY() + 1.0D, targetPlayer.getZ(), 20, 0.3, 0.3, 0.3, 0.1);
                }
            }
        }
    }
}
