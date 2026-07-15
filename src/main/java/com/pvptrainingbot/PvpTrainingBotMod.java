package com.pvptrainingbot;

import com.pvptrainingbot.command.BotCommand;
import com.pvptrainingbot.entity.ModEntities;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PvpTrainingBotMod implements ModInitializer {
    public static final String MOD_ID = "pvptrainingbot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing PvP Training Bot Mod (1.21+ Max Breach IV & 20-Tech Edition)...");
        ModEntities.registerAttributes();
        BotCommand.register();
        LOGGER.info("PvP Training Bot Mod initialized successfully! Run /bot spawn <username> to start.");
    }
}
