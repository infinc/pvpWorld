package org.tofu.pvpWorld.utils.oneVersusOne;

import org.bukkit.*;
import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.bukkit.entity.Player;
import org.tofu.pvpWorld.utils.textComponent;
import org.tofu.pvpWorld.utils.titleMaker;
import org.tofu.pvpWorld.utils.yamlProperties.coinUtils;
import org.tofu.pvpWorld.utils.yamlProperties.expUtils;

import java.util.ArrayList;
import java.util.List;

public class SumoActivities {
    public static final List<String> sumoQueueingList = new ArrayList<>();

    public static Location player1Location, player2Location;

    public static void setupLocations(World world) {
        player1Location = new Location(world, 50.500, 4.500, -108.500, 180, 0);
        player2Location = new Location(world, 50.500, 4.500, -120.500, 0, 0);
    }

    public static void sumoStartAction(Player player, PvpWorld plugin) {
        if (sumoQueueingList.size() < 2) return;

        Player first = Bukkit.getPlayerExact(sumoQueueingList.get(0));
        Player second = Bukkit.getPlayerExact(sumoQueueingList.get(1));
        if (first == null || second == null) {
            player.sendMessage(textComponent.parse("<white>対戦相手がオフラインになったため、開始できませんでした"));
            return;
        }

        first.getInventory().setItem(8, null);
        second.getInventory().setItem(8, null);
        if (player1Location != null) first.teleport(player1Location);
        if (player2Location != null) second.teleport(player2Location);

        TimeUpTimer.startTimer(player, plugin, 300);
        for (String playerName : List.copyOf(sumoQueueingList)) {
            Config.addIfAbsent(Config.NoWalkList, playerName);
        }
        StartTimerUtils.startTimer(player, plugin, sumoQueueingList);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            OneVersusOneGames.noWalkRemoveAction(sumoQueueingList);
            for (String playerName : List.copyOf(sumoQueueingList)) {
                Config.DoNotReceiveDamageList.remove(playerName);
            }
        }, 100L);
    }

    public static void sumoCloseAction(Player player, PvpWorld plugin) {
        if (!sumoQueueingList.remove(player.getName())) return;

        StartTimerUtils.stopTimer(player);
        TimeUpTimer.stopTimer(player);
        Config.addIfAbsent(Config.DoNotReceiveDamageList, player.getName());
        Config.addIfAbsent(Config.TeleportToLobbyList, player.getName());
        expUtils.playerSetExp(player, 4);
        coinUtils.playerSetCoin(player, 3);
        player.playSound(player.getLocation(), Sound.AMBIENT_BASALT_DELTAS_ADDITIONS, 1, 1);
        player.showTitle(titleMaker.title(textComponent.parse("<red>敗北"), textComponent.parse("<yellow>もう一度挑戦しよう!"), 0, 2000, 0));

        for (String playerName : List.copyOf(sumoQueueingList)) {
            Config.addIfAbsent(Config.DoNotReceiveDamageList, playerName);
            Config.addIfAbsent(Config.TeleportToLobbyList, playerName);
            Player winner = Bukkit.getPlayerExact(playerName);
            if (winner == null) continue;
            StartTimerUtils.stopTimer(winner);
            TimeUpTimer.stopTimer(winner);
            expUtils.playerSetExp(winner, 6);
            coinUtils.playerSetCoin(winner, 5);
            winner.playSound(winner.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_HIT, 1, 1);
            winner.showTitle(titleMaker.title(textComponent.parse("<green>勝利"), textComponent.parse("<yellow>おめでとう!!"), 0, 3000, 0));
        }

        sumoQueueingList.clear();
        OneVersusOneGames.gameCloseAction(plugin);
    }
}
