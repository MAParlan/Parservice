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

    public void openMainGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§0Parservice Admin Panel");

        gui.setItem(11, createGuiItem(Material.BONE, "§c§lKill All Mobs", "§7Removes all hostile monsters."));
        gui.setItem(13, createGuiItem(Material.DIAMOND, "§b§lClear Lag", "§7Removes all ground items."));
        gui.setItem(15, createGuiItem(Material.PLAYER_HEAD, "§e§lManage Players", "§7(Work in Progress)"));

        player.openInventory(gui);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("§0Parservice Admin Panel")) {
            event.setCancelled(true); 

            if (event.getCurrentItem() == null) return;
            Player player = (Player) event.getWhoClicked();
            Material clicked = event.getCurrentItem().getType();

            if (clicked == Material.BONE) {
                for (LivingEntity entity : player.getWorld().getLivingEntities()) {
                    if (entity instanceof Monster) entity.remove();
                }
                player.sendMessage("§c[Parservice] All monsters cleared!");
                player.closeInventory();
            } 
            else if (clicked == Material.DIAMOND) {
                for (Entity entity : player.getWorld().getEntities()) {
                    if (entity instanceof Item) entity.remove();
                }
                player.sendMessage("§b[Parservice] All ground items cleared!");
                player.closeInventory();
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
