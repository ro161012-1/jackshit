package com.pvptrainingbot.config;

import java.util.HashSet;
import java.util.Set;

public class BotConfig {
    public static final String DEFAULT_MODE = "mace";
    public static final String DEFAULT_DIFFICULTY = "god";
    public static final String DEFAULT_BEHAVIOR = "aggressive";

    public static final int MAX_BREACH_LEVEL = 4; // Breach IV (Breach 4) is the maximum in 1.21

    // List of all 21 techniques (20 from video transcript + Fishing Rod Grapple)
    public static final Set<String> ALL_TECHNIQUES = Set.of(
        // Beginner (3)
        "beginner_windcharge",
        "beginner_shield",
        "beginner_elytra_boost",
        // Intermediate (4)
        "pearl_catching",
        "elytra_macing",
        "windcharge_cancel",
        "lunge_swapping",
        // Advanced (7)
        "stun_slamming",
        "sword_attribute_swap",
        "backstabbing",
        "breach_swapping", // Uses Breach IV Mace
        "pearl_grapple",   // Pearl Grapple / Far Pearl
        "spear_double_pop",
        "rod_grapple",     // Fishing Rod Grapple
        // Master (5)
        "elytra_stun_slam",
        "diagonal_pearl_catch",
        "shield_draining",
        "mace_dtap",
        "elytra_preservation",
        // Bonus (2)
        "rocket_macing",
        "wall_pearling"
    );

    public static int getReactionDelayTicks(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "easy" -> 16;
            case "medium" -> 8;
            case "hard" -> 3;
            case "god", "insane" -> 0; // Frame-1 / 0-tick reaction
            default -> 4;
        };
    }

    public static float getTechAccuracy(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "easy" -> 0.55f;
            case "medium" -> 0.78f;
            case "hard" -> 0.94f;
            case "god", "insane" -> 1.00f; // 100% precision
            default -> 0.90f;
        };
    }

    public static int getCooldownMultiplierTicks(String difficulty, int baseCooldown) {
        float factor = switch (difficulty.toLowerCase()) {
            case "easy" -> 2.0f;
            case "medium" -> 1.4f;
            case "hard" -> 1.0f;
            case "god", "insane" -> 0.6f;
            default -> 1.0f;
        };
        return Math.max(1, Math.round(baseCooldown * factor));
    }
}
