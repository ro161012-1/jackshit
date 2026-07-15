package com.pvptrainingbot.entity.ai;

import com.pvptrainingbot.entity.PvpBotEntity;
import com.pvptrainingbot.entity.ai.beginner.*;
import com.pvptrainingbot.entity.ai.intermediate.*;
import com.pvptrainingbot.entity.ai.advanced.*;
import com.pvptrainingbot.entity.ai.master.*;
import com.pvptrainingbot.entity.ai.bonus.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;

import java.util.EnumSet;

public class MacePvpGoal extends Goal {
    private final PvpBotEntity bot;
    private int attackDelay = 0;

    public MacePvpGoal(PvpBotEntity bot) {
        this.bot = bot;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        LivingEntity target = bot.getTarget();
        if (target == null && bot.getAssignedTarget() != null && bot.getAssignedTarget().isAlive()) {
            bot.setTarget(bot.getAssignedTarget());
            target = bot.getTarget();
        }
        return target != null && target.isAlive() && bot.getBotMode().equalsIgnoreCase("mace");
    }

    @Override
    public boolean shouldContinue() {
        LivingEntity target = bot.getTarget();
        return target != null && target.isAlive() && bot.distanceTo(target) < 64.0F && bot.getBotMode().equalsIgnoreCase("mace");
    }

    @Override
    public void start() {
        this.attackDelay = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = bot.getTarget();
        if (target == null) return;

        bot.getLookControl().lookAt(target, 35.0F, 35.0F);
        float distance = bot.distanceTo(target);

        // Movement & Positioning Tactic
        String behavior = bot.getBotBehavior().toLowerCase();
        switch (behavior) {
            case "aggressive" -> {
                if (distance > 2.2F) {
                    bot.getNavigation().startMovingTo(target, 1.15D);
                } else {
                    bot.getNavigation().stop();
                }
            }
            case "tactical" -> {
                if (distance > 4.5F) {
                    bot.getNavigation().startMovingTo(target, 1.05D);
                } else if (distance < 2.4F && bot.isOnGround()) {
                    bot.getNavigation().stop();
                    bot.addVelocity((bot.getX() - target.getX()) * 0.15D, 0.0D, (bot.getZ() - target.getZ()) * 0.15D);
                }
            }
            case "kiting" -> {
                if (distance < 4.2F && bot.isOnGround()) {
                    bot.addVelocity((bot.getX() - target.getX()) * 0.25D, 0.1D, (bot.getZ() - target.getZ()) * 0.25D);
                } else if (distance > 8.0F) {
                    bot.getNavigation().startMovingTo(target, 1.10D);
                }
            }
            case "passive" -> bot.getNavigation().stop();
        }

        // --- Execute All 21 Techniques (Transcript + Fishing Rod Grapple) in Priority Sequence ---
        BeginnerShieldTech.tick(bot, target);
        ElytraPreservationTech.tick(bot, target);
        MaceDTapTech.tick(bot, target);
        ElytraStunSlamTech.tick(bot, target);
        ElytraMacingTech.tick(bot, target);
        StunSlammingTech.tick(bot, target);
        SpearDoublePopTech.tick(bot, target);
        ShieldDrainingTech.tick(bot, target);
        BreachSwappingTech.tick(bot, target);
        SwordAttributeSwapTech.tick(bot, target);
        BackstabbingTech.tick(bot, target);
        WindChargeCancelTech.tick(bot, target);
        LungeSwappingTech.tick(bot, target);
        PearlCatchingTech.tick(bot, target);
        DiagonalPearlCatchTech.tick(bot, target);
        PearlGrappleTech.tick(bot, target);
        FishingRodGrappleTech.tick(bot, target);
        WallPearlingTech.tick(bot, target);
        BeginnerElytraBoost.tick(bot, target);
        RocketMacingTech.tick(bot, target);
        BeginnerWindChargeTech.tick(bot, target);

        // Standard Attack Execution Check
        if (attackDelay > 0) attackDelay--;

        float attackRange = (bot.getMainHandStack().isOf(Items.MACE) && bot.fallDistance > 1.2F) ? 4.6F : 3.6F;
        if (distance <= attackRange && attackDelay <= 0 && bot.getMaceAttackCooldown() <= 0 && bot.getSwordAttackCooldown() <= 0) {
            if (bot.getMainHandStack().isOf(Items.MACE)) {
                int optimalSlot = bot.getOptimalMaceSlot(target);
                bot.equipHotbarSlot(optimalSlot);
            }
            bot.swingHand(Hand.MAIN_HAND);
            bot.tryAttack(target);
            bot.resetLastAttackedTicks();
            attackDelay = bot.getBotDifficulty().equalsIgnoreCase("god") ? 6 : 12;
        }

        // Emergency Self-Healing: Trigger when below 15.0 HP (7.5 hearts), heals 4.0 HP (2.0 hearts)
        if (bot.getHealth() < 15.0F && bot.age % 45 == 0) {
            bot.setHealth(Math.min(bot.getMaxHealth(), bot.getHealth() + 4.0F));
            if (!bot.getWorld().isClient) {
                bot.playSound(SoundEvents.ENTITY_GENERIC_EAT.value(), 0.8F, 1.1F);
            }
        }
    }
}
