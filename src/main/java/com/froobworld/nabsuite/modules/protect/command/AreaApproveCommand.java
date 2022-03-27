package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.Area;
import com.froobworld.nabsuite.modules.protect.command.argument.AreaArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class AreaApproveCommand extends NabCommand {
    private final ProtectModule protectModule;

    public AreaApproveCommand(ProtectModule protectModule) {
        super(
                "approve",
                "Approve a requested area.",
                "nabsuite.command.area.approve",
                CommandSender.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Area area = context.get("area");
        area.setApproved(true);
        context.getSender().sendMessage(
                Component.text("Area approved.").color(NamedTextColor.YELLOW)
        );
        UUID creator = area.getCreator();
        if (creator != null) {
            protectModule.getPlugin().getModule(BasicsModule.class).getMailCentre().sendSystemMail(creator, "Your area '" + area.getName() + "' was approved.");
        }
        AdminModule adminModule = protectModule.getPlugin().getModule(AdminModule.class);
        if (adminModule != null) {
            adminModule.getDiscordStaffLog().sendAreaRequestHandleNotification(context.getSender(), area, true, null);
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        new AreaArgument<>(
                                true,
                                "area",
                                protectModule.getAreaManager(),
                                new ArgumentPredicate<>(
                                        false,
                                        (context, area) -> !area.isApproved(),
                                        "This area has already been approved."
                                )
                        )
                );
    }

    @Override
    public String getUsage() {
        return "/area approve <area>";
    }
}
