package ru.vlad.announcer.listener;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import ru.vlad.announcer.AnnouncerPlugin;
import ru.vlad.announcer.manager.TemplateManager;

public class EventListener implements Listener {
    private final AnnouncerPlugin plugin;
    private final TemplateManager templates;

    public EventListener(AnnouncerPlugin plugin, TemplateManager templates) { 
        this.plugin = plugin; 
        this.templates = templates; 
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!plugin.getConfig().getBoolean("events.rare-ore.enabled", true)) return;
        Material m = e.getBlock().getType();
        if (m == Material.DIAMOND_ORE || m == Material.ANCIENT_DEBRIS || m == Material.EMERALD_ORE) {
            String tpl = plugin.getConfig().getString("events.rare-ore.template", "rare-ore");
            templates.broadcastTemplate(tpl, plugin.getConfig().getString("events.rare-ore.mode","actionbar"), e.getPlayer());
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        // Проверка на смерть игрока
        if (e.getEntityType() == EntityType.PLAYER) {
            if (!plugin.getConfig().getBoolean("events.player-death.enabled", true)) return;
            String tpl = plugin.getConfig().getString("events.player-death.template", "player-death");
            
            // Здесь была ошибка. Мы явно приводим тип LivingEntity к Player.
            Player deadPlayer = (Player) e.getEntity();
            templates.broadcastTemplate(tpl, plugin.getConfig().getString("events.player-death.mode","all"), deadPlayer);
            return; // Чтобы не проверять другие условия, если это игрок
        }

        // Проверка на смерть босса (эндер-дракона)
        if (!plugin.getConfig().getBoolean("events.boss-kill.enabled", true)) return;
        if (e.getEntityType() == EntityType.ENDER_DRAGON) {
            String tpl = plugin.getConfig().getString("events.boss-kill.template", "dragon-kill");
            templates.broadcastTemplate(tpl, plugin.getConfig().getString("events.boss-kill.mode","all"), null);
        }
    }
}
