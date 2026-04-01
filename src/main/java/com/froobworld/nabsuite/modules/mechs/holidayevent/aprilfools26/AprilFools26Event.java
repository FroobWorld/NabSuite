package com.froobworld.nabsuite.modules.mechs.holidayevent.aprilfools26;

import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.data.playervar.PlayerVars;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.mechs.MechsModule;
import com.froobworld.nabsuite.modules.mechs.holidayevent.HolidayEvent;
import com.froobworld.nabsuite.modules.nabmode.NabModeModule;
import com.froobworld.nabsuite.modules.protect.util.PlayerCauser;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class AprilFools26Event extends HolidayEvent implements Listener {
    private static final Random random = new Random();
    private static final String TEAM_KEY = "april-fools-26-team";
    private static final String POINTS_KEY = "april-fools-26-points";
    static final String TEAM_CHOF = "chof";
    static final String TEAM_FROB = "frob";
    private static final Component PREFIX_CHOF = Component.text("[chof] ").color(NamedTextColor.RED);
    private static final Component PREFIX_FROB = Component.text("[frob] ").color(NamedTextColor.BLUE);
    private static final long POINTS_ROLLOVER_MS = TimeUnit.MINUTES.toMillis(30);
    private static final List<EntityType> KILLABLE_ENTITIES = List.of(
            EntityType.BEE, EntityType.BLAZE, EntityType.HORSE, EntityType.ENDERMITE, EntityType.MULE,
            EntityType.PIGLIN, EntityType.ARMADILLO, EntityType.TURTLE, EntityType.LLAMA,
            EntityType.AXOLOTL, EntityType.BAT, EntityType.PHANTOM, EntityType.DOLPHIN, EntityType.FROG,
            EntityType.HUSK, EntityType.WARDEN, EntityType.TROPICAL_FISH, EntityType.COD, EntityType.SALMON,
            EntityType.ALLAY, EntityType.SNIFFER
    );

    private final MechsModule mechsModule;
    private final Map<String, Integer> actionPoints = new HashMap<>();
    private final Map<String, Long> actionPointsExpiry = new HashMap<>();
    private final Map<EntityType, Integer> killPoints = new HashMap<>();
    private long refreshTime = 0;
    private int scoreChof = 0;
    private int scoreFrob = 0;

    public AprilFools26Event(MechsModule mechsModule) {
        super("april-fools-2026");
        this.mechsModule = mechsModule;
        loadScores();
        new AprilFools26Sidebar(this, mechsModule);
        Bukkit.getPluginManager().registerEvents(this, mechsModule.getPlugin());
    }

    private void loadScores() {
        this.scoreChof = 0;
        this.scoreFrob = 0;
        for (PlayerIdentity player : mechsModule.getPlugin().getPlayerIdentityManager().getAllPlayerIdentities()) {
            String team = mechsModule.getPlugin().getPlayerVarsManager().getVars(player.getUuid()).get("april-fools-26-team", String.class);
            int points = mechsModule.getPlugin().getPlayerVarsManager().getVars(player.getUuid()).getOrDefault("april-fools-26-points", int.class, 0);

            if (team == null) {
                continue;
            }

            if (team.equals(TEAM_CHOF)) {
                scoreChof += points;
            } else if (team.equals(TEAM_FROB)) {
                scoreFrob += points;
            }
        }
    }

    public int getScoreChof() { return scoreChof; }

    public int getScoreFrob() { return scoreFrob; }

    public String getTeam(Player player) {
        PlayerVars vars = mechsModule.getPlugin().getPlayerVarsManager().getVars(player.getUniqueId());
        if (vars.get(TEAM_KEY, String.class) == null) {
            vars.put(TEAM_KEY, random.nextBoolean() ? TEAM_CHOF : TEAM_FROB);
        }
        return vars.get(TEAM_KEY, String.class);
    }

    public int getScore(Player player) {
        return getScore(player.getUniqueId());
    }

    public int getScore(UUID uuid) {
        return mechsModule.getPlugin().getPlayerVarsManager().getVars(uuid).getOrDefault(POINTS_KEY, int.class, 0);
    }

    public Map<EntityType, Integer> getKillPoints() {
        long curTime = System.currentTimeMillis();
        if (curTime > this.refreshTime) {
            this.refreshTime = curTime + POINTS_ROLLOVER_MS;
            killPoints.clear();
            while (killPoints.size() < 3) {
                int next = random.nextInt(KILLABLE_ENTITIES.size());
                killPoints.put(KILLABLE_ENTITIES.get(next), random.nextInt(3, 11));
            }
        }
        return killPoints;
    }

    public int getActionPoints(Player player, EntityType type) {
        int points = getKillPoints().get(type);

        BasicsModule basicsModule = mechsModule.getPlugin().getModule(BasicsModule.class);
        AdminModule adminModule = mechsModule.getPlugin().getModule(AdminModule.class);
        if (basicsModule != null &&  basicsModule.getAfkManager().isAfk(player)) {
            points = -points;
        } else if (adminModule != null && adminModule.getVanishManager().isVanished(player)) {
            points = 0;
        }

        return points;
    }

    public void addPoints(Player player, EntityType type) {
        if (!mechsModule.getHolidayEventManager().isEnabled(player, getHolidayKey())) {
            return;
        }

        int points = getActionPoints(player, type);
        String team = getTeam(player);

        PlayerVars vars = mechsModule.getPlugin().getPlayerVarsManager().getVars(player.getUniqueId());

        int totalPoints = vars.getOrDefault(POINTS_KEY, int.class, 0);
        totalPoints += points;
        vars.put(POINTS_KEY, totalPoints);

        if (team.equals(TEAM_CHOF)) {
            scoreChof += points;
        } else if (team.equals(TEAM_FROB)) {
            scoreFrob += points;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onPlayerChat(AsyncChatEvent event) {
        String team = getTeam(event.getPlayer());

        Component prefix;
        if (team.equals(TEAM_CHOF)) {
            prefix = PREFIX_CHOF;
        } else if (team.equals(TEAM_FROB)) {
            prefix = PREFIX_FROB;
        } else {
            return;
        }

        Component formatted = event.renderer().render(event.getPlayer(), event.getPlayer().displayName(), event.message(), Audience.empty());

        event.renderer(
                ChatRenderer.viewerUnaware((source, displayName, message) -> prefix.append(formatted))
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onEntityKill(EntityDeathEvent event) {
        Player causer = PlayerCauser.getPlayerCauser(event.getDamageSource().getCausingEntity());

        if (causer == null) return;
        if (event.getEntity().equals(causer)) return;
        NabModeModule nabModeModule = mechsModule.getPlugin().getModule(NabModeModule.class);
        if (nabModeModule != null && event.getEntity().getLocation().getWorld().equals(nabModeModule.getNabModeManager().getNabDimensionManager().getNabWorld())) {
            return;
        }

        addPoints(causer, event.getEntity().getType());
    }

    @Override
    public void toggleOff(Player player) {}

    @Override
    public void toggleOn(Player player) {}
}
