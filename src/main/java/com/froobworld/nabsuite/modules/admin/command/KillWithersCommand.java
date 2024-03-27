package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.NumberArgument;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;

public class KillWithersCommand extends NabCommand implements Listener {
    private final AdminModule adminModule;

    public KillWithersCommand(AdminModule adminModule) {
        super(
                "killwithers",
                "Kill nearby withers.",
                "nabsuite.command.killwithers",
                Player.class
        );
        this.adminModule = adminModule;
        Bukkit.getPluginManager().registerEvents(this, adminModule.getPlugin());
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        int radius = context.get("radius");
        List<Entity> entities = player.getNearbyEntities(radius, 1000, radius);
        int count = 0;
        for (Entity entity : entities) {
            if (entity instanceof Wither wither) {
                wither.setMetadata("nabsuite-killwithers-wither", new FixedMetadataValue(adminModule.getPlugin(), true));
                wither.setHealth(0);
                count++;
            }
        }
        if (count == 0) {
            player.sendMessage(Component.text("No withers found within provided distance.", NamedTextColor.RED));
        } else {
            player.sendMessage(Component.text("Killed " + NumberDisplayer.toStringWithModifier(count, " wither", " withers", false) + ".", NamedTextColor.YELLOW));
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new NumberArgument<>(
                        true,
                        "radius",
                        context -> 1,
                        context -> 100
                ));
    }

    @EventHandler
    private void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Wither) {
            if (event.getEntity().hasMetadata("nabsuite-killwithers-wither")) {
                event.getDrops().clear();
                event.setDroppedExp(0);
            }
        }
    }

}
