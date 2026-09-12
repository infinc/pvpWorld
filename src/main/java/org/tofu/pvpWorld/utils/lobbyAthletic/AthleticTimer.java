package org.tofu.pvpWorld.utils.lobbyAthletic;

import org.tofu.pvpWorld.PvpWorld;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.tofu.pvpWorld.utils.textComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AthleticTimer {

    private static final int TIME_LIMIT = 500;

    private static final Map<UUID, BukkitTask> tasks = new HashMap<>();

    private static final Map<UUID, Integer> playerTimes = new HashMap<>();

    public static void startTimer(Player player, PvpWorld plugin) {
        UUID uuid = player.getUniqueId();
        stopTimer(player);
        playerTimes.put(uuid, 0);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    stopTimer(player);
                    return;
                }

                int elapsedTime = playerTimes.getOrDefault(uuid, 0) + 1;

                if (elapsedTime > TIME_LIMIT) {
                    player.sendMessage(textComponent.parse("<aqua>時間制限です!"));
                    stopTimer(player);
                    return;
                }

                playerTimes.put(uuid, elapsedTime);
                player.setLevel(elapsedTime);
            }
        }.runTaskTimer(plugin, 20L, 20L);

        tasks.put(uuid, task);
    }

    public static void stopTimer(Player player) {
        UUID uuid = player.getUniqueId();
        BukkitTask task = tasks.remove(uuid);
        if (task != null) task.cancel();
        playerTimes.remove(uuid);
        player.setLevel(0);
    }

    public static boolean isRunning(Player player) {
        return tasks.containsKey(player.getUniqueId());
    }

    public static int getElapsedTime(Player player) {
        return playerTimes.getOrDefault(player.getUniqueId(), 0);
    }
}
