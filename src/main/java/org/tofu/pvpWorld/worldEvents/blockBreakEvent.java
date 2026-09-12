package org.tofu.pvpWorld.worldEvents;

import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.tofu.pvpWorld.utils.ffaGames.SpleefActivities;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.tofu.pvpWorld.utils.textComponent;

public final class blockBreakEvent implements Listener {
    public blockBreakEvent(PvpWorld plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e) {
        Player player = e.getPlayer();
        if (!Config.isPvpWorld(player.getWorld())) return;

        String playerName = player.getName();
        if (Config.AdminBuildModeList.contains(playerName)) return;

        if (!SpleefActivities.spleefPlayingList.contains(playerName)) {
            e.setCancelled(true);
            player.sendMessage(textComponent.parse("地形は破壊できません!"));
            return;
        }

        if (player.getInventory().getItemInMainHand().getType() != Material.DIAMOND_SHOVEL
                || e.getBlock().getType() != Material.SNOW_BLOCK) {
            e.setCancelled(true);
            return;
        }

        e.setDropItems(false);
        SpleefActivities.locationList.add(e.getBlock().getLocation());
        SpleefActivities.snowBallAction(player);
    }
}
