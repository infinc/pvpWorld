package org.tofu.pvpWorld.worldEvents;

import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.tofu.pvpWorld.utils.ffaGames.SpleefActivities;
import org.tofu.pvpWorld.utils.freePvp.FreePvpUtils;
import org.tofu.pvpWorld.utils.oneVersusOne.SumoActivities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.tofu.pvpWorld.utils.speedRun.SpeedRunActionMulti;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class playerMoveEvent implements Listener {
    private final PvpWorld plugin;

    private final Set<UUID> freePvpException = new HashSet<>();

    public playerMoveEvent(PvpWorld plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (!Config.isPvpWorld(player.getWorld())) return;

        if (Config.NoWalkList.contains(player.getName())) {
            e.setCancelled(true);
            return;
        }
        if (Config.AdminBuildModeList.contains(player.getName())) return;

        if (!e.hasChangedBlock()) return;

        Material type = player.getLocation().getBlock().getType();
        if (type == Material.TRIPWIRE) {
            if (SpeedRunActionMulti.multiPlayingList.contains(player.getName())) return;
            if (Config.overLappingTrigger(player)) {
                if (freePvpException.contains(player.getUniqueId())) return;
                Config.overLappingMessage(player);
                return;
            }
            FreePvpUtils.joinAction(player, plugin);
            freePvpException.add(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin, () -> freePvpException.remove(player.getUniqueId()), 20L);
        } else if (type == Material.WATER) {
            if (SumoActivities.sumoQueueingList.contains(player.getName())) {
                SumoActivities.sumoCloseAction(player, plugin);
            } else if (SpleefActivities.spleefPlayingList.contains(player.getName())) {
                SpleefActivities.voidAction(player, plugin);
            }
        }
    }
}
