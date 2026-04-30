package com.example.combatzoneguard;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatZoneGuard extends JavaPlugin implements Listener {

    private static final int COMBAT_DURATION = 15;
    private final Map<UUID, Long> combatTagMap = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("CombatZoneGuard aktif!");
    }

    @Override
    public void onDisable() {
        combatTagMap.clear();
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        long expiry = System.currentTimeMillis() + (COMBAT_DURATION * 1000L);
        combatTagMap.put(victim.getUniqueId(), expiry);
        combatTagMap.put(attacker.getUniqueId(), expiry);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        if (!isInCombat(player)) return;
        if (isPvpEnabled(event.getTo())) return;

        event.setCancelled(true);
        player.teleport(event.getFrom());
        long remaining = getRemainingSeconds(player);
        player.sendMessage("\u00a7c\u00a7l\u2694 Savas Korumasi \u00a7r\u00a7c\u2014 Savastayken guvenli bolgeye giremezsin!");
        player.sendMessage("\u00a77Kalan sure: \u00a7e" + remaining + " \u00a77saniye");
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getTo() == null) return;

        Player player = event.getPlayer();
        if (!isInCombat(player)) return;
        if (isPvpEnabled(event.getTo())) return;

        event.setCancelled(true);
        long remaining = getRemainingSeconds(player);
        player.sendMessage("\u00a7c\u00a7l\u2694 Savas Korumasi \u00a7r\u00a7c\u2014 Savastayken guvenli bolgeye giremezsin!");
        player.sendMessage("\u00a77Kalan sure: \u00a7e" + remaining + " \u00a77saniye");
    }

    private boolean isInCombat(Player player) {
        Long expiry = combatTagMap.get(player.getUniqueId());
        if (expiry == null) return false;
        if (System.currentTimeMillis() > expiry) {
            combatTagMap.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    private long getRemainingSeconds(Player player) {
        Long expiry = combatTagMap.get(player.getUniqueId());
        if (expiry == null) return 0;
        return Math.max(0, (expiry - System.currentTimeMillis()) / 1000);
    }

    private boolean isPvpEnabled(Location loc) {
        return loc.getWorld().getPVP();
    }
}
