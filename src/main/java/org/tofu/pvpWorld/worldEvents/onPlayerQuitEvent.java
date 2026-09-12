package org.tofu.pvpWorld.worldEvents;

import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class onPlayerQuitEvent implements Listener {
    public onPlayerQuitEvent(PvpWorld plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onOnPlayerQuitEvent(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (!Config.isPvpWorld(player.getWorld())) return;
        Config.handlePlayerLeave(player);
    }
}
