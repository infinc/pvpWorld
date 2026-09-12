package org.tofu.pvpWorld.utils.oneVersusOne;

import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.tofu.pvpWorld.utils.textComponent;
import org.tofu.pvpWorld.utils.titleMaker;
import org.tofu.pvpWorld.utils.yamlProperties.coinUtils;
import org.tofu.pvpWorld.utils.yamlProperties.expUtils;

import java.util.ArrayList;
import java.util.List;

public class TopfightActivities {
    public static final List<String> topfightQueueingList = new ArrayList<>();

    public static Location player1Location, player2Location;

    public static void setupLocations(World world) {
        player1Location = new Location(world, 137.500, 16.500, -138.500, 180, 0);
        player2Location = new Location(world, 137.500, 16.500, -160.500);
    }

    public static void topfightStartAction(Player player, PvpWorld plugin) {
        if (topfightQueueingList.size() < 2) return;

        Player first = Bukkit.getPlayerExact(topfightQueueingList.get(0));
        Player second = Bukkit.getPlayerExact(topfightQueueingList.get(1));
        if (first == null || second == null) {
            player.sendMessage(textComponent.parse("<white>対戦相手がオフラインになったため、開始できませんでした"));
            return;
        }

        first.getInventory().setItem(8, null);
        second.getInventory().setItem(8, null);
        if (player1Location != null) first.teleport(player1Location);
        if (player2Location != null) second.teleport(player2Location);

        TimeUpTimer.startTimer(player, plugin, 300);
        for (String playerName : List.copyOf(topfightQueueingList)) {
            Config.addIfAbsent(Config.NoWalkList, playerName);
        }
        StartTimerUtils.startTimer(player, plugin, topfightQueueingList);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            OneVersusOneGames.noWalkRemoveAction(topfightQueueingList);
            for (String playerName : List.copyOf(topfightQueueingList)) {
                Config.DoNotReceiveDamageList.remove(playerName);
            }
        }, 100L);
    }

    public static void topfightCloseAction(Player player, PvpWorld plugin) {
        if (!topfightQueueingList.remove(player.getName())) return;

        StartTimerUtils.stopTimer(player);
        TimeUpTimer.stopTimer(player);
        Config.addIfAbsent(Config.DoNotReceiveDamageList, player.getName());
        Config.addIfAbsent(Config.TeleportToLobbyList, player.getName());
        player.showTitle(titleMaker.title(textComponent.parse("<red>敗北"), textComponent.parse("<yellow>もう一度挑戦しよう"), 0, 3000, 0));
        expUtils.playerSetExp(player, 4);
        coinUtils.playerSetCoin(player, 3);

        for (String playerName : List.copyOf(topfightQueueingList)) {
            Config.addIfAbsent(Config.DoNotReceiveDamageList, playerName);
            Config.addIfAbsent(Config.TeleportToLobbyList, playerName);
            Player winner = Bukkit.getPlayerExact(playerName);
            if (winner == null) continue;
            StartTimerUtils.stopTimer(winner);
            TimeUpTimer.stopTimer(winner);
            winner.showTitle(titleMaker.title(textComponent.parse("<green>勝利"), textComponent.parse("<yellow>すごい!!"), 0, 3000, 0));
            expUtils.playerSetExp(winner, 6);
            coinUtils.playerSetCoin(winner, 5);
        }

        topfightQueueingList.clear();
        OneVersusOneGames.gameCloseAction(plugin);
    }
}
