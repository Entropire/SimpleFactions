package com.entropire.simplefactions;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.entropire.simplefactions.database.DataBaseContext;
import com.entropire.simplefactions.faction.Faction;
import com.entropire.simplefactions.faction.FactionService;
import com.entropire.simplefactions.faction.exception.FactionNameDuplicateException;
import com.entropire.simplefactions.faction.exception.FactionOwnerDuplicateException;
import com.entropire.simplefactions.faction.membership.FactionMembership;
import com.entropire.simplefactions.faction.membership.FactionMembershipService;
import com.entropire.simplefactions.faction.membership.Role;

public class FactionApplication {
    private DataBaseContext db;
    private FactionService factionService;
    private FactionMembershipService factionMembershipService;

    public FactionApplication(
        DataBaseContext db,
        FactionService factionService,
        FactionMembershipService factionMembershipService
    ){
        this.db = db;
        this.factionService = factionService;
        this.factionMembershipService = factionMembershipService;
    }

    public void createFaction(String name, String color, Player player){
        try{
            db.transaction(conn -> {
                FactionMembership membership = factionMembershipService.getByPlayerUuid(conn, player.getUniqueId());

                if(membership == null){
                    Faction faction = factionService.save(conn, new Faction(name, color, player.getUniqueId()));
                    factionMembershipService.save(conn, new FactionMembership(player.getUniqueId(), faction.uuid(), Role.OWNER, null));
                    player.sendMessage("Faction created!");
                }
                else{
                    player.sendMessage("You are already part of an faction!");
                }
            });
        }
        catch(FactionOwnerDuplicateException e){
            player.sendMessage("You already own a faction.");
        }
        catch(FactionNameDuplicateException e){
            player.sendMessage("The name " + name + " is already in use by another faction.");
        }
        catch(Exception e){
            player.sendMessage("Something went wrong while making your faction!");
            e.printStackTrace();
        }  
    }

    public void deleteFaction(Player player){
        try{
            db.transaction(conn -> {
                FactionMembership member = factionMembershipService.getByPlayerUuid(conn, player.getUniqueId());       
                
                if(member.role() != Role.OWNER){
                    player.sendMessage("Only the owner of the faction can delete the faction!");
                    return;
                }

                List<FactionMembership> members = factionMembershipService.getAllByFactionUuid(conn, member.factionUuid());
                factionService.delete(conn, member.factionUuid());

                for (FactionMembership factionMembership : members) {
                    Player memberPlayer = Bukkit.getPlayer(factionMembership.playerUuid());

                    if(memberPlayer.getUniqueId() == member.playerUuid()){
                        memberPlayer.sendMessage("You have deleted your faction.");
                    }
                    else{
                        memberPlayer.sendMessage("Your faction has been deleted!");
                    }
                }
            }); 
        }
        catch(Exception e){
            player.sendMessage("Somthing whent wrong while deleting your faction.");
            e.printStackTrace();
        }
    }
}
