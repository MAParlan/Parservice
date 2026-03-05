package me.mark.parservice;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // This line is VERY important. It tells the server to watch for clicks!
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Parservice is Online and Listening!");
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

    public void openMainGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§0Parservice Admin Panel");

        gui.setItem(11, createGuiItem(Material.BONE, "§c§lKill All Mobs", "§7Removes all hostile monsters in this world."));
        gui.setItem(13, createGuiItem(Material.DIAMOND, "§b§lClear Lag", "§7Removes all dropped items on the ground."));
        gui.setItem(15, createGuiItem(Material.PLAYER_HEAD, "§e§lManage Players", "§7Coming soon..."));

        player.openInventory(gui);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        // 1. Check if it's our menu
        if (event.getView().getTitle().equals("§0Parservice Admin Panel")) {
            event.setCancelled(true); // Stop player from taking the item

            if (event.getCurrentItem() == null) return;
            Player player = (Player) event.getWhoClicked();
            Material clicked = event.getCurrentItem().getType();

            // 2. Button Logic
            if (clicked == Material.BONE) {
                killAllMobs(player.getWorld());
                player.sendMessage("§c[Parservice] All monsters have been cleared!");
                player.closeInventory();
            } 
            else if (clicked == Material.DIAMOND) {
                clearDroppedItems(player.getWorld());
                player.sendMessage("§b[Parservice] All ground items have been cleared!");
                player.closeInventory();
            }
        }
    }

    private void killAllMobs(World world) {
        for (LivingEntity entity : world.getLivingEntities()) {
            if (entity instanceof Monster) { // Only kills hostile mobs like Zombies/Creepers
                entity.remove();
            }
        }
    }

    private void clearDroppedItems(World world) {
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Item) { // Only removes items on the ground
                entity.remove();
            }
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
        // Button 1: Kill All Mobs (Bone)
        gui.setItem(11, createGuiItem(Material.BONE, "§c§lKill All Mobs", "§7Clears all hostile monsters nearby."));

        // Button 2: Clear Lag (Diamond)
        gui.setItem(13, createGuiItem(Material.DIAMOND, "§b§lClear Lag", "§7Removes ground items and clears RAM."));

        // Button 3: Player Management (Player Head)
        gui.setItem(15, createGuiItem(Material.PLAYER_HEAD, "§e§lManage Players", "§7Open the player moderation list."));

        player.openInventory(gui);
    }

    // Helper method to make creating items easier
    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}
