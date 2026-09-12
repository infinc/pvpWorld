package org.tofu.pvpWorld.utils.speedRun;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.tofu.pvpWorld.PvpWorld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SpeedRunScheduledTimer {

    private static final int INTERVAL_SECONDS = 11;

    private static final Map<UUID, BukkitTask> scheduledTasks = new HashMap<>();
    private static final Map<UUID, Integer> playerTimes = new HashMap<>();

    public static void startTimer(Player player, PvpWorld plugin, boolean multi) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();

        stopTimer(player);
        playerTimes.put(uuid, INTERVAL_SECONDS);

        BukkitTask timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    stopTimer(player);
                    return;
                }

                int elapsedTime = playerTimes.getOrDefault(uuid, INTERVAL_SECONDS) - 1;

                if (elapsedTime <= 0) {
                    player.setLevel(0);
                    if (multi) {
                        for (String playerName : List.copyOf(SpeedRunActionMulti.multiPlayingList)) {
                            Player member = Bukkit.getPlayerExact(playerName);
                            if (member != null) SpeedRunAction.randomEvent(member, plugin);
                        }
                    } else {
                        SpeedRunAction.randomEvent(player, plugin);
                    }
                    playerTimes.put(uuid, INTERVAL_SECONDS);
                    return;
                }

                playerTimes.put(uuid, elapsedTime);
                player.setLevel(elapsedTime);

                if (multi) {
                    for (String playerName : List.copyOf(SpeedRunActionMulti.multiPlayingList)) {
                        Player p = Bukkit.getPlayerExact(playerName);
                        if (p != null) p.setLevel(elapsedTime);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);

        scheduledTasks.put(uuid, timerTask);
    }

    public static void stopTimer(Player player) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();
        BukkitTask task = scheduledTasks.remove(uuid);
        if (task != null) task.cancel();
        playerTimes.remove(uuid);
    }
}
