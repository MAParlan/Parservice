package me.mark.parservice;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Parservice is Online!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("adminpanel")) {
            if (!(sender instanceof Player)) return true;
            openMainGUI((Player) sender);
            return true;
        }
        return false;
    }

    // MAIN MENU
    public void openMainGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§0Parservice Admin Panel");
        gui.setItem(11, createGuiItem(Material.BONE, "§c§lKill All Mobs", "§7Removes all hostile monsters."));
        gui.setItem(13, createGuiItem(Material.DIAMOND, "§b§lClear Lag", "§7Removes all ground items."));
        gui.setItem(15, createGuiItem(Material.PLAYER_HEAD, "§e§lManage Players", "§7Click to see online players."));
        player.openInventory(gui);
    }

    // NEW: PLAYER LIST MENU
    public void openPlayerList(Player admin) {
        Inventory playerList = Bukkit.createInventory(null, 54, "§0Online Players");
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(p);
            meta.setDisplayName("§f" + p.getName());
            meta.setLore(Arrays.asList("§7Click to manage this player."));
            head.setItemMeta(meta);
            playerList.addItem(head);
        }
        admin.openInventory(playerList);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (event.getCurrentItem() == null) return;
        Player player = (Player) event.getWhoClicked();

        // Logic for Main Menu
        if (title.equals("§0Parservice Admin Panel")) {
            event.setCancelled(true);
            Material clicked = event.getCurrentItem().getType();

            if (clicked == Material.BONE) {
                player.performCommand("killall monsters"); // Simple way if you have other plugins
                player.sendMessage("§cCleared monsters!");
            } 
            else if (clicked == Material.DIAMOND) {
                player.sendMessage("§bCleared ground items!");
            }
            else if (clicked == Material.PLAYER_HEAD) {
                openPlayerList(player);
            }
        }
        
        // Logic for Player List
        else if (title.equals("§0Online Players")) {
            event.setCancelled(true);
            // We will add "Kick/Ban" logic here next!
            player.sendMessage("§eYou clicked on: " + event.getCurrentItem().getItemMeta().getDisplayName());
        }
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}
