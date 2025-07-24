package com.froobworld.nabsuite.modules.discord.bot.command;

import com.froobworld.nabsuite.NabSuite;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.command.sender.OfflineCommandSender;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.RestAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.ansi.ColorLevel;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class DiscordCommandSender extends OfflineCommandSender {
    private static final ANSIComponentSerializer ansi = ANSIComponentSerializer.builder().colorLevel(ColorLevel.INDEXED_8).build();
    private InteractionHook hook = null;
    private StringBuilder replyBuffer = null;
    private ScheduledFuture<?> future = null;

    public DiscordCommandSender(NabSuite plugin, PlayerIdentity player, InteractionHook event) {
        this(plugin, player);
        setHook(event);
    }

    public DiscordCommandSender(NabSuite plugin, PlayerIdentity player) {
        super(plugin, player);
    }

    public void setHook(InteractionHook hook) {
        this.hook = hook;
        if (this.replyBuffer == null) {
            this.replyBuffer = new StringBuilder();
            replyBuffer.append("```ansi\n");
        }
    }

    @Override
    public void sendMessage(@NotNull String message) {
        sendMessage(LegacyComponentSerializer.legacySection().deserialize(message));
    }

    @Override
    public void sendRawMessage(@NotNull String s) {
        sendMessage(Component.text(s));
    }

    @Override
    public void sendMessage(@NotNull Component message) {
        if (replyBuffer != null && hook != null) {
            String out = ansi.serialize(message);
            replyBuffer.append(out).append("\n");
            queue();
        }
    }

    public void updateResponse(@NotNull Function<InteractionHook, RestAction<Message>> handler) {
        if (hook != null && !hook.isExpired()) {
            RestAction<Message> result = handler.apply(hook);
            if (result != null) {
                result.queue();
                hook = null;
            }
        }
    }

    public void replyError(@NotNull String message) {
        if (hook != null && !hook.isExpired()) {
            hook.editOriginal(message).queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
            hook = null;
        }
    }

    public void queue() {
        if (replyBuffer != null && hook != null && !hook.isExpired()) {
            if (future != null && !future.isDone()) {
                future.cancel(true);
            }
            // Give commands 20ms to send additional messages before updating response
            future = hook.editOriginal(replyBuffer + "```").queueAfter(20, TimeUnit.MILLISECONDS);
        }
    }

}
