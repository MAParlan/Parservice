package me.mark.parservice;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("Parservice is now online!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("adminpanel")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.sendMessage("§aOpening Parservice Panel...");
                // We will add the GUI code here next!
            }
            return true;
        }
        return false;
    }
}
