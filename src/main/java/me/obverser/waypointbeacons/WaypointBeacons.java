/*
Hello! This is the plugin's source code--nothing too special.
I might add a section in the config file to toggle features, but that will be in a later revision.
 */

package me.obverser.waypointbeacons;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/*
Importing and packaging, all that good stuff.
 */

public final class WaypointBeacons extends JavaPlugin implements Listener {

    /*
    Establishing some variables (Is that what they are called in Java? Classes maybe?), for future use.
     */

    Plugin plugin = this; // Getting the plugin.
    Inventory beaconGUI; // Creating the Beacon Primary GUI.
    Inventory nameGUI; // Creating the Beacon Name GUI (Not yet added)
    WaypointListings availableWaypoints = new WaypointListings(); // Referencing the other java file, creating a paged list for the waypoints.
    Inventory conditionalWaypoints; // Creating a conditional waypoint holder, for "availableWaypoints." This is so I can check if the player is interacting with the right GUI.

    public void createBeaconInventory() { // Creating the inventories, run on onEnable()
        // Creating Beacon GUI
        beaconGUI = Bukkit.createInventory(null, 9);
        ItemStack nameChange = new ItemStack(Material.NAME_TAG, 1);
        ItemMeta nameChangeMeta = nameChange.getItemMeta();
        nameChangeMeta.setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.ITALIC + "Change Beacon Name");
        nameChange.setItemMeta(nameChangeMeta);
        beaconGUI.setItem(0, nameChange);
        ItemStack teleportLocation = new ItemStack(Material.BEACON, 1);
        ItemMeta teleportLocationMeta = teleportLocation.getItemMeta();
        teleportLocationMeta.setDisplayName(ChatColor.GOLD + "Teleport To...");
        teleportLocation.setItemMeta(teleportLocationMeta);
        teleportLocation.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 10);
        teleportLocation.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        beaconGUI.setItem(8, teleportLocation);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Getting GUI interact events.
        if (event.getInventory() == beaconGUI) {
            // So there is no duplication.
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            // Conditionals! You gotta love 'em.
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            Player plr = (Player) event.getWhoClicked();
            if (clickedItem.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.GOLD + "Teleport To...")) {
                // Opening the paged GUI and saving it for conditionals later on.
                conditionalWaypoints = availableWaypoints.setPage(((Player) event.getWhoClicked()).getPlayer(), 0);
                plr.openInventory(conditionalWaypoints);
            }
        }
        // Here is the conditionals (the later on ones!).
        if (event.getInventory() == conditionalWaypoints) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            Player plr = (Player) event.getWhoClicked();
            // Changing pages.
            if (clickedItem.getItemMeta().getDisplayName().equalsIgnoreCase("Next")) {
                conditionalWaypoints = availableWaypoints.nextPage(((Player) event.getWhoClicked()).getPlayer(), true);
                plr.openInventory(conditionalWaypoints);
            } else if (clickedItem.getItemMeta().getDisplayName().equalsIgnoreCase("Previous")) {
                conditionalWaypoints = availableWaypoints.nextPage(((Player) event.getWhoClicked()).getPlayer(), false);
                plr.openInventory(conditionalWaypoints);
            } else {
                // The teleportation. Will add some pizzaz here later, maybe even a range--who knows.
                Location toLoc = getConfig().getLocation("waypoints." + clickedItem.getItemMeta().getDisplayName()).clone().add(0.5, 1, 0.5);
                plr.teleport(toLoc);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryDragEvent event) {
        // More duplication prevention.
        if (event.getInventory() != beaconGUI || event.getInventory() != conditionalWaypoints) return;
        event.setCancelled(true);
    }

    @Override
    public void onEnable() {
        // Sending the plugin data to the other java file--so it only gets called on once. (Don't have to extend JavaPlugin there--which would cause it to run itself.)
        availableWaypoints.establishPlugin(plugin);
        // Getting config file. (You have to manually create it because I have no idea how to do so automatically.)
        plugin.saveDefaultConfig();
        // Registering the plugin.
        getServer().getPluginManager().registerEvents(this, this);
        // The cool beacon items.
        ItemStack wayBeacon = new ItemStack(Material.BEACON, 1);
        ItemMeta wayBeaconMeta = wayBeacon.getItemMeta();
        wayBeaconMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Waypoint Beacon");
        wayBeacon.setItemMeta(wayBeaconMeta);
        wayBeacon.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 10);
        wayBeacon.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        // Creating a crafting recipe for it, very easy to change.
        NamespacedKey wayKey = new NamespacedKey(this, "waypoint_beacon");
        ShapedRecipe wayRecipe = new ShapedRecipe(wayKey, wayBeacon);
        wayRecipe.shape("ENE", "GBG", "OOO");
        wayRecipe.setIngredient('E', Material.ENDER_PEARL);
        wayRecipe.setIngredient('N', Material.BLAZE_POWDER);
        wayRecipe.setIngredient('G', Material.GLASS);
        wayRecipe.setIngredient('B', Material.BEACON);
        wayRecipe.setIngredient('O', Material.OBSIDIAN);
        Bukkit.addRecipe(wayRecipe);
        // Creating the beacon's inventory.
        createBeaconInventory();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player plr = event.getPlayer();
        Block blck = event.getBlockPlaced();
        ItemStack itm = event.getItemInHand();
        // Checking if it's the waypoint beacon.
        if (itm.getItemMeta().hasEnchant(Enchantment.ARROW_KNOCKBACK)) {
            if (itm.getType() == Material.BEACON) {
                // Saving it to the config file.
                Location beaconLocation = blck.getLocation();
                Boolean cond = false;
                Integer placement = 0;
                String name = "";
                // Cycling through default names, can be changed later from beacon menu.
                while (!cond) {
                    if (getConfig().getConfigurationSection("waypoints").contains("Unnamed" + placement.toString())) {
                        placement += 1;
                    } else {
                        name = "Unnamed" + placement.toString();
                        cond = true;
                    }
                }
                // Saving to config.
                getConfig().set("waypoints." + name, beaconLocation);
                saveConfig();
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Removing the beacon and refunding the player. (You can add a conditional that only gives it if the tool used could pickup a regular beacon.
        Block blk = event.getBlock();
        Player plr = event.getPlayer();
        if (blk.getType().equals(Material.BEACON)) {
            Object[] listOfWaypoints = getConfig().getConfigurationSection("waypoints").getKeys(false).toArray();
            // We don't want to waste any resources, more than this plugin already takes up.
            if (listOfWaypoints == null) return;
            for (Object key : listOfWaypoints) {
                if (blk.getLocation().equals(getConfig().getLocation("waypoints." + key))) {
                    // Removing the beacon from config.
                    getConfig().set("waypoints." + key, null);
                    saveConfig();
                    // Getting the waypoint beacon again.
                    ItemStack wayBeacon = new ItemStack(Material.BEACON, 1);
                    ItemMeta wayBeaconMeta = wayBeacon.getItemMeta();
                    wayBeaconMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Waypoint Beacon");
                    wayBeacon.setItemMeta(wayBeaconMeta);
                    wayBeacon.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 10);
                    wayBeacon.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    blk.setType(Material.AIR);
                    blk.getLocation().getWorld().dropItemNaturally(blk.getLocation(), wayBeacon);
                }
            }
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        // Interact event--for opening the better GUI.
        Player plr = (Player) event.getPlayer();
        // Checking if it is a waypoint beacon.
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock().getBlockData().getMaterial() == Material.BEACON) {
                Object[] listOfWaypoints = getConfig().getConfigurationSection("waypoints").getKeys(false).toArray();
                if (listOfWaypoints == null) return;
                for (Object key : listOfWaypoints) {
                    // Disabling the default beacon GUI and opening the custom GUI.
                    if (event.getClickedBlock().getLocation().equals(getConfig().getLocation("waypoints." + key))) {
                        event.setCancelled(true);
                        plr.openInventory(beaconGUI);
                    }
                }
            }
        }
    }
}

/*
And that's it! You can read the other java file for more info. This is a very simple plugin and will get some pizzaz maybe tomorrow.

Thank you for checking it out!

- Gabriel (Obverser)
 */
