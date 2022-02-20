package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MailSendCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public MailSendCommand(BasicsModule basicsModule) {
        super("send",
                "Send another player some mail.",
                "nabsuite.command.mail.send",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        PlayerIdentity recipient = context.get("recipient");
        String message = context.get("message");
        basicsModule.getMailCentre().sendMail(recipient.getUuid(), sender.getUniqueId(), message);
        sender.sendMessage(Component.text("Mail sent.", NamedTextColor.YELLOW));
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new PlayerIdentityArgument<>(
                        true,
                        "recipient",
                        basicsModule.getPlugin().getPlayerIdentityManager(),
                        false
                ))
                .argument(StringArgument.greedy("message"));
    }

    @Override
    public String getUsage() {
        return "/mail send <player> <message>";
    }

}
