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
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class SetGroupCommand extends NabCommand {
    private final BasicsModule basicsModule;
    private final AdminModule adminModule;

    public SetGroupCommand(BasicsModule basicsModule) {
        super(
                "setgroup",
                "Change the primary group for a player.",
                "nabsuite.command.setgroup",
                CommandSender.class
        );
        this.basicsModule = basicsModule;
        this.adminModule = basicsModule.getPlugin().getModule(AdminModule.class);
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        PlayerIdentity target = context.get("player");
        String group = context.get("group");
        basicsModule.getGroupManager().changePrimaryGroup(target.getUuid(), group);
        adminModule.getDiscordStaffLog().sendGroupChangeNotification(context.getSender(), target, group);
        context.getSender().sendMessage(Component.text("Changed group for " + target.getLastName() + " to '" + group + "'").color(NamedTextColor.YELLOW));
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
                ),
                new ArgumentPredicate<>(
                        true,
                        (context, player) -> {
                            User user = adminModule.getGroupManager().getUser(player.getUuid());
                            return user != null &&
                                    context.getSender().hasPermission(GroupManager.SETGROUP_PERMISSION_PREFIX + user.getPrimaryGroup().toLowerCase()) &&
                                    !user.getCachedData().getPermissionData().checkPermission(GroupManager.SETGROUP_IMMUNE_PERMISSION).asBoolean();
                        },
                        "You don't have permission to change the group of this user."
                )
        )).argument(new GroupArgument<>(
                true,
                "group",
                basicsModule.getPlugin().getUserManager().getGroupUserManager(),
                new ArgumentPredicate<>(
                        true,
                        (context, group) -> context.getSender().hasPermission(GroupManager.SETGROUP_PERMISSION_PREFIX + group.toLowerCase()),
                        "You don't have permission for that group."
                ),
                new ArgumentPredicate<>(
                        false,
                        (context, group) -> {
                            User user = adminModule.getGroupManager().getUser(((PlayerIdentity)context.get("player")).getUuid());
                            return user != null && !group.equalsIgnoreCase(user.getPrimaryGroup());
                        },
                        "User already belongs to that group."
                )
        ));
    }
}
