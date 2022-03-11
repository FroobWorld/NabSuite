package com.froobworld.nabsuite.modules.admin.ticket;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.tasks.StaffTask;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TicketManager {
    private static final Pattern fileNamePattern = Pattern.compile("^[0-9]+\\.json$");
    private final AdminModule adminModule;
    protected final DataSaver ticketSaver;
    private final BiMap<Integer, Ticket> ticketMap = HashBiMap.create();
    private final File directory;
    private final AtomicInteger idSupplier;

    public TicketManager(AdminModule adminModule) {
        this.adminModule = adminModule;
        directory = new File(adminModule.getDataFolder(), "tickets/");
        ticketSaver = new DataSaver(adminModule.getPlugin(), 1200);
        ticketMap.putAll(DataLoader.loadAll(
                directory,
                fileName -> fileNamePattern.matcher(fileName.toLowerCase()).matches(),
                bytes -> Ticket.fromJsonString(this, new String(bytes)),
                (fileName, ticket) -> ticket.getId()
        ));
        ticketSaver.start();
        ticketSaver.addDataType(Ticket.class, ticket -> ticket.toJsonString().getBytes(), ticket -> new File(directory, ticket.getId() + ".json"));

        int largestExistingId = 0;
        for (Ticket ticket : ticketMap.values()) {
            if (ticket.getId() > largestExistingId) {
                largestExistingId = ticket.getId();
            }
        }
        idSupplier = new AtomicInteger(largestExistingId + 1);
    }

    public void postStartup() {
        Supplier<List<StaffTask>> openTicketTaskSupplier = () -> {
            List<StaffTask> tasks = new ArrayList<>();
            List<Integer> openTicketIds = ticketMap.entrySet().stream()
                    .filter(entry -> entry.getValue().isOpen())
                    .map(Map.Entry::getKey)
                    .sorted()
                    .toList();
            for (int id : openTicketIds) {
                StaffTask task = new StaffTask(
                        "nabsuite.command.ticket",
                        Component.text("Ticket with id '" + id + "' needs resolving (/ticket).")
                                .clickEvent(ClickEvent.suggestCommand("/ticket read " + id))
                );
                tasks.add(task);
            }
            return tasks;
        };
        adminModule.getStaffTaskManager().addStaffTaskSupplier(openTicketTaskSupplier);
    }

    public void shutdown() {
        ticketSaver.stop();
    }

    public Ticket createTicket(Player player, String message) {
        Ticket ticket = new Ticket(this, idSupplier.getAndIncrement(), player.getUniqueId(), player.getLocation(), message);
        ticketMap.put(ticket.getId(), ticket);
        ticketSaver.scheduleSave(ticket);
        adminModule.getStaffTaskManager().notifyNewTask("nabsuite.command.ticket");
        return ticket;
    }

    public Ticket getTicket(int id) {
        return ticketMap.get(id);
    }

    public Set<Ticket> getTickets() {
        return ticketMap.values();
    }

}
