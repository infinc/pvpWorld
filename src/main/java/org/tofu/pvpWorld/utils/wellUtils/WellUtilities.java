package org.tofu.pvpWorld.utils.wellUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.tofu.pvpWorld.utils.textComponent;
import org.tofu.pvpWorld.utils.yamlProperties.coinUtils;
import org.tofu.pvpWorld.utils.yamlProperties.expUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WellUtilities {
    private static final int SMALL_COST = 10, BIG_COST = 100;

    private static final Random RANDOM = new Random();

    public static void openInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, textComponent.parse("<bold><dark_purple>井戸"));
        inventory.setItem(29, pickItemProperties());
        inventory.setItem(33, pickItemPropertiesBIG());
        player.openInventory(inventory);
    }

    public static ItemStack pickItemProperties() {
        ItemStack item = new ItemStack(Material.GOLD_INGOT, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        List<Component> loreList = new ArrayList<>();
        loreList.add(textComponent.parse("<white>" + SMALL_COST + "Goldを使用して、レアアイテムや"));
        loreList.add(textComponent.parse("<white>Goldなどを入手できます!!"));
        loreList.add(textComponent.parse("<green>何がでるかはわかりません"));
        loreList.add(Component.empty());
        loreList.add(textComponent.parse("<red>消費: <gold>" + SMALL_COST + "Gold"));
        loreList.add(textComponent.parse("<yellow>>>右クリックして井戸から入手<<"));
        meta.lore(loreList);
        meta.displayName(textComponent.parse("<red>小さな井戸"));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack pickItemPropertiesBIG() {
        ItemStack item = new ItemStack(Material.GOLD_BLOCK, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        List<Component> loreList = new ArrayList<>();
        loreList.add(textComponent.parse("<white>" + BIG_COST + "Goldを使用して、レアアイテムや"));
        loreList.add(textComponent.parse("<white>Goldなどを入手できます!!"));
        loreList.add(textComponent.parse("<green>何がでるかはわかりません"));
        loreList.add(textComponent.parse("<yellow>⚠注意:基本は損します"));
        loreList.add(textComponent.parse("><yellow>一攫千金です"));
        loreList.add(Component.empty());
        loreList.add(textComponent.parse("<red>消費: <gold>" + BIG_COST + "Gold"));
        loreList.add(textComponent.parse("<yellow>>>右クリックして井戸から入手<<"));
        meta.lore(loreList);
        meta.displayName(textComponent.parse("<dark_purple>大きな井戸"));
        item.setItemMeta(meta);
        return item;
    }

    public static void rollItems(Player player, PvpWorld plugin) {
        player.closeInventory();
        if (coinUtils.getPlayerCoin(player) < SMALL_COST) {
            player.sendMessage(textComponent.parse("<red>あなたは" + SMALL_COST + "coinを所持していません!"));
            return;
        }
        coinUtils.playerSetCoin(player, -SMALL_COST);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            int value = RANDOM.nextInt(10000) + 1;
            String rarity;
            int reward;
            boolean announce = false;

            if (value <= 5000) {
                rarity = "<white>コモン";
                reward = 8;
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            } else if (value <= 7000) {
                rarity = "<green>アンコモン";
                reward = 12;
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            } else if (value <= 8000) {
                rarity = "<aqua>レア";
                reward = 20;
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            } else if (value <= 8100) {
                rarity = "<blue>ユニーク";
                reward = 100;
                announce = true;
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1, 1);
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR, 1, 1);
            } else if (value <= 8150) {
                rarity = "<dark_purple>エピック";
                reward = 400;
                announce = true;
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
            } else if (value <= 8155) {
                rarity = "<gold>レジェンダリー";
                reward = 700;
                announce = true;
                player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1F, 1);
            } else if (value <= 8156) {
                rarity = "<black>インセイン";
                reward = 800;
                announce = true;
                player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1F, 0.5F);
            } else {
                rarity = "<gray>はずれ";
                reward = 0;
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            }

            giveReward(player, rarity, reward, announce);
        }, 80L);
    }

    public static void rollItemsBIG(Player player, PvpWorld plugin) {
        player.closeInventory();
        if (coinUtils.getPlayerCoin(player) < BIG_COST) {
            player.sendMessage(textComponent.parse("<red>あなたは" + BIG_COST + "coinを所持していません!"));
            return;
        }
        coinUtils.playerSetCoin(player, -BIG_COST);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            int value = RANDOM.nextInt(10000) + 1;
            String rarity;
            int reward;
            boolean announce = false;

            if (value <= 5000) {
                rarity = "<red>損";
                reward = 50;
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            } else if (value <= 7000) {
                rarity = "<yellow>利益!";
                reward = 100;
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            } else if (value <= 7100) {
                rarity = "<blue>利益!!";
                reward = 200;
                announce = true;
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            } else if (value <= 7105) {
                rarity = "<gold>極上";
                reward = 700;
                announce = true;
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
            } else if (value <= 7106) {
                rarity = "<black>伝説";
                reward = 1000;
                announce = true;
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
            } else {
                rarity = "<gray>はずれ";
                reward = 0;
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            }

            giveReward(player, rarity, reward, announce);
        }, 80L);
    }

    private static void giveReward(Player player, String rarity, int reward, boolean announce) {
        Title.Times titleTimes = Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(1));
        player.showTitle(Title.title(textComponent.parse(rarity), Component.empty(), titleTimes));

        if (reward <= 0) {
            player.sendMessage(textComponent.parse("<gray>今回は何も出ませんでした..."));
        } else {
            player.sendMessage(textComponent.parse("<gold>" + reward + "Gold<white>を入手しました!!"));
            if (announce) sendRareMessage(player, rarity, String.valueOf(reward));
            coinUtils.playerSetCoin(player, reward);
        }
        expUtils.playerSetExp(player, 2);
    }

    public static void sendRareMessage(Player player, String rare, String gold) {
        for (String playerName : List.copyOf(Config.WorldAllPlayerList)) {
            Player other = Bukkit.getPlayerExact(playerName);
            if (other == null) continue;
            other.sendMessage(textComponent.parse("<gold>" + player.getName() + "<white>さんが井戸で" + rare + "<gold>" + gold + "gold<white>を入手しました!!"));
        }
    }
}
