package com.kamesuta.smashcraft.listener;

import com.kamesuta.smashcraft.Config;
import com.kamesuta.smashcraft.SmashCraft;
import dev.kotx.flylib.ListenerAction;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Score;
import org.bukkit.util.Vector;

public class PlayerDamageListener implements ListenerAction<EntityDamageEvent> {
    public PlayerDamageListener() {
    }

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
        double damage = e.getDamage();
        if (e.getCause() != EntityDamageEvent.DamageCause.VOID) {
            // ダメージは0
            e.setDamage(0);
        }

        // 人に殴られたときのみ吹っ飛ぶ
        if (e instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) e;
            final Entity damager = ev.getDamager();
            Entity originalDamager = ev.getDamager();

            // 弓やトライデント
            if (originalDamager instanceof Projectile) {
                ProjectileSource shooter = ((Projectile) originalDamager).getShooter();
                if (shooter instanceof Player)
                    originalDamager = (Player) shooter;
            }

            // 人に殴られたときのみ
            if (!(originalDamager instanceof Player)) {
                return;
            }

            // 吹っ飛び率
            Score score = SmashCraft.instance.objective.getScore(e.getEntity().getName());
            Score scorePercent = SmashCraft.instance.objectivePercent.getScore(e.getEntity().getName());
            // ダメージ量を吹っ飛び率に加算
            score.setScore(score.getScore() + (int) damage);
            scorePercent.setScore(score.getScore());

            // 強さ
            double knockbackCoefficient = score.getScore() / 10.0;

            // ノックバックの方向
            Vector knockback = knockbackDirection(damager, e.getEntity(), knockbackCoefficient);
            Vector direction = knockback.clone().normalize();
            double force = knockback.length();

            // 吹っ飛ぶ
            new SetVelocityTask((Player) e.getEntity(), direction, force)
                    .runTaskTimerAsynchronously(SmashCraft.instance, 0, 4);
        }
    }

    // ノックバックの方向
    private Vector knockbackDirection(Entity damager, Entity entity, double force) {
        // Minecraftのノックバック挙動を再現
        double x = Math.sin(damager.getLocation().getYaw() * 0.017453292F);
        double z = -Math.cos(damager.getLocation().getYaw() * 0.017453292F);

        Vector vec3d = entity.getVelocity();
        Vector vec3d2 = (new Vector(x, 0.0D, z)).normalize().multiply(force);
        return new Vector(vec3d.getX() / 2.0D - vec3d2.getX(),
                entity.isOnGround() ? Math.min(0.4D, vec3d.getY() / 2.0D + force) : vec3d.getY(),
                vec3d.getZ() / 2.0D - vec3d2.getZ());
    }
}

class SetVelocityTask extends BukkitRunnable {
    private int count = 0;
    private final int numberOfTimes;
    private final Player target;
    private final Vector vector;

    SetVelocityTask(Player target, Vector direction, double knockbackCoefficient) {
        this.target = target;
        this.numberOfTimes = ((int) Math.ceil(knockbackCoefficient / 4));

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