package com.pvptrainingbot.entity.ai.advanced;

import com.pvptrainingbot.config.BotConfig;
import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class FishingRodGrappleTech {

    public static void tick(PvpBotEntity bot, LivingEntity target) {
        if (!bot.isTechEnabled("rod_grapple") || target == null) {
            return;
        }

        float distance = bot.distanceTo(target);
        boolean drill = bot.getActiveDrillTech().equalsIgnoreCase("rod_grapple");

        if ((drill || (distance > 5.5F && distance < 14.0F && bot.getRandom().nextFloat() < 0.12F)) && bot.isOnGround() && bot.getMaceAttackCooldown() <= 0) {
            executeRodGrapple(bot, target);
            if (drill) {
                bot.setActiveDrillTech("");
            }
        }
    }

    public static void executeRodGrapple(PvpBotEntity bot, LivingEntity target) {
        // 1. Temporarily equip Fishing Rod in offhand to cast hook
        bot.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.FISHING_ROD));
        bot.swingHand(Hand.OFF_HAND);
        bot.getWorld().playSound(null, bot.getX(), bot.getY(), bot.getZ(), SoundEvents.ENTITY_FISHING_BOBBER_THROW, bot.getSoundCategory(), 1.0F, 1.0F);

        // 2. Grapple Pull: hook pulls target rapidly towards bot mid-air
        Vec3d pullDir = bot.getPos().subtract(target.getPos()).normalize();
        target.addVelocity(pullDir.x * 1.15D, 0.45D, pullDir.z * 1.15D);
        target.velocityDirty = true;

        bot.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE, bot.getSoundCategory(), 1.0F, 0.9F);
        if (bot.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.FISHING, target.getX(), target.getY() + 1.0D, target.getZ(), 15, 0.3, 0.3, 0.3, 0.1);
        }

        // 3. Launch bot forward into the grappled target with optimal mace
        Vec3d botLeap = target.getPos().subtract(bot.getPos()).normalize();
        bot.addVelocity(botLeap.x * 0.65D, 0.4D, botLeap.z * 0.65D);
        bot.velocityDirty = true;
        bot.equipHotbarSlot(bot.getOptimalMaceSlot(target));

        // Restore offhand to Totem
        bot.clearActiveItem();
        bot.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));

        if (!bot.getWorld().isClient && target instanceof net.minecraft.entity.player.PlayerEntity p) {
            p.sendMessage(Text.literal("§b[PvP Bot] Fishing Rod Grapple! Pulled right into Mace Smash range!"), true);
        }
    }
}
