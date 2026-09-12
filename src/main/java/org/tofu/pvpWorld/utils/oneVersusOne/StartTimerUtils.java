package org.tofu.pvpWorld.utils.oneVersusOne;

import org.tofu.pvpWorld.PvpWorld;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.tofu.pvpWorld.utils.textComponent;
import org.tofu.pvpWorld.utils.titleMaker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StartTimerUtils {

    private static final int COUNTDOWN_SECONDS = 6;

    private static final Map<UUID, BukkitTask> tasks = new HashMap<>();

    private static final Map<UUID, Integer> playerTimes = new HashMap<>();

    public static void startTimer(Player player, PvpWorld plugin, List<String> arrayList) {
        UUID uuid = player.getUniqueId();
        stopTimer(player);
        playerTimes.put(uuid, COUNTDOWN_SECONDS);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                int elapsedTime = playerTimes.getOrDefault(uuid, 0) - 1;

                if (elapsedTime <= 0) {
                    stopTimer(player);
                    return;
                }

                for (String playerName : List.copyOf(arrayList)) {
                    Player queued = Bukkit.getPlayerExact(playerName);
                    if (queued == null) continue;
                    queued.showTitle(titleMaker.title(textComponent.parse("<red>" + elapsedTime), textComponent.parse(""), 0, 1000, 0));
                    queued.playSound(queued.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1, 1);
                }

                playerTimes.put(uuid, elapsedTime);
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
}
