package com.kamesuta.smashcraft.listener;

import com.kamesuta.smashcraft.SmashCraft;
import dev.kotx.flylib.ListenerAction;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerUseItemListener implements ListenerAction<PlayerInteractEvent> {
    @Override
    public void execute(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.SHIELD)
            return;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!e.getPlayer().isBlocking()) {
                    cancel();
                    return;
                }
                ItemMeta meta = item.getItemMeta();
                if (!(meta instanceof Damageable))
                    return;
                Damageable damagable = ((Damageable) meta);

                damagable.setDamage(Math.max(damagable.getDamage() + 1, item.getType().getMaxDurability() - 5));
                item.setItemMeta(meta);
            }
        }.runTaskTimer(SmashCraft.instance, 20, 20);
    }
}
