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
import java.util.HashMap;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {

    // This helps the plugin remember which player an admin is looking at
    private final HashMap<UUID, UUID> targetCache = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Parservice: Punish Module Loaded!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("adminpanel") && sender instanceof Player) {
            openMainGUI((Player) sender);
            return true;
        }
        return false;
    }

    public void openMainGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§0Parservice Admin Panel");
        gui.setItem(11, createGuiItem(Material.BONE, "§c§lKill All Mobs", "§7Removes all hostile monsters."));
        gui.setItem(13, createGuiItem(Material.DIAMOND, "§b§lClear Lag", "§7Removes all ground items."));
        gui.setItem(15, createGuiItem(Material.PLAYER_HEAD, "§e§lManage Players", "§7Click to see online players."));
        player.openInventory(gui);
    }

    public void openPlayerList(Player admin) {
        Inventory playerList = Bukkit.createInventory(null, 54, "§0Online Players");
        for (Player p : Bukkit.getOnlinePlayers()) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(p);
            meta.setDisplayName("§f" + p.getName());
            head.setItemMeta(meta);
            playerList.addItem(head);
        }
        admin.openInventory(playerList);
    }

    // NEW: The specific menu for Kicking/Teleporting
    public void openPunishMenu(Player admin, Player target) {
        Inventory punish = Bukkit.createInventory(null, 9, "§cManage: " + target.getName());
        
        punish.setItem(2, createGuiItem(Material.COMPASS, "§aTeleport", "§7Go to this player."));
        punish.setItem(4, createGuiItem(Material.BARRIER, "§6Kick Player", "§7Remove them from the server."));
        punish.setItem(6, createGuiItem(Material.BEDROCK, "§4Ban Player", "§7Permanent ban."));

        targetCache.put(admin.getUniqueId(), target.getUniqueId());
        admin.openInventory(punish);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        String title = event.getView().getTitle();
        Player admin = (Player) event.getWhoClicked();

        // 1. MAIN MENU LOGIC
        if (title.equals("§0Parservice Admin Panel")) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == Material.PLAYER_HEAD) openPlayerList(admin);
        }
        
        // 2. PLAYER LIST LOGIC
        else if (title.equals("§0Online Players")) {
            event.setCancelled(true);
            String targetName = event.getCurrentItem().getItemMeta().getDisplayName().substring(2);
            Player target = Bukkit.getPlayer(targetName);
            if (target != null) openPunishMenu(admin, target);
        }

        // 3. PUNISH MENU LOGIC
        else if (title.startsWith("§cManage: ")) {
            event.setCancelled(true);
            UUID targetUUID = targetCache.get(admin.getUniqueId());
            if (targetUUID == null) return;
            Player target = Bukkit.getPlayer(targetUUID);

            if (target == null) {
                admin.sendMessage("§cPlayer is no longer online.");
                return;
            }

            Material clicked = event.getCurrentItem().getType();
            if (clicked == Material.COMPASS) {
                admin.teleport(target.getLocation());
                admin.sendMessage("§aTeleported to " + target.getName());
            } else if (clicked == Material.BARRIER) {
                target.kickPlayer("§cKicked by an admin using Parservice.");
            } else if (clicked == Material.BEDROCK) {
                Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(target.getName(), "§cBanned by Parservice", null, null);
                target.kickPlayer("§cBanned!");
            }
            admin.closeInventory();
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
