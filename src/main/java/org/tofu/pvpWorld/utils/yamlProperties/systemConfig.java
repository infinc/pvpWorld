package org.tofu.pvpWorld.utils.yamlProperties;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.tofu.pvpWorld.PvpWorld;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class systemConfig {
    private static final String ATHLETIC_TIME_RESET_PATH = "athleticTimeReset";

    public static File systemConfigFile;

    public static FileConfiguration systemConfig;

    public static void systemConfigSetUp(PvpWorld plugin) {
        systemConfigFile = new File(plugin.getDataFolder(), "systemConfig.yml");
        systemConfig = YamlConfiguration.loadConfiguration(systemConfigFile);
        checkAndCreateKey("reward.athletic.40s");
        checkAndCreateKey("reward.athletic.35s");
        checkAndCreateKey("reward.athletic.30s");

        saveConfig();
    }

    private static boolean isRewardKey(String request) {
        return request.equals("reward.athletic.40s")
                || request.equals("reward.athletic.35s")
                || request.equals("reward.athletic.30s");
    }

    public static String getValueWithoutPlayerInfo(String request) {
        if (!request.equals(ATHLETIC_TIME_RESET_PATH)) return null;

        long nowTime = System.currentTimeMillis();
        long before = systemConfig.getLong(ATHLETIC_TIME_RESET_PATH);
        long difference = nowTime - before;
        long twoWeek = 24L * 60 * 60 * 1000 * 14;
        return twoWeek > difference ? "false" : "true";
    }

    public static boolean getValueWithPlayerInfoBOOLEAN(String request, Player player) {
        if (!isRewardKey(request)) return false;
        return systemConfig.getStringList(request).contains(player.getUniqueId().toString());
    }

    public static void setValueWithPlayerInfoLIST(String request, Player player) {
        if (!isRewardKey(request)) return;

        String playerUuid = player.getUniqueId().toString();
        List<String> currentList = systemConfig.getStringList(request);

        if (!currentList.contains(playerUuid)) {
            currentList.add(playerUuid);
            systemConfig.set(request, currentList);
            saveConfig();
        }
    }

    public static void setSystemConfigAthleticTime(long time) {
        systemConfig.set(ATHLETIC_TIME_RESET_PATH, time);
        saveConfig();
    }

    public static void saveConfig() {
        if (systemConfig == null || systemConfigFile == null) return;
        try {
            systemConfig.save(systemConfigFile);
        } catch (IOException e) {
            PvpWorld.getPlugin(PvpWorld.class).getLogger()
                    .warning("systemConfig.yml の保存に失敗しました: " + e.getMessage());
        }
    }

    public static void checkAndCreateKey(String path) {
        if (!systemConfig.contains(path)) {
            systemConfig.set(path, new ArrayList<String>());
        }
    }
}
