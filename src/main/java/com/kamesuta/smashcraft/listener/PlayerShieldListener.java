package com.kamesuta.smashcraft.listener;

import com.kamesuta.smashcraft.SmashCraft;
import dev.kotx.flylib.ListenerAction;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.bukkit.event.block.Action.*;

public class PlayerShieldListener {
    private static final int TIME = 5;
    private static final int PERIOD = 10;

    private static void itemBack(Player player) {
        PlayerInventory inv = player.getInventory();
        int slot = inv.getHeldItemSlot();

        new BukkitRunnable() {
            public void run() {
                ItemStack newShield = new ItemStack(Material.SHIELD);
                if (inv.getItem(slot) != null) inv.addItem(newShield);
                else inv.setItem(slot, newShield);
                cancel();
            }
        }.runTaskTimer(SmashCraft.instance, 100, 0);
    }

    public static class PlayerShieldBreakListener implements ListenerAction<PlayerItemBreakEvent> {
        @Override
        public void execute(PlayerItemBreakEvent e) {
            ItemStack item = e.getBrokenItem();
            if (item.getType() != Material.SHIELD) return;

            itemBack(e.getPlayer());
        }
    }

    public static class PlayerShieldDamageListener implements ListenerAction<PlayerItemDamageEvent> {
        @Override
        public void execute(PlayerItemDamageEvent e) {
            ItemStack item = e.getItem();
            if (item.getType() != Material.SHIELD) return;

            int maxDurability = item.getType().getMaxDurability();
            e.setDamage(e.getDamage() + maxDurability / (10 * TIME / PERIOD));
        }
    }

    public static class PlayerShieldInteractListener implements ListenerAction<PlayerInteractEvent> {
        @Override
        public void execute(PlayerInteractEvent e) {
            ItemStack item = e.getItem();

            if (item == null)
                return;

            if (item.getType() == Material.APPLE)
                e.getPlayer().setFoodLevel(12);

            if (item.getType() == Material.NETHERITE_SWORD) {
                ItemMeta meta = item.getItemMeta();
                if (!(meta instanceof Damageable))
                    return;
                Damageable damagable = ((Damageable) meta);
                damagable.setDamage(damagable.getDamage() + 1);
                if (damagable.getDamage() >= item.getType().getMaxDurability() - 1) {
                    e.getPlayer().getInventory().removeItem(item);
                } else {
                    item.setItemMeta(meta);
                }
            }

            // 盾コマンド
            {
                Player player = e.getPlayer();
                Action action = e.getAction();
                if (action == LEFT_CLICK_AIR ||
                        action == LEFT_CLICK_BLOCK ||
                        action == PHYSICAL
                ) return;

                PlayerInventory inv = player.getInventory();
                ItemStack main = inv.getItemInMainHand();
                ItemStack off = inv.getItemInOffHand();
                ItemStack shield = main.getType() == Material.SHIELD ?
                        main : off.getType() == Material.SHIELD ? off : null;
                if (shield == null) return;

                int maxDurability = shield.getType().getMaxDurability();
                AtomicBoolean first = new AtomicBoolean(true);

                new BukkitRunnable() {
                    public void run() {
                        ItemMeta meta = shield.getItemMeta();
                        assert meta != null;
                        Damageable damageable = ((Damageable) meta);
                        int damage = damageable.getDamage();

                        if (!first.get() && !player.isBlocking()) {
                            cancel();
                            return;
                        }

                        if (maxDurability - damage <= 0) {
                            if (inv.getItemInOffHand().equals(shield))
                                inv.setItemInOffHand(null);
                            else inv.remove(shield);
                            cancel();

                            itemBack(player);
                            return;
                        }

                        damageable.setDamage(damage + maxDurability / (20 * TIME / PERIOD));
                        shield.setItemMeta(meta);
                        first.set(false);
                    }
                }.runTaskTimer(SmashCraft.instance, 0, PERIOD);
            }
        }
    }
}
