package org.tofu.pvpWorld.utils.speedRun;

import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.tofu.pvpWorld.utils.textComponent;
import org.tofu.pvpWorld.utils.titleMaker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpeedRunTimer {

    private static final int COUNTDOWN_SECONDS = 16;

    private static final Map<UUID, BukkitTask> tasks = new HashMap<>();

    private static final Map<UUID, Integer> playerTimes = new HashMap<>();

    public static void startTimer(Player player, PvpWorld plugin) {
        UUID uuid = player.getUniqueId();
        stopTimer(player);
        playerTimes.put(uuid, COUNTDOWN_SECONDS);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    Config.SpeedRunSingleOnHoldList.remove(player.getName());
                    stopTimer(player);
                    return;
                }

                int elapsedTime = playerTimes.getOrDefault(uuid, 0) - 1;

                if (elapsedTime <= 0) {
                    stopTimer(player);
                    player.setLevel(0);
                    Config.SpeedRunSingleOnHoldList.remove(player.getName());
                    SpeedRunAction.startSingleMode(player, plugin);
                    return;
                }

                if (elapsedTime <= 5) sendMessage(player, elapsedTime);
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
    }

    public static void sendMessage(Player player, int elapsedTime) {
        player.sendMessage(textComponent.parse("<aqua>" + elapsedTime + "秒!"));
        player.showTitle(titleMaker.title(textComponent.parse(String.valueOf(elapsedTime)), textComponent.parse(""), 0, 1000, 0));
    }
}
