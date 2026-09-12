package org.tofu.pvpWorld;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.tofu.pvpWorld.utils.itemStackMaker;
import org.tofu.pvpWorld.utils.textComponent;
import org.tofu.pvpWorld.utils.ffaGames.SpleefActivities;
import org.tofu.pvpWorld.utils.lobbyAthletic.AthleticTimer;
import org.tofu.pvpWorld.utils.oneVersusOne.StartTimerUtils;
import org.tofu.pvpWorld.utils.oneVersusOne.SumoActivities;
import org.tofu.pvpWorld.utils.oneVersusOne.TimeUpTimer;
import org.tofu.pvpWorld.utils.oneVersusOne.TopfightActivities;
import org.tofu.pvpWorld.utils.speedRun.SpeedRunActionMulti;
import org.tofu.pvpWorld.utils.speedRun.SpeedRunScheduledTimer;
import org.tofu.pvpWorld.utils.speedRun.SpeedRunTimer;
import org.tofu.pvpWorld.utils.textDisplay.TextDisplayUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Config {
    public static final String WORLD_NAME = "pvpWorld";

    public static World world;

    public static File playerLastLoginFile;
    public static FileConfiguration playerLastLogin;

    public static final List<String> WorldAllPlayerList = new ArrayList<>(),
            DoNotReceiveDamageList = new ArrayList<>(),
            SpeedRunSingleOnHoldList = new ArrayList<>(),
            AdminBuildModeList = new ArrayList<>(),
            SpeedRunSingleList = new ArrayList<>(),
            NoWalkList = new ArrayList<>(),
            FreePvpPlayerList = new ArrayList<>(),
            TeleportToLobbyList = new ArrayList<>();

    public static Location lobby,
            lobbyAthleticStart,
            lobbyAthleticFinish,
            speedRunSingleOnholdRoom,
            speedRunSingleMap1SpawnPoint,
            speedRunSingleMap1UnderSandPoint,
            speedRunSingleMap1UpSandPoint,
            freePvpJoinPoint,
            freePvpSpawnPoint;

    private static final Random RANDOM = new Random();

    private static final Map<UUID, Integer> quizAnswers = new ConcurrentHashMap<>();

    private static final Map<UUID, ItemStack[]> menuItems = new HashMap<>();

    private static final int MENU_FIRST_SLOT = 9, MENU_SLOT_COUNT = 3;

    private Config() {
    }

    public static boolean setupWorld() {
        world = Bukkit.getWorld(WORLD_NAME);
        if (world == null) return false;

        lobby = new Location(world, 0.500, 5.500, -0.500, 90, 0);
        lobbyAthleticStart = new Location(world, -28, 4, 6);
        lobbyAthleticFinish = new Location(world, -29, 7, -1);
        speedRunSingleOnholdRoom = new Location(world, -77.500, 4, -0.500, 90, 0);
        speedRunSingleMap1SpawnPoint = new Location(world, -13.500, 4, 107.500, 0, 0);
        speedRunSingleMap1UnderSandPoint = new Location(world, -14, 4, 109);
        speedRunSingleMap1UpSandPoint = new Location(world, -14, 5, 109);
        freePvpJoinPoint = new Location(world, 42, 4, -1);
        freePvpSpawnPoint = new Location(world, 54.500, 14.500, -0.500, -90, 0);
        return true;
    }

    public static boolean isPvpWorld(World target) {
        return world != null && world.equals(target);
    }

    public static String worldUpdateNotice() {
        String notice = "Free PVPスペースが解放されました!!";
        String date = "2025/10/10";
        String description = "ロビーの後ろにあるので、足を運んでみてください!";
        return date + ": {" + notice + "}: " + description;
    }

    public static boolean testPlayerLastLoginTime(Player player) {
        long oneDay = 24L * 60 * 60 * 1000;
        long nowTime = System.currentTimeMillis();
        long lastPlayed = getPlayerLastLogin(player);
        if (lastPlayed == 0) {
            player.sendMessage(textComponent.parse("<aqua>初参加です!"));
            setPlayerLastLogin(player);
            return true;
        }
        long difference = nowTime - lastPlayed;
        return difference >= oneDay;
    }

    public static void playerLastLoginSetup(PvpWorld plugin) {
        playerLastLoginFile = new File(plugin.getDataFolder(), "playerLastLoginTime.yml");
        if (!playerLastLoginFile.exists()) {
            try {
                if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                    plugin.getLogger().warning("データフォルダを作成できませんでした");
                }
                if (!playerLastLoginFile.createNewFile()) {
                    plugin.getLogger().warning("playerLastLoginTime.yml を作成できませんでした");
                }
            } catch (IOException e) {
                plugin.getLogger().warning("playerLastLoginTime.yml の作成に失敗しました: " + e.getMessage());
            }
        }
        playerLastLogin = YamlConfiguration.loadConfiguration(playerLastLoginFile);
    }

    public static long getPlayerLastLogin(Player player) {
        if (playerLastLogin == null) return 0;
        return playerLastLogin.getLong(player.getUniqueId().toString(), 0);
    }

    public static void setPlayerLastLogin(Player player) {
        if (playerLastLogin == null || playerLastLoginFile == null) return;
        playerLastLogin.set(player.getUniqueId().toString(), System.currentTimeMillis());
        try {
            playerLastLogin.save(playerLastLoginFile);
        } catch (IOException e) {
            PvpWorld.getPlugin(PvpWorld.class).getLogger()
                    .warning("playerLastLoginTime.yml の保存に失敗しました: " + e.getMessage());
        }
    }

    public static void sendQuiz(Player player) {
        int first = RANDOM.nextInt(10) + 1;
        int second = RANDOM.nextInt(10) + 1;
        quizAnswers.put(player.getUniqueId(), first + second);
        player.sendMessage(textComponent.parse("<yellow>クイズ!間違えたら脱落、8秒以内に答えられなくても脱落!"));
        player.sendMessage(textComponent.parse("<aqua>" + first + " + " + second + " = "));
    }

    public static void checkQuizAnswer(Player player, String chat) {
        Integer answer = quizAnswers.remove(player.getUniqueId());
        if (answer == null) return;

        if (String.valueOf(answer).equals(chat)) {
            player.sendMessage(textComponent.parse("<green>合格!"));
            NoWalkList.remove(player.getName());
        } else {
            player.playSound(player.getLocation(), Sound.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE, 1, 1);
            player.sendMessage(textComponent.parse("<red>間違えてしまった!"));
            player.sendMessage(Component.text(String.valueOf(answer)));
            Title.Times times = Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(4), Duration.ofSeconds(1));
            player.showTitle(Title.title(textComponent.parse("<red>ペナルティ"), textComponent.parse("鈍足"), times));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 10));
            NoWalkList.remove(player.getName());
        }
    }

    public static boolean hasQuiz(Player player) {
        return quizAnswers.containsKey(player.getUniqueId());
    }

    public static void clearInventory(Player player) {
        player.getInventory().clear();
        ItemStack[] saved = menuItems.get(player.getUniqueId());
        if (saved == null) return;
        for (int i = 0; i < MENU_SLOT_COUNT; i++) {
            player.getInventory().setItem(MENU_FIRST_SLOT + i, saved[i]);
        }
    }

    public static void checkInventoryItem(Player player) {
        ItemStack[] saved = new ItemStack[MENU_SLOT_COUNT];
        for (int i = 0; i < MENU_SLOT_COUNT; i++) {
            ItemStack item = player.getInventory().getItem(MENU_FIRST_SLOT + i);
            saved[i] = item == null ? null : item.clone();
        }
        menuItems.put(player.getUniqueId(), saved);
    }

    public static void forgetPlayer(Player player) {
        quizAnswers.remove(player.getUniqueId());
        menuItems.remove(player.getUniqueId());
    }

    public static boolean overLappingTrigger(Player player) {
        String playerName = player.getName();
        return AthleticTimer.isRunning(player)
                || SpleefActivities.spleefQueueingList.contains(playerName)
                || SpleefActivities.spleefPlayingList.contains(playerName)
                || SumoActivities.sumoQueueingList.contains(playerName)
                || TopfightActivities.topfightQueueingList.contains(playerName)
                || SpeedRunSingleOnHoldList.contains(playerName)
                || SpeedRunSingleList.contains(playerName)
                || FreePvpPlayerList.contains(playerName)
                || SpeedRunActionMulti.multiPlayingList.contains(playerName);
    }

    public static void addIfAbsent(List<String> list, String playerName) {
        if (!list.contains(playerName)) list.add(playerName);
    }

    public static void overLappingMessage(Player player) {
        player.sendMessage(Component.text("あなたは既に他のゲームに参加しています!"));
        player.sendMessage(Component.text("退出してからゲームに参加してください"));
        if (lobby != null) player.teleport(lobby);
    }

    public static void handlePlayerLeave(Player player) {
        PvpWorld plugin = PvpWorld.getPlugin(PvpWorld.class);
        String playerName = player.getName();

        if (SpeedRunActionMulti.multiPlayingList.contains(playerName)) {
            SpeedRunActionMulti.playerLeaveAction(player);
        }

        SpeedRunTimer.stopTimer(player);
        SpeedRunScheduledTimer.stopTimer(player);
        StartTimerUtils.stopTimer(player);
        TimeUpTimer.stopTimer(player);
        org.tofu.pvpWorld.utils.ffaGames.StartTimerUtils.stopTimer(player);
        org.tofu.pvpWorld.utils.ffaGames.TimeUpTimer.stopTimer(player);
        AthleticTimer.stopTimer(player);

        boolean wasSpleefPlaying = SpleefActivities.spleefPlayingList.remove(playerName);

        SpeedRunSingleOnHoldList.remove(playerName);
        SpeedRunSingleList.remove(playerName);
        NoWalkList.remove(playerName);
        FreePvpPlayerList.remove(playerName);
        TeleportToLobbyList.remove(playerName);
        DoNotReceiveDamageList.remove(playerName);
        WorldAllPlayerList.remove(playerName);

        boolean wasQueueing = SumoActivities.sumoQueueingList.remove(playerName)
                | TopfightActivities.topfightQueueingList.remove(playerName);
        boolean wasSpleefQueueing = SpleefActivities.spleefQueueingList.remove(playerName);

        forgetPlayer(player);

        if (wasQueueing) TextDisplayUtils.renameOneVersusOneSize(org.tofu.pvpWorld.utils.oneVersusOne.OneVersusOneGames.OneVersusOneAllPlayer());
        if (wasSpleefQueueing || wasSpleefPlaying) {
            TextDisplayUtils.renameFfaGamesSize(org.tofu.pvpWorld.utils.ffaGames.FfaGames.allFfaGamesPlayer());
        }
        if (wasSpleefPlaying) SpleefActivities.winnerChecker(plugin);
        TextDisplayUtils.renameSpeedRun(SpeedRunSingleList.size() + SpeedRunSingleOnHoldList.size());
    }

    public static void beforeGame(Player player) {
        String playerName = player.getName();
        if (SpeedRunSingleOnHoldList.contains(playerName) || SpeedRunSingleList.contains(playerName)) {
            SpeedRunSingleOnHoldList.remove(playerName);
            SpeedRunSingleList.remove(playerName);
            SpeedRunTimer.stopTimer(player);
            SpeedRunScheduledTimer.stopTimer(player);
            player.sendMessage(Component.text("SpeedRunをキャンセルしました"));
            TextDisplayUtils.renameSpeedRun(SpeedRunSingleOnHoldList.size() + SpeedRunSingleList.size());
        }
        StartTimerUtils.stopTimer(player);
        TimeUpTimer.stopTimer(player);
        clearInventory(player);
        player.getInventory().setItem(0, itemStackMaker.createItem(textComponent.parse("<white>ロビーに戻る"), Material.RED_MUSHROOM, 1));
        player.setLevel(0);
        AthleticTimer.stopTimer(player);
        if (FreePvpPlayerList.remove(playerName)) {
            if (!DoNotReceiveDamageList.contains(playerName)) DoNotReceiveDamageList.add(playerName);
            player.sendMessage(Component.text("Free PVPを退出しました"));
        }
        if (SpeedRunActionMulti.multiPlayingList.contains(playerName)) {
            SpeedRunActionMulti.playerLeaveAction(player);
        }
    }
}
