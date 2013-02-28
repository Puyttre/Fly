package me.puyttre.fly;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Fly extends JavaPlugin {
    
    private PluginManager pm;
    private Command cmd;
    
    @Override
    public void onEnable() {
        this.cmd = new Command(this);
        getCommand("fly").setExecutor(cmd);
        getCommand("flytimer").setExecutor(cmd);
        
        Bukkit.getLogger().info("[Fly] Fly v1.3 successfully enabled.");
    }
    
    @Override
    public void onDisable() {
        Bukkit.getLogger().info("[Fly] Forcing all flying players to stop.");
        for (Player p : cmd.flying) {
            p.setFlying(false);
            p.setAllowFlight(false);
        }
        Bukkit.getLogger().info("[Fly] Fly v1.3 successfully disabled.");
    }
    
}
