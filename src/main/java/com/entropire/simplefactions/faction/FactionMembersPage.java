package com.entropire.simplefactions.faction;

import com.entropire.simplefactions.objects.Pageable;
import com.entropire.simplefactions.player.FactionPlayer;

public record FactionMembersPage(Faction faction, Pageable<FactionPlayer> members) {
}
