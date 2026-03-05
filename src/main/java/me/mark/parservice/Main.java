package me.mark.parservice;

import net.milkbowl.vault.economy.Economy;
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
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {

    private static Economy econ = null;
    private final HashMap<UUID, UUID> targetCache = new HashMap<>();
    private final ArrayList<UUID> vanished = new ArrayList<>();

    @Override
    public void onEnable() {
        setupEconomy();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Parservice: Dashboard & Vanish Loaded!");
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) econ = rsp.getProvider();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("adminpanel") && sender instanceof Player) {
            openMainGUI((Player) sender);
            return true;
        }
        return false;
    }

    // --- MENUS ---

    public void openMainGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§0Parservice Admin Panel");
        
        gui.setItem(10, createGuiItem(Material.BONE, "§c§lKill All Mobs", "§7Removes all hostile monsters."));
        gui.setItem(12, createGuiItem(Material.DIAMOND, "§b§lClear Lag", "§7Removes all ground items."));
        gui.setItem(14, createGuiItem(Material.PLAYER_HEAD, "§e§lManage Players", "§7Manage online users."));
        
        // New Dashboard and Vanish Buttons
        gui.setItem(16, createGuiItem(Material.REDSTONE_TORCH, "§6§lServer Health", "§7View TPS and RAM usage."));
        
        Material vanishMat = vanished.contains(player.getUniqueId()) ? Material.ENDER_EYE : Material.ENDER_PEARL;
        String vanishName = vanished.contains(player.getUniqueId()) ? "§aVanish: ON" : "§cVanish: OFF";
        gui.setItem(26, createGuiItem(vanishMat, vanishName, "§7Hide from other players."));

        player.openInventory(gui);
    }

    public void openHealthMenu(Player player) {
        Inventory health = Bukkit.createInventory(null, 9, "§0Server Health");
        
        // RAM Info
        long usedRAM = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576L;
        long maxRAM = Runtime.getRuntime().maxMemory() / 1048576L;
        
        health.setItem(4, createGuiItem(Material.CLOCK, "§6Performance", 
            "§7RAM: §f" + usedRAM + "MB / " + maxRAM + "MB",
            "§7Online: §f" + Bukkit.getOnlinePlayers().size()));
            
        health.setItem(8, createGuiItem(Material.ARROW, "§7Back"));
        player.openInventory(health);
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

    public void openPunishMenu(Player admin, Player target) {
        Inventory punish = Bukkit.createInventory(null, 9, "§cManage: " + target.getName());
        punish.setItem(0, createGuiItem(Material.COMPASS, "§aTeleport"));
        punish.setItem(2, createGuiItem(Material.GOLD_INGOT, "§eGive $1,000"));
        punish.setItem(4, createGuiItem(Material.BARRIER, "§6Kick"));
        punish.setItem(8, createGuiItem(Material.ARROW, "§7Back"));
        targetCache.put(admin.getUniqueId(), target.getUniqueId());
        admin.openInventory(punish);
    }

    // --- EVENTS ---

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        String title = event.getView().getTitle();
        Player admin = (Player) event.getWhoClicked();

        if (title.equals("§0Parservice Admin Panel")) {
            event.setCancelled(true);
            Material m = event.getCurrentItem().getType();
            if (m == Material.PLAYER_HEAD) openPlayerList(admin);
            else if (m == Material.REDSTONE_TORCH) openHealthMenu(admin);
            else if (m == Material.ENDER_PEARL || m == Material.ENDER_EYE) toggleVanish(admin);
            else if (m == Material.BONE) admin.getWorld().getEntitiesByClass(org.bukkit.entity.Monster.class).forEach(e -> e.remove());
        }
        else if (title.equals("§0Server Health")) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == Material.ARROW) openMainGUI(admin);
        }
        else if (title.equals("§0Online Players")) {
            event.setCancelled(true);
            String name = event.getCurrentItem().getItemMeta().getDisplayName().substring(2);
            Player target = Bukkit.getPlayer(name);
            if (target != null) openPunishMenu(admin, target);
        }
        else if (title.startsWith("§cManage: ")) {
            event.setCancelled(true);
            handlePunishClick(admin, event.getCurrentItem().getType());
        }
    }

    private void toggleVanish(Player p) {
        if (vanished.contains(p.getUniqueId())) {
            vanished.remove(p.getUniqueId());
            Bukkit.getOnlinePlayers().forEach(online -> online.showPlayer(this, p));
            p.sendMessage("§cYou are no longer invisible.");
        } else {
            vanished.add(p.getUniqueId());
            Bukkit.getOnlinePlayers().forEach(online -> online.hidePlayer(this, p));
            p.sendMessage("§aYou are now invisible!");
        }
        openMainGUI(p); // Refresh menu to show icon change
    }

    private void handlePunishClick(Player admin, Material clicked) {
        Player target = Bukkit.getPlayer(targetCache.get(admin.getUniqueId()));
        if (target == null) return;

        if (clicked == Material.COMPASS) admin.teleport(target.getLocation());
        else if (clicked == Material.GOLD_INGOT && econ != null) econ.depositPlayer(target, 1000);
        else if (clicked == Material.BARRIER) target.kickPlayer("§cKicked!");
        else if (clicked == Material.ARROW) openPlayerList(admin);
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
