package org.tofu.pvpWorld;

import org.tofu.pvpWorld.pvpWorldCommand.pvpWorldCommand;
import org.tofu.pvpWorld.utils.athletic.AthleticProperties;
import org.tofu.pvpWorld.utils.ffaGames.SpleefActivities;
import org.tofu.pvpWorld.utils.oneVersusOne.SumoActivities;
import org.tofu.pvpWorld.utils.oneVersusOne.TopfightActivities;
import org.tofu.pvpWorld.utils.speedRun.SpeedRunActionMulti;
import org.tofu.pvpWorld.utils.textDisplay.TextDisplayUtils;
import org.tofu.pvpWorld.utils.yamlProperties.athleticTimeUtils;
import org.tofu.pvpWorld.utils.yamlProperties.coinUtils;
import org.tofu.pvpWorld.utils.yamlProperties.expUtils;
import org.tofu.pvpWorld.utils.yamlProperties.systemConfig;
import org.tofu.pvpWorld.worldEvents.*;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import static org.tofu.pvpWorld.utils.yamlProperties.athleticTimeUtils.lobbyAthleticSetUp;
import static org.tofu.pvpWorld.utils.yamlProperties.coinUtils.playerCoinSetUp;
import static org.tofu.pvpWorld.utils.yamlProperties.expUtils.playerExpSetup;
import static org.tofu.pvpWorld.utils.yamlProperties.playerAdminList.playerAdminListSetup;

public final class PvpWorld extends JavaPlugin {

    @Override
    public void onEnable() {
        playerExpSetup(this);
        Config.playerLastLoginSetup(this);
        lobbyAthleticSetUp(this);
        playerCoinSetUp(this);
        playerAdminListSetup(this);
        systemConfig.systemConfigSetUp(this);
        AthleticProperties.athleticYamlSetup(this);
        expUtils.sortEntries();
        coinUtils.sortEntries();
        athleticTimeUtils.sortEntries();

        registerListeners();

        PluginCommand command = getCommand("pvpworld");
        if (command == null) {
            getLogger().severe("pvpworld コマンドが plugin.yml に定義されていません");
        } else {
            pvpWorldCommand executor = new pvpWorldCommand();
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        if (!Config.setupWorld()) {
            getLogger().warning("ワールド '" + Config.WORLD_NAME + "' が見つからないため、ワールド内の機能は無効です");
            return;
        }

        SpleefActivities.setupLocations(Config.world);
        SumoActivities.setupLocations(Config.world);
        TopfightActivities.setupLocations(Config.world);
        SpeedRunActionMulti.setupLocations(Config.world);
        TextDisplayUtils.locationSetUp();
        AthleticProperties.setup();

        getLogger().info("pvpWorld enabled!");
    }

    private void registerListeners() {
        new playerChangeWorldEvent(this);
        new playerDeathEvent(this);
        new playerRespawnEvent(this);
        new entityDamageEvent(this);
        new blockBreakEvent(this);
        new playerInteractEvent(this);
        new foodLevelChangeEvent(this);
        new onPlayerQuitEvent(this);
        new inventoryClickEvent(this);
        new blockPlaceEvent(this);
        new playerMoveEvent(this);
        new asyncChatEvent(this);
        new playerInteractAtEntityEvent(this);
        new playerOpenSignEvent(this);
        new projectileHitEvent(this);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }
}
