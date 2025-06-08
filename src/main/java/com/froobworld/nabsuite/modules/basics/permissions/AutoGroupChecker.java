package com.froobworld.nabsuite.modules.basics.permissions;

import com.froobworld.nabsuite.data.playervar.PlayerVars;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.player.PlayerData;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.track.Track;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

public class AutoGroupChecker {
    private final BasicsModule basicsModule;
    private final LuckPerms luckPerms;

    public AutoGroupChecker(BasicsModule basicsModule, LuckPerms luckPerms) {
        this.basicsModule = basicsModule;
        this.luckPerms = luckPerms;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(basicsModule.getPlugin(), () -> {
            Bukkit.getOnlinePlayers().forEach(this::updatePlayer);
        }, 20, 200);
    }

    private void updatePlayer(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(basicsModule.getPlugin(), () -> {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) {
                return;
            }
            Track track = luckPerms.getTrackManager().getTrack(basicsModule.getConfig().autoPromote.track.get());
            if (track == null) {
                return;
            }
            PlayerData playerData = basicsModule.getPlayerDataManager().getPlayerData(player);
            long timePlayedMillis = System.currentTimeMillis() - playerData.getFirstJoined();
            double daysPlayed = (double) timePlayedMillis / 1000.0 / 60.0 / 60.0 / 24.0;

            String trackPosition = getTrackPosition(user, track);
            if (trackPosition == null) {
                return;
            }
            int daysRequired = basicsModule.getConfig().autoPromote.requiredTime.of(trackPosition).get();
            if (daysRequired > 0 && daysPlayed > daysRequired) {
                track.promote(user, luckPerms.getContextManager().getStaticContext());
                luckPerms.getUserManager().saveUser(user);
                PlayerVars playerVars = basicsModule.getPlugin().getPlayerVarsManager().getVars(user.getUniqueId());
                playerVars.put("last-group-change", System.currentTimeMillis());
            }

        });
    }

    private String getTrackPosition(User user, Track track) {
        Collection<Group> userGroups = user.getInheritedGroups(QueryOptions.defaultContextualOptions());
        Group highestTrackPosition = null;
        for (String group : track.getGroups()) {
            for (Group userGroup : userGroups) {
                if (group.equalsIgnoreCase(userGroup.getName())) {
                    if (highestTrackPosition == null || userGroup.getWeight().orElse(Integer.MIN_VALUE) > highestTrackPosition.getWeight().orElse(Integer.MIN_VALUE)) {
                        highestTrackPosition = userGroup;
                    }
                }
            }
        }
        return highestTrackPosition == null ? null : highestTrackPosition.getName();
    }

}
