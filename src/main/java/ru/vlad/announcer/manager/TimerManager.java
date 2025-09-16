package ru.vlad.announcer.manager;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import ru.vlad.announcer.AnnouncerPlugin;

import java.util.*;

public class TimerManager {
    private final AnnouncerPlugin plugin;
    private final TemplateManager templates;
    private final Map<String, BukkitTask> tasks = new HashMap<>();

    public TimerManager(AnnouncerPlugin plugin, TemplateManager templates) { this.plugin = plugin; this.templates = templates; }

    public void loadTimersFromConfig() {
        cancelAll();
        List<?> list = plugin.getConfig().getList("timers", Collections.emptyList());
        for (Object o : list) {
            if (!(o instanceof Map)) continue;
            Map map = (Map) o;
            String tpl = (String) map.get("template");
            int interval = ((Number)map.getOrDefault("interval", 300)).intValue();
            boolean enabled = (boolean) map.getOrDefault("enabled", true);
            if (!enabled) continue;
            startTimer(tpl, interval);
        }
    }

    public void startTimer(String tpl, int intervalSeconds) {
        if (templates.get(tpl) == null) return;
        if (tasks.containsKey(tpl)) tasks.get(tpl).cancel();
        BukkitTask t = Bukkit.getScheduler().runTaskTimer(plugin, () -> templates.broadcastTemplate(tpl, "all", null), intervalSeconds*20L, intervalSeconds*20L);
        tasks.put(tpl, t);
    }

    public void cancelAll() { for (BukkitTask t : tasks.values()) t.cancel(); tasks.clear(); }
}
