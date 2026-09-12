package org.tofu.pvpWorld.worldEvents;

import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.tofu.pvpWorld.utils.textComponent;
import org.tofu.pvpWorld.utils.textDisplay.TextDisplayUtils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public final class playerInteractAtEntityEvent implements Listener {
    public playerInteractAtEntityEvent(PvpWorld plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent e) {
        Player player = e.getPlayer();
        if (!Config.isPvpWorld(player.getWorld())) return;
        if (!(e.getRightClicked() instanceof ArmorStand as)) return;

        Location location = as.getLocation();
        if (!isNear(location, TextDisplayUtils.expRanking)
                && !isNear(location, TextDisplayUtils.coinRanking)
                && !isNear(location, TextDisplayUtils.athleticRanking)) return;

        TextDisplayUtils.latestRanking();
        player.sendMessage(textComponent.parse("<green>スコアボードを更新しました"));
    }

    private boolean isNear(Location location, Location target) {
        if (target == null || target.getWorld() == null) return false;
        if (!target.getWorld().equals(location.getWorld())) return false;
        return Math.abs(location.getX() - target.getX()) < 1
                && Math.abs(location.getZ() - target.getZ()) < 1
                && location.getY() <= target.getY() + 1
                && location.getY() >= target.getY() - 4;
    }
}
