package org.delawarex.dbz.raids.managers;

import org.delawarex.dbz.raids.models.Party;
import org.delawarex.dbz.raids.models.PartyStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PartyManager {

    private static final Map<String, Party> parties = new ConcurrentHashMap<>();
    private static final Map<UUID, String> playerToParty = new ConcurrentHashMap<>();
    private static final AtomicInteger counter = new AtomicInteger(0);

    public static Party createParty(UUID leader, int maxSize) {
        String id = "party_" + counter.incrementAndGet() + "_" + System.currentTimeMillis();
        Party party = new Party(id, leader, maxSize);
        parties.put(id, party);
        playerToParty.put(leader, id);
        return party;
    }

    public static Party getByPlayer(UUID uuid) {
        String id = playerToParty.get(uuid);
        return id != null ? parties.get(id) : null;
    }

    public static Party getById(String id) { return parties.get(id); }
    public static boolean isInParty(UUID uuid) { return playerToParty.containsKey(uuid); }

    public static boolean joinParty(UUID uuid, Party party) {
        if (party == null || party.isFull() || playerToParty.containsKey(uuid)) return false;
        party.addMember(uuid);
        playerToParty.put(uuid, party.getPartyId());
        return true;
    }

    public static void leaveParty(UUID uuid) {
        String id = playerToParty.remove(uuid);
        if (id == null) return;
        Party party = parties.get(id);
        if (party == null) return;
        party.removeMember(uuid);
        if (party.getMemberCount() == 0 || !party.hasMember(party.getLeader())) {
            dissolve(id);
        }
    }

    public static void dissolve(String id) {
        Party party = parties.remove(id);
        if (party != null) {
            party.getActivePlayers().forEach(playerToParty::remove);
        }
    }

    public static void setStatus(Party party, PartyStatus status) {
        if (party != null) party.setStatus(status);
    }

    public static int getTotalParties() { return parties.size(); }

    public static void clearAll() {
        parties.clear();
        playerToParty.clear();
    }
}