package org.tofu.pvpWorld.worldEvents;

import io.papermc.paper.event.player.PlayerOpenSignEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;

public final class playerOpenSignEvent implements Listener {
    public playerOpenSignEvent(PvpWorld plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerOpenSignEvent(PlayerOpenSignEvent e) {
        Player player = e.getPlayer();
        if (!Config.isPvpWorld(player.getWorld())) return;
        if (Config.AdminBuildModeList.contains(player.getName())) return;
        e.setCancelled(true);
    }
}
