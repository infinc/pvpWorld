package org.tofu.pvpWorld.utils.oneVersusOne;

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

public class OneVersusOneGames {
    private static final int MAX_PLAYERS = 2;

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
            TextDisplayUtils.renameOneVersusOneSize(OneVersusOneAllPlayer());
        }, 60L);
    }

    public static void drawAction(List<String> arrayList, PvpWorld plugin) {
        for (String playerName : List.copyOf(arrayList)) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null) {
                player.showTitle(titleMaker.title(textComponent.parse("<yellow>引き分け"), textComponent.parse("<yellow>勝利までもう少し!!"), 0, 3000, 0));
            }
            Config.addIfAbsent(Config.DoNotReceiveDamageList, playerName);
            Config.addIfAbsent(Config.TeleportToLobbyList, playerName);
        }
        arrayList.clear();
        TextDisplayUtils.renameOneVersusOneSize(OneVersusOneAllPlayer());
        gameCloseAction(plugin);
    }

    public static void noWalkRemoveAction(List<String> arrayList) {
        for (String playerName : List.copyOf(arrayList)) {
            Config.NoWalkList.remove(playerName);
        }
    }

    public static void timeUpAction(Player player, PvpWorld plugin) {
        String playerName = player.getName();
        if (SumoActivities.sumoQueueingList.contains(playerName)) {
            drawAction(SumoActivities.sumoQueueingList, plugin);
        } else if (TopfightActivities.topfightQueueingList.contains(playerName)) {
            drawAction(TopfightActivities.topfightQueueingList, plugin);
        }
    }

    public static void queueingActivities(Player player, InventoryClickEvent e, PvpWorld plugin, List<String> arrayList) {
        e.setCancelled(true);
        player.closeInventory();

        if (arrayList.contains(player.getName())) {
            player.sendMessage(textComponent.parse("<white>既に参加しています!"));
            player.sendMessage(textComponent.parse("<white>退出するにはインベントリ内の赤い染料を右クリックしてください"));
            return;
        }

        if (arrayList.size() >= MAX_PLAYERS) {
            player.sendMessage(textComponent.parse("<white>既に誰かがプレイ中です"));
            return;
        }

        boolean opponentWaiting = arrayList.size() == 1;
        arrayList.add(player.getName());
        TextDisplayUtils.renameOneVersusOneSize(OneVersusOneAllPlayer());
        InventoryUtils.replaceInventoryCheck(player);

        if (opponentWaiting) {
            player.sendMessage(textComponent.parse("<white>相手が見つかりました!"));
            dividePlayer(arrayList, player, plugin);
        } else {
            player.sendMessage(textComponent.parse("<white>他の人を待っています..."));
            player.sendMessage(textComponent.parse("<white>参加をやめるには、インベントリの中の赤色の染料を右クリックしてください"));
            player.getInventory().setItem(8, itemStackMaker.createItem(textComponent.parse("<white>ゲームをやめる"), Material.RED_DYE, 1));
            encourageJoinGame(player);
        }
    }

    public static void dividePlayer(List<String> arrayList, Player player, PvpWorld plugin) {
        if (arrayList == SumoActivities.sumoQueueingList) {
            SumoActivities.sumoStartAction(player, plugin);
        } else if (arrayList == TopfightActivities.topfightQueueingList) {
            TopfightActivities.topfightStartAction(player, plugin);
        } else {
            player.sendMessage(textComponent.parse("<white>エラー"));
        }
    }

    public static void encourageJoinGame(Player player) {
        String base = "1人が対戦相手を待機中です!";
        String prefix;
        if (SumoActivities.sumoQueueingList.contains(player.getName())) {
            prefix = "<yellow>[Sumo] <white>";
        } else if (TopfightActivities.topfightQueueingList.contains(player.getName())) {
            prefix = "<red>[TopFight] <white>";
        } else {
            return;
        }

        for (String playerName : List.copyOf(Config.WorldAllPlayerList)) {
            Player player2 = Bukkit.getPlayerExact(playerName);
            if (player2 == null) continue;
            player2.playSound(player2.getLocation(), Sound.ENTITY_ARMADILLO_BRUSH, 1, 1);
            player2.sendMessage(textComponent.parse(prefix + base));
        }
    }

    public static int OneVersusOneAllPlayer() {
        return SumoActivities.sumoQueueingList.size() + TopfightActivities.topfightQueueingList.size();
    }

    public static void clickedRed_Dye(Player player) {
        if (SumoActivities.sumoQueueingList.remove(player.getName())
                || TopfightActivities.topfightQueueingList.remove(player.getName())) {
            StartTimerUtils.stopTimer(player);
            TimeUpTimer.stopTimer(player);
            player.setLevel(0);
            TextDisplayUtils.renameOneVersusOneSize(OneVersusOneAllPlayer());
        }
        if (player.getInventory().getItemInMainHand().getType() == Material.RED_DYE) {
            player.getInventory().setItemInMainHand(null);
            player.sendMessage(Component.text("退出しました"));
        }
    }
}
