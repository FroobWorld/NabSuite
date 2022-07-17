package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SeeInventoryCommand extends NabCommand implements Listener {

    public SeeInventoryCommand(AdminModule adminModule) {
        super(
                "seeinv",
                "Look at another players inventory.",
                "nabsuite.command.seeinv",
                Player.class,
                "viewinv",
                "openinv"
        );
        Bukkit.getPluginManager().registerEvents(this, adminModule.getPlugin());
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        Player player = context.get("player");

        sender.openInventory(player.getInventory());
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        new PlayerArgument<>(
                                true,
                                "player",
                                new ArgumentPredicate<>(
                                        false,
                                        (context, player) -> !player.equals(context.getSender()),
                                        "You can't view your own inventory this way"
                                )
                        )
                );
    }

    @EventHandler(ignoreCancelled = true)
    private void onInventoryInteract(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof HumanEntity) {
            if (!event.getWhoClicked().equals(event.getInventory().getHolder())) {
                event.setCancelled(true);
            }
        }
    }


}
