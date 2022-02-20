package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WeatherCommand extends NabCommand {

    public WeatherCommand() {
        super("weather",
                "Set the weather in your world.",
                "nabsuite.command.weather",
                Player.class
        );
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        World world = ((Player) context.getSender()).getWorld();
        WeatherType weatherType = context.get("weather");

        String weatherDescription;
        if (weatherType == WeatherType.CLEAR) {
            world.setStorm(false);
            weatherDescription = "clear";
        } else {
            world.setStorm(true);
            weatherDescription = "stormy";
        }
        context.getSender().sendMessage(
                Component.text("Set weather to " + weatherDescription + " in your world.").color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(EnumArgument.of(WeatherType.class, "weather"), ArgumentDescription.of("weather"));
    }
}
