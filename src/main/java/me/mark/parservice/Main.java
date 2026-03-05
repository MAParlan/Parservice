package me.mark.parservice;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
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
    private boolean chatMuted = false;

    @Override
    public void onEnable() {
        setupEconomy();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Parservice: Spawner Module Loaded!");
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
        
        gui.setItem(10, createGuiItem(Material.BONE, "§c§lKill All Mobs"));
        gui.setItem(11, createGuiItem(Material.DIAMOND, "§b§lClear Lag"));
        gui.setItem(13, createGuiItem(Material.PLAYER_HEAD, "§e§lManage Players"));
        gui.setItem(15, createGuiItem(Material.GRASS_BLOCK, "§a§lWorld Settings"));
        gui.setItem(16, createGuiItem(Material.CHICKEN_SPAWN_EGG, "§6§lSpawn Menu", "§7Summon entities and items."));
        
        Material vanishMat = vanished.contains(player.getUniqueId()) ? Material.ENDER_EYE : Material.ENDER_PEARL;
        gui.setItem(26, createGuiItem(vanishMat, "§d§lVanish Mode"));

        player.openInventory(gui);
    }

    public void openSpawnMenu(Player player) {
        Inventory spawn = Bukkit.createInventory(null, 9, "§0Spawn Menu");
        
        spawn.setItem(0, createGuiItem(Material.TNT, "§cSummon Primed TNT"));
        spawn.setItem(1, createGuiItem(Material.IRON_BLOCK, "§fSummon Iron Golem"));
        spawn.setItem(2, createGuiItem(Material.VILLAGER_SPAWN_EGG, "§6Summon Villager"));
        
        spawn.setItem(5, createGuiItem(Material.BARRIER, "§cGet Barrier Block"));
        spawn.setItem(6, createGuiItem(Material.COMMAND_BLOCK, "§6Get Command Block"));
        
        spawn.setItem(8, createGuiItem(Material.ARROW, "§7Back"));
        player.openInventory(spawn);
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
            if (m == Material.CHICKEN_SPAWN_EGG) openSpawnMenu(admin);
            else if (m == Material.GRASS_BLOCK) openWorldSettings(admin);
            else if (m == Material.PLAYER_HEAD) openPlayerList(admin);
            else if (m == Material.ENDER_PEARL || m == Material.ENDER_EYE) toggleVanish(admin);
            else if (m == Material.BONE) admin.getWorld().getEntitiesByClass(Monster.class).forEach(Entity::remove);
            else if (m == Material.DIAMOND) admin.getWorld().getEntitiesByClass(Item.class).forEach(Entity::remove);
        }
        else if (title.equals("§0Spawn Menu")) {
            event.setCancelled(true);
            Material m = event.getCurrentItem().getType();
            if (m == Material.TNT) admin.getWorld().spawnEntity(admin.getLocation(), EntityType.TNT);
            else if (m == Material.IRON_BLOCK) admin.getWorld().spawnEntity(admin.getLocation(), EntityType.IRON_GOLEM);
            else if (m == Material.VILLAGER_SPAWN_EGG) admin.getWorld().spawnEntity(admin.getLocation(), EntityType.VILLAGER);
            else if (m == Material.BARRIER) admin.getInventory().addItem(new ItemStack(Material.BARRIER));
            else if (m == Material.COMMAND_BLOCK) admin.getInventory().addItem(new ItemStack(Material.COMMAND_BLOCK));
            else if (m == Material.ARROW) openMainGUI(admin);
        }
        // (Keep World Settings, Player List, and Punish logic here)
    }

    // Helper methods (createGuiItem, openWorldSettings, openPlayerList, toggleVanish, etc.)
    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void openWorldSettings(Player player) {
        Inventory world = Bukkit.createInventory(null, 9, "§0World Settings");
        world.setItem(0, createGuiItem(Material.SUNFLOWER, "§eSet Day"));
        world.setItem(3, createGuiItem(Material.WATER_BUCKET, "§bClear Weather"));
        world.setItem(8, createGuiItem(Material.ARROW, "§7Back"));
        player.openInventory(world);
    }

    public void openPlayerList(Player admin) {
        Inventory playerList = Bukkit.createInventory(null, 54, "§0Online Players");
        for (Player p : Bukkit.getOnlinePlayers()) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(p);
                meta.setDisplayName("§f" + p.getName());
                head.setItemMeta(meta);
            }
            playerList.addItem(head);
        }
        admin.openInventory(playerList);
    }

    private void toggleVanish(Player p) {
        if (vanished.contains(p.getUniqueId())) {
            vanished.remove(p.getUniqueId());
            Bukkit.getOnlinePlayers().forEach(online -> online.showPlayer(this, p));
            p.sendMessage("§cVanish Off");
        } else {
            vanished.add(p.getUniqueId());
            Bukkit.getOnlinePlayers().forEach(online -> online.hidePlayer(this, p));
            p.sendMessage("§aVanish On");
        }
        openMainGUI(p);
    }
}
