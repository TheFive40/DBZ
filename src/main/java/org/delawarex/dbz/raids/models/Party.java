package org.delawarex.dbz.raids.models;

import java.util.*;

public class Party {

    private final String partyId;
    private UUID leaderUuid;
    private final Set<UUID> members;
    private final int maxSize;
    private final long createdAt;
    private PartyStatus status;

    public Party(String partyId, UUID leaderUuid, int maxSize) {
        this.partyId = partyId;
        this.leaderUuid = leaderUuid;
        this.maxSize = maxSize;
        this.members = new LinkedHashSet<>();
        this.createdAt = System.currentTimeMillis();
        this.status = PartyStatus.WAITING;
        this.members.add(leaderUuid);
    }

    public boolean addMember(UUID uuid) {
        if (isFull()) return false;
        return members.add(uuid);
    }

    public void removeMember(UUID uuid) { members.remove(uuid); }
    public boolean hasMember(UUID uuid) { return members.contains(uuid); }
    public boolean isFull() { return members.size() >= maxSize; }
    public int getMemberCount() { return members.size(); }
    public boolean isLeader(UUID uuid) { return leaderUuid.equals(uuid); }
    public List<UUID> getActivePlayers() { return new ArrayList<>(members); }

    public String getPartyId() { return partyId; }
    public UUID getLeader() { return leaderUuid; }
    public void setLeader(UUID leaderUuid) { this.leaderUuid = leaderUuid; }
    public int getMaxSize() { return maxSize; }
    public PartyStatus getStatus() { return status; }
    public void setStatus(PartyStatus status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
}