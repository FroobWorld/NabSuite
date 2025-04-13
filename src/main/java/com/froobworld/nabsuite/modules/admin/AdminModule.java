package com.froobworld.nabsuite.modules.admin;

import com.froobworld.nabsuite.NabModule;
import com.froobworld.nabsuite.NabSuite;
import com.froobworld.nabsuite.modules.admin.chat.ProfanityFilter;
import com.froobworld.nabsuite.modules.admin.command.*;
import com.froobworld.nabsuite.modules.admin.config.AdminConfig;
import com.froobworld.nabsuite.modules.admin.contingency.ContingencyManager;
import com.froobworld.nabsuite.modules.admin.greylist.GreylistManager;
import com.froobworld.nabsuite.modules.admin.inventory.InvSeeManager;
import com.froobworld.nabsuite.modules.admin.jail.JailManager;
import com.froobworld.nabsuite.modules.admin.note.NoteManager;
import com.froobworld.nabsuite.modules.admin.notification.DiscordStaffLog;
import com.froobworld.nabsuite.modules.admin.notification.NotificationCentre;
import com.froobworld.nabsuite.modules.admin.punishment.PunishmentManager;
import com.froobworld.nabsuite.modules.admin.suspicious.SuspiciousActivityMonitor;
import com.froobworld.nabsuite.modules.admin.tasks.StaffTaskManager;
import com.froobworld.nabsuite.modules.admin.theft.TheftPreventionManager;
import com.froobworld.nabsuite.modules.admin.ticket.TicketManager;
import com.froobworld.nabsuite.modules.admin.vanish.VanishManager;
import com.froobworld.nabsuite.modules.admin.xray.OreStatsManager;
import com.froobworld.nabsuite.modules.admin.xray.XrayMonitor;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;

public class AdminModule extends NabModule {
    private AdminConfig adminConfig;
    private NotificationCentre notificationCentre;
    private JailManager jailManager;
    private PunishmentManager punishmentManager;
    private VanishManager vanishManager;
    private GreylistManager greylistManager;
    private OreStatsManager oreStatsManager;
    private StaffTaskManager staffTaskManager;
    private TicketManager ticketManager;
    private DiscordStaffLog discordStaffLog;
    private TheftPreventionManager theftPreventionManager;
    private XrayMonitor xrayMonitor;
    private SuspiciousActivityMonitor suspiciousActivityMonitor;
    private ContingencyManager contingencyManager;
    private NoteManager noteManager;
    private ProfanityFilter profanityFilter;

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
        jailManager = new JailManager(this);
        punishmentManager = new PunishmentManager(this);
        vanishManager = new VanishManager(this);
        //greylistManager = new GreylistManager(this);
        oreStatsManager = new OreStatsManager(this);
        staffTaskManager = new StaffTaskManager(this);
        ticketManager = new TicketManager(this);
        profanityFilter = new ProfanityFilter(this);
        discordStaffLog = new DiscordStaffLog(this);
        theftPreventionManager = new TheftPreventionManager(this);
        xrayMonitor = new XrayMonitor(this);
        this.suspiciousActivityMonitor = new SuspiciousActivityMonitor(this);
        this.contingencyManager = new ContingencyManager(this);
        this.noteManager = new NoteManager(this);
        new InvSeeManager(this);

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
                new OreIgnoreCommand(this),
                new StaffTasksCommand(this),
                new PunishmentLogCommand(this),
                new ModReqCommand(this),
                new TicketCommand(this),
                new NoStealingCommand(this),
                new SeeInventoryCommand(this),
                new RestrictCommand(this),
                new UnrestrictCommand(this),
                new LockdownCommand(this),
                new NoXrayCommand(this),
                new NotesCommand(this),
                new WarnCommand(this),
                new ConfineCommand(this),
                new UnconfineCommand(this),
                new KillWithersCommand(this),
                new AntixrayCommand(this)
        ).forEach(getPlugin().getCommandManager()::registerCommand);
    }

    @Override
    public void onDisable() {
        punishmentManager.shutdown();
        jailManager.shutdown();
        //greylistManager.shutdown();
        oreStatsManager.shutdown();
        ticketManager.shutdown();
    }

    @Override
    public void postModulesEnable() {
        ticketManager.postStartup();
    }

    public AdminConfig getAdminConfig() {
        return adminConfig;
    }

    public JailManager getJailManager() {
        return jailManager;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
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

    public TheftPreventionManager getTheftPreventionManager() {
        return theftPreventionManager;
    }

    public SuspiciousActivityMonitor getSuspiciousActivityMonitor() {
        return suspiciousActivityMonitor;
    }

    public ContingencyManager getContingencyManager() {
        return contingencyManager;
    }

    public XrayMonitor getXrayMonitor() {
        return xrayMonitor;
    }

    public NoteManager getNoteManager() {
        return noteManager;
    }

    public ProfanityFilter getProfanityFilter() {
        return profanityFilter;
    }
}
