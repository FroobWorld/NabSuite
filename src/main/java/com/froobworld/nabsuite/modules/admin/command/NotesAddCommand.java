package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.util.ConsoleUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class NotesAddCommand extends NabCommand {
    private final AdminModule adminModule;

    public NotesAddCommand(AdminModule adminModule) {
        super("add",
                "Add a note to a player.",
                "nabsuite.command.notes.add",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        UUID creator = ConsoleUtils.getSenderUUID(context.getSender());
        PlayerIdentity target = context.get("target");
        String note = context.get("note");
        adminModule.getNoteManager().createNote(target.getUuid(), creator, note);
        context.getSender().sendMessage(Component.text("Note added.", NamedTextColor.YELLOW));
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new PlayerIdentityArgument<>(
                        true,
                        "target",
                        adminModule.getPlugin().getPlayerIdentityManager(),
                        false
                ))
                .argument(StringArgument.greedy("note"));
    }

    @Override
    public String getUsage() {
        return "/notes add <player> <note>";
    }

}
