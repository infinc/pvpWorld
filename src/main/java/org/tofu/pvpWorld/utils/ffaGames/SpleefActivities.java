package org.tofu.pvpWorld.utils.ffaGames;

import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.tofu.pvpWorld.utils.itemStackMaker;
import org.tofu.pvpWorld.utils.textComponent;
import org.tofu.pvpWorld.utils.titleMaker;
import org.tofu.pvpWorld.utils.yamlProperties.coinUtils;
import org.tofu.pvpWorld.utils.yamlProperties.expUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpleefActivities {
    public static final List<String> spleefQueueingList = new ArrayList<>(),
                                     spleefPlayingList = new ArrayList<>();

    public static final List<Location> locationList = new ArrayList<>();

    private static final Random RANDOM = new Random();

    public static Location spawnPoint;

    public static void setupLocations(World world) {
        spawnPoint = new Location(world, 183.500, 4.500, -24.500);
    }

    public static ItemStack shovelItem() {
        return new ItemStack(Material.DIAMOND_SHOVEL, 1);
    }

    public static void spleefStartAction(PvpWorld plugin) {
        if (spleefQueueingList.isEmpty()) return;

        Player timerOwner = null;
        for (String playerName : List.copyOf(spleefQueueingList)) {
            Player queued = Bukkit.getPlayerExact(playerName);
            if (queued == null) {
                spleefQueueingList.remove(playerName);
                continue;
            }
            Config.clearInventory(queued);
            if (spawnPoint != null) queued.teleport(spawnPoint);
            queued.getInventory().setItem(0, shovelItem());
            if (timerOwner == null) timerOwner = queued;
        }

        if (timerOwner == null) {
            spleefQueueingList.clear();
            return;
        }

        spleefPlayingList.addAll(spleefQueueingList);
        spleefQueueingList.clear();
        TimeUpTimer.startTimer(timerOwner, plugin, 600);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (String playerName : List.copyOf(spleefPlayingList)) {
                Config.DoNotReceiveDamageList.remove(playerName);
            }
        }, 100L);
    }

    public static void spleefCloseAction(PvpWorld plugin) {
        Config.DoNotReceiveDamageList.addAll(spleefPlayingList);
        Config.TeleportToLobbyList.addAll(spleefPlayingList);
        for (String playerName : List.copyOf(spleefPlayingList)) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player == null) continue;
            player.showTitle(titleMaker.title(textComponent.parse("<green>勝利"), textComponent.parse("<yellow>おめでとう!!!"), 0, 3000, 0));
            expUtils.playerSetExp(player, 10);
            coinUtils.playerSetCoin(player, 10);
        }
        spleefPlayingList.clear();
        FfaGames.gameCloseAction(plugin);
        Bukkit.getScheduler().runTaskLater(plugin, SpleefActivities::resetSnowBlock, 80L);
    }

    public static void voidAction(Player player, PvpWorld plugin) {
        if (!spleefPlayingList.remove(player.getName())) return;
        player.showTitle(titleMaker.title(textComponent.parse("<red>敗北"), textComponent.parse("<yellow>もう一度挑戦しよう!"), 0, 3000, 0));
        Config.clearInventory(player);
        expUtils.playerSetExp(player, 5);
        coinUtils.playerSetCoin(player, 4);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (Config.lobby != null) player.teleport(Config.lobby);
        }, 60L);
        winnerChecker(plugin);
    }

    public static void winnerChecker(PvpWorld plugin) {
        if (spleefPlayingList.size() <= 1) {
            spleefCloseAction(plugin);
        }
    }

    public static void snowBallAction(Player player) {
        if (RANDOM.nextInt(10) + 1 <= 4) {
            player.getInventory().addItem(itemStackMaker.createItem(textComponent.parse("あぶない雪玉"), Material.SNOWBALL, 1));
        }
    }

    public static void resetSnowBlock() {
        for (Location loc : locationList) {
            loc.getBlock().setType(Material.SNOW_BLOCK);
        }
        locationList.clear();
    }
}
