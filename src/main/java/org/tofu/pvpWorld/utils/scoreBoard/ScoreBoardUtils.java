package org.tofu.pvpWorld.utils.scoreBoard;

import net.kyori.adventure.text.Component;
import org.bukkit.scoreboard.*;
import org.tofu.pvpWorld.utils.textComponent;
import org.tofu.pvpWorld.utils.yamlProperties.athleticTimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

import static org.tofu.pvpWorld.utils.yamlProperties.athleticTimeUtils.getPlayerLobbyAthleticTime;
import static org.tofu.pvpWorld.utils.yamlProperties.coinUtils.getPlayerCoin;
import static org.tofu.pvpWorld.utils.yamlProperties.expUtils.getPlayerExp;

public class ScoreBoardUtils {
    private static final String OBJECTIVE_NAME = "lobby";

    public static Scoreboard createScoreBoard() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return null;
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY, textComponent.parse("<b><yellow>==PVPWORLD=="));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        return scoreboard;
    }

    public static void updateScoreBoard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) {
            player.sendMessage(textComponent.parse("<white>error"));
            return;
        }

        Scoreboard scoreBoard = player.getScoreboard();
        if (scoreBoard.equals(manager.getMainScoreboard()) || scoreBoard.getObjective(OBJECTIVE_NAME) == null) {
            scoreBoard = createScoreBoard();
        }
        if (scoreBoard == null) {
            player.sendMessage(textComponent.parse("<white>error"));
            return;
        }

        Objective objective = scoreBoard.getObjective(OBJECTIVE_NAME);
        if (objective == null) return;

        Map<String, Integer> timesMap = new HashMap<>();
        if (athleticTimeUtils.playerLobbyAthleticTimeData != null) {
            for (String uuid : athleticTimeUtils.playerLobbyAthleticTimeData.getKeys(false)) {
                timesMap.put(uuid, athleticTimeUtils.playerLobbyAthleticTimeData.getInt(uuid));
            }
        }

        setLine(objective, 12, textComponent.parse("<u><aqua>" + player.getName() + "さん"));
        setLine(objective, 11, Component.empty());
        setLine(objective, 10, textComponent.parse("<white>EXP: <green>" + getPlayerExp(player)));
        setLine(objective, 9, textComponent.parse("<white>Coin: <gold>" + getPlayerCoin(player)));
        setLine(objective, 8, Component.empty());
        setLine(objective, 7, textComponent.parse("<white>アスレチックのランキング"));
        setLine(objective, 6, textComponent.parse("<white>あなたのスコア: " + getPlayerLobbyAthleticTime(player)));
        setLine(objective, 5, textComponent.parse("<white>1番の人のスコア: <gold>" + getSortedInteger(timesMap)));
        setLine(objective, 4, textComponent.parse("<white>名前: <gold>" + getSortedKey(timesMap)));
        setLine(objective, 3, Component.empty());
        setLine(objective, 2, textComponent.parse("<white>分からないことがあったら..."));
        setLine(objective, 1, textComponent.parse("<aqua><bold>/pvpworld help"));

        player.setScoreboard(scoreBoard);
    }

    private static void setLine(Objective objective, int line, Component text) {
        Score score = objective.getScore("line" + line);
        score.setScore(line);
        score.customName(text);
    }

    public static void removeScoreBoard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) {
            player.sendMessage(textComponent.parse("<white>error"));
            return;
        }
        player.setScoreboard(manager.getMainScoreboard());
    }

    public static Integer getSortedInteger(Map<String, Integer> map) {
        if (map.isEmpty()) return 0;
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(map.entrySet());
        sortedEntries.sort(Map.Entry.comparingByValue());
        return sortedEntries.getFirst().getValue();
    }

    public static String getSortedKey(Map<String, Integer> map) {
        if (map.isEmpty()) return "N/A";
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(map.entrySet());
        sortedEntries.sort(Map.Entry.comparingByValue());
        String playerUUID = sortedEntries.getFirst().getKey();
        try {
            UUID uuid = UUID.fromString(playerUUID);
            String name = Bukkit.getOfflinePlayer(uuid).getName();
            return name != null ? name : "Unknown";
        } catch (IllegalArgumentException e) {
            return "N/A";
        }
    }
}
