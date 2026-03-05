package me.mark.parservice;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
        getLogger().info("Parservice: World Settings Loaded!");
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
        gui.setItem(12, createGuiItem(Material.DIAMOND, "§b§lClear Lag"));
        gui.setItem(14, createGuiItem(Material.PLAYER_HEAD, "§e§lManage Players"));
        gui.setItem(16, createGuiItem(Material.GRASS_BLOCK, "§a§lWorld Settings", "§7Control time, weather, and chat."));
        
        // Vanish Button
        Material vanishMat = vanished.contains(player.getUniqueId()) ? Material.ENDER_EYE : Material.ENDER_PEARL;
        gui.setItem(26, createGuiItem(vanishMat, "§d§lVanish Mode"));

        player.openInventory(gui);
    }

    public void openWorldSettings(Player player) {
        Inventory world = Bukkit.createInventory(null, 9, "§0World Settings");
        
        world.setItem(0, createGuiItem(Material.SUNFLOWER, "§eSet Day"));
        world.setItem(1, createGuiItem(Material.CLOCK, "§8Set Night"));
        world.setItem(3, createGuiItem(Material.WATER_BUCKET, "§bClear Weather"));
        world.setItem(4, createGuiItem(Material.LAVA_BUCKET, "§cStart Storm"));
        
        Material chatMat = chatMuted ? Material.RED_DYE : Material.LIME_DYE;
        String chatName = chatMuted ? "§cChat: MUTED" : "§aChat: UNMUTED";
        world.setItem(7, createGuiItem(chatMat, chatName, "§7Click to toggle global chat."));
        
        world.setItem(8, createGuiItem(Material.ARROW, "§7Back"));
        player.openInventory(world);
    }

    // --- EVENTS ---

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (chatMuted && !event.getPlayer().isOp()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cChat is currently muted by an admin.");
        }
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        String title = event.getView().getTitle();
        Player admin = (Player) event.getWhoClicked();
        World world = admin.getWorld();

        if (title.equals("§0Parservice Admin Panel")) {
            event.setCancelled(true);
            Material m = event.getCurrentItem().getType();
            if (m == Material.GRASS_BLOCK) openWorldSettings(admin);
            else if (m == Material.PLAYER_HEAD) openPlayerList(admin);
            else if (m == Material.ENDER_PEARL || m == Material.ENDER_EYE) toggleVanish(admin);
        }
        else if (title.equals("§0World Settings")) {
            event.setCancelled(true);
            Material m = event.getCurrentItem().getType();
            if (m == Material.SUNFLOWER) world.setTime(1000);
            else if (m == Material.CLOCK) world.setTime(13000);
            else if (m == Material.WATER_BUCKET) world.setStorm(false);
            else if (m == Material.LAVA_BUCKET) world.setStorm(true);
            else if (m == Material.RED_DYE || m == Material.LIME_DYE) {
                chatMuted = !chatMuted;
                Bukkit.broadcastMessage(chatMuted ? "§cGlobal chat has been muted." : "§aGlobal chat has been unmuted.");
                openWorldSettings(admin);
            }
            else if (m == Material.ARROW) openMainGUI(admin);
        }
        // ... (Keep existing logic for Player List and Punish Menu)
    }

    // (Include the rest of the helper methods like createGuiItem, openPlayerList, toggleVanish, etc. from the previous code)
    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
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
        openMainGUI(p);
    }
}
