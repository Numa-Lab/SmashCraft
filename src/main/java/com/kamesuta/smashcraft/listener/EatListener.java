package com.kamesuta.smashcraft.listener;

import com.kamesuta.smashcraft.SmashCraft;
import dev.kotx.flylib.ListenerAction;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.scoreboard.Score;

public class EatListener implements ListenerAction<PlayerItemConsumeEvent> {
    @Override
    public void execute(PlayerItemConsumeEvent e) {
//        // 飯かどうかを判定
//        if (e.getItem() instanceof Food)

        // 吹っ飛び率
        Score score = SmashCraft.instance.objective.getScore(e.getPlayer().getName());
        Score scorePercent = SmashCraft.instance.objectivePercent.getScore(e.getPlayer().getName());
        // ダメージ量を吹っ飛び率に加算
        score.setScore(Math.max(0, score.getScore() - 10));
        scorePercent.setScore(score.getScore());

        e.getPlayer().setFoodLevel(12);
    }
}
