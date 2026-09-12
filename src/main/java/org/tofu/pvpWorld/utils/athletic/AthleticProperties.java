package org.tofu.pvpWorld.utils.athletic;

import net.kyori.adventure.text.Component;
import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.tofu.pvpWorld.utils.textComponent;
import org.tofu.pvpWorld.utils.textDisplay.TextDisplayUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AthleticProperties {
    public static final int DEFAULT_TIME = 10000;

    public static Location boxDescription, boxRankingLoc;

    public static final List<Component> boxDesc = new ArrayList<>(),
                                        boxRanking = new ArrayList<>();

    public static FileConfiguration atheticBoxData;

    public static File athleticBoxFile;

    public static void setup() {
        if (Config.world == null) return;

        boxDescription = new Location(Config.world, 79.500, 6.000, 145.500);
        boxRankingLoc = new Location(Config.world, 75.500, 6.000, 145.500);

        boxDesc.clear();
        boxDesc.add(textComponent.parse("箱の中でアスレチックをします!"));
        boxDesc.add(textComponent.parse("難易度: <green>低</green>"));

        updateRanking();
    }

    public static void updateRanking() {
        boxRanking.clear();
        boxRanking.add(textComponent.parse("<yellow>Box"));

        showAllText();
    }

    public static void showAllText() {
        TextDisplayUtils.renderLines("athleticBoxDesc", boxDescription, boxDesc, false);
        TextDisplayUtils.renderLines("athleticBoxRanking", boxRankingLoc, boxRanking, false);
    }

    public static void athleticYamlSetup(PvpWorld plugin) {
        athleticBoxFile = new File(plugin.getDataFolder(), "athletic.yml");
        if (!athleticBoxFile.exists()) {
            try {
                if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                    plugin.getLogger().warning("データフォルダを作成できませんでした");
                }
                if (!athleticBoxFile.createNewFile()) {
                    plugin.getLogger().warning("athletic.yml を作成できませんでした");
                }
            } catch (IOException e) {
                plugin.getLogger().warning("athletic.yml の作成に失敗しました: " + e.getMessage());
            }
        }
        atheticBoxData = YamlConfiguration.loadConfiguration(athleticBoxFile);
    }

    public static int getAthleticTime(FileConfiguration fileConfiguration, Player player, String path) {
        if (fileConfiguration == null) return DEFAULT_TIME;
        return fileConfiguration.getInt(path + player.getUniqueId(), DEFAULT_TIME);
    }

    public static boolean setAthleticTime(FileConfiguration fileConfiguration, int time, Player player) {
        if (atheticBoxData == null || fileConfiguration != atheticBoxData) return false;
        if (getAthleticTime(atheticBoxData, player, "Box.") < time) return false;

        atheticBoxData.set("Box." + player.getUniqueId(), time);
        try {
            atheticBoxData.save(athleticBoxFile);
        } catch (IOException e) {
            PvpWorld.getPlugin(PvpWorld.class).getLogger()
                    .warning("athletic.yml の保存に失敗しました: " + e.getMessage());
            return false;
        }
        return true;
    }
}
