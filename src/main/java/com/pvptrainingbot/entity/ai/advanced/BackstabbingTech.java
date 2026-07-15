package com.pvptrainingbot.entity.ai.advanced;

import com.pvptrainingbot.entity.PvpBotEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class BackstabbingTech {

    public static void tick(PvpBotEntity bot, LivingEntity target) {
        if (!bot.isTechEnabled("backstabbing") || target == null) {
            return;
        }

        float distance = bot.distanceTo(target);

        // Check if opponent is blocking with a shield
        if (target instanceof PlayerEntity player && player.isUsingItem() && player.getActiveItem().isOf(Items.SHIELD)) {
            if (distance < 6.0F && distance > 2.0F && bot.getWindChargeCooldown() <= 0) {
                boolean shouldBackstep = bot.getRandom().nextFloat() < 0.20F || bot.getActiveDrillTech().equalsIgnoreCase("backstabbing");
                if (shouldBackstep) {
                    executeGroundBackstab(bot, target);
                    if (bot.getActiveDrillTech().equalsIgnoreCase("backstabbing")) {
                        bot.setActiveDrillTech("");
                    }
                }
            } else if (bot.isElytraGliding() && distance < 5.0F) {
                executeElytraBackstab(bot, target);
            }
        }
    }

    public static void executeGroundBackstab(PvpBotEntity bot, LivingEntity target) {
        Vec3d targetLook = target.getRotationVector().normalize();
        Vec3d behindPos = target.getPos().subtract(targetLook.multiply(1.5D));
        Vec3d jumpDir = behindPos.subtract(bot.getPos()).normalize();

        bot.jump();
        bot.addVelocity(jumpDir.x * 1.3D, 0.45D, jumpDir.z * 1.3D);
        bot.velocityDirty = true;
        
        bot.getLookControl().lookAt(target.getX(), target.getY() + 0.2D, target.getZ(), 60.0F, 60.0F);
    }

    public static void executeElytraBackstab(PvpBotEntity bot, LivingEntity target) {
        bot.toggleHotbarElytra(false); // Unequip elytra
        // Select optimal mace (Density + WB 1 if high, Breach IV if low on ground)
        bot.equipHotbarSlot(bot.getOptimalMaceSlot(target));
        
        Vec3d targetLook = target.getRotationVector().normalize();
        bot.setPosition(target.getX() - targetLook.x * 1.2D, target.getY() + 0.5D, target.getZ() - targetLook.z * 1.2D);
        bot.getLookControl().lookAt(target, 90.0F, 90.0F);
    }
}
