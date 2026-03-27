package org.delawarex.dbz.raids.managers;

import org.delawarex.dbz.raids.models.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RaidSessionManager {

    private static final Map<String, RaidSession> sessions = new ConcurrentHashMap<>();
    private static final Map<UUID, String> playerToSession = new ConcurrentHashMap<>();
    private static final AtomicInteger counter = new AtomicInteger(0);

    public static RaidSession createSession(Raid raid, Party party) {
        String id = "session_" + counter.incrementAndGet() + "_" + System.currentTimeMillis();
        RaidSession session = new RaidSession(id, raid, party);
        sessions.put(id, session);
        party.getActivePlayers().forEach(uuid -> playerToSession.put(uuid, id));
        return session;
    }

    public static RaidSession getByPlayer(UUID uuid) {
        String id = playerToSession.get(uuid);
        return id != null ? sessions.get(id) : null;
    }

    public static RaidSession getById(String id) { return sessions.get(id); }
    public static Collection<RaidSession> getAll() { return new ArrayList<>(sessions.values()); }
    public static int getTotalActive() { return sessions.size(); }

    public static boolean hasActiveSession(String raidId) {
        return sessions.values().stream()
                .anyMatch(s -> s.getRaid().getRaidId().equals(raidId)
                        && s.getStatus() == RaidStatus.IN_PROGRESS);
    }

    public static void completeRaid(RaidSession session) {
        session.setStatus(RaidStatus.COMPLETED);
        session.setEndTime(System.currentTimeMillis());
        session.getActivePlayers().forEach(uuid ->
                CooldownManager.setCooldown(uuid, session.getRaid().getRaidId(),
                        session.getRaid().getCooldownSeconds())
        );
        removeSession(session.getSessionId());
    }

    public static void failRaid(RaidSession session) {
        session.setStatus(RaidStatus.FAILED);
        session.setEndTime(System.currentTimeMillis());
        removeSession(session.getSessionId());
    }

    public static void playerLeft(UUID uuid) {
        RaidSession session = getByPlayer(uuid);
        if (session == null) return;
        session.playerLeft(uuid);
        playerToSession.remove(uuid);
    }

    public static void playerDied(UUID uuid) {
        RaidSession session = getByPlayer(uuid);
        if (session == null) return;
        session.playerDied(uuid);
    }

    public static void removeSession(String id) {
        RaidSession session = sessions.remove(id);
        if (session != null) {
            session.getActivePlayers().forEach(playerToSession::remove);
            session.getDeadPlayers().forEach(playerToSession::remove);
        }
    }

    public static void clearAll() {
        sessions.clear();
        playerToSession.clear();
    }
}