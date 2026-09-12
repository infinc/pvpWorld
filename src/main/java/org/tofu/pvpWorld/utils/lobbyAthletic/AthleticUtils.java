package org.tofu.pvpWorld.utils.lobbyAthletic;

import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.tofu.pvpWorld.utils.scoreBoard.ScoreBoardUtils;
import org.tofu.pvpWorld.utils.textComponent;
import org.tofu.pvpWorld.utils.titleMaker;
import org.tofu.pvpWorld.utils.yamlProperties.athleticTimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.tofu.pvpWorld.utils.yamlProperties.coinUtils;
import org.tofu.pvpWorld.utils.yamlProperties.expUtils;
import org.tofu.pvpWorld.utils.yamlProperties.systemConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.tofu.pvpWorld.utils.yamlProperties.athleticTimeUtils.setPlayerLobbyAthleticTime;

public class AthleticUtils {
    public static void startAthleticAction(Player player, PvpWorld plugin) {
        if (Config.lobbyAthleticStart == null || Config.lobbyAthleticStart.getWorld() == null) {
            player.sendMessage(textComponent.parse("<white>問題が発生しました"));
            return;
        }

        player.sendMessage(textComponent.parse("<aqua>アスレチックスタート!"));
        clearPotionEffects(player);
        Config.lobbyAthleticStart.getWorld().playEffect(player.getLocation(), Effect.PORTAL_TRAVEL, 0, 10);
        AthleticTimer.startTimer(player, plugin);
    }

    public static void stopAthleticAction(Player player) {
        if (Config.lobbyAthleticFinish == null || Config.lobbyAthleticFinish.getWorld() == null) {
            player.sendMessage(textComponent.parse("<white>問題が発生しました"));
            return;
        }

        clearPotionEffects(player);
        player.sendMessage(textComponent.parse("<white>ゴールにつきました!おめでとうございます!"));
        Config.lobbyAthleticFinish.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
        Config.lobbyAthleticFinish.getWorld().playEffect(player.getLocation(), Effect.DRAGON_BREATH, 0, 2);

        int playerScoreInt = player.getLevel();
        String playerScore = String.valueOf(playerScoreInt);
        setPlayerLobbyAthleticTime(player, playerScoreInt, false);
        ScoreBoardUtils.updateScoreBoard(player);
        player.showTitle(titleMaker.title(textComponent.parse("<aqua>おめでとう!"), textComponent.parse("<aqua>" + playerScore + "秒でした!"), 1000, 2000, 1000));
        AthleticTimer.stopTimer(player);
        player.sendMessage(textComponent.parse("<white>あなたの記録は<aqua>" + playerScore + "秒<white>でした!"));

        if (playerScoreInt <= 30) {
            giveReward(player, "reward.athletic.30s", "30秒以内");
        } else if (playerScoreInt <= 35) {
            giveReward(player, "reward.athletic.35s", "35秒以内");
        } else if (playerScoreInt <= 40) {
            giveReward(player, "reward.athletic.40s", "40秒以内");
        }

        player.setLevel(0);

        Map<String, Integer> timesMap = new HashMap<>();
        for (String uuid : athleticTimeUtils.playerLobbyAthleticTimeData.getKeys(false)) {
            timesMap.put(uuid, athleticTimeUtils.playerLobbyAthleticTimeData.getInt(uuid));
        }
        if (player.getName().equals(ScoreBoardUtils.getSortedKey(timesMap))) {
            player.sendMessage(textComponent.parse("<gold>あなたは1位です!!"));
        }

        for (String playerName : List.copyOf(Config.WorldAllPlayerList)) {
            Player other = Bukkit.getPlayerExact(playerName);
            if (other != null) ScoreBoardUtils.updateScoreBoard(other);
        }
    }

    private static void giveReward(Player player, String rewardKey, String label) {
        if (systemConfig.getValueWithPlayerInfoBOOLEAN(rewardKey, player)) return;

        systemConfig.setValueWithPlayerInfoLIST(rewardKey, player);
        expUtils.playerSetExp(player, 20);
        coinUtils.playerSetCoin(player, 25);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        player.sendMessage(textComponent.parse("<gold>報酬を獲得しました!!"));
        player.sendMessage(textComponent.parse("<gold>ロビーのアスレチックタイム: " + label));
    }

    private static void clearPotionEffects(Player player) {
        for (PotionEffect effect : List.copyOf(player.getActivePotionEffects())) {
            player.removePotionEffect(effect.getType());
        }
    }

    public static void clearAthleticTimes(Player player) {
        setPlayerLobbyAthleticTime(player, 10000, true);
        player.sendMessage(textComponent.parse("<white>あなたのスコアをリセットしました"));
    }

    public static void sendClearAthleticTimeRequest(Player player) {
        player.sendMessage(textComponent.parse("<yellow>注意!<white>本当にスコアをリセットしますか?"));
        player.sendMessage(textComponent.parse("<click:run_command:'/pvpworld actions lobbyAthletic clear'><red>[はい]</click>"));
    }
}
