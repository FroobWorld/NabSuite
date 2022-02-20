package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MailClearCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public MailClearCommand(BasicsModule basicsModule) {
        super("clear",
                "Clear your mail box.",
                "nabsuite.command.mail.clear",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        basicsModule.getMailCentre().getMailBox(player.getUniqueId()).clearMail();
        player.sendMessage(Component.text("Mail cleared.", NamedTextColor.YELLOW));
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }

    @Override
    public String getUsage() {
        return "/mail clear";
    }

}
