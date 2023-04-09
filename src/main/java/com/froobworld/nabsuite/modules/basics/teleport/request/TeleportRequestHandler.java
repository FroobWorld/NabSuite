package com.froobworld.nabsuite.modules.basics.teleport.request;

import com.froobworld.nabsuite.modules.basics.BasicsModule;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TeleportRequestHandler {
    private final BasicsModule basicsModule;
    private final Map<Player, RequestedTeleport> teleportRequestMap = new ConcurrentHashMap<>();

    public TeleportRequestHandler(BasicsModule basicsModule) {
        this.basicsModule = basicsModule;
        basicsModule.getPlugin().getHookManager().getSchedulerHook().runRepeatingTask(this::cleanup, 100, 100);
    }

    public RequestedTeleport getRequestedTeleport(Player subject) {
        return teleportRequestMap.get(subject);
    }

    public void requestTeleportTo(Player requester, Player subject) {
        teleportRequestMap.put(subject, new RequestedTeleport(requester, subject, RequestType.TO));
    }

    public void requestSummon(Player requester, Player subject) {
        teleportRequestMap.put(subject, new RequestedTeleport(requester, subject, RequestType.SUMMON));
    }

    private void cleanup() {
        teleportRequestMap.entrySet().removeIf(entry -> !entry.getValue().isValid());
    }

    public class RequestedTeleport {
        private final Player requester;
        private final Player subject;
        private final RequestType type;
        private final long requestTime;
        private boolean valid = true;

        private RequestedTeleport(Player requester, Player subject, RequestType type) {
            this.requester = requester;
            this.subject = subject;
            this.type = type;
            this.requestTime = System.currentTimeMillis();
        }

        public void carryOut() {
            if (!valid) {
                return;
            }
            if (type == RequestType.TO) {
                TeleportRequestHandler.this.basicsModule.getPlayerTeleporter().teleportAsync(requester, subject);
            } else {
                TeleportRequestHandler.this.basicsModule.getPlayerTeleporter().teleportAsync(subject, requester);
            }
            invalidate();
        }

        public void invalidate() {
            this.valid = false;
        }

        public Player getRequester() {
            return requester;
        }

        public Player getSubject() {
            return subject;
        }

        public boolean isValid() {
            return valid && requester.isValid() && subject.isValid() && !expired();
        }

        private boolean expired() {
            return System.currentTimeMillis() - requestTime > TimeUnit.MINUTES.toMillis(1);
        }

    }

    public enum RequestType {
        TO,
        SUMMON
    }

}
