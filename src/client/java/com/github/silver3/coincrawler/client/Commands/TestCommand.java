package com.github.silver3.coincrawler.client.Commands;

import com.github.silver3.coincrawler.client.Classes.ModConfig;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TestCommand {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("ccm")
                .then(ClientCommandManager.literal("test")
                        .executes(context -> {
                            FabricClientCommandSource source = context.getSource();

                            String apiKey = ModConfig.load().apiKey;
                            if (apiKey.isEmpty()) {
                                source.sendFeedback(Component.literal("No API Key found. Use /ccm set-key first.")
                                        .withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            String localUuid = source.getClient().player.getStringUUID().replace("-", "");
                            source.sendFeedback(Component.literal("Checking premium status...").withStyle(ChatFormatting.GRAY));

                            HttpRequest request = HttpRequest.newBuilder()
                                    .uri(URI.create("https://coincrawler.mine.bz/auth/premium"))
                                    .header("apiKey", apiKey)
                                    .GET()
                                    .build();

                            CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                                    .thenApply(HttpResponse::body)
                                    .thenAccept(responseBody -> {
                                        try {
                                            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                                            boolean isPremium = json.has("premium") && json.get("premium").getAsBoolean();

                                            if (isPremium) {
                                                String serverUuid = json.get("uuid").getAsString();

                                                if (localUuid.equalsIgnoreCase(serverUuid)) {
                                                    source.sendFeedback(Component.literal("You are a premium user!")
                                                            .withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD));
                                                } else {
                                                    source.sendFeedback(Component.literal("UUID mismatch, you are not a premium user")
                                                            .withStyle(ChatFormatting.RED));
                                                }
                                            } else {
                                                source.sendFeedback(Component.literal("You are not a premium user")
                                                        .withStyle(ChatFormatting.RED));
                                            }
                                        } catch (Exception e) {
                                            source.sendFeedback(Component.literal("Error parsing server response.")
                                                    .withStyle(ChatFormatting.RED));
                                        }
                                    })
                                    .exceptionally(ex -> {
                                        source.sendFeedback(Component.literal("Failed to connect to authentication server.")
                                                .withStyle(ChatFormatting.RED));
                                        return null;
                                    });

                            return 1;
                        })
                )
        );
    }
}