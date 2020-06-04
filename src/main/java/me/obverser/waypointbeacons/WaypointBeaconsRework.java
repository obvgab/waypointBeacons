package me.obverser.waypointbeacons;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.List;

public final class WaypointBeaconsRework extends JavaPlugin implements Listener {
    List<InventoryListeners> listOfListeners = new ArrayList<InventoryListeners>();

    public void reloadAllListenersRadius() {
        for (InventoryListeners listener : listOfListeners) {
            listener.refreshValues();
        }
    }

    public ItemStack getBeaconItemStack() {
        ItemStack wayBeacon = new ItemStack(Material.BEACON, 1);
        ItemMeta wayBeaconMeta = wayBeacon.getItemMeta();
        wayBeaconMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Waypoint Beacon");
        wayBeacon.setItemMeta(wayBeaconMeta);
        wayBeacon.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 10);
        wayBeacon.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return wayBeacon;
    }

    public void removeInstanceFromMemory(InventoryListeners instance) {
        HandlerList.unregisterAll(instance);
        listOfListeners.remove(listOfListeners.indexOf(instance));
    }

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(this, this);
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        saveDefaultConfig();
        NamespacedKey wayKey = new NamespacedKey(this, "waypoint_beacon");
        ShapedRecipe wayRecipe = new ShapedRecipe(wayKey, getBeaconItemStack()); wayRecipe.shape(getConfig().getString("settings.recipe.recipeFormat.line1"), getConfig().getString("settings.recipe.recipeFormat.line2"), getConfig().getString("settings.recipe.recipeFormat.line3"));
        Object[] listOfRecipeItems = getConfig().getConfigurationSection("settings.recipe.recipeItems").getKeys(false).toArray();
        for (Object recipeItem : listOfRecipeItems) {
            Material tempMaterial = Material.getMaterial(getConfig().getString("settings.recipe.recipeItems." + recipeItem.toString()));
            wayRecipe.setIngredient(recipeItem.toString().toCharArray()[0], tempMaterial);
        }
        // TODO: this is reload-unfriendly.  Run `Bukkit.removeRecipe(wayRecipe)` in onDisable().
        Bukkit.addRecipe(wayRecipe);
        WaypointCommands waypointCommandScript = new WaypointCommands();
        waypointCommandScript.getPlugin(this, this);
        getCommand("waypoints").setExecutor(waypointCommandScript);
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        Player plr = (Player) event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock().getBlockData().getMaterial() == Material.BEACON) {
                Object[] listOfWaypoints = getConfig().getConfigurationSection("waypoints").getKeys(false).toArray();
                if (listOfWaypoints == null) return;
                for (Object key : listOfWaypoints) {
                    if (event.getClickedBlock().getLocation().equals(getConfig().getLocation("waypoints." + key + ".Location"))) {
                        event.setCancelled(true);
                        InventoryListeners temporaryListener = new InventoryListeners();
                        ItemStack tempTeleport = getBeaconItemStack();
                        ItemMeta tempMeta = tempTeleport.getItemMeta();
                        tempMeta.setDisplayName(ChatColor.GOLD + "Teleport To...");
                        tempTeleport.setItemMeta(tempMeta);
                        temporaryListener.establishVars(this, event.getPlayer(), event.getClickedBlock(), tempTeleport, this);
                        getServer().getPluginManager().registerEvents(temporaryListener, this);
                        listOfListeners.add(temporaryListener);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack item = event.getItemInHand();
        if (item.getItemMeta().hasEnchant(Enchantment.ARROW_KNOCKBACK)) {
            Location location = block.getLocation();
            Boolean condition = false;
            Integer placement = 0;
            String name = "";
            while (!condition) {
                if (getConfig().getConfigurationSection("waypoints").contains("Unnamed" + placement.toString())) {
                    placement += 1;
                } else {
                    name = "Unnamed" + placement.toString();
                    condition = true;
                }
            }
            getConfig().set("waypoints." + name + ".Location", location);
            getConfig().set("waypoints." + name + ".Display", "BEACON");
            getConfig().set("waypoints." + name + ".Access.isPublic", true);
            saveConfig();
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block blk = event.getBlock();
        Player plr = event.getPlayer();
        if (blk.getType().equals(Material.BEACON)) {
            Object[] listOfWaypoints = getConfig().getConfigurationSection("waypoints").getKeys(false).toArray();
            if (listOfWaypoints == null) return;
            for (Object key : listOfWaypoints) {
                if (blk.getLocation().equals(getConfig().getLocation("waypoints." + key + ".Location"))) {
                    getConfig().set("waypoints." + key, null);
                    saveConfig();
                    if (event.getBlock().breakNaturally()) {
                        ItemStack wayBeacon = new ItemStack(Material.BEACON, 1);
                        ItemMeta wayBeaconMeta = wayBeacon.getItemMeta();
                        wayBeaconMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Waypoint Beacon");
                        wayBeacon.setItemMeta(wayBeaconMeta);
                        wayBeacon.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 10);
                        wayBeacon.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        blk.setType(Material.AIR);
                        blk.getLocation().getWorld().dropItemNaturally(blk.getLocation(), wayBeacon);
                    } else {
                        blk.setType(Material.AIR);
                    }
                }
            }
        }
    }

}
