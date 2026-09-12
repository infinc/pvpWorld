package org.tofu.pvpWorld.worldEvents;

import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.tofu.pvpWorld.utils.freePvp.FreePvpUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class playerRespawnEvent implements Listener {
    private final PvpWorld plugin;

    public playerRespawnEvent(PvpWorld plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        if (!Config.isPvpWorld(player.getWorld())) return;

        player.setLevel(0);
        Config.clearInventory(player);
        if (Config.FreePvpPlayerList.contains(player.getName())) {
            FreePvpUtils.freePvpPlayerRespawnAction(player, plugin);
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            player.setGameMode(GameMode.SURVIVAL);
            if (Config.lobby != null) player.teleport(Config.lobby);
        }, 2L);
    }
}
