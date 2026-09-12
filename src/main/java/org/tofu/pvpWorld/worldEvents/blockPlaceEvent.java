package org.tofu.pvpWorld.worldEvents;

import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.tofu.pvpWorld.utils.textComponent;

public final class blockPlaceEvent implements Listener {
    public blockPlaceEvent(PvpWorld plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        if (!Config.isPvpWorld(player.getWorld())) return;
        if (Config.AdminBuildModeList.contains(player.getName())) return;

        e.setCancelled(true);
        player.sendMessage(textComponent.parse("ブロックは置けません!"));
    }
}
