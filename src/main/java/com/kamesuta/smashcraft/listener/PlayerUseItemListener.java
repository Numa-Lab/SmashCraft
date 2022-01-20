package com.kamesuta.smashcraft.listener;

import com.kamesuta.smashcraft.SmashCraft;
import dev.kotx.flylib.ListenerAction;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import static org.bukkit.event.block.Action.*;

public class PlayerUseItemListener implements ListenerAction<PlayerInteractEvent> {
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
            final int TIME = 5;
            final int PERIOD = 10;

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

            ItemMeta meta = shield.getItemMeta();
            assert meta != null;
            int maxDurability = shield.getType().getMaxDurability();
            final boolean[] first = {true};
            SmashCraft pl = SmashCraft.instance;

            new BukkitRunnable() {
                public void run() {
                    Damageable damageable = ((Damageable) meta);
                    int damage = damageable.getDamage();

                    if (!first[0] && !player.isBlocking()) {
                        cancel();
                        return;
                    }

                    if (maxDurability - damage <= 0) {
                        if (inv.getItemInOffHand().equals(shield))
                            inv.setItemInOffHand(null);
                        else inv.remove(shield);
                        cancel();

                        int slot = inv.getHeldItemSlot();
                        new BukkitRunnable() {
                            public void run() {
                                ItemStack newShield = new ItemStack(Material.SHIELD);
                                if (inv.getItem(slot) != null) inv.addItem(newShield);
                                else inv.setItem(slot, newShield);
                                cancel();
                            }
                        }.runTaskTimer(pl, 100, 0);
                        return;
                    }

                    damageable.setDamage(damage + maxDurability / (TIME * 20 / PERIOD));
                    shield.setItemMeta(meta);
                    first[0] = false;
                }
            }.runTaskTimer(pl, 0, PERIOD);
        }
    }
}
