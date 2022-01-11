package com.kamesuta.smashcraft.listener;

import com.kamesuta.smashcraft.Config;
import com.kamesuta.smashcraft.SmashCraft;
import dev.kotx.flylib.ListenerAction;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class PlayerDamageListener implements ListenerAction<EntityDamageEvent> {
    @Override
    public void execute(EntityDamageEvent e) {
        // SmashCraftモードがONのときのみ
        if (!Config.isEnabled) {
            return;
        }

        // プレイヤーだけ
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        // 奈落 または /kill 以外ならダメージ0
        if (e.getCause() != EntityDamageEvent.DamageCause.VOID) {
            // ダメージは0
            e.setDamage(0);
        }

        // 人に殴られたときのみ吹っ飛ぶ
        if (e instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) e;

            // 人に殴られたときのみ
            if (!(ev.getDamager() instanceof Player)) {
                return;
            }

            // ノックバックの方向 = 殴った人の顔の向き
            Vector direction = ((Player) ev.getDamager()).getEyeLocation().getDirection();

            // 吹っ飛ぶ
            new SetVelocityTask(((Player) e.getEntity()), direction, 1).runTaskTimerAsynchronously(SmashCraft.instance, 0, 4);
        }
    }
}

class SetVelocityTask extends BukkitRunnable {
    private int count = 0;
    private final int numberOfTimes = ((int) Math.ceil(Config.knockbackCoefficient / 4));
    private final Player target;
    private final Vector vector;
    // 吹っ飛び率
    private final float knockbackCoefficient;

    SetVelocityTask(Player target, Vector direction, float knockbackCoefficient) {
        this.target = target;
        this.knockbackCoefficient = knockbackCoefficient;

        if (knockbackCoefficient > 4) {
            direction.multiply(4);
        } else {
            direction.multiply(knockbackCoefficient);
        }
        this.vector = direction;
    }

    @Override
    public void run() {
        if (target.isDead()) {
            this.cancel();
            return;
        }

        target.setVelocity(vector);

        count++;
        if (count >= numberOfTimes) {
            this.cancel();
        }
    }
}