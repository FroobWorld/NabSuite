package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.command.argument.GroupArgument;
import com.froobworld.nabsuite.modules.basics.permissions.GroupManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class SetGroupCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public SetGroupCommand(BasicsModule basicsModule) {
        super(
                "setgroup",
                "Change the primary group for a player.",
                "nabsuite.command.setgroup",
                CommandSender.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        PlayerIdentity target = context.get("player");
        String group = context.get("group");
        basicsModule.getGroupManager().changePrimaryGroup(
                target.getUuid(),
                group,
                user -> context.getSender().hasPermission(GroupManager.SETGROUP_PERMISSION_PREFIX + user.getPrimaryGroup().toLowerCase()),
                user -> context.getSender() instanceof ConsoleCommandSender || !user.getCachedData().getPermissionData().checkPermission(GroupManager.SETGROUP_IMMUNE_PERMISSION).asBoolean()
            ).handleAsync((v, e) -> {
                    if (e != null && e.getCause() instanceof IllegalArgumentException) {
                        context.getSender().sendMessage(Component.text(e.getCause().getMessage()).color(NamedTextColor.RED));
                    } else if (e != null) {
                        basicsModule.getPlugin().getSLF4JLogger().error("Error trying to set group of {} to {}", target.getLastName(), group, e.getCause());
                        context.getSender().sendMessage(Component.text("Internal error").color(NamedTextColor.RED));
                    } else {
                        context.getSender().sendMessage(Component.text("Changed group for " + target.getLastName() + " to '" + group + "'").color(NamedTextColor.YELLOW));
                        basicsModule.getPlugin().getModule(AdminModule.class).getDiscordStaffLog().sendGroupChangeNotification(context.getSender(), target, group);
                    }
                    return null;
                }, Bukkit.getScheduler().getMainThreadExecutor(basicsModule.getPlugin()));
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder.argument(new PlayerIdentityArgument<>(
                true,
                "player",
                basicsModule.getPlugin().getPlayerIdentityManager(),
                true,
                new ArgumentPredicate<>(
                        true,
                        (context, player) -> !(context.getSender() instanceof Player sender) || !sender.getUniqueId().equals(player.getUuid()),
                        "You can't change your own group."
                )
        )).argument(new GroupArgument<>(
                true,
                "group",
                basicsModule.getPlugin().getUserManager().getGroupUserManager(),
                new ArgumentPredicate<>(
                        true,
                        (context, group) -> context.getSender().hasPermission(GroupManager.SETGROUP_PERMISSION_PREFIX + group.toLowerCase()),
                        "You don't have permission for that group."
                )
        ));
    }
}
