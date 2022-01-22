package com.kamesuta.smashcraft.listener;

import com.kamesuta.smashcraft.Config;
import com.kamesuta.smashcraft.SmashCraft;
import dev.kotx.flylib.ListenerAction;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Score;
import org.bukkit.util.Vector;

public class PlayerDamageListener implements ListenerAction<EntityDamageEvent> {
    public PlayerDamageListener() {
        // 重力加速度
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // 吹っ飛び率
                    Score score = SmashCraft.instance.objective.getScore(player.getName());
                    Score scorePercent = SmashCraft.instance.objectivePercent.getScore(player.getName());
                    // %にコピー
                    scorePercent.setScore(score.getScore());
                    // 経験値レベルにセット
                    player.setLevel(score.getScore());
                    player.setExp(Math.max(0, Math.min(1, score.getScore() / 300.0f)));
                }
            }
        }.runTaskTimer(SmashCraft.instance, 0, 10);
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
        Player player = (Player) e.getEntity();

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
            Player originalPlayer = (Player) originalDamager;

            // 吹っ飛び率
            Score score = SmashCraft.instance.objective.getScore(player.getName());
            // ダメージ量を吹っ飛び率に加算
            score.setScore(Math.min(score.getScore() + (int) damage, 999));

            // アイテム
            ItemStack item = originalPlayer.getInventory().getItemInMainHand();
            int knockbackLevel = item.getEnchantmentLevel(Enchantment.KNOCKBACK);
            knockbackLevel = knockbackLevel > 40000 ? knockbackLevel - 65536 : knockbackLevel;
            //Bukkit.broadcast(Component.text(knockbackLevel));

            // 強さ
            double knockbackCoefficient = Math.min(999, Math.max(0, score.getScore()));
            knockbackCoefficient = 0.000000002 * Math.pow(knockbackCoefficient, 4) + knockbackCoefficient;
            knockbackCoefficient *= 0.005 * (0.5 * damage + 1.5) * (0.1 * knockbackLevel + 1);

            // ノックバックの方向
            Vector knockback = knockbackDirection(damager, player, knockbackCoefficient);
            Vector direction = knockback.clone().normalize();
            double force = knockback.length();

            // 吹っ飛ぶ
            new SetVelocityTask(player, direction, force)
                    .runTaskTimerAsynchronously(SmashCraft.instance, 0, 4);
        }
    }

    // ノックバックの方向
    private Vector knockbackDirection(Entity damager, Entity entity, double force) {
        // Minecraftのノックバック挙動を再現
        double x = Math.sin(damager.getLocation().getYaw() * 0.017453292F);
        double z = -Math.cos(damager.getLocation().getYaw() * 0.017453292F);

        // -1 ～ 1
        double y = -Math.sin(damager.getLocation().getPitch() * 0.017453292F);

        // 0 ～ 2
        y += 1;
        // 0 ～ 1
        y /= 2;

        Vector vec3d = entity.getVelocity();
        Vector vec3d2 = (new Vector(x, 0.0D, z)).normalize().multiply(force);
        return new Vector(vec3d.getX() / 2.0D - vec3d2.getX(),
                (force * 0.5 + Math.random() * force * 0.5 * y),
                vec3d.getZ() / 2.0D - vec3d2.getZ());
    }

    private static class SetVelocityTask extends BukkitRunnable {
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
}
