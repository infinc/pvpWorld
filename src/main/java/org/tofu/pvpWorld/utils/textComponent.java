package org.tofu.pvpWorld.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class textComponent {
    private static final MiniMessage mm = MiniMessage.miniMessage();

    public static Component parse(String message) {
        return (message == null) ? Component.empty() : mm.deserialize(message);
    }
}
