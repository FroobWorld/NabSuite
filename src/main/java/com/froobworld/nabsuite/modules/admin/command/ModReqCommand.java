package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.ticket.Ticket;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ModReqCommand extends NabCommand {
    private final AdminModule adminModule;

    public ModReqCommand(AdminModule adminModule) {
        super(
                "modreq",
                "Request staff assistance.",
                "nabsuite.command.modreq",
                Player.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        String message = context.get("message");
        Ticket ticket = adminModule.getTicketManager().createTicket(sender, message);
        sender.sendMessage(Component.text("Ticket opened. You will be notified when your issue is resolved.", NamedTextColor.YELLOW));
        adminModule.getPlugin().getModule(BasicsModule.class).getMailCentre().sendSystemMail(sender.getUniqueId(), "You opened a ticket with message '" + message + "'. Your reference id is " + ticket.getId() + ".");
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder.argument(StringArgument.<CommandSender>newBuilder("message").greedy());
    }
}
