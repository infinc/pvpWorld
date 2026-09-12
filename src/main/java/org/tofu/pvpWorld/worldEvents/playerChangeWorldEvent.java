package org.tofu.pvpWorld.worldEvents;

import net.kyori.adventure.title.Title;
import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.tofu.pvpWorld.utils.itemStackMaker;
import org.tofu.pvpWorld.utils.scoreBoard.ScoreBoardUtils;
import org.tofu.pvpWorld.utils.textComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.time.Duration;

import static org.tofu.pvpWorld.utils.yamlProperties.coinUtils.getPlayerCoin;
import static org.tofu.pvpWorld.utils.yamlProperties.coinUtils.playerSetCoin;
import static org.tofu.pvpWorld.utils.yamlProperties.expUtils.getPlayerExp;
import static org.tofu.pvpWorld.utils.yamlProperties.expUtils.playerSetExp;

public final class playerChangeWorldEvent implements Listener {
    private static final int LOGIN_REWARD_EXP = 10, LOGIN_REWARD_COIN = 7;

    private final PvpWorld plugin;

    public playerChangeWorldEvent(PvpWorld plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerChangeWorldEvent(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();

        if (!Config.isPvpWorld(player.getWorld())) {
            if (!Config.isPvpWorld(e.getFrom())) return;
            Config.clearInventory(player);
            ScoreBoardUtils.removeScoreBoard(player);
            Config.handlePlayerLeave(player);
            return;
        }

        String playerName = player.getName();
        Config.addIfAbsent(Config.WorldAllPlayerList, playerName);
        Config.addIfAbsent(Config.DoNotReceiveDamageList, playerName);

        if (Config.lobby != null) player.teleport(Config.lobby);
        player.setGameMode(GameMode.SURVIVAL);
        player.setFoodLevel(20);
        player.setHealth(maxHealth(player));
        Config.checkInventoryItem(player);
        player.setLevel(0);

        player.sendMessage(textComponent.parse("<gold>" + Config.worldUpdateNotice()));

        Title.Times times = Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(1));
        Title title = Title.title(textComponent.parse(playerName + "<aqua>さん"), textComponent.parse("<aqua>こんにちは！"), times);
        player.showTitle(title);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            player.getInventory().setItem(0, itemStackMaker.createItem(textComponent.parse("<white>ロビーに戻る"), Material.RED_MUSHROOM, 1));
            ScoreBoardUtils.updateScoreBoard(player);
        }, 10L);

        if (Config.testPlayerLastLoginTime(player)) {
            playerSetExp(player, LOGIN_REWARD_EXP);
            playerSetCoin(player, LOGIN_REWARD_COIN);
            player.sendMessage(textComponent.parse("<aqua>最後にログイン時のexpを受け取ってから1日以上経過したので、"
                    + LOGIN_REWARD_EXP + "expと" + LOGIN_REWARD_COIN + "coinを獲得しました!"));
            player.sendMessage(textComponent.parse("<aqua>現在のあなたのexp: " + getPlayerExp(player) + "exp"));
            player.sendMessage(textComponent.parse("<aqua>現在のあなたのcoin: " + getPlayerCoin(player) + "coin"));
            Config.setPlayerLastLogin(player);
        } else {
            player.sendMessage(textComponent.parse("最後にログイン時のexpとcoinを受け取ってから1日以上経っていないので、ログイン時のexpとcoinは獲得できません!"));
        }
    }

    private double maxHealth(Player player) {
        var attribute = player.getAttribute(Attribute.MAX_HEALTH);
        return attribute == null ? 20 : attribute.getValue();
    }
}
