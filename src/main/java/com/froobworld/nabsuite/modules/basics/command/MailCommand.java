package com.froobworld.nabsuite.modules.basics.command;

import com.froobworld.nabsuite.command.NabParentCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import org.bukkit.entity.Player;

import java.util.List;

public class MailCommand extends NabParentCommand {

    public MailCommand(BasicsModule basicsModule) {
        super(
                "mail",
                "Manage your mail box.",
                "nabsuite.command.mail",
                Player.class
        );
        childCommands.addAll(List.of(
                new MailReadCommand(basicsModule),
                new MailSendCommand(basicsModule),
                new MailClearCommand(basicsModule)
        ));
    }
}
