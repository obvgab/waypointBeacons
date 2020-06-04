package me.obverser.waypointbeacons;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public final class InventoryListeners implements Listener {

    private Plugin plugin;
    private Player player;
    private Block block;
    private ItemStack customBeacon;
    private Inventory tempPage;
    private Boolean isInventoryOpen = true;
    private Integer radiusValue;
    private Inventory beaconBaseGUI;
    private WaypointBeaconsRework host;
    private Inventory nameGUI;
    WaypointListingsRework availableWaypoints = new WaypointListingsRework();

    public ItemStack getBeaconItemStack() {
        ItemStack wayBeacon = new ItemStack(Material.BEACON, 1);
        ItemMeta wayBeaconMeta = wayBeacon.getItemMeta();
        wayBeaconMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Waypoint Beacon");
        wayBeacon.setItemMeta(wayBeaconMeta);
        wayBeacon.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 10);
        wayBeacon.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return wayBeacon;
    }

    public Inventory createBeaconGUI(Player owner, String name) {
        Inventory tempBeaconBaseGUI = Bukkit.createInventory(owner, 9, name);
        ItemStack tempTeleport = getBeaconItemStack();
        ItemMeta tempMeta = tempTeleport.getItemMeta();
        tempMeta.setDisplayName(ChatColor.GOLD + "Teleport To...");
        tempTeleport.setItemMeta(tempMeta);
        ItemStack renameItem = new ItemStack(Material.ANVIL, 1);
        ItemMeta renameMeta = renameItem.getItemMeta();
        renameMeta.setDisplayName(ChatColor.GOLD + "Change Name/Display...");
        renameItem.setItemMeta(renameMeta);
        tempBeaconBaseGUI.setItem(0, tempTeleport);
        tempBeaconBaseGUI.setItem(8, renameItem);
        return tempBeaconBaseGUI;
    }

    public String getBeaconName(Location locationIn) {
        Object[] listOfWaypoints = plugin.getConfig().getConfigurationSection("waypoints").getKeys(false).toArray();
        if (listOfWaypoints == null) return null;
        for (Object key : listOfWaypoints) {
            if (locationIn.equals(plugin.getConfig().getLocation("waypoints." + key + ".Location"))) {
                return key.toString();
            }
        }
        return null;
    }

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

    public void refreshValues() {
        if (plugin.getConfig().getBoolean("settings.radius.radiusEnabled")) {
            this.radiusValue = plugin.getConfig().getInt("settings.radius.value");
        } else {
            this.radiusValue = null;
        }
    }

    public void establishVars(Plugin plugin, Player player, Block block, ItemStack customBeacon, WaypointBeaconsRework host) {
        this.host = host;
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.customBeacon = customBeacon;
        if (plugin.getConfig().getBoolean("settings.radius.radiusEnabled")) {
            this.radiusValue = plugin.getConfig().getInt("settings.radius.value");
        } else {
            this.radiusValue = null;
        }
        this.beaconBaseGUI = createBeaconGUI(player, getBeaconName(block.getLocation()));
        this.nameGUI = Bukkit.createInventory(player, 9, "Changing Name/Display");
        ItemStack tempItem1 = new ItemStack(Material.NAME_TAG);
        ItemMeta tempMeta1 = tempItem1.getItemMeta();
        tempMeta1.setDisplayName(ChatColor.GOLD + "Change Display");
        tempMeta1.setLore(Arrays.asList("Changes the item displayed on", "the menu to the item in", "your off hand."));
        tempItem1.setItemMeta(tempMeta1);
        this.nameGUI.setItem(0, tempItem1);
        ItemStack tempItem2 = new ItemStack(Material.LECTERN);
        ItemMeta tempMeta2 = tempItem1.getItemMeta();
        tempMeta2.setDisplayName(ChatColor.GOLD + "Change Name");
        tempMeta2.setLore(Arrays.asList("Changes the name of the beacon", "shown in the menu."));
        tempItem2.setItemMeta(tempMeta2);
        this.nameGUI.setItem(8, tempItem2);
        player.openInventory(beaconBaseGUI);
        availableWaypoints.establishPlugin(plugin, plugin.getConfig().getBoolean("settings.radius.radiusEnabled"), radiusValue, block, player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getWhoClicked().equals(player)) {
            return;
        }
        if (event.getInventory() == beaconBaseGUI) {
            event.setCancelled(true);
            ItemStack tempTeleport = getBeaconItemStack();
            ItemMeta tempMeta = tempTeleport.getItemMeta();
            tempMeta.setDisplayName(ChatColor.GOLD + "Teleport To...");
            tempTeleport.setItemMeta(tempMeta);
            ItemStack renameItem = new ItemStack(Material.ANVIL, 1);
            ItemMeta renameMeta = renameItem.getItemMeta();
            renameMeta.setDisplayName(ChatColor.GOLD + "Change Name/Display...");
            renameItem.setItemMeta(renameMeta);
            if (event.getCurrentItem().equals(tempTeleport)) {
                tempPage = availableWaypoints.setPage(0);
                isInventoryOpen = !isInventoryOpen;
                player.openInventory(tempPage);
                isInventoryOpen = !isInventoryOpen;
            }
            if (event.getCurrentItem().equals(renameItem)) {
                isInventoryOpen = !isInventoryOpen;
                player.openInventory(nameGUI);
                isInventoryOpen = !isInventoryOpen;
            }
        } else if (event.getInventory() == tempPage) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            Player plr = (Player) event.getWhoClicked();
            if (clickedItem.getItemMeta().getDisplayName().equalsIgnoreCase("Next")) {
                tempPage = availableWaypoints.nextPage(true);
                isInventoryOpen = !isInventoryOpen;
                plr.openInventory(tempPage);
                isInventoryOpen = !isInventoryOpen;
            } else if (clickedItem.getItemMeta().getDisplayName().equalsIgnoreCase("Previous")) {
                tempPage = availableWaypoints.nextPage(false);
                isInventoryOpen = !isInventoryOpen;
                plr.openInventory(tempPage);
                isInventoryOpen = !isInventoryOpen;
            } else {
                Location toLoc = plugin.getConfig().getLocation("waypoints." + clickedItem.getItemMeta().getDisplayName() + ".Location").clone().add(0.5, 1, 0.5);
                plr.teleport(toLoc);
            }
        } else if (event.getInventory() == nameGUI) {
            ItemStack tempItem1 = new ItemStack(Material.NAME_TAG);
            ItemMeta tempMeta1 = tempItem1.getItemMeta();
            tempMeta1.setDisplayName(ChatColor.GOLD + "Change Display");
            tempMeta1.setLore(Arrays.asList("Changes the item displayed on", "the menu to the item in", "your off hand."));
            tempItem1.setItemMeta(tempMeta1);
            ItemStack tempItem2 = new ItemStack(Material.LECTERN);
            ItemMeta tempMeta2 = tempItem1.getItemMeta();
            tempMeta2.setDisplayName(ChatColor.GOLD + "Change Name");
            tempMeta2.setLore(Arrays.asList("Changes the name of the beacon", "shown in the menu."));
            tempItem2.setItemMeta(tempMeta2);
            event.setCancelled(true);
            if (event.getCurrentItem().equals(tempItem2)) {
                isInventoryOpen = !isInventoryOpen;
                new AnvilGUI.Builder()
                        .onClose(player -> {
                            removeListing();
                            host.removeInstanceFromMemory(this);
                        })
                        .onComplete((player, text) -> {
                            String tempBeaconName = getBeaconName(block.getLocation());
                            if (plugin.getConfig().getConfigurationSection("waypoints").getKeys(false).toString().contentEquals(text)) {
                                return AnvilGUI.Response.text("Beacon already exists.");
                            }
                            copyConfigSection(plugin.getConfig(), "waypoints." + tempBeaconName, "waypoints." + text);
                            plugin.getConfig().set("waypoints." + tempBeaconName, null);
                            plugin.saveConfig();
                            return AnvilGUI.Response.close();
                        })
                        .text(getBeaconName(block.getLocation()))
                        .item(new ItemStack(Material.BEACON))
                        .title("Enter new beacon name.")
                        .plugin(host)
                        .open(player);
            } else if (event.getCurrentItem().equals(tempItem1)) {
                if (player.getInventory().getItemInOffHand().getType() == null || player.getInventory().getItemInOffHand().getType() == Material.AIR) {
                    player.sendMessage("You must have an item in your off hand!");
                    return;
                }
                plugin.getConfig().set("waypoints." + getBeaconName(block.getLocation()) + ".Display", player.getInventory().getItemInOffHand().getType().toString());
                plugin.saveConfig();
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory() == beaconBaseGUI || event.getInventory() == tempPage) {
            if (isInventoryOpen) {
                removeListing();
                host.removeInstanceFromMemory(this);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer().equals(player)) {
            removeListing();
            host.removeInstanceFromMemory(this);
        }
    }

    public void removeListing() {
        availableWaypoints = null;
        tempPage = null;
    }
}
