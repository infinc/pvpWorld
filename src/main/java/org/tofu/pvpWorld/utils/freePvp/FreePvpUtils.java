package org.tofu.pvpWorld.utils.freePvp;

import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.tofu.pvpWorld.utils.itemStackMaker;
import org.tofu.pvpWorld.utils.lobbyAthletic.AthleticTimer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.tofu.pvpWorld.utils.textComponent;

import java.util.List;

public class FreePvpUtils {
    public static void ruleExplain(Player player) {
        player.sendMessage(textComponent.parse("<aqua>-----Free PVP-----"));
        player.sendMessage(textComponent.parse("<white>このゲームは、自由参加型の<gold>FFA<white>PVPゲームです!"));
        player.sendMessage(textComponent.parse("<white>FFAとは、味方がいない、全員敵のゲームのことです。"));
        player.sendMessage(textComponent.parse("<white>下の闘技場に飛び込むことで参加でき、3秒後に無敵化が切れてアイテムが支給されます"));
        player.sendMessage(textComponent.parse("<white>鉄剣、弓、矢8本、ヘルメット以外のチェーン装備が支給されます。"));
        player.sendMessage(textComponent.parse("<white>敵を倒すと金リンゴがもらえ、継続的に試合を続けられます!"));
        player.sendMessage(textComponent.parse("<white>退出する際は、インベントリ内にある赤いキノコをホットバーに移動させて、右クリックしてください。"));
        player.sendMessage(textComponent.parse("<aqua>---------------------"));
    }

    public static void joinAction(Player player, PvpWorld plugin) {
        if (Config.FreePvpPlayerList.contains(player.getName())) {
            player.sendMessage(textComponent.parse("エラーが発生しました"));
            return;
        }

        AthleticTimer.stopTimer(player);
        Config.FreePvpPlayerList.add(player.getName());
        player.getInventory().setItem(0, null);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline() || !Config.FreePvpPlayerList.contains(player.getName())) return;
            Config.DoNotReceiveDamageList.remove(player.getName());
            player.getInventory().setItem(0, itemStackMaker.createItem(textComponent.parse("聖なる剣"), Material.IRON_SWORD, 1));
            player.getInventory().setItem(1, itemStackMaker.createItem(textComponent.parse("弓"), Material.BOW, 1));
            player.getInventory().setItem(8, itemStackMaker.createItem(textComponent.parse("矢"), Material.ARROW, 8));
            player.getInventory().setItem(38, itemStackMaker.createItem(textComponent.parse("チェストプレート"), Material.CHAINMAIL_CHESTPLATE, 1));
            player.getInventory().setItem(37, itemStackMaker.createItem(textComponent.parse("レギンス"), Material.CHAINMAIL_LEGGINGS, 1));
            player.getInventory().setItem(36, itemStackMaker.createItem(textComponent.parse("ブーツ"), Material.CHAINMAIL_BOOTS, 1));
            player.getInventory().setItem(12, itemStackMaker.createItem(textComponent.parse("ロビーに戻る"), Material.RED_MUSHROOM, 1));
        }, 60L);

        for (String playerName : List.copyOf(Config.WorldAllPlayerList)) {
            Player player2 = Bukkit.getPlayerExact(playerName);
            if (player2 == null) continue;
            player2.sendMessage(textComponent.parse("<gold>" + player.getName() + "さんがFree PVPスペースに参加しました"));
        }
    }

    public static void freePvpPlayerRespawnAction(Player player, PvpWorld plugin) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            Config.FreePvpPlayerList.remove(player.getName());
            Config.addIfAbsent(Config.DoNotReceiveDamageList, player.getName());
            player.setGameMode(GameMode.SURVIVAL);
            if (Config.freePvpSpawnPoint != null) player.teleport(Config.freePvpSpawnPoint);
        }, 2L);
    }
}
