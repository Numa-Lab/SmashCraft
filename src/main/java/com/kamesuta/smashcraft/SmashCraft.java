package com.kamesuta.smashcraft;

import com.kamesuta.smashcraft.command.MainCommand;
import com.kamesuta.smashcraft.listener.PlayerDamageListener;
import com.kamesuta.smashcraft.listener.PlayerShieldListener;
import dev.kotx.flylib.FlyLib;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public final class SmashCraft extends JavaPlugin {
    public static SmashCraft instance;

    public Objective objective;
    public Objective objectivePercent;

    @Override
    public void onEnable() {
        instance = this;

        registerScoreboard();

        FlyLib.create(this, builder -> {
            builder.command(new MainCommand("smashcraft"));

            builder.listen(EntityDamageEvent.class, new PlayerDamageListener());
            builder.listen(PlayerInteractEvent.class, new PlayerShieldListener.PlayerShieldInteractListener());
            builder.listen(PlayerItemDamageEvent.class, new PlayerShieldListener.PlayerShieldDamageListener());
            builder.listen(PlayerItemBreakEvent.class, new PlayerShieldListener.PlayerShieldBreakListener());
            //builder.listen(PlayerItemConsumeEvent.class, new EatListener());
        });
    }

    public void registerScoreboard() {
        // スコアボードを取得
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();

        // 吹っ飛び率計算用
        {
            DisplaySlot slot = null;
            objective = sb.getObjective("futtobi");
            if (objective != null) {
                slot = objective.getDisplaySlot();
                objective.unregister();
            }
            objective = sb.registerNewObjective("futtobi", "dummy", Component.text("吹っ飛び率"));
            objective.setDisplaySlot(slot);
        }

        // 吹っ飛び率表示用
        {
            DisplaySlot slot = null;
            objectivePercent = sb.getObjective("futtobi_per");
            if (objectivePercent != null) {
                slot = objectivePercent.getDisplaySlot();
                objectivePercent.unregister();
            }
            objectivePercent = sb.registerNewObjective("futtobi_per", "dummy", Component.text("%"));
            objectivePercent.setDisplaySlot(slot);
        }
    }

    @Override
    public void onDisable() {
    }
}
