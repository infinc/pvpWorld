package org.tofu.pvpWorld.worldEvents;

import org.bukkit.entity.Arrow;
import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.tofu.pvpWorld.utils.ffaGames.SpleefActivities;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.tofu.pvpWorld.utils.speedRun.SpeedRunActionMulti;

public final class projectileHitEvent implements Listener {
    private final PvpWorld plugin;

    public projectileHitEvent(PvpWorld plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!Config.isPvpWorld(e.getEntity().getWorld())) return;
        if (!(e.getEntity().getShooter() instanceof Player player)) return;

        if (e.getEntity() instanceof Snowball) {
            if (Config.AdminBuildModeList.contains(player.getName())) return;
            if (!SpleefActivities.spleefPlayingList.contains(player.getName())) return;
            if (e.getHitEntity() != null) return;

            Block hitBlock = e.getHitBlock();
            if (hitBlock == null || hitBlock.getType() != Material.SNOW_BLOCK) return;
            SpleefActivities.locationList.add(hitBlock.getLocation());
            hitBlock.setType(Material.AIR);
        } else if (e.getEntity() instanceof Arrow) {
            if (!SpeedRunActionMulti.multiPlayingList.contains(player.getName())) return;

            Block hitBlock = e.getHitBlock();
            if (hitBlock == null || hitBlock.getType() != Material.WHITE_WOOL) return;
            e.getEntity().remove();
            SpeedRunActionMulti.checkArrowInfo(hitBlock.getLocation(), player, plugin);
        }
    }
}
