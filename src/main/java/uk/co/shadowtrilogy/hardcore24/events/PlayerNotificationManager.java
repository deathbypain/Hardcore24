package uk.co.shadowtrilogy.hardcore24.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import uk.co.shadowtrilogy.hardcore24.Hardcore24;

import java.util.UUID;

public class PlayerNotificationManager implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent ev){
        UUID playerId = ev.getPlayer().getUniqueId();
        String pendingMessage = Hardcore24.pendingPlayerNotifications.remove(playerId);

        if(pendingMessage != null && !pendingMessage.isEmpty()){
            ev.getPlayer().sendMessage(pendingMessage);
        }
    }
}
