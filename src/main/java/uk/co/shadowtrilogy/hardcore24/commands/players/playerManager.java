package uk.co.shadowtrilogy.hardcore24.commands.players;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import uk.co.shadowtrilogy.hardcore24.Hardcore24;
import uk.co.shadowtrilogy.hardcore24.PlayerDeathData;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class playerManager implements CommandExecutor {


    final int OPERATION = 0;
    final int PLAYER = 1;
    final int WORLD = 2;

    final String REMOVE = "UNBAN";
    final String ADD = "BAN";
    final String LIST = "LIST";



    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if(!commandSender.hasPermission("hardcore.manage.players")){
            commandSender.sendMessage(ChatColor.RED + "You don't have permission to use this command...");
            return true;
        }

        if(args.length>0){

            if(args[OPERATION].toUpperCase().contains(REMOVE)){
                if(args.length != 2){
                    commandSender.sendMessage(ChatColor.RED + "Argument error! Please use: /player unban <player>");
                    return true;
                }

                if(!playerExists(args[PLAYER])){
                    commandSender.sendMessage(ChatColor.RED + "Error! Player \"" + args[PLAYER] + "\" not found...");
                    return true;
                }

              /*  if(!worldExists(args[WORLD])){
                    commandSender.sendMessage(ChatColor.RED + "Error! World \"" + args[WORLD] + "\" not found...");
                    return true;
                }*/


                UUID player = getPlayer(args[PLAYER]);
                if(player==null){
                    commandSender.sendMessage(ChatColor.RED + "Error! Player \"" + args[PLAYER] + "\" not found...");
                    return true;
                }

                PlayerDeathData activeBan = Hardcore24.deadPlayers.get(player);
                if(activeBan == null){
                    commandSender.sendMessage(ChatColor.RED + "Error! Player \"" + args[PLAYER] + "\" is not banned from hardcore...");
                    return true;
                }

                Hardcore24.deadPlayers.remove(player);
                commandSender.sendMessage(ChatColor.BLUE + "Successfully unbanned player \"" + ChatColor.LIGHT_PURPLE + args[PLAYER] + ChatColor.BLUE + "\" from all hardcore bans.");


               return true;

            }
            if(args[OPERATION].toUpperCase().contains(ADD)){
                if(args.length<3){
                    commandSender.sendMessage(ChatColor.RED + "Argument error! Please use: /player ban <player> <world>");
                    return true;
                }

                if(!playerExists(args[PLAYER])){
                    commandSender.sendMessage(ChatColor.RED + "Error! Player \"" + args[PLAYER] + "\" not found...");
                    return true;
                }

                if(!worldIsInConfiguredGroup(args[WORLD])){
                    commandSender.sendMessage(ChatColor.RED + "Error! World \"" + args[WORLD] + "\" is not configured in any hardcore group...");
                    return true;
                }


                try{
                    UUID player = (getPlayer(args[PLAYER]));

                    if(player==null){
                        throw new NullPointerException();
                    }
                        Hardcore24.deadPlayers.put(player, new PlayerDeathData(args[WORLD], LocalDateTime.now()));
                    commandSender.sendMessage(ChatColor.BLUE + "Successfully banned player \"" + ChatColor.LIGHT_PURPLE + args[PLAYER] +  ChatColor.BLUE + "\"  from hardcore world \"" + ChatColor.GREEN + args[WORLD] + ChatColor.BLUE + "\"");


                } catch (NullPointerException ex){
                    commandSender.sendMessage(ChatColor.RED + "Error! Player \"" + args[PLAYER] + "\" not found...");

                }

                return true;



            }
            if(args[OPERATION].toUpperCase().contains(LIST)){
                if(args.length==1){
                    if(Hardcore24.deadPlayers.isEmpty()){
                        commandSender.sendMessage(ChatColor.GREEN + "There are no players currently banned from hardcore.");
                        return true;
                    }

                    commandSender.sendMessage(ChatColor.BLUE + "Hardcore banned players (" + Hardcore24.deadPlayers.size() + "):");
                    for(Map.Entry<UUID, PlayerDeathData> entry : Hardcore24.deadPlayers.entrySet()){
                        String listedName = Hardcore24.plugin.getServer().getOfflinePlayer(entry.getKey()).getName();
                        if(listedName == null){
                            listedName = entry.getKey().toString();
                        }

                        PlayerDeathData data = entry.getValue();
                        LocalDateTime unbanTime = LocalDateTime.of(data.deathYear, data.deathMonth, data.deathDayOfMonth, data.deathHour, data.deathMinute, data.deathSecond);
                        String groupName = Hardcore24.worlds.get(data.world);
                        if(groupName==null){
                            groupName = "(unmapped for world \"" + data.world + "\")";
                        }

                        commandSender.sendMessage(ChatColor.BLUE + "- " + ChatColor.LIGHT_PURPLE + listedName + ChatColor.BLUE + " | Group: " + ChatColor.GREEN + groupName + ChatColor.BLUE + " | Unban: " + ChatColor.GREEN + unbanTime);
                    }
                    return true;
                }

                if(args.length>2){
                    commandSender.sendMessage(ChatColor.RED + "Argument error! Please use: /player list or /player list <player>");
                    return true;
                }

                if(!playerExists(args[PLAYER])){
                    commandSender.sendMessage(ChatColor.RED + "Error! Player \"" + args[PLAYER] + "\" not found...");
                    return true;
                }

                UUID player = getPlayer(args[PLAYER]);
                if(player==null){
                    commandSender.sendMessage(ChatColor.RED + "Error! Player \"" + args[PLAYER] + "\" not found...");
                    return true;
                }

                if(!Hardcore24.deadPlayers.containsKey(player)){
                    commandSender.sendMessage(ChatColor.GREEN + "Player \"" + args[PLAYER] + "\" is not currently banned from hardcore.");
                    return true;
                }

                PlayerDeathData data = Hardcore24.deadPlayers.get(player);
                LocalDateTime unbanTime = LocalDateTime.of(data.deathYear, data.deathMonth, data.deathDayOfMonth, data.deathHour, data.deathMinute, data.deathSecond);
                String groupName = Hardcore24.worlds.get(data.world);
                boolean banElapsed = LocalDateTime.now().isAfter(unbanTime);

                if(groupName==null){
                    groupName = "(unmapped for world \"" + data.world + "\")";
                }

                commandSender.sendMessage(ChatColor.BLUE + "Hardcore status for \"" + ChatColor.LIGHT_PURPLE + args[PLAYER] + ChatColor.BLUE + "\": " + ChatColor.RED + "BANNED");
                commandSender.sendMessage(ChatColor.BLUE + "Group: " + ChatColor.GREEN + groupName + ChatColor.BLUE + " | Death world: " + ChatColor.GREEN + data.world);
                commandSender.sendMessage(ChatColor.BLUE + "Unban time: " + ChatColor.GREEN + unbanTime + ChatColor.BLUE + " | Ban elapsed: " + ChatColor.GREEN + banElapsed);

                return true;

            }



        }

        return true;




    }


    boolean playerExists(String p){

        for(OfflinePlayer player : Hardcore24.plugin.getServer().getOfflinePlayers()){
            if(p.toLowerCase().equalsIgnoreCase((player.getName().toLowerCase()))){
                return true;
            }
        }

        return false;

    }

    UUID getPlayer(String p){

        for(OfflinePlayer player : Hardcore24.plugin.getServer().getOfflinePlayers()){
            if(p.toLowerCase().equalsIgnoreCase((player.getName().toLowerCase()))){
                return player.getUniqueId();
            }
        }

        return null;

    }



    boolean worldExists(String p){

        for(World w : Hardcore24.plugin.getServer().getWorlds()){
            if(p.toLowerCase().equalsIgnoreCase((w.getName().toLowerCase()))){
                return true;
            }



    }
        return false;

    }

    boolean worldIsInConfiguredGroup(String worldName){
        if(!worldExists(worldName)){
            return false;
        }
        return Hardcore24.worlds.containsKey(worldName);
    }
}
