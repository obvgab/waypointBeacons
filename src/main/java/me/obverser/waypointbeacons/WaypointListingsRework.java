package me.obverser.waypointbeacons;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class WaypointListingsRework implements InventoryHolder {
    private int page = 0;
    private Plugin plugin;
    private Boolean radiusEnabled;
    private Integer radius;
    private Block currentBlock;
    private Player player;

    public void establishPlugin(Plugin plugin, Boolean radiusEnabled, Integer radius, Block currentBlock, Player player) {
        this.plugin = plugin;
        this.radiusEnabled = radiusEnabled;
        this.radius = radius;
        this.currentBlock = currentBlock;
        this.player = player;
    }

    public Inventory getInventory() {
        ItemStack previousItem = new ItemStack(Material.BARRIER);
        ItemMeta prevMeta = previousItem.getItemMeta();
        prevMeta.setDisplayName("Previous");
        previousItem.setItemMeta(prevMeta);
        ItemStack nextItem = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextItem.getItemMeta();
        nextMeta.setDisplayName("Next");
        nextItem.setItemMeta(nextMeta);
        Inventory GUI = Bukkit.createInventory(this, 54, "Waypoints");
        List<ItemStack> wloc = new ArrayList<>();
        plugin.getConfig().getConfigurationSection("waypoints").getKeys(false).forEach(key -> {
            ItemStack tempItem = new ItemStack(Material.getMaterial(plugin.getConfig().getString("waypoints." + key.toString() + ".Display")), 1);
            ItemMeta tempMeta = tempItem.getItemMeta();
            tempMeta.setDisplayName(key.toString().replaceAll("%_", " "));
            Location tempLocation = plugin.getConfig().getLocation("waypoints." + key + ".Location");
            if (!(tempLocation == null)) {
                OfflinePlayer tempPlayer = Bukkit.getOfflinePlayer(UUID.fromString(plugin.getConfig().getString("waypoints." + key + ".Access.Owner")));
                tempMeta.setLore(Arrays.asList("X : " + tempLocation.getBlockX(), "Y : " + tempLocation.getBlockY(), "Z : " + tempLocation.getBlockZ(), "Owner : " + tempPlayer.getName()));
            }
            tempItem.setItemMeta(tempMeta);
            Boolean isPublic = true;
            Boolean isAccessible = false;
            if (!plugin.getConfig().getBoolean("waypoints." + key + ".Access.isPublic")) {
                isPublic = false;
                List<String> listOfPlayers = plugin.getConfig().getStringList("waypoints." + key + ".Access.Players");
                for (String uuid : listOfPlayers) {
                    if (player.getUniqueId().equals(UUID.fromString(uuid))) isAccessible = true;
                }
                if (UUID.fromString(plugin.getConfig().getString("waypoints." + key + ".Access.Owner")).equals(player.getUniqueId())) {
                    isAccessible = true;
                }
            }
            if (radiusEnabled) {
                if (plugin.getConfig().getLocation("waypoints." + key + ".Location").distance(currentBlock.getLocation()) <= radius) {
                    if (isPublic || isAccessible) {
                        wloc.add(tempItem);
                    }
                } else { return; }
            } else {
                wloc.add(tempItem);
            }
        });
        if (wloc.size() > 45) {
            if (page > 0) {
                GUI.setItem(45, previousItem);
            }
            if (page < wloc.size() / 45) {
                GUI.setItem(53, nextItem);
            }
        }
        for (int i = page * 45; i < 45 + (page * 45); i ++) {
            if (i >= wloc.size()) break;
            GUI.addItem(wloc.get(i));
        }
        return GUI;
    }

    public Inventory setPage(int page) {
        this.page = page;
        return getInventory();
    }

    public Inventory nextPage(boolean pageForward) {
        if (pageForward) {
            this.page += 1;
        } else {
            this.page -= 1;
        }
        return getInventory();
    }
}
