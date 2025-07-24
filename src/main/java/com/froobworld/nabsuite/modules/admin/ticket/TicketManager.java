package com.froobworld.nabsuite.modules.admin.ticket;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.tasks.StaffTask;
import com.froobworld.nabsuite.util.ConsoleUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class TicketManager {
    private static final Pattern fileNamePattern = Pattern.compile("^[0-9]+\\.json$");
    private final AdminModule adminModule;
    protected final DataSaver ticketSaver;
    private final BiMap<Integer, Ticket> ticketMap = HashBiMap.create();
    private final File directory;
    private final AtomicInteger idSupplier;

    private final Map<String, BiFunction<Ticket, PlayerIdentity, Component>> typeSummaryProvider = new HashMap<>();

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

    public void registerTicketType(String type, BiFunction<Ticket, PlayerIdentity, Component> typeSummaryProvider) {
        this.typeSummaryProvider.put(type, typeSummaryProvider);
    }

    public void postStartup() {
        Supplier<List<StaffTask>> openTicketTaskSupplier = () -> {
            List<StaffTask> tasks = new ArrayList<>();
            List<Ticket> openTickets = ticketMap.values().stream()
                    .filter(Ticket::isOpen)
                    // Sort by highest level and lowest id
                    .sorted((a,b) -> !a.getLevel().equals(b.getLevel()) ?
                                    adminModule.getAdminConfig().ticketLevels.get().indexOf(b.getLevel()) - adminModule.getAdminConfig().ticketLevels.get().indexOf(a.getLevel()) :
                            a.getId() - b.getId()
                    )
                    .toList();
            for (Ticket ticket : openTickets) {
                StaffTask task = new StaffTask(
                        ticket.getPermission(),
                        ticket.getSummary()
                            .clickEvent(ClickEvent.suggestCommand("/ticket read " + ticket.getId()))
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
        Ticket ticket = new Ticket(this, idSupplier.getAndIncrement(), player.getUniqueId(), null, player.getLocation(), "modreq", message);
        ticketMap.put(ticket.getId(), ticket);
        ticketSaver.scheduleSave(ticket);
        adminModule.getStaffTaskManager().notifyNewTask(ticket.getPermission());
        adminModule.getDiscordStaffLog().sendTicketCreationNotification(ticket);
        return ticket;
    }

    public Ticket createSystemTicket(Location location, UUID subject, String type, String message) {
        Ticket ticket = new Ticket(this, idSupplier.getAndIncrement(), ConsoleUtils.CONSOLE_UUID, subject, location, type, message);
        ticketMap.put(ticket.getId(), ticket);
        ticketSaver.scheduleSave(ticket);
        adminModule.getStaffTaskManager().notifyNewTask(ticket.getPermission());
        adminModule.getDiscordStaffLog().sendTicketCreationNotification(ticket);
        return ticket;
    }

    public Ticket createSystemTicket(UUID subject, String type, String message) {
        return createSystemTicket(null, subject, type, message);
    }

    public Ticket getTicket(int id) {
        return ticketMap.get(id);
    }

    public Set<Ticket> getTickets() {
        return ticketMap.values();
    }

    protected AdminModule getAdminModule() {
        return adminModule;
    }

    protected Component getTicketSummary(Ticket ticket) {

        TextComponent.Builder summary = Component.text();
        summary.append(Component.text("Ticket #"+ticket.getId()+" "));

        if (!ticket.getLevel().equals("default")) {
            summary.append(Component.text("[" + ticket.getLevel() + "] "));
        }

        if (ticket.getType() != null && typeSummaryProvider.containsKey(ticket.getType())) {
            PlayerIdentity subject = ticket.getSubject() != null ? adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(ticket.getSubject()) : null;
            summary.append(typeSummaryProvider.get(ticket.getType()).apply(ticket, subject));
        } else {
            if (ticket.getCreator() != null && !ticket.getCreator().equals(ConsoleUtils.CONSOLE_UUID)) {
                PlayerIdentity creator = adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(ticket.getCreator());
                summary.append(Component.text("by "))
                        .append(creator.displayName());
            }
            summary.append(Component.text(" - \""));
            if (ticket.getMessage().length() > 18) {
                summary.append(Component.text(ticket.getMessage().substring(0, 15) + "...\""));
            } else {
                summary.append(Component.text(ticket.getMessage()+"\""));
            }
        }

        return summary.build();
    }

}
