package org.tofu.pvpWorld.utils.textDisplay;

import net.kyori.adventure.text.Component;
import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.utils.athletic.AthleticProperties;
import org.tofu.pvpWorld.utils.textComponent;
import org.tofu.pvpWorld.utils.yamlProperties.athleticTimeUtils;
import org.tofu.pvpWorld.utils.yamlProperties.coinUtils;
import org.tofu.pvpWorld.utils.yamlProperties.expUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextDisplayUtils {
    public static final String ARMOR_STAND_TAG = "pvpWorldText";

    private static final double LINE_SPACING = 0.25;

    private static final int RANKING_SIZE = 10;

    public static Location SpeedRun, OneVersusOne, FfaGames, expRanking, coinRanking, athleticRanking;

    public static Component SpeedRunSize, OneVersusOneSize, FfaGamesSize;

    public static List<Component> exp, coin, athletic;

    private static final Map<String, List<ArmorStand>> spawnedLines = new HashMap<>();

    private static World world;

    public static void locationSetUp() {
        world = Config.world;
        if (world == null) return;

        SpeedRun = new Location(world, -18.500, 7.000, -0.500);
        OneVersusOne = new Location(world, 0.500, 7.000, -19.500);
        FfaGames = new Location(world, 0.500, 7.000, 18.500);
        SpeedRunSize = textComponent.parse("<gold>0<white>人がプレイ中!");
        OneVersusOneSize = textComponent.parse("<gold>0<white>人がプレイ中!");
        FfaGamesSize = textComponent.parse("<gold>0<white>人がプレイ中!");
        expRanking = new Location(world, -5.500, 6.000, 44.000);
        coinRanking = new Location(world, 6.500, 6.000, 44.000);
        athleticRanking = new Location(world, -5.500, 6.000, 55.000);

        removeAllText();
        latestRanking();
    }

    public static void latestRanking() {
        if (world == null) return;

        exp = new ArrayList<>();
        exp.add(textComponent.parse("<green>EXP<red>ランキング"));
        for (int i = 1; i <= RANKING_SIZE; i++) exp.add(expUtils.getRanking(i));
        exp.add(textComponent.parse("<green>クリックして更新"));

        coin = new ArrayList<>();
        coin.add(textComponent.parse("<gold>COIN<red>ランキング"));
        for (int i = 1; i <= RANKING_SIZE; i++) coin.add(coinUtils.getRanking(i));
        coin.add(textComponent.parse("<green>クリックして更新"));

        athletic = new ArrayList<>();
        athletic.add(textComponent.parse("<yellow>Athletic<red>ランキング"));
        for (int i = 1; i <= RANKING_SIZE; i++) athletic.add(athleticTimeUtils.getRanking(i));
        athletic.add(textComponent.parse("<green>クリックして更新"));

        showAllText();
    }

    public static void renameSpeedRun(int size) {
        SpeedRunSize = textComponent.parse("<gold>" + size + "<white>人がプレイ中!");
        showAllText();
    }

    public static void renameOneVersusOneSize(int size) {
        OneVersusOneSize = textComponent.parse("<gold>" + size + "<white>人がプレイ中!");
        showAllText();
    }

    public static void renameFfaGamesSize(int size) {
        FfaGamesSize = textComponent.parse("<gold>" + size + "<white>人がプレイ中!");
        showAllText();
    }

    public static void showAllText() {
        if (world == null) return;

        renderLines("speedRun", SpeedRun, List.of(SpeedRunSize), true);
        renderLines("oneVersusOne", OneVersusOne, List.of(OneVersusOneSize), true);
        renderLines("ffaGames", FfaGames, List.of(FfaGamesSize), true);
        if (exp != null) renderLines("expRanking", expRanking, exp, false);
        if (coin != null) renderLines("coinRanking", coinRanking, coin, false);
        if (athletic != null) renderLines("athleticRanking", athleticRanking, athletic, false);

        AthleticProperties.showAllText();
    }

    public static void renderLines(String key, Location origin, List<Component> lines, boolean marker) {
        if (origin == null || origin.getWorld() == null) return;

        List<ArmorStand> existing = spawnedLines.get(key);
        if (existing != null && existing.size() == lines.size() && allValid(existing)) {
            for (int i = 0; i < lines.size(); i++) {
                existing.get(i).customName(lines.get(i));
            }
            return;
        }

        if (existing != null) {
            for (ArmorStand as : existing) as.remove();
        }

        List<ArmorStand> created = new ArrayList<>(lines.size());
        Location cursor = origin.clone();
        for (Component line : lines) {
            ArmorStand as = origin.getWorld().spawn(cursor.clone(), ArmorStand.class);
            armorStandSettings(as, line, marker);
            created.add(as);
            cursor.add(0, -LINE_SPACING, 0);
        }
        spawnedLines.put(key, created);
    }

    private static boolean allValid(List<ArmorStand> stands) {
        for (ArmorStand as : stands) {
            if (!as.isValid()) return false;
        }
        return true;
    }

    public static void removeAllText() {
        if (world == null) return;
        for (Entity entity : world.getEntities()) {
            if (entity instanceof ArmorStand && entity.getScoreboardTags().contains(ARMOR_STAND_TAG)) {
                entity.remove();
            }
        }
        spawnedLines.clear();
    }

    public static void armorStandSettings(ArmorStand as, Component text, boolean marker) {
        as.addScoreboardTag(ARMOR_STAND_TAG);
        as.setBasePlate(false);
        as.setCustomNameVisible(true);
        as.customName(text);
        as.setArms(false);
        as.setVisible(false);
        as.setInvulnerable(true);
        as.setCanPickupItems(false);
        as.setGravity(false);
        as.setMarker(marker);
    }
}
