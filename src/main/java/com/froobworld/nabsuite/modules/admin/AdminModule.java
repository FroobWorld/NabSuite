package com.froobworld.nabsuite.modules.admin;

import com.froobworld.nabsuite.NabModule;
import com.froobworld.nabsuite.NabSuite;
import com.froobworld.nabsuite.modules.admin.chat.ProfanityFilter;
import com.froobworld.nabsuite.modules.admin.command.*;
import com.froobworld.nabsuite.modules.admin.config.AdminConfig;
import com.froobworld.nabsuite.modules.admin.greylist.GreylistManager;
import com.froobworld.nabsuite.modules.admin.jail.JailManager;
import com.froobworld.nabsuite.modules.admin.notification.DiscordStaffLog;
import com.froobworld.nabsuite.modules.admin.notification.NotificationCentre;
import com.froobworld.nabsuite.modules.admin.punishment.PunishmentManager;
import com.froobworld.nabsuite.modules.admin.tasks.StaffTaskManager;
import com.froobworld.nabsuite.modules.admin.ticket.TicketManager;
import com.froobworld.nabsuite.modules.admin.vanish.VanishManager;
import com.froobworld.nabsuite.modules.admin.xray.OreStatsManager;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;

public class AdminModule extends NabModule {
    private AdminConfig adminConfig;
    private NotificationCentre notificationCentre;
    private PunishmentManager punishmentManager;
    private JailManager jailManager;
    private VanishManager vanishManager;
    private GreylistManager greylistManager;
    private OreStatsManager oreStatsManager;
    private StaffTaskManager staffTaskManager;
    private TicketManager ticketManager;
    private DiscordStaffLog discordStaffLog;

    public AdminModule(NabSuite nabSuite) {
        super(nabSuite, "admin");
    }

    @Override
    public void onEnable() {
        adminConfig = new AdminConfig(this);
        try {
            adminConfig.load();
        } catch (Exception e) {
            getPlugin().getSLF4JLogger().error("Exception while loading config", e);
            Bukkit.getPluginManager().disablePlugin(getPlugin());
            return;
        }
        notificationCentre = new NotificationCentre();
        punishmentManager = new PunishmentManager(this);
        vanishManager = new VanishManager(this);
        jailManager = new JailManager(this);
        //greylistManager = new GreylistManager(this);
        oreStatsManager = new OreStatsManager(this);
        staffTaskManager = new StaffTaskManager(this);
        ticketManager = new TicketManager(this);
        new ProfanityFilter(this);
        discordStaffLog = new DiscordStaffLog(this);

        Lists.newArrayList(
                new BanCommand(this),
                new TempBanCommand(this),
                new UnbanCommand(this),
                new PunishmentHistoryCommand(this),
                new VanishCommand(this),
                new MuteCommand(this),
                new UnmuteCommand(this),
                new SetJailCommand(this),
                new DeleteJailCommand(this),
                new JailCommand(this),
                new UnjailCommand(this),
                new JailsCommand(this),
                //new ApplyCommand(this),
                //new AcceptCommand(this),
                //new DenyCommand(this),
                //new GreylistAddCommand(this),
                //new GreylistRemoveCommand(this),
                //new GreylistCheckCommand(this),
                //new GreylistRequestsCommand(this),
                new OreStatsCommand(this),
                new StaffTasksCommand(this),
                new PunishmentLogCommand(this),
                new ModReqCommand(this),
                new TicketCommand(this)
        ).forEach(getPlugin().getCommandManager()::registerCommand);
    }

    @Override
    public void onDisable() {
        punishmentManager.shutdown();
        jailManager.shutdown();
        //greylistManager.shutdown();
        oreStatsManager.shutdown();
    }

    @Override
    public void postModulesEnable() {
        ticketManager.postStartup();
    }

    public AdminConfig getAdminConfig() {
        return adminConfig;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public JailManager getJailManager() {
        return jailManager;
    }

    public VanishManager getVanishManager() {
        return vanishManager;
    }

    public GreylistManager getGreylistManager() {
        return greylistManager;
    }

    public OreStatsManager getOreStatsManager() {
        return oreStatsManager;
    }

    public StaffTaskManager getStaffTaskManager() {
        return staffTaskManager;
    }

    public NotificationCentre getNotificationCentre() {
        return notificationCentre;
    }

    public TicketManager getTicketManager() {
        return ticketManager;
    }

    public DiscordStaffLog getDiscordStaffLog() {
        return discordStaffLog;
    }
}
