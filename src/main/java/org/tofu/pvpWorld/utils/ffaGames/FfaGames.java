package org.tofu.pvpWorld.utils.ffaGames;

import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.tofu.pvpWorld.utils.itemStackMaker;
import org.tofu.pvpWorld.utils.textComponent;
import org.tofu.pvpWorld.utils.textDisplay.TextDisplayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.tofu.pvpWorld.utils.titleMaker;

import java.util.List;

public class FfaGames {
    public static void gameCloseAction(PvpWorld plugin) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (String playerName : List.copyOf(Config.TeleportToLobbyList)) {
                Player player = Bukkit.getPlayerExact(playerName);
                if (player == null) continue;
                player.getInventory().clear();
                StartTimerUtils.stopTimer(player);
                TimeUpTimer.stopTimer(player);
                if (Config.lobby != null) player.teleport(Config.lobby);
                player.getInventory().setItem(0, itemStackMaker.createItem(textComponent.parse("<white>ロビーに戻る"), Material.RED_MUSHROOM, 1));
            }
            Config.TeleportToLobbyList.clear();
            TextDisplayUtils.renameFfaGamesSize(allFfaGamesPlayer());
        }, 60L);
    }

    public static void timeUpAction(PvpWorld plugin) {
        if (SpleefActivities.spleefPlayingList.isEmpty()) return;

        Config.DoNotReceiveDamageList.addAll(SpleefActivities.spleefPlayingList);
        Config.TeleportToLobbyList.addAll(SpleefActivities.spleefPlayingList);
        for (String playerName : List.copyOf(SpleefActivities.spleefPlayingList)) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player == null) continue;
            player.showTitle(titleMaker.title(textComponent.parse("<yellow>引き分け"), textComponent.parse("<yellow>時間切れです"), 0, 3000, 0));
        }
        SpleefActivities.spleefPlayingList.clear();
        gameCloseAction(plugin);
        Bukkit.getScheduler().runTaskLater(plugin, SpleefActivities::resetSnowBlock, 80L);
    }

    public static void noWalkRemoveAction(List<String> arrayList) {
        for (String playerName : List.copyOf(arrayList)) {
            Config.NoWalkList.remove(playerName);
        }
    }

    public static void ffaQueueingActivities(Player player, List<String> arrayList, PvpWorld plugin, InventoryClickEvent e) {
        e.setCancelled(true);
        player.closeInventory();

        if (arrayList.contains(player.getName())) {
            player.sendMessage(textComponent.parse("既に参加しています!"));
            player.sendMessage(textComponent.parse("退出するにはインベントリ内の青い染料を右クリックしてください"));
            return;
        }

        boolean wasWaitingAlone = arrayList.size() == 1;
        arrayList.add(player.getName());
        player.getInventory().setItem(8, itemStackMaker.createItem(textComponent.parse("ゲームをやめる"), Material.BLUE_DYE, 1));
        TextDisplayUtils.renameFfaGamesSize(allFfaGamesPlayer());
        InventoryUtils.replaceInventoryCheck(player);

        if (arrayList.size() == 1) {
            player.sendMessage(textComponent.parse("他の人を待っています..."));
            player.sendMessage(textComponent.parse("参加をやめるには、インベントリの中の青色の染料を右クリックしてください"));
            encourageJoinGame(player);
        } else if (wasWaitingAlone) {
            player.sendMessage(textComponent.parse("相手が見つかりました!"));
            player.sendMessage(textComponent.parse("追加の人を探しています..."));
            StartTimerUtils.startTimer(player, plugin, arrayList);
        } else {
            player.sendMessage(textComponent.parse("参加しました"));
        }
    }

    public static void playerQuitByBlueDyeAction(List<String> arrayList, Player player) {
        if (!arrayList.remove(player.getName())) return;
        if (arrayList.size() != 1) return;

        StartTimerUtils.stopTimer(player);
        for (String playerName : List.copyOf(arrayList)) {
            Player waiting = Bukkit.getPlayerExact(playerName);
            if (waiting == null) continue;
            waiting.sendMessage(textComponent.parse("最低人数に達していないため、タイマーを止めます"));
            StartTimerUtils.stopTimer(waiting);
            waiting.setLevel(0);
        }
    }

    public static void playerQuitByLeaveWorldAction(List<String> arrayList, String playerName, PvpWorld plugin) {
        if (!arrayList.remove(playerName)) return;
        if (arrayList.size() != 1) return;

        for (String remaining : List.copyOf(arrayList)) {
            Player player = Bukkit.getPlayerExact(remaining);
            if (player != null) {
                player.showTitle(titleMaker.title(textComponent.parse("<green>勝利"), textComponent.parse("<yellow>対戦相手が放棄しました"), 0, 3000, 0));
                StartTimerUtils.stopTimer(player);
            }
            Config.TeleportToLobbyList.add(remaining);
        }
        arrayList.clear();
        gameCloseAction(plugin);
    }

    public static void encourageJoinGame(Player player) {
        String base = "1人が対戦相手を待機中です!";
        if (!SpleefActivities.spleefQueueingList.contains(player.getName())) return;

        for (String playerName : List.copyOf(Config.WorldAllPlayerList)) {
            Player player2 = Bukkit.getPlayerExact(playerName);
            if (player2 == null) continue;
            player2.playSound(player2.getLocation(), Sound.ENTITY_ARMADILLO_BRUSH, 1, 1);
            player2.sendMessage(textComponent.parse("<yellow>[Spleef]</yellow><white>" + base));
        }
    }

    public static int allFfaGamesPlayer() {
        return SpleefActivities.spleefQueueingList.size() + SpleefActivities.spleefPlayingList.size();
    }

    public static void clickedBlue_Dye(Player player) {
        if (SpleefActivities.spleefQueueingList.contains(player.getName())) {
            playerQuitByBlueDyeAction(SpleefActivities.spleefQueueingList, player);
            TextDisplayUtils.renameFfaGamesSize(allFfaGamesPlayer());
            player.setLevel(0);
        }
        player.getInventory().setItemInMainHand(null);
        player.sendMessage(Component.text("退出しました"));
    }
}
