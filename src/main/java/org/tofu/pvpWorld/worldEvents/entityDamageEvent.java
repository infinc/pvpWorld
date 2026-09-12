package org.tofu.pvpWorld.worldEvents;

import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.tofu.pvpWorld.utils.ffaGames.SpleefActivities;
import org.tofu.pvpWorld.utils.oneVersusOne.SumoActivities;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public final class entityDamageEvent implements Listener {
    public entityDamageEvent(PvpWorld plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        if (!Config.isPvpWorld(player.getWorld())) return;

        String playerName = player.getName();
        if (Config.DoNotReceiveDamageList.contains(playerName)) {
            e.setCancelled(true);
        } else if (SumoActivities.sumoQueueingList.contains(playerName)
                || SpleefActivities.spleefQueueingList.contains(playerName)) {
            e.setDamage(0);
        } else if (SpleefActivities.spleefPlayingList.contains(playerName)) {
            e.setDamage(0.1);
        }
    }
}
