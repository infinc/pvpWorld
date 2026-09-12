package org.tofu.pvpWorld.utils.speedRun;

import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.tofu.pvpWorld.utils.itemStackMaker;
import org.tofu.pvpWorld.utils.lobbyAthletic.AthleticTimer;
import org.tofu.pvpWorld.utils.textComponent;
import org.tofu.pvpWorld.utils.textDisplay.TextDisplayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.tofu.pvpWorld.utils.titleMaker;

import java.util.Random;

public class SpeedRunAction {
    private static final Random RANDOM = new Random();

    public static void openGameListInventory(Player player) {
        Inventory gameList = Bukkit.createInventory(null, 9, Component.text("SpeedRun: モード選択"));
        gameList.setItem(0, itemStackMaker.createItem(textComponent.parse("SpeedRunシングルプレイ"), Material.PAPER, 1));
        gameList.setItem(1, itemStackMaker.createItem(textComponent.parse("SpeedRunマルチプレイ"), Material.PAPER, 1));
        player.openInventory(gameList);
    }

    public static void singleOnHoldAction(Player player, PvpWorld plugin) {
        AthleticTimer.stopTimer(player);
        if (!Config.SpeedRunSingleOnHoldList.isEmpty()) {
            player.sendMessage(textComponent.parse("<aqua>既に誰かがプレイしています!</aqua>"));
            player.sendMessage(textComponent.parse("<aqua>少々お待ちください</aqua>"));
            return;
        }
        player.sendMessage(textComponent.parse("<aqua>誰もプレイしていなかったので、開始します</aqua>"));
        if (Config.speedRunSingleOnholdRoom != null) player.teleport(Config.speedRunSingleOnholdRoom);
        player.getInventory().setItem(0, itemStackMaker.createItem(textComponent.parse("ロビーに戻る"), Material.RED_MUSHROOM, 1));
        Config.addIfAbsent(Config.SpeedRunSingleOnHoldList, player.getName());
        SpeedRunTimer.startTimer(player, plugin);
        TextDisplayUtils.renameSpeedRun(Config.SpeedRunSingleList.size() + Config.SpeedRunSingleOnHoldList.size());
    }

    public static void startSingleMode(Player player, PvpWorld plugin) {
        if (Config.speedRunSingleMap1SpawnPoint == null) return;
        Config.speedRunSingleMap1UnderSandPoint.getBlock().setType(Material.SAND);
        Config.speedRunSingleMap1UpSandPoint.getBlock().setType(Material.SAND);
        player.teleport(Config.speedRunSingleMap1SpawnPoint);
        long delay = 20L + RANDOM.nextInt(80);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            SpeedRunScheduledTimer.startTimer(player, plugin, false);
            Config.addIfAbsent(Config.SpeedRunSingleList, player.getName());
            Config.speedRunSingleMap1UnderSandPoint.getBlock().setType(Material.AIR);
            Config.speedRunSingleMap1UpSandPoint.getBlock().setType(Material.AIR);
            player.sendMessage(textComponent.parse("<aqua>スタート!!!</aqua>"));
            TextDisplayUtils.renameSpeedRun(Config.SpeedRunSingleList.size() + Config.SpeedRunSingleOnHoldList.size());
        }, delay);
    }

    public static void randomEvent(Player player, PvpWorld plugin) {
        int ran = 1 + RANDOM.nextInt(11);
        if (ran == 1) {
            Location playerLocation = player.getLocation();
            Block originalBlock = playerLocation.getBlock();
            Material material = originalBlock.getType();
            playerLocation.getBlock().setType(Material.COBWEB);
            player.playSound(playerLocation, Sound.BLOCK_COBWEB_PLACE, 1, 1);
            player.sendMessage(textComponent.parse("<red>神からの天罰</red>"));
            player.sendMessage(textComponent.parse("<red>蜘蛛の巣に引っかかってしまった!</red>"));
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    playerLocation.getBlock().setType(material);
                }
            }, 60L);
        } else if (ran == 2) {
            PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 60, 2);
            player.addPotionEffect(speed);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            player.sendMessage(textComponent.parse("<green>天使からのささやかな贈り物</green>"));
            player.sendMessage(textComponent.parse("<green>3秒間歩くスピードが速くなった!</green>"));
            Bukkit.getScheduler().runTaskLater(plugin, () -> player.removePotionEffect(PotionEffectType.SPEED), 60L);
        } else if (ran == 3) {
            PotionEffect jump = new PotionEffect(PotionEffectType.JUMP_BOOST, 60, 2);
            player.addPotionEffect(jump);
            player.playSound(player.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1, 1);
            player.sendMessage(textComponent.parse("<green>天使からのささやかな贈り物</green>"));
            player.sendMessage(textComponent.parse("<green>3秒間ジャンプしたときの高さが高くなった!</green>"));
        } else if (ran == 4) {
            Config.NoWalkList.add(player.getName());
            player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT,1 ,1);
            player.sendMessage(textComponent.parse("<red>宇宙人の攻撃</red>"));
            player.sendMessage(textComponent.parse("<red>3秒間動けなくなってしまった!</red>"));
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    Config.NoWalkList.remove(player.getName());
                }
            }, 60L);
        } else if (ran == 5) {
            player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1, 1.6F);
            player.getInventory().addItem(itemStackMaker.createItem(textComponent.parse("浮遊する"), Material.FEATHER, 1));
            player.sendMessage(textComponent.parse("<green>空からの贈り物</green>"));
            player.sendMessage(textComponent.parse("<green>5秒間浮遊できるアイテムをゲットした!</green>"));
        } else if (ran == 6) {
            PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 100, 1);
            player.addPotionEffect(blindness);
            player.playSound(player.getLocation(), Sound.ENTITY_MULE_DEATH, 1, 1);
            player.sendMessage(textComponent.parse("<red>地球の怒り</red>"));
            player.sendMessage(textComponent.parse("<red>5秒間盲目になってしまった!</red>"));
        } else if (ran == 7) {
            player.playSound(player.getLocation(), Sound.ENTITY_WANDERING_TRADER_AMBIENT, 1, 1);
            player.sendMessage(textComponent.parse("<gold>神からの挑戦状</gold>"));
            Config.NoWalkList.add(player.getName());
            Config.sendQuiz(player);
        } else if (ran == 8) {
            double x = RANDOM.nextDouble() * 2 - 1;
            double y = 0.5 + RANDOM.nextDouble() * 0.5;
            double z = RANDOM.nextDouble() * 2 - 1;
            Vector direction = new Vector(x, y, z);
            direction.normalize().multiply(1);
            player.setVelocity(direction);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1, 1);
            player.sendMessage(textComponent.parse("<yellow>知らない人からのちょっかい</yellow>"));
            player.sendMessage(textComponent.parse("<yellow>ノックバックを受けてしまった!</yellow>"));
        } else if (ran == 9) {
            player.playSound(player.getLocation(), Sound.ENTITY_LLAMA_SWAG,1 ,1);
            player.getInventory().addItem(itemStackMaker.createItem(textComponent.parse("ラッキーブロック"), Material.GOLD_BLOCK, 1));
            player.sendMessage(textComponent.parse("<yellow>運試し</yellow>"));
            player.sendMessage(textComponent.parse("<yellow>1/2ラッキーブロックを入手した!</yellow>"));
            player.sendMessage(textComponent.parse("右クリックすると半分の確率で良いものを得られ、半分の確率で悪い効果を受けます"));
        } else if (ran == 10) {
            player.sendMessage(textComponent.parse("<red>キーボードの逆襲"));
            player.sendMessage(textComponent.parse("<red>7秒間操作が反転してしまっている!"));
            player.setWalkSpeed(-0.2f);
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    player.sendMessage(textComponent.parse("操作が正常になった"));
                    player.setWalkSpeed(0.2f);
                }
            }, 70L);
        } else if (ran == 11) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 50));
            player.sendMessage(textComponent.parse("<yellow>ジャンプ力の王からの贈り物"));
            player.sendMessage(textComponent.parse("<yellow>5秒間ジャンプ力が極端に高くなってしまった!"));

        }
    }

    public static void ruleDescription(Player player) {
        player.sendMessage(textComponent.parse("<aqua>-----SpeedRunシングルプレイ-----"));
        player.sendMessage(Component.text("このゲームは、アスレチックを走り抜けてゴールにあるボタンを押す速さを争うゲームです!"));
        player.sendMessage(Component.text("でも、ただアスレチックをするだけではありません!"));
        player.sendMessage(textComponent.parse("<yellow>10秒に1回ランダムでイベントが発生します!!"));
        player.sendMessage(textComponent.parse("<green>歩く速さが速く<white>なったり、<red>周りが見えなく<white>なったり..."));
        player.sendMessage(Component.text("リーダーボードも作る予定です!"));
        player.sendMessage(textComponent.parse("<aqua>---------------------------"));
    }

    public static void clickedFeather(Player player) {
        if (player.getInventory().getItemInMainHand().getType() != Material.FEATHER) return;
        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 1));
        player.getInventory().setItemInMainHand(null);
        player.sendMessage(Component.text("使用しました!"));
    }

    public static void clickedGoldBlock(Player player) {
        if (player.getInventory().getItemInMainHand().getType() != Material.GOLD_BLOCK) return;
        player.getInventory().setItemInMainHand(null);

        if (RANDOM.nextInt(2) == 0) {
            player.showTitle(titleMaker.title(textComponent.parse("<gold>当たり!</gold>"), Component.empty(), 1000, 2000, 1000));
            player.getInventory().addItem(itemStackMaker.createItem(textComponent.parse("スピード"), Material.NETHER_STAR, 1));
            player.sendMessage(Component.text("右クリックで5秒間のスピードの効果を得られます!"));
        } else {
            player.showTitle(titleMaker.title(textComponent.parse("はずれ"), Component.empty(), 1000, 2000, 1000));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 10));
            player.sendMessage(textComponent.parse("<red>5秒間視界が歪むようになってしまった!</red>"));
        }
    }

    public static void clickedNetherStar(Player player) {
        if (player.getInventory().getItemInMainHand().getType() != Material.NETHER_STAR) return;
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
        player.getInventory().setItemInMainHand(null);
        player.sendMessage(Component.text("使用しました!"));
    }

    public static void mutiMapSelecting(Player player) {
        Inventory gameList = Bukkit.createInventory(null, 9, textComponent.parse("<red>speedRun multi</red>"));
        gameList.setItem(0, itemStackMaker.createItem(textComponent.parse("SpeedRunシングルプレイ"), Material.PAPER, 1));
        gameList.setItem(1, itemStackMaker.createItem(textComponent.parse("SpeedRunマルチプレイ"), Material.PAPER, 1));
        player.openInventory(gameList);
    }
}
