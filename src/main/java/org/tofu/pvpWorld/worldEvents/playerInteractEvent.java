package org.tofu.pvpWorld.worldEvents;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;
import org.tofu.pvpWorld.utils.ffaGames.FfaGames;
import org.tofu.pvpWorld.utils.ffaGames.InventoryUtils;
import org.tofu.pvpWorld.utils.freePvp.FreePvpUtils;
import org.tofu.pvpWorld.utils.lobbyAthletic.AthleticUtils;
import org.tofu.pvpWorld.utils.oneVersusOne.*;
import org.tofu.pvpWorld.utils.speedRun.SpeedRunAction;
import org.tofu.pvpWorld.utils.speedRun.SpeedRunActionMulti;
import org.tofu.pvpWorld.utils.textComponent;
import org.tofu.pvpWorld.utils.wellUtils.WellUtilities;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

import static org.tofu.pvpWorld.utils.oneVersusOne.InventoryUtils.openGameListInventory;

public final class playerInteractEvent implements Listener {
    private final PvpWorld plugin;

    public playerInteractEvent(PvpWorld plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (!Config.isPvpWorld(player.getWorld())) return;
        if (Config.AdminBuildModeList.contains(player.getName())) return;

        if (e.getAction() == Action.PHYSICAL) {
            handlePressurePlate(e, player);
        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handleRightClickBlock(e, player);
        } else if (e.getAction() == Action.RIGHT_CLICK_AIR) {
            handleRightClickAir(player);
        }
    }

    private void handlePressurePlate(PlayerInteractEvent e, Player player) {
        Block block = e.getClickedBlock();
        if (block == null || block.getType() != Material.STONE_PRESSURE_PLATE) return;

        if (isSameBlock(block, Config.lobbyAthleticFinish)) {
            if (player.getLevel() == 0) {
                player.sendMessage(textComponent.parse("<aqua>あなたのタイムは現在0です。"));
                player.sendMessage(textComponent.parse("<aqua>もう一度アスレチックに挑戦しましょう!"));
                return;
            }
            AthleticUtils.stopAthleticAction(player);
        } else if (isSameBlock(block, Config.lobbyAthleticStart)) {
            AthleticUtils.startAthleticAction(player, plugin);
        }
    }

    private boolean isSameBlock(Block block, Location location) {
        if (location == null || location.getWorld() == null) return false;
        if (!location.getWorld().equals(block.getWorld())) return false;
        return block.getX() == location.getBlockX()
                && block.getY() == location.getBlockY()
                && block.getZ() == location.getBlockZ();
    }

    private void handleRightClickBlock(PlayerInteractEvent e, Player player) {
        Block block = e.getClickedBlock();
        if (block == null) return;

        if (block.getType() == Material.OAK_SIGN || block.getType() == Material.OAK_WALL_SIGN) {
            if (!(block.getState() instanceof Sign sign)) return;

            ItemStack item = e.getItem();
            if (item != null) {
                Material material = item.getType();
                boolean isDye = material.name().endsWith("_DYE");
                boolean isInk = (material == Material.INK_SAC || material == Material.GLOW_INK_SAC);
                if (isDye || isInk) {
                    e.setCancelled(true);
                    player.sendMessage(textComponent.parse("染料以外のアイテムか素手でクリックしてください!"));
                    return;
                }
            }

            String[] lines = new String[4];
            for (int i = 0; i < 4; i++) {
                lines[i] = PlainTextComponentSerializer.plainText().serialize(sign.getSide(Side.FRONT).line(i));
            }

            if (block.getType() == Material.OAK_SIGN) {
                handleRuleSign(player, lines);
            } else {
                handleWallSign(player, lines);
            }
        } else if (block.getType() == Material.END_PORTAL_FRAME) {
            WellUtilities.openInventory(player);
        } else if (block.getType() == Material.OAK_BUTTON) {
            SpeedRunActionMulti.checkButton(block, player, plugin);
        }
    }

    private void handleRuleSign(Player player, String[] lines) {
        if (!Objects.equals(lines[0], "ルール説明")) return;

        if (Objects.equals(lines[1], "SpeedRun")) {
            if (Objects.equals(lines[2], "シングルプレイ")) SpeedRunAction.ruleDescription(player);
        } else if (Objects.equals(lines[1], "FreePVP")) {
            FreePvpUtils.ruleExplain(player);
        }
    }

    private void handleWallSign(Player player, String[] lines) {
        if (Objects.equals(lines[0], "SpeedRunTest")) {
            if (blockedByOtherGame(player)) return;
            SpeedRunAction.openGameListInventory(player);
        } else if (Objects.equals(lines[0], "1v1test")) {
            if (blockedByOtherGame(player)) return;
            openGameListInventory(player);
        } else if (Objects.equals(lines[0], "FFA Games test")) {
            if (blockedByOtherGame(player)) return;
            InventoryUtils.openGameListInventory(player);
        } else if (Objects.equals(lines[0], "右クリックして")) {
            if (Objects.equals(lines[1], "あなたのスコアを") && Objects.equals(lines[2], "リセットします")) {
                AthleticUtils.sendClearAthleticTimeRequest(player);
            } else if (Objects.equals(lines[1], "弓と矢をゲット")) {
                player.getInventory().addItem(new ItemStack(Material.ARROW, 1));
                player.getInventory().addItem(new ItemStack(Material.BOW, 1));
            }
        }
    }

    private void handleRightClickAir(Player player) {
        Material inHand = player.getInventory().getItemInMainHand().getType();

        if (inHand == Material.RED_MUSHROOM) {
            Config.beforeGame(player);
            if (Config.lobby != null) player.teleport(Config.lobby);
        } else if (inHand == Material.FEATHER) {
            SpeedRunAction.clickedFeather(player);
        } else if (inHand == Material.GOLD_BLOCK) {
            SpeedRunAction.clickedGoldBlock(player);
        } else if (inHand == Material.NETHER_STAR) {
            SpeedRunAction.clickedNetherStar(player);
        } else if (inHand == Material.RED_DYE) {
            OneVersusOneGames.clickedRed_Dye(player);
        } else if (inHand == Material.BLUE_DYE) {
            FfaGames.clickedBlue_Dye(player);
        }
    }

    private boolean blockedByOtherGame(Player player) {
        if (!Config.overLappingTrigger(player)) return false;
        Config.overLappingMessage(player);
        return true;
    }
}
