package org.tofu.pvpWorld.worldEvents;

import net.kyori.adventure.text.Component;
import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.tofu.pvpWorld.utils.ffaGames.FfaGames;
import org.tofu.pvpWorld.utils.ffaGames.SpleefActivities;
import org.tofu.pvpWorld.utils.oneVersusOne.OneVersusOneGames;
import org.tofu.pvpWorld.utils.oneVersusOne.SumoActivities;
import org.tofu.pvpWorld.utils.oneVersusOne.TopfightActivities;
import org.tofu.pvpWorld.utils.speedRun.SpeedRunAction;
import org.tofu.pvpWorld.utils.speedRun.SpeedRunActionMulti;
import org.tofu.pvpWorld.utils.textComponent;
import org.tofu.pvpWorld.utils.wellUtils.WellUtilities;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class inventoryClickEvent implements Listener {
    private static final Component SPEED_RUN_TITLE = Component.text("SpeedRun: モード選択");
    private static final Component ONE_VERSUS_ONE_TITLE = textComponent.parse("<b><yellow>1v1ゲームス");
    private static final Component FFA_TITLE = textComponent.parse("<b><green>FFAゲームス");
    private static final Component WELL_TITLE = textComponent.parse("<bold><dark_purple>井戸");

    private final PvpWorld plugin;

    public inventoryClickEvent(PvpWorld plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!Config.isPvpWorld(player.getWorld())) return;

        Component title = e.getView().title();
        boolean speedRunMenu = SPEED_RUN_TITLE.equals(title);
        boolean oneVersusOneMenu = ONE_VERSUS_ONE_TITLE.equals(title);
        boolean ffaMenu = FFA_TITLE.equals(title);
        boolean wellMenu = WELL_TITLE.equals(title);
        if (!speedRunMenu && !oneVersusOneMenu && !ffaMenu && !wellMenu) return;

        e.setCancelled(true);
        if (e.getClickedInventory() == null || !e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        ItemStack itemStack = e.getCurrentItem();
        if (itemStack == null) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return;
        Component displayName = meta.displayName();
        if (displayName == null) return;

        if (speedRunMenu && itemStack.getType() == Material.PAPER) {
            if (blockedByOtherGame(player)) return;
            player.closeInventory();
            if (displayName.equals(textComponent.parse("SpeedRunシングルプレイ"))) {
                SpeedRunAction.singleOnHoldAction(player, plugin);
            } else if (displayName.equals(textComponent.parse("SpeedRunマルチプレイ"))) {
                SpeedRunActionMulti.multiOnHoldAction(player, plugin);
            }
        } else if (oneVersusOneMenu && itemStack.getType() == Material.LEAD) {
            if (!displayName.equals(textComponent.parse("<yellow>sumo"))) return;
            Config.beforeGame(player);
            OneVersusOneGames.queueingActivities(player, e, plugin, SumoActivities.sumoQueueingList);
        } else if (oneVersusOneMenu && itemStack.getType() == Material.IRON_BLOCK) {
            if (!displayName.equals(textComponent.parse("<red>topfight"))) return;
            Config.beforeGame(player);
            OneVersusOneGames.queueingActivities(player, e, plugin, TopfightActivities.topfightQueueingList);
        } else if (ffaMenu && itemStack.getType() == Material.DIAMOND_SHOVEL) {
            if (!displayName.equals(textComponent.parse("<green>spleef"))) return;
            Config.beforeGame(player);
            FfaGames.ffaQueueingActivities(player, SpleefActivities.spleefQueueingList, plugin, e);
        } else if (wellMenu && itemStack.getType() == Material.GOLD_INGOT) {
            if (!displayName.equals(textComponent.parse("<red>小さな井戸"))) return;
            Config.beforeGame(player);
            WellUtilities.rollItems(player, plugin);
        } else if (wellMenu && itemStack.getType() == Material.GOLD_BLOCK) {
            if (!displayName.equals(textComponent.parse("<dark_purple>大きな井戸"))) return;
            Config.beforeGame(player);
            WellUtilities.rollItemsBIG(player, plugin);
        }
    }

    private boolean blockedByOtherGame(Player player) {
        if (!Config.overLappingTrigger(player)) return false;
        player.closeInventory();
        Config.overLappingMessage(player);
        return true;
    }
}
