package me.obverser.waypointbeacons;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
* = Not Added.
Commands:
 /waypoints remove (NAME) [Removes waypoint by its name]
 /waypoints giveBlock (PLAYER) [Gives the player the special beacon block]
 /waypoints settings (SETTING PATH) (VALUE) (VALUE TYPE) [Changes settings via the setting name and its desired value]
 /waypoints forcePublic (NAME) [Forces a beacon to be public by its name]
 /waypoints forcePrivate (NAME) (ALLOWED PLAYERS) [Forces a beacon to be private by its name and the players allowed to access it] *
 /waypoints appendPrivate (NAME) (PLAYER) [Appends a player to the list of users able to access a beacon via its name] *
 /waypoints removePrivate (NAME) (PLAYER) [Removes a player from the list of users able to access a beacon via its name] *
 /waypoints forceName (NAME) (NEW NAME) [Forces a beacon's name to be changed]
 /waypoints forceDisplay (NAME) (MATERIAL) [Forces a beacon's display item/color]
 /waypoints help *
*/

public class WaypointCommands implements Listener, CommandExecutor {

    Plugin plugin;
    WaypointBeaconsRework host;

    public static void copyConfigSection(FileConfiguration config, String fromPath, String toPath){
        Map<String, Object> vals = config.getConfigurationSection(fromPath).getValues(true);
        String toDot = toPath.equals("") ? "" : ".";
        for (String s : vals.keySet()){
            System.out.println(s);
            Object val = vals.get(s);
            if (val instanceof List)
                val = new ArrayList((List)val);
            config.set(toPath + toDot + s, val);
        }
    }

    public void getPlugin(Plugin plugin, WaypointBeaconsRework host) {
        this.plugin = plugin;
        this.host = host;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if (plugin.getConfig().getBoolean("settings.commandsEnabled")) {
            if (sender instanceof Player) {
                if (!((Player) sender).getPlayer().isOp()) {
                    ((Player) sender).getPlayer().sendMessage(ChatColor.RED + "Player must be a server operator to issue this command.");
                    return true;
                }
            }

            if (args.length == 0) {
                // TODO put usage
                sender.sendMessage("TODO put usage");
                return true;
            }

            if (args[0].equalsIgnoreCase("remove")) {
                if (args.length < 2) {

                    // TODO put usage
                    sender.sendMessage("TODO put usage");

                    return true;
                }
                try {
                    plugin.getConfig().set("waypoints." + args[1], null);
                    plugin.saveConfig();
                } catch(Error exc) {
                    exc.printStackTrace();
                    if (sender instanceof Player) {
                        ((Player) sender).getPlayer().sendMessage(ChatColor.RED + "An error occurred or the waypoint was not found.");
                    } else {
                        Bukkit.getLogger().warning("An error occurred or the waypoint was not found.");
                    }
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("giveBlock")) {
                if (args.length < 2) {

                    // TODO put usage
                    sender.sendMessage("TODO put usage");

                    return true;
                }
                ItemStack wayBeacon = new ItemStack(Material.BEACON, 1);
                ItemMeta wayBeaconMeta = wayBeacon.getItemMeta();
                wayBeaconMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Waypoint Beacon");
                wayBeacon.setItemMeta(wayBeaconMeta);
                wayBeacon.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 10);
                wayBeacon.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                try {
                    Bukkit.getPlayer(args[1].toString()).getInventory().addItem(wayBeacon);
                } catch (Error exc) {
                    if (sender instanceof Player) {
                        ((Player) sender).getPlayer().sendMessage(ChatColor.RED + "An error occurred or the player was not found.");
                    } else {
                        Bukkit.getLogger().warning("An error occurred or the player was not found.");
                    }
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("settings")) {
                if (args.length < 3) {

                    // TODO put usage
                    sender.sendMessage("TODO put usage");

                    return true;
                }
                try {
                    if (args[3].equalsIgnoreCase("string")) {
                        String tempVarSettings = args[2].toString();
                        plugin.getConfig().set(args[1], tempVarSettings);
                    } else if (args[3].equalsIgnoreCase("integer")) {
                        Integer tempVarSettings = Integer.valueOf(args[2]);
                        plugin.getConfig().set(args[1], tempVarSettings);
                    } else if (args[3].equalsIgnoreCase("boolean")) {
                        Boolean tempVarSettings = Boolean.valueOf(args[2]);
                        plugin.getConfig().set(args[1], tempVarSettings);
                    }
                    Boolean tempBool = args[3].equalsIgnoreCase("integer");
                    sender.sendMessage(tempBool.toString());
                    sender.sendMessage(args[3].toString());
                    plugin.saveConfig();
                    host.reloadAllListenersRadius();
                } catch (Error exc) {
                    if (sender instanceof Player) {
                        ((Player) sender).getPlayer().sendMessage(ChatColor.RED + "An error occurred.");
                    } else {
                        Bukkit.getLogger().warning("An error occurred.");
                    }
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("forcePublic")) {
                if (args.length < 2) {

                    // TODO put usage
                    sender.sendMessage("TODO put usage");

                    return true;
                }
                try {
                    plugin.getConfig().set("waypoints." + args[1] + ".Access.isPublic", true);
                    plugin.saveConfig();
                } catch (Error exc) {
                    if (sender instanceof Player) {
                        ((Player) sender).getPlayer().sendMessage(ChatColor.RED + "An error occurred.");
                    } else {
                        Bukkit.getLogger().warning("An error occurred.");
                    }
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("forceName")) {
                if (args.length < 3) {

                    // TODO put usage
                    sender.sendMessage("TODO put usage");

                    return true;
                }
                try {
                    copyConfigSection(plugin.getConfig(), "waypoints." + args[1], "waypoints." + args[2]);
                    plugin.getConfig().set("waypoints." + args[1], null);
                    plugin.saveConfig();
                } catch (Error exc) {
                    if (sender instanceof Player) {
                        ((Player) sender).getPlayer().sendMessage(ChatColor.RED + "An error occurred or the beacon was not found.");
                    } else {
                        Bukkit.getLogger().warning("An error occurred or the beacon was not found.");
                    }
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("forceDisplay")) {
                if (args.length < 3) {

                    // TODO put usage
                    sender.sendMessage("TODO put usage");

                    return true;
                }
                try {
                    plugin.getConfig().set("waypoints." + args[1] + ".Display", args[2]);
                    plugin.saveConfig();
                } catch (Error exc) {
                    if (sender instanceof Player) {
                        ((Player) sender).getPlayer().sendMessage(ChatColor.RED + "An error occurred or the beacon was not found.");
                    } else {
                        Bukkit.getLogger().warning("An error occurred or the beacon was not found.");
                    }
                }
                return true;
            }
        }
        if (sender instanceof Player) {
            ((Player) sender).getPlayer().sendMessage(ChatColor.RED + "Commands are currently not enabled.");
        } else {
            Bukkit.getLogger().warning("Commands are currently not enabled.");
        }
        return true;
    }
}
