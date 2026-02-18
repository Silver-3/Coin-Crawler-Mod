package com.github.silver3.coincrawler.client.Commands;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class TestCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("cc")
                .then(ClientCommandManager.literal("test")
                        .executes(context -> {
                            FabricClientCommandSource source = context.getSource();
                            source.sendFeedback(Component.literal("Test").withStyle(ChatFormatting.YELLOW));

                            return 1;
                        })
                )
        );
    }
}