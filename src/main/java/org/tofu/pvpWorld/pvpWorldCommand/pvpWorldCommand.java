package org.tofu.pvpWorld.pvpWorldCommand;

import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.utils.lobbyAthletic.AthleticUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tofu.pvpWorld.utils.textComponent;
import org.tofu.pvpWorld.utils.yamlProperties.coinUtils;
import org.tofu.pvpWorld.utils.yamlProperties.expUtils;
import org.tofu.pvpWorld.utils.yamlProperties.playerAdminList;

import java.util.ArrayList;
import java.util.List;

public class pvpWorldCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ使用できます");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(textComponent.parse("PVP WORLDへようこそ"));
            player.sendMessage(textComponent.parse("<red>></red><dark_purple>></dark_purple><dark_red>></dark_red>コマンド一覧<dark_red><</dark_red><dark_purple><</dark_purple><red><</red>"));
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "op" -> handleOp(player, args);
            case "notice" -> {
                player.sendMessage(textComponent.parse(Config.worldUpdateNotice()));
                yield true;
            }
            case "actions" -> handleActions(player, args);
            case "help" -> {
                player.sendMessage(textComponent.parse("<click:run_command:'/pvpworld command'><aqua>・コマンド一覧を表示する</click>"));
                player.sendMessage(textComponent.parse("<click:open_url:'https://google.com'><aqua>・ホームページにアクセスする(現在アクセスできません)</click>"));
                player.sendMessage(textComponent.parse("ヘルプが必要ですか？"));
                player.sendMessage(textComponent.parse("押すと、それに応じたヘルプが表示されます"));
                yield true;
            }
            case "command" -> {
                player.sendMessage(textComponent.parse("<b><yellow>-----PVP WORLD-----</yellow></b>"));
                player.sendMessage(textComponent.parse("<click:run_command:'/pvpworld'>/pvpworld -このワールドについてを表示します</click>"));
                player.sendMessage(textComponent.parse("<click:run_command:'/pvpworld help'>/pvpworld help -ヘルプを表示します</click>"));
                player.sendMessage(textComponent.parse("<click:run_command:'/pvpworld command'>/pvpworld command -コマンドのリストを表示します</click>"));
                player.sendMessage(textComponent.parse("<click:run_command:'/pvpworld notice'>/pvpworld notice -お知らせを表示します</click>"));
                yield true;
            }
            default -> {
                player.sendMessage(textComponent.parse("<red>不明なコマンドです。/pvpworld command で一覧を表示できます"));
                yield true;
            }
        };
    }

    private boolean handleOp(Player player, String[] args) {
        if (!playerAdminList.playerHasAdmin(player)) {
            player.sendMessage(textComponent.parse("<aqua>ADMINユーザーのみ使用できます!</aqua>"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(textComponent.parse("使い方: /pvpworld op <bm|gm|info|getexp|getcoin|fill|tp> ..."));
            return true;
        }

        String playerName = player.getName();
        switch (args[1].toLowerCase()) {
            case "bm" -> {
                if (args.length < 3) {
                    player.sendMessage(textComponent.parse("使い方: /pvpworld op bm <true|false>"));
                    return true;
                }
                if (args[2].equals("true")) {
                    if (Config.AdminBuildModeList.contains(playerName)) {
                        player.sendMessage(textComponent.parse("既にビルドモードです"));
                    } else {
                        Config.AdminBuildModeList.add(playerName);
                        player.sendMessage(textComponent.parse("ビルドモードに切り替えました"));
                    }
                } else if (args[2].equals("false")) {
                    Config.AdminBuildModeList.remove(playerName);
                    player.sendMessage(textComponent.parse("ノーマルモードに切り替えました"));
                } else {
                    player.sendMessage(textComponent.parse("使い方: /pvpworld op bm <true|false>"));
                }
            }
            case "gm" -> {
                if (args.length < 3) {
                    player.sendMessage(textComponent.parse("使い方: /pvpworld op gm <c|s>"));
                    return true;
                }
                if (args[2].equals("c")) {
                    player.setGameMode(GameMode.CREATIVE);
                    player.sendMessage(textComponent.parse("クリエイティブモードに切り替えました"));
                } else if (args[2].equals("s")) {
                    player.setGameMode(GameMode.SURVIVAL);
                    player.sendMessage(textComponent.parse("サバイバルモードに切り替えました"));
                } else {
                    player.sendMessage(textComponent.parse("使い方: /pvpworld op gm <c|s>"));
                }
            }
            case "info" -> {
                player.sendMessage(textComponent.parse("WorldAllPlayerList"));
                player.sendMessage(textComponent.parse(String.valueOf(Config.WorldAllPlayerList)));
            }
            case "getexp" -> expUtils.playerSetExp(player, args.length >= 3 ? parseInt(player, args[2], 5) : 5);
            case "getcoin" -> {
                if (args.length < 3) {
                    player.sendMessage(textComponent.parse("使い方: /pvpworld op getcoin <数値>"));
                    return true;
                }
                coinUtils.playerSetCoin(player, parseInt(player, args[2], 0));
            }
            case "fill" -> {
                if (args.length < 5) {
                    player.sendMessage(textComponent.parse("使い方: /pvpworld op fill <x> <y> <z>"));
                    return true;
                }
                World world = player.getWorld();
                Location location = new Location(world,
                        parseInt(player, args[2], 0),
                        parseInt(player, args[3], 0),
                        parseInt(player, args[4], 0));
                location.getBlock().setType(Material.AIR);
                player.sendMessage(textComponent.parse("x: " + args[2] + " y: " + args[3] + " z: " + args[4] + " を正常にクリアしました"));
            }
            case "tp" -> {
                if (args.length < 3) {
                    player.sendMessage(textComponent.parse("使い方: /pvpworld op tp <プレイヤー名>"));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[2]);
                if (target == null) {
                    player.sendMessage(textComponent.parse("<red>" + args[2] + " は見つかりませんでした"));
                    return true;
                }
                target.teleport(player.getLocation());
                player.sendMessage(textComponent.parse(target.getName() + " を呼び寄せました"));
            }
            default -> player.sendMessage(textComponent.parse("使い方: /pvpworld op <bm|gm|info|getexp|getcoin|fill|tp> ..."));
        }
        return true;
    }

    private boolean handleActions(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(textComponent.parse("使い方: /pvpworld actions lobbyAthletic clear"));
            return true;
        }
        if (args[1].equals("lobbyAthletic") && args[2].equals("clear")) {
            AthleticUtils.clearAthleticTimes(player);
        } else {
            player.sendMessage(textComponent.parse("使い方: /pvpworld actions lobbyAthletic clear"));
        }
        return true;
    }

    private int parseInt(Player player, String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            player.sendMessage(textComponent.parse("<red>" + value + " は数値ではありません"));
            return fallback;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("help");
            completions.add("command");
            completions.add("notice");
            if (sender instanceof Player player && playerAdminList.playerHasAdmin(player)) {
                completions.add("op");
                completions.add("actions");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("op")) {
            completions.add("bm");
            completions.add("gm");
            completions.add("info");
            completions.add("getexp");
            completions.add("getcoin");
            completions.add("fill");
            completions.add("tp");
        }
        completions.removeIf(option -> !option.startsWith(args[args.length - 1].toLowerCase()));
        return completions;
    }
}
