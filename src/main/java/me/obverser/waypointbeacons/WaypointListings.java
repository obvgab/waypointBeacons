/*
Hello! This is a separate file for the source code, just for paged GUI essentially.
Credit goes to whoever made this tutorial/example on the forums.
 */

package me.obverser.waypointbeacons;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;


import java.util.ArrayList;
import java.util.List;

// All those good imports.

public class WaypointListings implements InventoryHolder {
    // Setting some essential variables, like the page number and the plugin for configs and the such.
    private int page = 0;
    private Plugin plugin;

    // Getting the plugin from the host script (Probably not the right term.)
    public void establishPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    // This sets and creates the temporary paged inventory.
    public Inventory getInventory() {
        // Items for navigation. Might add a setPage item when I figure out how to get text input. Trying to make the plugin not dependant on any other plugins.
        ItemStack previousItem = new ItemStack(Material.BARRIER);
        ItemMeta prevMeta = previousItem.getItemMeta();
        prevMeta.setDisplayName("Previous");
        previousItem.setItemMeta(prevMeta);
        ItemStack nextItem = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextItem.getItemMeta();
        nextMeta.setDisplayName("Next");
        nextItem.setItemMeta(nextMeta);
        // Creating a default GUI that will be returned.
        Inventory GUI = Bukkit.createInventory(this, 54, "Waypoints");
        // Creating the ItemStack list.
        List<ItemStack> wloc = new ArrayList<>();
        plugin.getConfig().getConfigurationSection("waypoints").getKeys(false).forEach(key -> {
            ItemStack tempItem = new ItemStack(Material.BEACON, 1);
            ItemMeta tempMeta = tempItem.getItemMeta();
            tempMeta.setDisplayName(key.toString());
            tempItem.setItemMeta(tempMeta);
            wloc.add(tempItem);
        });
        // Adding the navigation items if necessary.
        if (wloc.size() > 45) {
            if (page > 0) {
                GUI.setItem(45, previousItem);
            }
            if (page < wloc.size() / 45) {
                GUI.setItem(53, nextItem);
            }
        }
        // Adding the items to the GUI, breaks when there are none left.
        for (int i = page * 45; i < 45 + (page * 45); i ++) {
            if (i >= wloc.size()) break;
            GUI.addItem(wloc.get(i));
        }
        // Giving the host/function the GUI.
        return GUI;
    }

    public Inventory setPage(Player plr, int page) {
        // Setting the page, usually for the initial open of the GUI.
        this.page = page;
        // Sending back the GUI.
        return getInventory();
    }

    public Inventory nextPage(Player plr, boolean pageForward) {
        // Moving through pages in the GUI, pageForward determines if it goes a page back or a page forward.
        if (pageForward) {
            this.page += 1;
        } else {
            this.page -= 1;
        }
        // Sending back the GUI.
        return getInventory();
    }
}

/*
And that's it. Hope this is useful!

-Gabriel (Obverser)
 */
