package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.NabParentCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerArgument;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.xray.PlayerOreStatsData;
import com.froobworld.nabsuite.modules.basics.command.IgnoreAddCommand;
import com.froobworld.nabsuite.modules.basics.command.IgnoreListCommand;
import com.froobworld.nabsuite.modules.basics.command.IgnoreRemoveCommand;
import com.froobworld.nabsuite.util.ComponentUtils;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class OreIgnoreCommand extends NabParentCommand {

    public OreIgnoreCommand(AdminModule adminModule) {
        super(
                "oreignore",
                "Ignores a player's ore alerts for this session.",
                "nabsuite.command.oreignore",
                Player.class
        );
        childCommands.addAll(List.of(
                new OreIgnoreAddCommand(adminModule),
                new OreIgnoreRemoveCommand(adminModule),
                new OreIgnoreListCommand(adminModule)
        ));
    }

}
