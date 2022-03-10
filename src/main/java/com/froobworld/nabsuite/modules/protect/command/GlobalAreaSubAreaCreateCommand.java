package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.bukkit.parsers.WorldArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.StringArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.AreaManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class GlobalAreaSubAreaCreateCommand extends NabCommand {
    private final ProtectModule protectModule;

    public GlobalAreaSubAreaCreateCommand(ProtectModule protectModule) {
        super(
                "create",
                "Create a global sub area.",
                "nabsuite.command.globalarea.subarea.create",
                CommandSender.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        World world = context.get("world");
        String name = context.get("name");
        int bound1 = context.get("bound1");
        int bound2 = context.get("bound2");
        protectModule.getAreaManager().getGlobalAreaManager().createGlobalSubArea(world, name, bound1, bound2);
        context.getSender().sendMessage(Component.text("Global sub area created.", NamedTextColor.YELLOW));
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(WorldArgument.newBuilder("world"))
                .argument(new StringArgument<>(
                        true,
                        "name",
                        false,
                        new ArgumentPredicate<>(
                                true,
                                (context, name) -> AreaManager.areaNamePattern.matcher(name).matches(),
                                "Name must only contain letters, numbers, underscores and dashes."
                        ),
                        new ArgumentPredicate<>(
                                true,
                                (context, name) -> protectModule.getAreaManager().getGlobalAreaManager().getGlobalSubArea(context.get("world"), name) == null,
                                "A sub area by that name already exists."
                        )
                ))
                .argument(IntegerArgument.newBuilder("bound1"))
                .argument(IntegerArgument.newBuilder("bound2"));
    }

    @Override
    public String getUsage() {
        return "/globalarea subarea create <world> <name> <bound1> <bound2>";
    }
}
