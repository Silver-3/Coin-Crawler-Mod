package com.github.silver3.coincrawler.client.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import com.github.silver3.coincrawler.client.Classes.ModConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SetKeyCommand {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("ccm")
                .then(ClientCommandManager.literal("set-key")
                        .then(ClientCommandManager.argument("key", StringArgumentType.string())
                                .executes(context -> {
                                    FabricClientCommandSource source = context.getSource();
                                    String inputKey = StringArgumentType.getString(context, "key");

                                    if (source.getClient().player == null) {
                                        source.sendFeedback(Component.literal("Player not available.")
                                                .withStyle(ChatFormatting.RED));
                                        return 0;
                                    }

                                    String localUuid = source.getClient().player.getStringUUID().replace("-", "");
                                    source.sendFeedback(Component.literal("Validating API key...").withStyle(ChatFormatting.GRAY));

                                    HttpRequest request = HttpRequest.newBuilder()
                                            .uri(URI.create("https://coincrawler.mine.bz/auth/premium"))
                                            .header("apiKey", inputKey)
                                            .GET()
                                            .build();

                                    CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                                            .thenApply(HttpResponse::body)
                                            .thenAccept(responseBody -> {
                                                try {
                                                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                                                    boolean isPremium = json.has("premium") && json.get("premium").getAsBoolean();

                                                    if (!isPremium) {
                                                        source.getClient().execute(() ->
                                                                source.sendFeedback(Component.literal("API key is not valid.")
                                                                        .withStyle(ChatFormatting.RED))
                                                        );
                                                        return;
                                                    }

                                                    if (!json.has("uuid")) {
                                                        source.getClient().execute(() ->
                                                                source.sendFeedback(Component.literal("Server response missing uuid.")
                                                                        .withStyle(ChatFormatting.RED))
                                                        );
                                                        return;
                                                    }

                                                    String serverUuid = json.get("uuid").getAsString().replace("-", "");
                                                    if (localUuid.equalsIgnoreCase(serverUuid)) {
                                                        ModConfig config = ModConfig.load();
                                                        config.apiKey = inputKey;
                                                        config.save();

                                                        source.getClient().execute(() ->
                                                                source.sendFeedback(Component.literal("API Key saved")
                                                                        .withStyle(ChatFormatting.GREEN))
                                                        );
                                                    } else {
                                                        source.getClient().execute(() ->
                                                                source.sendFeedback(Component.literal("UUID mismatch, this key belongs to a different account.")
                                                                        .withStyle(ChatFormatting.RED))
                                                        );
                                                    }
                                                } catch (Exception e) {
                                                    source.getClient().execute(() ->
                                                            source.sendFeedback(Component.literal("Error parsing server response.")
                                                                    .withStyle(ChatFormatting.RED))
                                                    );
                                                }
                                            })
                                            .exceptionally(ex -> {
                                                source.getClient().execute(() ->
                                                        source.sendFeedback(Component.literal("Failed to connect to authentication server.")
                                                                .withStyle(ChatFormatting.RED))
                                                );
                                                return null;
                                            });

                                    return 1;
                                })
                        )
                )
        );
    }
}
