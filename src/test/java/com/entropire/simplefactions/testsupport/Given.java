package com.entropire.simplefactions.testsupport;

import static com.entropire.simplefactions.testsupport.Results.assertSuccess;

import java.util.UUID;

import com.entropire.simplefactions.faction.Faction;
import com.entropire.simplefactions.faction.membership.FactionMembership;
import com.entropire.simplefactions.faction.membership.Role;

public final class Given {

    private final TestDatabase test;

    Given(TestDatabase test) {
        this.test = test;
    }

    public UUID player(String username) {
        UUID uuid = UUID.randomUUID();
        assertSuccess(test.playerService.registerJoin(uuid, username));
        return uuid;
    }

    public Faction faction(String name, UUID owner) {
        return assertSuccess(test.factionService.createFaction(name, "#ffffff", owner));
    }

    public void member(Faction faction, UUID player) {
        assertSuccess(test.db.transaction(conn -> test.membershipRepository.save(conn,
                new FactionMembership(player, faction.uuid(), Role.MEMBER, null))));
    }

    public UUID memberOf(Faction faction, String username) {
        UUID player = player(username);
        member(faction, player);
        return player;
    }
}
