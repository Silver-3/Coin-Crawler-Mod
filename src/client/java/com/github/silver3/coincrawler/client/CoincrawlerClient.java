package com.github.silver3.coincrawler.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.silver3.coincrawler.client.Commands.*;

public class CoincrawlerClient implements ClientModInitializer {
    public static final String MOD_ID = "coincrawler";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Coincrawler Client");

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            SetKeyCommand.register(dispatcher);
            TestCommand.register(dispatcher);
        });
    }
}
