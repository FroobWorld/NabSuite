package com.froobworld.nabsuite.modules.admin.tasks;

import com.froobworld.nabsuite.modules.admin.AdminModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StaffTaskManager implements Listener {
    private final AdminModule adminModule;
    private final List<Supplier<List<StaffTask>>> staffTaskSuppliers = new ArrayList<>();

    public StaffTaskManager(AdminModule adminModule) {
        this.adminModule = adminModule;
        Bukkit.getPluginManager().registerEvents(this, adminModule.getPlugin());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerJoin(PlayerJoinEvent event) {
        if (!getStaffTasks(event.getPlayer()).isEmpty()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(adminModule.getPlugin(), () -> {
                event.getPlayer().sendMessage(
                        Component.text("There are staff tasks that require action (/stafftasks).").color(NamedTextColor.YELLOW)
                                .clickEvent(ClickEvent.runCommand("/stafftasks"))
                );
            }, 20);
        }
    }

    public void addStaffTaskSupplier(Supplier<List<StaffTask>> supplier) {
        staffTaskSuppliers.add(supplier);
    }

    public List<StaffTask> getStaffTasks() {
        List<StaffTask> staffTasks = new ArrayList<>();
        staffTaskSuppliers.forEach(supplier -> staffTasks.addAll(supplier.get()));
        return staffTasks;
    }

    public List<StaffTask> getStaffTasks(CommandSender sender) {
        return getStaffTasks()
                .stream()
                .filter(staffTask -> sender.hasPermission(staffTask.getPermission()))
                .collect(Collectors.toList());
    }

    public void notifyNewTask(String permission) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                player.sendMessage(Component.text("There is a new staff task requiring action (/stafftasks).", NamedTextColor.YELLOW));
            }
        }
    }

}
