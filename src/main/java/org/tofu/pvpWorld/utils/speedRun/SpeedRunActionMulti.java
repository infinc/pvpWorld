package org.tofu.pvpWorld.utils.speedRun;

import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.tofu.pvpWorld.utils.itemStackMaker;
import org.tofu.pvpWorld.utils.textComponent;
import org.tofu.pvpWorld.utils.titleMaker;
import org.tofu.pvpWorld.utils.yamlProperties.coinUtils;
import org.tofu.pvpWorld.utils.yamlProperties.expUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SpeedRunActionMulti {
    public static final List<String> multiPlayingList = new ArrayList<>(),
                                     backToLobbyList = new ArrayList<>();

    public static boolean gamePlaying = false,
                          canPressButton = false;

    private static Player centrifugalPlayer;

    public static Location startLocation, wallLoc1, wallLoc2, buttonLoc, targetBlock1, targetBlock2, appearBlock;

    public static void setupLocations(World world) {
        startLocation = new Location(world, 154.500, 5.000, 113.500, -90, 0);
        wallLoc1 = new Location(world, 167, 5, 123);
        wallLoc2 = new Location(world, 167, 14, 103);
        buttonLoc = new Location(world, 423, 9, 113);
        targetBlock1 = new Location(world, 412, 10, 107);
        targetBlock2 = new Location(world, 412, 10, 119);
        appearBlock = new Location(world, 396, 7, 113);
    }

    private static Player resolveCentrifugalPlayer() {
        if (centrifugalPlayer != null && centrifugalPlayer.isOnline()
                && multiPlayingList.contains(centrifugalPlayer.getName())) {
            return centrifugalPlayer;
        }
        for (String playerName : List.copyOf(multiPlayingList)) {
            Player candidate = Bukkit.getPlayerExact(playerName);
            if (candidate != null) {
                centrifugalPlayer = candidate;
                return candidate;
            }
        }
        centrifugalPlayer = null;
        return null;
    }

    public static void multiOnHoldAction(Player player, PvpWorld plugin) {
        if (multiPlayingList.contains(player.getName())) {
            player.sendMessage("既に参加しています!");
            return;
        }
        if (Config.overLappingTrigger(player)) {
            Config.overLappingMessage(player);
            return;
        }
        Config.beforeGame(player);

        if (gamePlaying) {
            player.sendMessage("既にゲームが進行中です!");
            player.sendMessage("もう少しお待ちください");
            return;
        }

        int previousSize = multiPlayingList.size();
        player.playSound(player.getLocation(), Sound.ENTITY_ARMADILLO_BRUSH, 1, 1);
        if (startLocation != null) player.teleport(startLocation);
        Config.clearInventory(player);

        if (previousSize == 1) noticeToPlayer();
        multiPlayingList.add(player.getName());

        if (previousSize == 0) {
            player.sendMessage("誰もプレイしていません");
            player.sendMessage("プレイするには最低2人が必要です!");
            promoteGames();
        } else if (previousSize == 1) {
            centrifugalPlayer = player;
            player.sendMessage("現在2人が参加しています!");
            player.sendMessage("追加の参加者を募集しています...");
            SpeedRunTimerMulti.startTimer(player, plugin);
        } else {
            player.sendMessage("参加しました");
            plusExtraTime();
        }

        player.getInventory().setItem(0, itemStackMaker.createItem(textComponent.parse("ロビーに戻る"), Material.RED_MUSHROOM, 1));
    }

    public static void playerLeaveAction(Player player) {
        if (!multiPlayingList.remove(player.getName())) return;
        player.sendMessage("SpeedRunMultiを退出しました");
        player.setLevel(0);

        PvpWorld plugin = PvpWorld.getPlugin(PvpWorld.class);

        if (gamePlaying) {
            if (multiPlayingList.size() == 1) {
                Player lastPlayer = Bukkit.getPlayerExact(multiPlayingList.get(0));
                if (lastPlayer != null) {
                    winAction(lastPlayer, plugin);
                    return;
                }
                resetGame();
                return;
            }
            if (multiPlayingList.isEmpty()) {
                resetGame();
                return;
            }
            broadcastToPlayers(player.getName() + "さんが退出しました");
            return;
        }

        broadcastToPlayers(player.getName() + "さんが退出しました");
        if (multiPlayingList.size() <= 1) {
            SpeedRunTimerMulti.stopTimer();
            for (String playerName : List.copyOf(multiPlayingList)) {
                Player remaining = Bukkit.getPlayerExact(playerName);
                if (remaining == null) continue;
                remaining.sendMessage("最低人数に達していないため、タイマーをストップします");
                remaining.setLevel(0);
            }
        }
    }

    private static void broadcastToPlayers(String message) {
        for (String playerName : List.copyOf(multiPlayingList)) {
            Player member = Bukkit.getPlayerExact(playerName);
            if (member != null) member.sendMessage(message);
        }
    }

    private static void resetGame() {
        gamePlaying = false;
        canPressButton = false;
        SpeedRunTimerMulti.stopTimer();
        SpeedRunScheduledTimer.stopTimer(centrifugalPlayer);
        centrifugalPlayer = null;
        multiPlayingList.clear();
        fillBlock(wallLoc1, wallLoc2, Material.GLASS);
    }

    public static void winAction(Player player, PvpWorld plugin) {
        canPressButton = false;
        gamePlaying = false;
        SpeedRunTimerMulti.stopTimer();
        SpeedRunScheduledTimer.stopTimer(centrifugalPlayer);

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 2);
        player.showTitle(titleMaker.title(textComponent.parse("<green>勝利"), textComponent.parse("<yellow>おめでとう!!"), 0, 5000, 0));

        for (String playerName : List.copyOf(multiPlayingList)) {
            Player member = Bukkit.getPlayerExact(playerName);
            if (member != null) member.setLevel(0);
            Config.addIfAbsent(backToLobbyList, playerName);
        }

        multiPlayingList.remove(player.getName());
        fillBlock(wallLoc1, wallLoc2, Material.GLASS);
        loseAction();
        multiPlayingList.clear();
        centrifugalPlayer = null;
        expUtils.playerSetExp(player, 30);
        coinUtils.playerSetCoin(player, 38);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (String playerName : List.copyOf(backToLobbyList)) {
                Player member = Bukkit.getPlayerExact(playerName);
                if (member != null && Config.lobby != null) member.teleport(Config.lobby);
            }
            backToLobbyList.clear();
        }, 100L);
    }

    public static void loseAction() {
        for (String playerName : List.copyOf(multiPlayingList)) {
            Player member = Bukkit.getPlayerExact(playerName);
            if (member == null) continue;
            member.playSound(member.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1, 1);
            member.showTitle(titleMaker.title(textComponent.parse("<red>敗北"), textComponent.parse("<yellow>次も頑張ろう!!"), 0, 5000, 0));
            expUtils.playerSetExp(member, 10);
            coinUtils.playerSetCoin(member, 13);
        }
        gamePlaying = false;
    }

    public static void fillBlock(Location loc1, Location loc2, Material material) {
        if (loc1 == null || loc2 == null) return;
        World world = loc1.getWorld();
        if (world == null || !world.equals(loc2.getWorld())) return;

        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    world.getBlockAt(x, y, z).setType(material);
                }
            }
        }
    }

    public static void startAction(PvpWorld plugin) {
        Player owner = resolveCentrifugalPlayer();
        if (owner == null || multiPlayingList.size() < 2) {
            resetGame();
            return;
        }

        fillBlock(wallLoc1, wallLoc2, Material.AIR);
        gamePlaying = true;
        canPressButton = true;

        for (String playerName : List.copyOf(multiPlayingList)) {
            Player member = Bukkit.getPlayerExact(playerName);
            if (member == null) continue;
            member.playSound(member.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);
            member.showTitle(titleMaker.title(textComponent.parse("<aqua>スタート!"), textComponent.parse(""), 1000, 1000, 1000));
            member.setLevel(0);
        }

        SpeedRunScheduledTimer.startTimer(owner, plugin, true);
    }

    public static void checkButton(Block block, Player player, PvpWorld plugin) {
        if (buttonLoc == null || !canPressButton) return;
        if (!multiPlayingList.contains(player.getName())) return;
        if (!block.getWorld().equals(buttonLoc.getWorld())) return;
        if (block.getX() != buttonLoc.getBlockX()
                || block.getY() != buttonLoc.getBlockY()
                || block.getZ() != buttonLoc.getBlockZ()) return;
        winAction(player, plugin);
    }

    public static void promoteGames() {
        for (String playerName : List.copyOf(Config.WorldAllPlayerList)) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player == null) continue;
            player.playSound(player.getLocation(), Sound.ENTITY_ARMADILLO_BRUSH, 1, 1);
            player.sendMessage(textComponent.parse("<gold>[SpeedRun Multi]<white>1人が対戦相手を募集中です!"));
        }
    }

    public static void noticeToPlayer() {
        for (String playerName : List.copyOf(multiPlayingList)) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player == null) continue;
            player.sendMessage(textComponent.parse("<white>対戦相手が見つかったため、カウントダウンを開始します!"));
        }
    }

    public static void plusExtraTime() {
        if (!SpeedRunTimerMulti.isRunning() || SpeedRunTimerMulti.getRemainingTime() > 10) return;
        SpeedRunTimerMulti.addTime(10);
        int size = multiPlayingList.size();
        for (String playerName : List.copyOf(multiPlayingList)) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player == null) continue;
            player.sendMessage(textComponent.parse("<white>" + size + "人目が参加したため、10秒追加しました!"));
        }
    }

    public static void checkArrowInfo(Location location, Player player, PvpWorld plugin) {
        if (appearBlock == null || targetBlock1 == null || targetBlock2 == null) return;

        boolean isTarget1 = (location.getBlockX() == targetBlock1.getBlockX() &&
                location.getBlockY() == targetBlock1.getBlockY() &&
                location.getBlockZ() == targetBlock1.getBlockZ());

        boolean isTarget2 = (location.getBlockX() == targetBlock2.getBlockX() &&
                location.getBlockY() == targetBlock2.getBlockY() &&
                location.getBlockZ() == targetBlock2.getBlockZ());

        if (!isTarget1 && !isTarget2) return;

        Location currentLocation = appearBlock.clone();
        currentLocation.getBlock().setType(Material.GOLD_BLOCK);
        player.sendMessage(textComponent.parse("<gold>足場が出現しました!"));
        Bukkit.getScheduler().runTaskLater(plugin, () -> currentLocation.getBlock().setType(Material.AIR), 20L);
    }
}
