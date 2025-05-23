package com.froobworld.nabsuite.modules.basics;

import com.froobworld.nabsuite.NabModule;
import com.froobworld.nabsuite.NabSuite;
import com.froobworld.nabsuite.modules.basics.afk.AfkManager;
import com.froobworld.nabsuite.modules.basics.channel.ChatChannelManager;
import com.froobworld.nabsuite.modules.basics.command.*;
import com.froobworld.nabsuite.modules.basics.config.BasicsConfig;
import com.froobworld.nabsuite.modules.basics.help.HelpManager;
import com.froobworld.nabsuite.modules.basics.message.MessageCentre;
import com.froobworld.nabsuite.modules.basics.motd.AnnouncementCentre;
import com.froobworld.nabsuite.modules.basics.motd.MotdManager;
import com.froobworld.nabsuite.modules.basics.nametag.NameTagManager;
import com.froobworld.nabsuite.modules.basics.permissions.GroupManager;
import com.froobworld.nabsuite.modules.basics.player.PlayerDataManager;
import com.froobworld.nabsuite.modules.basics.player.mail.MailCentre;
import com.froobworld.nabsuite.modules.basics.teleport.BackManager;
import com.froobworld.nabsuite.modules.basics.teleport.PlayerTeleporter;
import com.froobworld.nabsuite.modules.basics.teleport.home.HomeManager;
import com.froobworld.nabsuite.modules.basics.teleport.portal.PortalManager;
import com.froobworld.nabsuite.modules.basics.teleport.random.RandomTeleportManager;
import com.froobworld.nabsuite.modules.basics.teleport.request.TeleportRequestHandler;
import com.froobworld.nabsuite.modules.basics.teleport.spawn.SpawnManager;
import com.froobworld.nabsuite.modules.basics.teleport.warp.WarpManager;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;

public class BasicsModule extends NabModule {
    private BasicsConfig basicsConfig;
    private MessageCentre messageCentre;
    private PlayerDataManager playerDataManager;
    private HomeManager homeManager;
    private WarpManager warpManager;
    private PortalManager portalManager;
    private TeleportRequestHandler teleportRequestHandler;
    private BackManager backManager;
    private PlayerTeleporter playerTeleporter;
    private MotdManager motdManager;
    private MailCentre mailCentre;
    private HelpManager helpManager;
    private GroupManager groupManager;
    private AfkManager afkManager;
    private SpawnManager spawnManager;
    private RandomTeleportManager randomTeleportManager;
    private ChatChannelManager chatChannelManager;
    private NameTagManager nameTagManager;

    public BasicsModule(NabSuite nabSuite) {
        super(nabSuite, "basics");
    }

    @Override
    public void onEnable() {
        basicsConfig = new BasicsConfig(this);
        try {
            basicsConfig.load();
        } catch (Exception e) {
            getPlugin().getSLF4JLogger().error("Exception while loading config", e);
            Bukkit.getPluginManager().disablePlugin(getPlugin());
            return;
        }
        playerDataManager = new PlayerDataManager(this);
        homeManager = new HomeManager(this);
        warpManager = new WarpManager(this);
        portalManager = new PortalManager(this);
        messageCentre = new MessageCentre(this);
        teleportRequestHandler = new TeleportRequestHandler(this);
        backManager = new BackManager(this);
        playerTeleporter = new PlayerTeleporter(this);
        motdManager = new MotdManager(this);
        mailCentre = new MailCentre(this);
        helpManager = new HelpManager();
        groupManager = new GroupManager(this);
        afkManager = new AfkManager(this);
        spawnManager = new SpawnManager(this);
        new AnnouncementCentre(this);
        randomTeleportManager = new RandomTeleportManager(this);
        chatChannelManager = new ChatChannelManager(this);
        nameTagManager = new NameTagManager(this);

        Lists.newArrayList(
                new MessageCommand(this),
                new ReplyCommand(this),
                new PingCommand(),
                new MeCommand(this),
                new HoldingCommand(this),
                new WeatherCommand(),
                new IgnoreCommand(this),
                new NamesCommand(this),
                new SetWarpCommand(this),
                new DeleteWarpCommand(this),
                new WarpCommand(this),
                new WarpsCommand(this),
                new SetHomeCommand(this),
                new DeleteHomeCommand(this),
                new HomesCommand(this),
                new HomeCommand(this),
                new TeleportCommand(this),
                new TeleportRequestCommand(this),
                new TeleportHereRequestCommand(this),
                new TeleportAcceptCommand(this),
                new TeleportDenyCommand(this),
                new BackCommand(this),
                new FriendCommand(this),
                new PlayerListCommand(this),
                new MotdCommand(this),
                new TeleportToggleFriendsCommand(this),
                new TeleportToggleRequestsCommand(this),
                new RulesCommand(this),
                new FirstJoinCommand(this),
                new SeenCommand(this),
                new MailCommand(this),
                new SetPortalCommand(this),
                new DeletePortalCommand(this),
                new LinkPortalsCommand(this),
                new PortalsCommand(this),
                new TeleportPortalCommand(this),
                new HelpCommand(this),
                new AfkCommand(this),
                new SpawnCommand(this),
                new SetSpawnCommand(this),
                new RandomTeleportCommand(this),
                new ChannelCommand(this),
                new JoinChannelCommand(this),
                new LeaveChannelCommand(this),
                new MessageChannelCommand(this),
                new ReplyChannelCommand(this),
                new ChannelsCommand(this)
        ).forEach(getPlugin().getCommandManager()::registerCommand);
    }

    @Override
    public void postModulesEnable() {
        groupManager.postStartup();
        nameTagManager.postStartup();
    }

    @Override
    public void onDisable() {
        playerDataManager.shutdown();
        homeManager.shutdown();
        warpManager.shutdown();
        portalManager.shutdown();
        mailCentre.shutdown();
        chatChannelManager.shutdown();
    }

    public BasicsConfig getConfig() {
        return basicsConfig;
    }

    public MessageCentre getMessageCentre() {
        return messageCentre;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public PortalManager getPortalManager() {
        return portalManager;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public TeleportRequestHandler getTeleportRequestHandler() {
        return teleportRequestHandler;
    }

    public BackManager getBackManager() {
        return backManager;
    }

    public PlayerTeleporter getPlayerTeleporter() {
        return playerTeleporter;
    }

    public MotdManager getMotdManager() {
        return motdManager;
    }

    public MailCentre getMailCentre() {
        return mailCentre;
    }

    public HelpManager getHelpManager() {
        return helpManager;
    }

    public AfkManager getAfkManager() {
        return afkManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public RandomTeleportManager getRandomTeleportManager() {
        return randomTeleportManager;
    }

    public ChatChannelManager getChatChannelManager() {
        return chatChannelManager;
    }

    public NameTagManager getNameTagManager() {
        return nameTagManager;
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }
}
