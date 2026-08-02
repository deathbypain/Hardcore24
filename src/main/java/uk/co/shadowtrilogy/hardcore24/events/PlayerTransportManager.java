package uk.co.shadowtrilogy.hardcore24.events;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import uk.co.shadowtrilogy.hardcore24.Hardcore24;
import uk.co.shadowtrilogy.hardcore24.PlayerDeathData;
import uk.co.shadowtrilogy.hardcore24.json.groups.groupdata;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class PlayerTransportManager implements Listener {

    @EventHandler
    public void world_move(PlayerTeleportEvent ev){

        try{
            if(Hardcore24.deadPlayers.containsKey(ev.getPlayer().getUniqueId())){


                PlayerDeathData data = Hardcore24.deadPlayers.get(ev.getPlayer().getUniqueId());
                String playerName = ev.getPlayer().getName();

                LocalDateTime deathTime = LocalDateTime.of(data.deathYear, data.deathMonth, data.deathDayOfMonth, data.deathHour, data.deathMinute, data.deathSecond);

                String group = Hardcore24.worlds.get(data.world);

                LocalDateTime now = LocalDateTime.now();
                boolean banElapsed = now.isAfter(deathTime);
                Hardcore24.plugin.getLogger().info("Teleport ban-time check for " + playerName + ": expires=" + deathTime + ", now=" + now + ", elapsed=" + banElapsed);


                if(banElapsed){

                    Hardcore24.deadPlayers.remove(ev.getPlayer().getUniqueId());
                    ev.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.ITALIC + "Congratulations, you have been unbanned from hardcore... Good luck");


                } else {
                    String destinationWorld = ev.getTo() != null && ev.getTo().getWorld() != null
                    ? ev.getTo().getWorld().getName() : null;

                    boolean destinationInBannedGroup = destinationWorld != null && doesGroupContainWorld(group, destinationWorld);
                    Hardcore24.plugin.getLogger().info("Teleport destination group check for " + playerName + ": destination=" + destinationWorld + ", bannedGroup=" + group + ", inBannedGroup=" + destinationInBannedGroup);

                    if (destinationInBannedGroup) {
                       ev.setCancelled(true);
                        ev.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.ITALIC
                            + "You died in hardcore... You will be unbanned at " + deathTime);
                    }

                }



            }

        }catch(NullPointerException ex){

        }




    }


    boolean doesGroupContainWorld(String group, String world){
        ArrayList<groupdata> groups = new ArrayList<>(Hardcore24.groups);
        for(groupdata g : groups){
            if(g.group_name.equalsIgnoreCase(group)){
                ArrayList<String> worlds = new ArrayList<>(g.worlds);
                for(String str : worlds){
                    if(str.equalsIgnoreCase(world)){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
