package me.puyttre.fly;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Command implements CommandExecutor {
    
    private Fly plugin;
    
    public Command(Fly plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("fly")) {
            if (args.length == 0) {
                if (!sender.hasPermission("fly.toggle.self")) {
                    sender.sendMessage(ChatColor.YELLOW + "You do not have permission to do that.");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("You must be a player to perform this command.");
                    return true;
                }
                Player p = (Player) sender;
                toggleFly(sender, p.getName());
            } else if (args.length == 1) {
                if (!sender.hasPermission("fly.toggle.other")) {
                    sender.sendMessage(ChatColor.YELLOW + "You do not have permission to do that.");
                    return true;
                }
                if (checkOnline(args[0])) {
                    toggleFly(sender, args[0]);
                } else {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
            } else if (args.length == 2) {
                if (!sender.hasPermission("fly.speed.self")) {
                    sender.sendMessage(ChatColor.YELLOW + "You do not have permission to do that.");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("You must be a player to perform this command.");
                    return true;
                }
                if (args[0].equalsIgnoreCase("speed")) {
                    Player p = (Player) sender;
                    setSpeed(sender, p, args[1]);
                } else {
                    sender.sendMessage(ChatColor.RED + "Please check your syntax.");
                    return true;
                }
            } else if (args.length == 3) {
                if (!sender.hasPermission("fly.speed.other")) {
                    sender.sendMessage(ChatColor.YELLOW + "You do not have permission to do that.");
                    return true;
                }
                Player p;
                if (checkOnline(args[0])) {
                    if (args[0].equalsIgnoreCase("speed")) {
                        if (checkOnline(args[1])) {
                            p = Bukkit.getPlayer(findPlayer(args[1]));
                            setSpeed(sender, p, args[2]);
                        } else {
                            sender.sendMessage(ChatColor.RED + "Player not found.");
                            return true;
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Please check your syntax.");
                        return true;
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                }
            }
        } else if (cmd.getName().equalsIgnoreCase("flytimer")) {
            if (!sender.hasPermission("fly.timer")) {
                sender.sendMessage(ChatColor.YELLOW + "You do not have permission to do that.");
                return true;
            }
            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "/flytimer <name> <time + s|m|h> (ie. /flytimer notch 10m");
            }
            
            final Player p;
            if (checkOnline(args[0])) {
                p = Bukkit.getPlayer(findPlayer(args[0]));
            } else {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
            
            int time = 0;
            if (args[1].endsWith("s")) {
                time = Integer.parseInt(args[1].substring(0, args[1].length() - 1)) * 20;
            } else if (args[1].endsWith("m")) {
                time = Integer.parseInt(args[1].substring(0, args[1].length() - 1)) * 20 * 60;
            } else if (args[1].endsWith("h")) {
                time = Integer.parseInt(args[1].substring(0, args[1].length() - 1)) * 20 * 60 * 60;
            } else {
                p.sendMessage(ChatColor.RED + "To specify a time, do a number followed by s|m|h (ie. 10m or 1h or 53s)");
                return true;
            }
            
            p.setAllowFlight(true);
            sender.sendMessage(ChatColor.YELLOW + p.getDisplayName() + " is allowed to fly for " + ChatColor.DARK_GREEN + args[1] + ChatColor.YELLOW + ".");
            p.sendMessage(ChatColor.YELLOW + "You are allowed to fly for " + ChatColor.DARK_GREEN + args[1] + ChatColor.YELLOW + ".");
            flying.add(p);
            
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    p.setAllowFlight(false);
                    flying.remove(p);
                    p.sendMessage(ChatColor.YELLOW + "Your fly limit has ended.");
                }
           }, (long) time);
        }
        return true;
    }
    
    public String findPlayer(String s) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getDisplayName().toLowerCase().contains(s.toLowerCase())) return p.getName();
        }
        return "PlayerNotFound";
    }
    
    public boolean checkOnline(String s) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getDisplayName().toLowerCase().contains(s.toLowerCase())) return true;
        }
        return false;
    }
    
    public Set<Player> flying = new HashSet<>();
    
    public void toggleFly(CommandSender sender, String name) {
        Player p = Bukkit.getPlayer(findPlayer(name));
        if (!p.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player is not online.");
            return;
        }
        if (flying.contains(p)) {
            p.sendMessage(ChatColor.YELLOW + "Your fly mode was toggled " + ChatColor.DARK_RED + "off" + ChatColor.YELLOW + ".");
            flying.remove(p);
            p.setAllowFlight(false);
            sender.sendMessage(ChatColor.YELLOW + "Fly mode " + ChatColor.DARK_RED + "disabled" + ChatColor.YELLOW + " for " + p.getDisplayName() + ChatColor.YELLOW + ".");
        } else {
            p.sendMessage(ChatColor.YELLOW + "Your fly mode was toggled " + ChatColor.DARK_GREEN + "on" + ChatColor.YELLOW + ".");
            flying.add(p);
            p.setAllowFlight(true);
            sender.sendMessage(ChatColor.YELLOW + "Fly mode " + ChatColor.DARK_GREEN + "enabled" + ChatColor.YELLOW + " for " + p.getDisplayName() + ChatColor.YELLOW + ".");
        }
    }
    
    public void setSpeed(CommandSender sender, Player p, String speed) {
        int integer = Integer.parseInt(speed);
        if (integer > 99) {
            sender.sendMessage(ChatColor.RED + "Please enter a number less than 100%.");
            return;
        }
        
        float f = (float) (integer * .01);
        p.setFlySpeed(f);
        
        sender.sendMessage(ChatColor.YELLOW + "Set " + p.getDisplayName() + ChatColor.YELLOW + "'s fly speed to " + ChatColor.GREEN + speed + "%" + ChatColor.YELLOW + ".");
        p.sendMessage(ChatColor.YELLOW + "Your fly speed was set to " + ChatColor.GREEN + speed + "%" + ChatColor.YELLOW + ".");
    }
    
}
