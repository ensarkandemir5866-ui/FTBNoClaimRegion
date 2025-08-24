package com.ensarcraft.ftbnc;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class FTBNoClaimRegion extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    private boolean isInNoClaimRegion(Player p) {
        RegionContainer container = WorldGuardPlugin.inst().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(p.getLocation());
        return set.getRegions().stream().anyMatch(r -> r.getFlag("ftb-no-claim") != null);
    }

    private void applyOrRemoveRank(Player p) {
        boolean inNoClaim = isInNoClaimRegion(p);
        String cmdAdd = "lp user " + p.getName() + " permission settemp ftbchunks.claim false 10s";
        String cmdDel = "lp user " + p.getName() + " permission unset ftbchunks.claim";
        if (inNoClaim) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmdAdd);
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmdDel);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLater(this, () -> applyOrRemoveRank(e.getPlayer()), 1L);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() == e.getTo().getBlockX() &&
                e.getFrom().getBlockY() == e.getTo().getBlockY() &&
                e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;
        applyOrRemoveRank(e.getPlayer());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Bukkit.getScheduler().runTaskLater(this, () -> applyOrRemoveRank(e.getPlayer()), 1L);
    }
}
