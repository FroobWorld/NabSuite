package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.NumberArgument;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.data.identity.PlayerIdentityManager;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.lock.LockManager;
import com.froobworld.nabsuite.modules.protect.lock.SignUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Queue;

public class LockCommand extends NabCommand {
    private final ProtectModule protectModule;

    public LockCommand(ProtectModule protectModule) {
        super(
                "lock",
                "Modify a lock sign.",
                "nabsuite.command.lock",
                Player.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        int line = context.get("line");
        Object input = context.get("input");
        LockManager lockManager = protectModule.getLockManager();
        Location location = lockManager.selectedSigns.get(sender);
        if (location == null) {
            sender.sendMessage(Component.text("You need to select a sign first by right clicking it.", NamedTextColor.RED));
            return;
        }
        if (!location.isChunkLoaded()) {
            sender.sendMessage(Component.text("The sign you selected is in an unloaded chunk.", NamedTextColor.RED));
            return;
        }
        Block block = location.getBlock();
        if (!(block.getBlockData() instanceof WallSign)) {
            sender.sendMessage(Component.text("You need to select a sign first by right clicking it.", NamedTextColor.RED));
            return;
        }
        boolean owner = false;
        for (Block lockable : lockManager.getAttachedLockables(SignUtils.getAttachedBlock(block))) {
            if (sender.getUniqueId().equals(lockManager.getOwner(lockable.getLocation(), false))) {
                owner = true;
            }
        }
        if (!owner && !sender.hasPermission(LockManager.PERM_BYPASS_LOCKS)) {
            sender.sendMessage(Component.text("You can only edit signs you own.", NamedTextColor.RED));
            return;
        }
        Sign sign = (Sign) block.getState();
        if (SignUtils.getLine(sign, 0).equalsIgnoreCase(LockManager.LOCK_HEADER) && line == 2 && !sender.hasPermission(LockManager.PERM_BYPASS_LOCKS)) {
            sender.sendMessage(Component.text("You can't change the owner of the sign.", NamedTextColor.RED));
            return;
        }
        if (input instanceof PlayerIdentity playerIdentity) {
            lockManager.updateUser(sign, line - 1, playerIdentity.getLastName(), playerIdentity.getUuid());
        } else {
            if (SignUtils.getLine(sign, 0).equalsIgnoreCase(LockManager.LOCK_HEADER) && line == 2) {
                // Don't allow null uuid for owner
                sender.sendMessage(Component.text("New owner must be a known player.", NamedTextColor.RED));
                return;
            }
            lockManager.updateUser(sign, line - 1, input.toString(), null);
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        new NumberArgument<>(
                                true,
                                "line",
                                context -> 2,
                                context -> 4
                        )
                )
                .argument(
                        new LockArgument<>(
                                protectModule.getPlugin().getPlayerIdentityManager(),
                                true,
                                "input"
                        )
                );
    }

    private static class LockArgument<C> extends CommandArgument<C, Object> {

        public LockArgument(PlayerIdentityManager playerIdentityManager, boolean required, String name) {
            super(required, name, new Parser<>(new PlayerIdentityArgument<>(required, "internal", playerIdentityManager, true)), Object.class);
        }

        private static final class Parser<C> implements ArgumentParser<C, Object> {
            private final PlayerIdentityArgument<C> playerIdentityArgument;

            private Parser(PlayerIdentityArgument<C> playerIdentityArgument) {
                this.playerIdentityArgument = playerIdentityArgument;
            }

            @Override
            public @NonNull ArgumentParseResult<@NonNull Object> parse(@NonNull CommandContext<@NonNull C> commandContext, @NonNull Queue<@NonNull String> inputQueue) {
                String input = inputQueue.peek();

                ArgumentParseResult<PlayerIdentity> parseResult = playerIdentityArgument.getParser().parse(commandContext, inputQueue);
                if (parseResult.getFailure().isEmpty()) {
                    return parseResult.mapParsedValue(identity -> identity);
                }
                return ArgumentParseResult.success(input);
            }

            @Override
            public @NonNull List<@NonNull String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
                List<String> suggestions = playerIdentityArgument.getParser().suggestions(commandContext, input);
                if ("[friends]".startsWith(input.toLowerCase())) {
                    suggestions.add("[Friends]");
                }
                return suggestions;
            }
        }

    }

}
