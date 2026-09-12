package org.tofu.pvpWorld.utils.ffaGames;

import net.kyori.adventure.text.Component;
import org.tofu.pvpWorld.Config;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.tofu.pvpWorld.utils.textComponent;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtils {
    private static final Component TITLE = textComponent.parse("<b><green>FFAゲームス");

    public static void openGameListInventory(Player player) {
        Inventory gameList = Bukkit.createInventory(null, 54, TITLE);
        gameList.setItem(10, spleefSetProperties());
        player.openInventory(gameList);
    }

    public static ItemStack spleefSetProperties() {
        ItemStack item = new ItemStack(Material.DIAMOND_SHOVEL, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        List<Component> loreList = new ArrayList<>();
        loreList.add(textComponent.parse("<green>相手を下に落とします!"));
        loreList.add(textComponent.parse("<white>ルール:"));
        loreList.add(textComponent.parse("<white>雪は掘れる!"));
        loreList.add(textComponent.parse("<white>掘ると雪玉が手に入る!"));
        loreList.add(textComponent.parse("<white>落ちたら負け!"));
        loreList.add(textComponent.parse("<white>待機中: <gold>" + SpleefActivities.spleefQueueingList.size()));
        meta.lore(loreList);
        meta.displayName(textComponent.parse("<green>spleef"));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public static void replaceInventoryCheck(Player player) {
        for (String playerName : List.copyOf(Config.WorldAllPlayerList)) {
            Player viewer = Bukkit.getPlayerExact(playerName);
            if (viewer == null || viewer.equals(player)) continue;
            InventoryView inventoryView = viewer.getOpenInventory();
            if (!TITLE.equals(inventoryView.title())) continue;
            inventoryView.getTopInventory().setItem(10, spleefSetProperties());
        }
    }
}
