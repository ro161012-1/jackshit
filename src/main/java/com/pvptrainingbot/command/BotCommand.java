package com.pvptrainingbot.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.pvptrainingbot.config.BotConfig;
import com.pvptrainingbot.entity.ModEntities;
import com.pvptrainingbot.entity.PvpBotEntity;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class BotCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register(BotCommand::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("bot")
            .executes(BotCommand::sendHelp)
            .then(CommandManager.literal("help").executes(BotCommand::sendHelp))
            .then(CommandManager.literal("spawn")
                .then(CommandManager.argument("username", StringArgumentType.word())
                    .executes(ctx -> spawnBot(ctx, StringArgumentType.getString(ctx, "username"), BotConfig.DEFAULT_MODE, BotConfig.DEFAULT_DIFFICULTY))
                    .then(CommandManager.argument("mode", StringArgumentType.word())
                        .executes(ctx -> spawnBot(ctx, StringArgumentType.getString(ctx, "username"), StringArgumentType.getString(ctx, "mode"), BotConfig.DEFAULT_DIFFICULTY))
                        .then(CommandManager.argument("difficulty", StringArgumentType.word())
                            .executes(ctx -> spawnBot(ctx, StringArgumentType.getString(ctx, "username"), StringArgumentType.getString(ctx, "mode"), StringArgumentType.getString(ctx, "difficulty")))
                        )
                    )
                )
            )
            .then(CommandManager.literal("mode")
                .then(CommandManager.argument("newMode", StringArgumentType.word())
                    .executes(ctx -> setMode(ctx, StringArgumentType.getString(ctx, "newMode")))
                )
            )
            .then(CommandManager.literal("difficulty")
                .then(CommandManager.argument("level", StringArgumentType.word())
                    .executes(ctx -> setDifficulty(ctx, StringArgumentType.getString(ctx, "level")))
                )
            )
            .then(CommandManager.literal("tech")
                .then(CommandManager.argument("techName", StringArgumentType.word())
                    .then(CommandManager.argument("action", StringArgumentType.word())
                        .executes(ctx -> setTech(ctx, StringArgumentType.getString(ctx, "techName"), StringArgumentType.getString(ctx, "action")))
                    )
                )
            )
            .then(CommandManager.literal("gear")
                .then(CommandManager.argument("kit", StringArgumentType.word())
                    .executes(ctx -> setGear(ctx, StringArgumentType.getString(ctx, "kit")))
                )
            )
            .then(CommandManager.literal("behavior")
                .then(CommandManager.argument("style", StringArgumentType.word())
                    .executes(ctx -> setBehavior(ctx, StringArgumentType.getString(ctx, "style")))
                )
            )
            .then(CommandManager.literal("target")
                .then(CommandManager.argument("player", StringArgumentType.word())
                    .executes(ctx -> setTarget(ctx, StringArgumentType.getString(ctx, "player")))
                )
            )
            .then(CommandManager.literal("status").executes(BotCommand::showStatus))
            .then(CommandManager.literal("listtech").executes(BotCommand::listAllTechniques))
            .then(CommandManager.literal("remove").executes(ctx -> removeBots(ctx, "all"))
                .then(CommandManager.argument("targetBot", StringArgumentType.word())
                    .executes(ctx -> removeBots(ctx, StringArgumentType.getString(ctx, "targetBot")))
                )
            )
            .then(CommandManager.literal("stop").executes(ctx -> removeBots(ctx, "all")))
            .then(CommandManager.literal("clear").executes(ctx -> removeBots(ctx, "all")))
            .then(CommandManager.literal("kill").executes(ctx -> removeBots(ctx, "all")))
            .then(CommandManager.argument("shortcutUsername", StringArgumentType.word())
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "shortcutUsername");
                    if (isReservedWord(name)) {
                        return sendHelp(ctx);
                    }
                    return spawnBot(ctx, name, BotConfig.DEFAULT_MODE, BotConfig.DEFAULT_DIFFICULTY);
                })
            )
        );
    }

    private static boolean isReservedWord(String word) {
        return List.of("help", "spawn", "mode", "difficulty", "tech", "gear", "behavior", "target", "status", "listtech", "remove", "stop", "clear", "kill").contains(word.toLowerCase());
    }

    private static int sendHelp(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        source.sendFeedback(() -> Text.literal("§6=== PvP Training Bot Commands (Dual-Mace & Grapple Edition) ===§r\n" +
                "§e/bot spawn <username> [mode] [difficulty] §7- Spawn bot mimicking <username>§r\n" +
                "§e/bot mode <mace|axe_shield|crystal|sword> §7- Switch combat mode§r\n" +
                "§e/bot difficulty <easy|medium|hard|god|insane> §7- Adjust reaction speed & CPS§r\n" +
                "§e/bot tech <tech_name|all> <on|off|trigger> §7- Toggle/trigger any of the 21 techniques§r\n" +
                "§e/bot listtech §7- List all 21 techniques (Pearl Grapple & Rod Grapple included)§r\n" +
                "§e/bot gear <reset|mace_kit|custom> §7- Equip Dual-Mace Kit (Density+WB1 Aerial & Breach IV Ground)§r\n" +
                "§e/bot behavior <aggressive|tactical|kiting|passive> §7- Set positioning strategy§r\n" +
                "§e/bot status §7- View bot stats and active tech mask§r\n" +
                "§e/bot remove <all|username> §7- Remove active bots§r"), false);
        return 1;
    }

    private static int listAllTechniques(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        source.sendFeedback(() -> Text.literal("§6=== All 21 Mace PvP Techniques (Grappling Included) ===§r\n" +
                "§a[Beginner]: §fbeginner_windcharge, beginner_shield, beginner_elytra_boost\n" +
                "§e[Intermediate]: §fpearl_catching, elytra_macing, windcharge_cancel, lunge_swapping\n" +
                "§c[Advanced]: §fstun_slamming, sword_attribute_swap, backstabbing, breach_swapping (§5Breach IV No WB§f), pearl_grapple (§dEnder Pearl Grapple§f), spear_double_pop, rod_grapple (§bFishing Rod Grapple§f)\n" +
                "§d[Master]: §felytra_stun_slam, diagonal_pearl_catch, shield_draining, mace_dtap, elytra_preservation\n" +
                "§b[Bonus]: §frocket_macing, wall_pearling"), false);
        return 1;
    }

    private static int spawnBot(CommandContext<ServerCommandSource> ctx, String username, String mode, String difficulty) {
        ServerCommandSource source = ctx.getSource();
        PvpBotEntity bot = ModEntities.PVP_BOT.create(source.getWorld());
        if (bot == null) {
            source.sendError(Text.literal("§cFailed to create PvP Training Bot entity."));
            return 0;
        }

        bot.refreshPositionAndAngles(source.getPosition().x, source.getPosition().y, source.getPosition().z, source.getRotation().y, 0.0F);
        bot.setBotUsername(username);
        bot.setBotMode(mode);
        bot.setBotDifficulty(difficulty);

        if (source.getEntity() instanceof PlayerEntity player) {
            bot.setTarget(player);
            bot.setAssignedTarget(player);
        }

        source.getWorld().spawnEntity(bot);
        source.sendFeedback(() -> Text.literal("§a[PvP Bot] Spawned bot mimicking §b" + username + " §a(Mode: §e" + mode.toUpperCase() + "§a, Diff: §c" + difficulty.toUpperCase() + "§a, Dual-Mace & Grapple Kit equipped)"), true);
        return 1;
    }

    private static List<PvpBotEntity> getActiveBots(ServerCommandSource source) {
        List<PvpBotEntity> bots = new ArrayList<>();
        for (Entity e : source.getWorld().iterateEntities()) {
            if (e instanceof PvpBotEntity bot) {
                bots.add(bot);
            }
        }
        return bots;
    }

    private static int setMode(CommandContext<ServerCommandSource> ctx, String mode) {
        ServerCommandSource source = ctx.getSource();
        List<PvpBotEntity> bots = getActiveBots(source);
        if (bots.isEmpty()) {
            source.sendError(Text.literal("§cNo active PvP Training Bots found. Spawn one first!"));
            return 0;
        }
        for (PvpBotEntity bot : bots) {
            bot.setBotMode(mode);
        }
        source.sendFeedback(() -> Text.literal("§a[PvP Bot] Updated " + bots.size() + " bot(s) to mode: §e" + mode.toUpperCase()), true);
        return 1;
    }

    private static int setDifficulty(CommandContext<ServerCommandSource> ctx, String level) {
        ServerCommandSource source = ctx.getSource();
        List<PvpBotEntity> bots = getActiveBots(source);
        if (bots.isEmpty()) {
            source.sendError(Text.literal("§cNo active PvP Training Bots found."));
            return 0;
        }
        for (PvpBotEntity bot : bots) {
            bot.setBotDifficulty(level);
        }
        source.sendFeedback(() -> Text.literal("§a[PvP Bot] Updated " + bots.size() + " bot(s) to difficulty: §c" + level.toUpperCase()), true);
        return 1;
    }

    private static int setTech(CommandContext<ServerCommandSource> ctx, String techName, String action) {
        ServerCommandSource source = ctx.getSource();
        List<PvpBotEntity> bots = getActiveBots(source);
        if (bots.isEmpty()) {
            source.sendError(Text.literal("§cNo active PvP Training Bots found."));
            return 0;
        }

        boolean enable = action.equalsIgnoreCase("on") || action.equalsIgnoreCase("enable") || action.equalsIgnoreCase("true");
        boolean trigger = action.equalsIgnoreCase("trigger") || action.equalsIgnoreCase("drill");

        for (PvpBotEntity bot : bots) {
            if (trigger) {
                if (source.getEntity() instanceof PlayerEntity player) {
                    bot.setTarget(player);
                }
                bot.setActiveDrillTech(techName);
            } else {
                bot.setTechEnabled(techName, enable);
            }
        }

        if (trigger) {
            source.sendFeedback(() -> Text.literal("§d[PvP Bot] Triggered instant practice drill: §l" + techName.toUpperCase()), true);
        } else {
            source.sendFeedback(() -> Text.literal("§a[PvP Bot] Tech §e" + techName + " §ais now " + (enable ? "§lENABLED" : "§c§lDISABLED") + " §aon " + bots.size() + " bot(s)"), true);
        }
        return 1;
    }

    private static int setGear(CommandContext<ServerCommandSource> ctx, String kit) {
        ServerCommandSource source = ctx.getSource();
        List<PvpBotEntity> bots = getActiveBots(source);
        for (PvpBotEntity bot : bots) {
            bot.equipCompetitiveMaceKit();
        }
        source.sendFeedback(() -> Text.literal("§a[PvP Bot] Equipped " + bots.size() + " bot(s) with Dual-Mace & Grapple Kit (§dAerial Density+WB1 §f& §5Ground Breach IV No-WB§a)."), true);
        return 1;
    }

    private static int setBehavior(CommandContext<ServerCommandSource> ctx, String style) {
        ServerCommandSource source = ctx.getSource();
        List<PvpBotEntity> bots = getActiveBots(source);
        for (PvpBotEntity bot : bots) {
            bot.setBotBehavior(style);
        }
        source.sendFeedback(() -> Text.literal("§a[PvP Bot] Behavior set to §b" + style.toUpperCase() + " §afor " + bots.size() + " bot(s)"), true);
        return 1;
    }

    private static int setTarget(CommandContext<ServerCommandSource> ctx, String playerName) {
        ServerCommandSource source = ctx.getSource();
        List<PvpBotEntity> bots = getActiveBots(source);
        if (playerName.equalsIgnoreCase("nearest") || playerName.equalsIgnoreCase("me")) {
            if (source.getEntity() instanceof PlayerEntity player) {
                for (PvpBotEntity bot : bots) {
                    bot.setAssignedTarget(player);
                    bot.setAssignedTargetName(player.getGameProfile().getName());
                }
                source.sendFeedback(() -> Text.literal("§a[PvP Bot] Targeted to you (§b" + player.getGameProfile().getName() + "§a)."), true);
            }
        } else {
            ServerPlayerEntity targetPlayer = source.getServer().getPlayerManager().getPlayer(playerName);
            if (targetPlayer != null) {
                for (PvpBotEntity bot : bots) {
                    bot.setAssignedTarget(targetPlayer);
                    bot.setAssignedTargetName(targetPlayer.getGameProfile().getName());
                }
                source.sendFeedback(() -> Text.literal("§a[PvP Bot] Targeted to §b" + targetPlayer.getGameProfile().getName()), true);
            } else {
                source.sendError(Text.literal("§cPlayer not found online: " + playerName));
            }
        }
        return 1;
    }

    private static int showStatus(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        List<PvpBotEntity> bots = getActiveBots(source);
        if (bots.isEmpty()) {
            source.sendFeedback(() -> Text.literal("§e[PvP Bot] No active bots currently spawned."), false);
            return 1;
        }

        for (PvpBotEntity bot : bots) {
            float hp = bot.getHealth();
            float maxHp = bot.getMaxHealth();
            source.sendFeedback(() -> Text.literal("§b--- PvP Bot (" + bot.getBotUsername() + ") ---§r\n" +
                    "§7Mode: §e" + bot.getBotMode().toUpperCase() + " §7| Diff: §c" + bot.getBotDifficulty().toUpperCase() + " §7| Dual-Mace & Grapple Kit Active§r\n" +
                    "§7Health: §a" + String.format("%.1f", hp) + "/" + String.format("%.1f", maxHp) + " HP (" + String.format("%.1f", hp / 2.0F) + "/" + String.format("%.1f", maxHp / 2.0F) + " ❤) §7| Combos Landed: §6" + bot.getComboCounter() + "§r\n" +
                    "§7Active Drill: §d" + (bot.getActiveDrillTech().isEmpty() ? "None" : bot.getActiveDrillTech()) + " §7| Tech Mask: §b0x" + Integer.toHexString(bot.getTechMask()).toUpperCase()), false);
        }
        return 1;
    }

    private static int removeBots(CommandContext<ServerCommandSource> ctx, String targetBot) {
        ServerCommandSource source = ctx.getSource();
        List<PvpBotEntity> bots = getActiveBots(source);
        int removed = 0;
        for (PvpBotEntity bot : bots) {
            if (targetBot.equalsIgnoreCase("all") || bot.getBotUsername().equalsIgnoreCase(targetBot)) {
                bot.discard();
                removed++;
            }
        }
        int count = removed;
        source.sendFeedback(() -> Text.literal("§e[PvP Bot] Removed " + count + " active PvP training bot(s)."), true);
        return 1;
    }
}
