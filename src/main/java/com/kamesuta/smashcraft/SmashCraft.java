package com.kamesuta.smashcraft;

import com.kamesuta.smashcraft.command.MainCommand;
import com.kamesuta.smashcraft.listener.PlayerDamageListener;
import dev.kotx.flylib.FlyLib;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class SmashCraft extends JavaPlugin {
    public static SmashCraft instance;

    @Override
    public void onEnable() {
        instance = this;

        FlyLib.create(this, builder -> {
            builder.command(new MainCommand("smashcraft"));

            builder.listen(EntityDamageEvent.class, new PlayerDamageListener());
        });
    }

    @Override
    public void onDisable() {
    }
}
