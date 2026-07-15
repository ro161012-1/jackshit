package com.pvptrainingbot.entity;

import com.pvptrainingbot.config.BotConfig;
import com.pvptrainingbot.entity.ai.MacePvpGoal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class PvpBotEntity extends HostileEntity {
    public static final TrackedData<String> BOT_USERNAME = DataTracker.registerData(PvpBotEntity.class, TrackedDataHandlerRegistry.STRING);
    public static final TrackedData<String> BOT_MODE = DataTracker.registerData(PvpBotEntity.class, TrackedDataHandlerRegistry.STRING);
    public static final TrackedData<String> BOT_DIFFICULTY = DataTracker.registerData(PvpBotEntity.class, TrackedDataHandlerRegistry.STRING);
    public static final TrackedData<String> BOT_BEHAVIOR = DataTracker.registerData(PvpBotEntity.class, TrackedDataHandlerRegistry.STRING);
    public static final TrackedData<String> TARGET_PLAYER_NAME = DataTracker.registerData(PvpBotEntity.class, TrackedDataHandlerRegistry.STRING);
    public static final TrackedData<Integer> TECH_MASK = DataTracker.registerData(PvpBotEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public static final List<String> TECH_LIST = List.of(
        "beginner_windcharge", "beginner_shield", "beginner_elytra_boost",
        "pearl_catching", "elytra_macing", "windcharge_cancel", "lunge_swapping",
        "stun_slamming", "sword_attribute_swap", "backstabbing", "breach_swapping", "pearl_grapple", "spear_double_pop",
        "elytra_stun_slam", "diagonal_pearl_catch", "shield_draining", "mace_dtap", "elytra_preservation",
        "rocket_macing", "wall_pearling"
    );

    private final ItemStack[] hotbarItems = new ItemStack[9];
    private int maceAttackCooldown = 0;      // 1.67s = 33 ticks
    private int swordAttackCooldown = 0;     // 0.625s = 12 ticks
    private int windChargeCooldown = 0;
    private int pearlCooldown = 0;
    private int shieldStunTimer = 0;
    private int attributeSwapTickTimer = 0;
    private int previousMainhandSlot = 0;
    private int comboCounter = 0;
    private boolean isElytraGliding = false;
    private int elytraPreservationTick = 0;  // Tech 18: 1s interval check
    private boolean isAttributeSwapped = false;
    private int rocketBoostTimer = 0;
    private int rocketType = 1;
    private String activeDrillTech = "";
    private LivingEntity assignedTarget = null;

    public PvpBotEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.setCanPickUpLoot(false);
        this.equipCompetitiveMaceKit();
    }

    public static DefaultAttributeContainer.Builder createBotAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0D) // Exactly 20.0 HP = 10 full hearts (20 half-hearts), standard vanilla player health!
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.36D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7.0D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0D)
                .add(EntityAttributes.GENERIC_ARMOR, 16.0D)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 8.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.25D);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(BOT_USERNAME, "Technoblade");
        builder.add(BOT_MODE, BotConfig.DEFAULT_MODE);
        builder.add(BOT_DIFFICULTY, BotConfig.DEFAULT_DIFFICULTY);
        builder.add(BOT_BEHAVIOR, BotConfig.DEFAULT_BEHAVIOR);
        builder.add(TARGET_PLAYER_NAME, "");
        builder.add(TECH_MASK, 0xFFFFF); // All 20 techniques enabled by default
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new MacePvpGoal(this));
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 32.0f));
        this.goalSelector.add(3, new LookAroundGoal(this));
    }

    public void equipCompetitiveMaceKit() {
        // Slot 0: Density V + Wind Burst I Mace (Dedicated to High-Altitude / Aerial Smash attacks)
        ItemStack aerialMace = new ItemStack(Items.MACE);
        aerialMace.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME, Text.literal("§d§lDensity V + Wind Burst I Mace"));
        hotbarItems[0] = aerialMace;

        // Slot 1: Breach IV Mace without Wind Burst (Dedicated to Grounded / Low-Altitude Breach Swapping)
        ItemStack breachMace = new ItemStack(Items.MACE);
        breachMace.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME, Text.literal("§5§lBreach IV Mace (No Wind Burst)"));
        hotbarItems[1] = breachMace;

        // Slot 2: Netherite Sword (for Sword Attribute Swapping & crits)
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME, Text.literal("§e§lAttribute Swap Sword"));
        hotbarItems[2] = sword;

        // Slot 3: Shield Stun Axe
        ItemStack axe = new ItemStack(Items.NETHERITE_AXE);
        axe.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME, Text.literal("§c§lShield Stun Axe"));
        hotbarItems[3] = axe;

        // Slot 4: Lunge Spear (Trident/Spear with Lunge attribute)
        ItemStack spear = new ItemStack(Items.TRIDENT);
        spear.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME, Text.literal("§b§lLunge Spear"));
        hotbarItems[4] = spear;

        // Slot 5: Wind Charges
        ItemStack windCharges = new ItemStack(Items.WIND_CHARGE, 64);
        hotbarItems[5] = windCharges;

        // Slot 6: Ender Pearls (For Ender Pearl Grapple, Pearl Catching, etc.)
        hotbarItems[6] = new ItemStack(Items.ENDER_PEARL, 16);

        // Slot 7: Elytra (for hotbar chestplate swapping / dive bombing)
        ItemStack elytra = new ItemStack(Items.ELYTRA);
        hotbarItems[7] = elytra;

        // Slot 8: Flight Duration 3 Rockets
        ItemStack rockets = new ItemStack(Items.FIREWORK_ROCKET, 64);
        hotbarItems[8] = rockets;

        // Equip default: Slot 0 (Aerial Mace) and Offhand Totem
        this.equipHotbarSlot(0);
        this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));

        // Equip Full Netherite Armor
        this.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
        this.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
        this.equipStack(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
        this.equipStack(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
    }

    public void equipHotbarSlot(int slot) {
        if (slot >= 0 && slot < hotbarItems.length && hotbarItems[slot] != null) {
            if (slot != 0 && slot != 1) {
                this.previousMainhandSlot = slot;
            }
            this.equipStack(EquipmentSlot.MAINHAND, hotbarItems[slot].copy());
            if (!this.getWorld().isClient) {
                this.playSound(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC.value(), 0.7F, 1.2F);
            }
        }
    }

    /**
     * Smart Weapon Selection Rule:
     * Use Density + Wind Burst I (Slot 0) when VERY HIGH or dive bombing (fallDistance > 3.0F or rapid descent / rocket macing).
     * Use Breach IV without Wind Burst (Slot 1) when LOWER OR ON GROUND (fallDistance <= 3.0F / grounded / 1v1 sword crits).
     */
    public int getOptimalMaceSlot(LivingEntity target) {
        if (this.fallDistance > 3.0F || this.getVelocity().y < -0.35D || this.isElytraGliding() || this.getRocketBoostTimer() > 0) {
            return 0; // Slot 0: Density V + Wind Burst I Mace
        }
        return 1; // Slot 1: Breach IV Mace without Wind Burst
    }

    public void toggleHotbarElytra(boolean enableGliding) {
        if (enableGliding) {
            this.equipStack(EquipmentSlot.CHEST, hotbarItems[7].copy());
            this.isElytraGliding = true;
        } else {
            this.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
            this.isElytraGliding = false;
        }
    }

    public void updateNametag() {
        String username = getBotUsername();
        if (username == null || username.trim().isEmpty()) {
            username = "Bot";
        }
        float currentHp = Math.max(0.0F, this.getHealth());
        float maxHp = this.getMaxHealth();

        String hpColor = "§a";
        if (currentHp < maxHp * 0.25F) {
            hpColor = "§c";
        } else if (currentHp < maxHp * 0.50F) {
            hpColor = "§e";
        }

        float currentHearts = currentHp / 2.0F;
        float maxHearts = maxHp / 2.0F;

        // Displays exact HP (20.0) AND exact hearts (10.0) clearly above the head
        String formattedHp = String.format("%.1f", currentHp) + "/" + String.format("%.1f", maxHp) + " HP (" + String.format("%.1f", currentHearts) + "/" + String.format("%.1f", maxHearts) + " ❤)";
        String modeInfo = getBotMode() != null ? getBotMode().toUpperCase() : "MACE";

        this.setCustomName(Text.literal("§b[PvP Bot] §f" + username + " " + hpColor + formattedHp + " §7[" + modeInfo + "]"));
        this.setCustomNameVisible(true);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean damaged = super.damage(source, amount);
        if (damaged && !this.getWorld().isClient) {
            this.updateNametag();
        }
        return damaged;
    }

    @Override
    public void setHealth(float health) {
        super.setHealth(health);
        if (!this.getWorld().isClient) {
            this.updateNametag();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (maceAttackCooldown > 0) maceAttackCooldown--;
        if (swordAttackCooldown > 0) swordAttackCooldown--;
        if (windChargeCooldown > 0) windChargeCooldown--;
        if (pearlCooldown > 0) pearlCooldown--;
        if (shieldStunTimer > 0) shieldStunTimer--;
        if (rocketBoostTimer > 0) rocketBoostTimer--;

        if (attributeSwapTickTimer > 0) {
            attributeSwapTickTimer--;
            if (attributeSwapTickTimer == 0 && isAttributeSwapped) {
                equipHotbarSlot(previousMainhandSlot);
                isAttributeSwapped = false;
            }
        }

        if (isOnGround() && isElytraGliding) {
            toggleHotbarElytra(false);
        }

        if (!this.getWorld().isClient && this.age % 5 == 0) {
            this.updateNametag();
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean attacked = super.tryAttack(target);
        if (target instanceof LivingEntity livingTarget) {
            this.handleMaceCombatCalculations(livingTarget);
        }
        return attacked;
    }

    public void handleMaceCombatCalculations(LivingEntity target) {
        ItemStack held = this.getMainHandStack();

        // 1. Axe Shield Disable
        if (held.isOf(Items.NETHERITE_AXE)) {
            if (target instanceof PlayerEntity player && player.isUsingItem() && player.getActiveItem().isOf(Items.SHIELD)) {
                player.disableShield(true);
                this.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_SHIELD_BREAK, this.getSoundCategory(), 1.0F, 1.0F);
                this.shieldStunTimer = 100; // 5 seconds shield cooldown
                this.comboCounter++;
                if (!this.getWorld().isClient && target instanceof PlayerEntity p) {
                    p.sendMessage(Text.literal("§c[PvP Bot] Shield Stunned (5s)! Prepare for Stun Slam!"), true);
                }
            }
        }

        // 2. Mace Smash Attack (Slot 0: Density V + Wind Burst I vs Slot 1: Breach IV without Wind Burst)
        if (held.isOf(Items.MACE)) {
            float fall = this.fallDistance;
            boolean isBreach4NoWindBurst = held.getName().getString().contains("Breach IV");

            if (fall > 1.1F || this.getVelocity().y < -0.15D || isElytraGliding || rocketBoostTimer > 0 || isBreach4NoWindBurst) {
                // Base Smash Attack fall calculation
                float bonusDamage = 0.0F;
                if (fall > 1.5F) {
                    if (fall <= 3.0F) bonusDamage += (fall - 1.5F) * 4.0F;
                    else if (fall <= 8.0F) bonusDamage += (1.5F * 4.0F) + (fall - 3.0F) * 2.0F;
                    else bonusDamage += (1.5F * 4.0F) + (5.0F * 2.0F) + (fall - 8.0F) * 1.0F;
                }
                if (bonusDamage < 8.0F && (this.getVelocity().y < -0.25D || rocketBoostTimer > 0)) {
                    bonusDamage = 13.0F; // Ensure high impact on rocket/windcharge dive bombs
                }

                if (isBreach4NoWindBurst) {
                    // Breach IV (Breach 4 is MAX!) Armor Bypass Math: 15% * 4 = 60% armor ignored / converted
                    if (target.getArmor() > 0) {
                        float breach4Extra = target.getArmor() * 0.60F;
                        bonusDamage += breach4Extra;
                    }
                } else {
                    // Density V calculation (Slot 0): +0.5F per block fallen per level = +2.5F * fallDistance
                    if (fall > 1.0F) {
                        float densityBonus = fall * 2.5F;
                        bonusDamage += densityBonus;
                    }
                }

                target.damage(this.getDamageSources().mobAttack(this), bonusDamage);
                this.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ITEM_MACE_SMASH_HEAVY, this.getSoundCategory(), 1.0F, 0.9F);
                
                if (this.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.GUST_EMITTER_LARGE, target.getX(), target.getY() + 0.5D, target.getZ(), 2, 0.2, 0.2, 0.2, 0.05);
                }

                this.fallDistance = 0.0F;
                this.maceAttackCooldown = 33; // 1.67 seconds cooldown
                this.comboCounter++;

                if (isBreach4NoWindBurst) {
                    if (isAttributeSwapped) {
                        attributeSwapTickTimer = 4;
                    }
                    if (!this.getWorld().isClient && target instanceof PlayerEntity p) {
                        p.sendMessage(Text.literal("§5[PvP Bot] Breach IV Ground Smash! (+ §l" + String.format("%.1f", bonusDamage) + "§5 dmg, No Wind Burst)"), true);
                    }
                } else {
                    this.setVelocity(this.getVelocity().x * 0.45D, 0.60D, this.getVelocity().z * 0.45D);
                    this.velocityDirty = true;
                    if (isAttributeSwapped) {
                        attributeSwapTickTimer = 4;
                    }
                    if (!this.getWorld().isClient && target instanceof PlayerEntity p) {
                        p.sendMessage(Text.literal("§d[PvP Bot] Density V + Wind Burst I Aerial Smash! (+ §l" + String.format("%.1f", bonusDamage) + "§d dmg)"), true);
                    }
                }
            }
        }
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        if (this.getMainHandStack().isOf(Items.MACE) || isElytraGliding || rocketBoostTimer > 0) {
            return false;
        }
        return super.handleFallDamage(fallDistance, damageMultiplier, damageSource);
    }

    public boolean isTechEnabled(String techName) {
        int index = TECH_LIST.indexOf(techName.toLowerCase());
        if (index == -1) return true;
        return (getTechMask() & (1 << index)) != 0;
    }

    public void setTechEnabled(String techName, boolean enabled) {
        if (techName.equalsIgnoreCase("all")) {
            this.dataTracker.set(TECH_MASK, enabled ? 0xFFFFF : 0);
            return;
        }
        int index = TECH_LIST.indexOf(techName.toLowerCase());
        if (index != -1) {
            int mask = getTechMask();
            if (enabled) mask |= (1 << index);
            else mask &= ~(1 << index);
            this.dataTracker.set(TECH_MASK, mask);
        }
    }

    public int getTechMask() { return this.dataTracker.get(TECH_MASK); }
    public String getBotUsername() { return this.dataTracker.get(BOT_USERNAME); }
    public void setBotUsername(String name) {
        this.dataTracker.set(BOT_USERNAME, name);
        this.updateNametag();
    }
    public String getBotMode() { return this.dataTracker.get(BOT_MODE); }
    public void setBotMode(String mode) {
        this.dataTracker.set(BOT_MODE, mode);
        this.updateNametag();
    }
    public String getBotDifficulty() { return this.dataTracker.get(BOT_DIFFICULTY); }
    public void setBotDifficulty(String diff) { this.dataTracker.set(BOT_DIFFICULTY, diff); }
    public String getBotBehavior() { return this.dataTracker.get(BOT_BEHAVIOR); }
    public void setBotBehavior(String behavior) { this.dataTracker.set(BOT_BEHAVIOR, behavior); }
    public String getAssignedTargetName() { return this.dataTracker.get(TARGET_PLAYER_NAME); }
    public void setAssignedTargetName(String name) { this.dataTracker.set(TARGET_PLAYER_NAME, name); }
    public LivingEntity getAssignedTarget() { return this.assignedTarget; }
    public void setAssignedTarget(LivingEntity target) {
        this.assignedTarget = target;
        this.setTarget(target);
    }

    public int getMaceAttackCooldown() { return maceAttackCooldown; }
    public void setMaceAttackCooldown(int cd) { this.maceAttackCooldown = cd; }
    public int getSwordAttackCooldown() { return swordAttackCooldown; }
    public void setSwordAttackCooldown(int cd) { this.swordAttackCooldown = cd; }
    public int getWindChargeCooldown() { return windChargeCooldown; }
    public void setWindChargeCooldown(int cd) { this.windChargeCooldown = cd; }
    public int getPearlCooldown() { return pearlCooldown; }
    public void setPearlCooldown(int cd) { this.pearlCooldown = cd; }
    public int getShieldStunTimer() { return shieldStunTimer; }
    public void setShieldStunTimer(int timer) { this.shieldStunTimer = timer; }
    public boolean isElytraGliding() { return isElytraGliding; }
    public int getElytraPreservationTick() { return elytraPreservationTick; }
    public void setElytraPreservationTick(int tick) { this.elytraPreservationTick = tick; }
    public boolean isAttributeSwapped() { return isAttributeSwapped; }
    public void setAttributeSwapped(boolean swapped) { this.isAttributeSwapped = swapped; }
    public int getAttributeSwapTickTimer() { return attributeSwapTickTimer; }
    public void setAttributeSwapTickTimer(int timer) { this.attributeSwapTickTimer = timer; }
    public int getRocketBoostTimer() { return rocketBoostTimer; }
    public void setRocketBoostTimer(int timer) { this.rocketBoostTimer = timer; }
    public int getRocketType() { return rocketType; }
    public void setRocketType(int type) { this.rocketType = type; }
    public String getActiveDrillTech() { return activeDrillTech; }
    public void setActiveDrillTech(String drill) { this.activeDrillTech = drill; }
    public int getComboCounter() { return comboCounter; }
    public ItemStack getHotbarStack(int index) { return index >= 0 && index < hotbarItems.length ? hotbarItems[index] : ItemStack.EMPTY; }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("BotUsername", getBotUsername());
        nbt.putString("BotMode", getBotMode());
        nbt.putString("BotDifficulty", getBotDifficulty());
        nbt.putString("BotBehavior", getBotBehavior());
        nbt.putString("TargetPlayerName", getAssignedTargetName());
        nbt.putInt("TechMask", getTechMask());
        nbt.putInt("ComboCounter", comboCounter);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("BotUsername")) setBotUsername(nbt.getString("BotUsername"));
        if (nbt.contains("BotMode")) setBotMode(nbt.getString("BotMode"));
        if (nbt.contains("BotDifficulty")) setBotDifficulty(nbt.getString("BotDifficulty"));
        if (nbt.contains("BotBehavior")) setBotBehavior(nbt.getString("BotBehavior"));
        if (nbt.contains("TargetPlayerName")) setAssignedTargetName(nbt.getString("TargetPlayerName"));
        if (nbt.contains("TechMask")) this.dataTracker.set(TECH_MASK, nbt.getInt("TechMask"));
        if (nbt.contains("ComboCounter")) this.comboCounter = nbt.getInt("ComboCounter");
        this.updateNametag();
    }
}
