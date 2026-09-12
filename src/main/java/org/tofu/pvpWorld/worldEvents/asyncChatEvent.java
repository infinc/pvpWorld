package org.tofu.pvpWorld.worldEvents;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.tofu.pvpWorld.Config;
import org.tofu.pvpWorld.PvpWorld;

public final class asyncChatEvent implements Listener {
    private final PvpWorld plugin;

    public asyncChatEvent(PvpWorld plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onAsyncChatEvent(AsyncChatEvent e) {
        Player player = e.getPlayer();
        if (!Config.isPvpWorld(player.getWorld())) return;
        if (!Config.hasQuiz(player)) return;

        String comment = PlainTextComponentSerializer.plainText().serialize(e.message()).trim();
        e.setCancelled(true);

        Bukkit.getScheduler().runTask(plugin, () -> Config.checkQuizAnswer(player, comment));
    }
}
