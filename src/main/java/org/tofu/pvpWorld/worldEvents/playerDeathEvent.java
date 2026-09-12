package org.tofu.pvpWorld.worldEvents;

import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.tofu.pvpWorld.utils.itemStackMaker;
import org.tofu.pvpWorld.utils.oneVersusOne.TopfightActivities;
import org.tofu.pvpWorld.utils.textComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;

import static org.tofu.pvpWorld.utils.yamlProperties.coinUtils.playerSetCoin;
import static org.tofu.pvpWorld.utils.yamlProperties.expUtils.playerSetExp;

public final class playerDeathEvent implements Listener {
    private final PvpWorld plugin;

    public playerDeathEvent(PvpWorld plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (!Config.isPvpWorld(player.getWorld())) return;

        String playerName = player.getName();
        e.getDrops().clear();
        if (Config.lobby != null) player.teleport(Config.lobby);

        if (Config.DoNotReceiveDamageList.contains(playerName)) return;

        if (Config.FreePvpPlayerList.contains(playerName)) {
            Player killer = player.getKiller();
            if (killer == null) {
                player.sendMessage(textComponent.parse("死んでしまった!!"));
                Config.clearInventory(player);
                return;
            }

            for (String freePvpName : List.copyOf(Config.FreePvpPlayerList)) {
                Player member = Bukkit.getPlayerExact(freePvpName);
                if (member == null) continue;
                member.sendMessage(textComponent.parse("<gold>" + playerName + "<white>は" + killer.getName() + "に殺されてしまった!!"));
            }
            playerSetExp(player, 1);
            playerSetExp(killer, 3);
            playerSetCoin(killer, 7);
            killer.getInventory().addItem(itemStackMaker.createItem(textComponent.parse("<white>金リンゴ"), Material.GOLDEN_APPLE, 1));
            killer.sendMessage(textComponent.parse("金リンゴを入手しました"));
        } else if (TopfightActivities.topfightQueueingList.contains(playerName)) {
            TopfightActivities.topfightCloseAction(player, plugin);
        }
    }
}
