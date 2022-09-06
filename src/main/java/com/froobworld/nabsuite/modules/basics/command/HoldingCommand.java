package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.mechs.MechsModule;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HoldingCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public HoldingCommand(BasicsModule basicsModule) {
        super("holding",
                "Display the item you are holding in chat.",
                "nabsuite.command.holding",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        ItemStack itemStack = sender.getInventory().getItemInMainHand();
        if (itemStack.getAmount() == 0) {
            sender.sendMessage(
                    Component.text("You are not holding anything.").color(NamedTextColor.RED)
            );
            return;
        }
        Component message = Component.text("* ")
                .append(sender.displayName())
                .append(Component.text(" is holding "))
                .append(itemStack.getMaxStackSize() > 1 ? Component.text(NumberDisplayer.numberToString(itemStack.getAmount(), false) + " ") : Component.empty())
                .append(itemStack.displayName());
        MechsModule mechsModule = basicsModule.getPlugin().getModule(MechsModule.class);
        if (mechsModule != null) {
            if (sender.getWorld().equals(mechsModule.getNabModeManager().getNabDimensionManager().getNabWorld())) {
                message = Component.text("* ")
                        .append(sender.displayName())
                        .append(Component.text(" is holding a bunch of nabby items."));
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!basicsModule.getPlayerDataManager().getIgnoreManager().isIgnoring(player, sender)) {
                player.sendMessage(message);
            }
        }
        Bukkit.getConsoleSender().sendMessage(message);
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
