package org.tofu.pvpWorld.utils.speedRun;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.tofu.pvpWorld.PvpWorld;
import org.tofu.pvpWorld.utils.textComponent;
import org.tofu.pvpWorld.utils.titleMaker;

import java.util.List;

public class SpeedRunTimerMulti {

    private static final int COUNTDOWN_SECONDS = 16;

    private static BukkitTask task;

    private static int remainingTime;

    public static void startTimer(Player starter, PvpWorld plugin) {
        stopTimer();
        remainingTime = COUNTDOWN_SECONDS;

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!starter.isOnline()) {
                    stopTimer();
                    return;
                }

                int nextTime = remainingTime - 1;

                if (nextTime <= 0) {
                    stopTimer();
                    SpeedRunActionMulti.startAction(plugin);
                    return;
                }

                if (nextTime <= 5) sendMessage(nextTime);

                for (String playerName : List.copyOf(SpeedRunActionMulti.multiPlayingList)) {
                    Player p = Bukkit.getPlayerExact(playerName);
                    if (p != null) p.setLevel(nextTime);
                }

                remainingTime = nextTime;
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public static void addTime(int seconds) {
        if (task == null) return;
        remainingTime = remainingTime + seconds;

        for (String playerName : List.copyOf(SpeedRunActionMulti.multiPlayingList)) {
            Player p = Bukkit.getPlayerExact(playerName);
            if (p != null) p.setLevel(remainingTime);
        }
    }

    public static boolean isRunning() {
        return task != null;
    }

    public static int getRemainingTime() {
        return remainingTime;
    }

    public static void stopTimer() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        remainingTime = 0;
    }

    public static void sendMessage(int elapsedTime) {
        for (String playerName : List.copyOf(SpeedRunActionMulti.multiPlayingList)) {
            Player p = Bukkit.getPlayerExact(playerName);
            if (p == null) continue;
            p.playSound(p.getLocation(), Sound.BLOCK_CALCITE_PLACE, 1, 2);
            p.sendMessage(textComponent.parse("<aqua>" + elapsedTime + "秒!"));
            p.showTitle(titleMaker.title(textComponent.parse(String.valueOf(elapsedTime)), textComponent.parse(""), 0, 20, 0));
        }
    }
}
