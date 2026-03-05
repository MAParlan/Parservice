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
        getLogger().info("Parservice: Fully Loaded!");
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

    public void openMainGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§0Parservice Admin Panel");
        gui.setItem(10, createGuiItem(Material.BONE, "§c§lKill All Mobs"));
        gui.setItem(11, createGuiItem(Material.DIAMOND, "§b§lClear Lag"));
        gui.setItem(13, createGuiItem(Material.PLAYER_HEAD, "§e§lManage Players"));
        gui.setItem(15, createGuiItem(Material.GRASS_BLOCK, "§a§lWorld Settings"));
        gui.setItem(16, createGuiItem(Material.CHICKEN_SPAWN_EGG, "§6§lSpawn Menu"));
        
        Material vanishMat = vanished.contains(player.getUniqueId()) ? Material.ENDER_EYE : Material.ENDER_PEARL;
        gui.setItem(26, createGuiItem(vanishMat, "§d§lVanish Mode"));
        player.openInventory(gui);
    }

    public void openWorldSettings(Player player) {
        Inventory world = Bukkit.createInventory(null, 9, "§0World Settings");
        world.setItem(0, createGuiItem(Material.SUNFLOWER, "§eSet Day"));
        world.setItem(1, createGuiItem(Material.CLOCK, "§8Set Night"));
        world.setItem(3, createGuiItem(Material.WATER_BUCKET, "§bClear Weather"));
        
        Material chatMat = chatMuted ? Material.RED_DYE : Material.LIME_DYE;
        world.setItem(7, createGuiItem(chatMat, chatMuted ? "§cChat: MUTED" : "§aChat: UNMUTED"));
        world.setItem(8, createGuiItem(Material.ARROW, "§7Back"));
        player.openInventory(world);
    }

    public void openSpawnMenu(Player player) {
        Inventory spawn = Bukkit.createInventory(null, 9, "§0Spawn Menu");
        spawn.setItem(0, createGuiItem(Material.TNT, "§cSummon TNT"));
        spawn.setItem(1, createGuiItem(Material.IRON_BLOCK, "§fSummon Golem"));
        spawn.setItem(4, createGuiItem(Material.BARRIER, "§cGet Barrier"));
        spawn.setItem(8, createGuiItem(Material.ARROW, "§7Back"));
        player.openInventory(spawn);
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

    public void openPunishMenu(Player admin, Player target) {
        Inventory punish = Bukkit.createInventory(null, 9, "§cManage: " + target.getName());
        punish.setItem(0, createGuiItem(Material.COMPASS, "§aTeleport"));
        punish.setItem(2, createGuiItem(Material.GOLD_INGOT, "§eGive $1,000"));
        punish.setItem(4, createGuiItem(Material.BARRIER, "§6Kick"));
        punish.setItem(8, createGuiItem(Material.ARROW, "§7Back"));
        targetCache.put(admin.getUniqueId(), target.getUniqueId());
        admin.openInventory(punish);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (chatMuted && !event.getPlayer().isOp()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cChat is muted!");
        }
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        String title = event.getView().getTitle();
        Player admin = (Player) event.getWhoClicked();

        if (title.equals("§0Parservice Admin Panel")) {
            event.setCancelled(true);
            Material m = event.getCurrentItem().getType();
            if (m == Material.GRASS_BLOCK) openWorldSettings(admin);
            else if (m == Material.CHICKEN_SPAWN_EGG) openSpawnMenu(admin);
            else if (m == Material.PLAYER_HEAD) openPlayerList(admin);
            else if (m == Material.ENDER_PEARL || m == Material.ENDER_EYE) toggleVanish(admin);
            else if (m == Material.BONE) admin.getWorld().getEntitiesByClass(Monster.class).forEach(Entity::remove);
            else if (m == Material.DIAMOND) admin.getWorld().getEntitiesByClass(Item.class).forEach(Entity::remove);
        }
        else if (title.equals("§0World Settings")) {
            event.setCancelled(true);
            handleWorldClick(admin, event.getCurrentItem().getType());
        }
        else if (title.equals("§0Spawn Menu")) {
            event.setCancelled(true);
            handleSpawnClick(admin, event.getCurrentItem().getType());
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

    private void handleWorldClick(Player admin, Material m) {
        if (m == Material.SUNFLOWER) admin.getWorld().setTime(1000);
        else if (m == Material.CLOCK) admin.getWorld().setTime(13000);
        else if (m == Material.WATER_BUCKET) admin.getWorld().setStorm(false);
        else if (m == Material.RED_DYE || m == Material.LIME_DYE) {
            chatMuted = !chatMuted;
            openWorldSettings(admin);
        }
        else if (m == Material.ARROW) openMainGUI(admin);
    }

    private void handleSpawnClick(Player admin, Material m) {
        if (m == Material.TNT) admin.getWorld().spawnEntity(admin.getLocation(), EntityType.PRIMED_TNT);
        else if (m == Material.IRON_BLOCK) admin.getWorld().spawnEntity(admin.getLocation(), EntityType.IRON_GOLEM);
        else if (m == Material.BARRIER) admin.getInventory().addItem(new ItemStack(Material.BARRIER));
        else if (m == Material.ARROW) openMainGUI(admin);
    }

    private void handlePunishClick(Player admin, Material m) {
        UUID targetId = targetCache.get(admin.getUniqueId());
        if (targetId == null) return;
        Player target = Bukkit.getPlayer(targetId);
        if (target == null) return;

        if (m == Material.COMPASS) admin.teleport(target.getLocation());
        else if (m == Material.GOLD_INGOT && econ != null) econ.depositPlayer(target, 1000);
        else if (m == Material.BARRIER) target.kickPlayer("§cKicked!");
        else if (m == Material.ARROW) openPlayerList(admin);
    }

    private void toggleVanish(Player p) {
        if (vanished.contains(p.getUniqueId())) {
            vanished.remove(p.getUniqueId());
            Bukkit.getOnlinePlayers().forEach(online -> online.showPlayer(this, p));
        } else {
            vanished.add(p.getUniqueId());
            Bukkit.getOnlinePlayers().forEach(online -> online.hidePlayer(this, p));
        }
        openMainGUI(p);
    }

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
}
