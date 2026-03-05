package me.mark.parservice;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Arrays;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Parservice is Online!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("adminpanel")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this!");
                return true;
            }
            openMainGUI((Player) sender);
            return true;
        }
        return false;
    }

    public void openMainGUI(Player player) {
        // Create a 27-slot menu (3 rows)
        Inventory gui = Bukkit.createInventory(null, 27, "§0Parservice Admin Panel");

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
