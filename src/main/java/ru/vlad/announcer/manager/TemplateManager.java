package ru.vlad.announcer.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import ru.vlad.announcer.AnnouncerPlugin;
import ru.vlad.announcer.data.Template;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class TemplateManager {
    private final AnnouncerPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final LinkedHashMap<String, Template> templates = new LinkedHashMap<>();

    public TemplateManager(AnnouncerPlugin plugin) { this.plugin = plugin; }

    public void loadFromConfig() {
        templates.clear();
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("templates");
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            ConfigurationSection t = sec.getConfigurationSection(key);
            String text = t.getString("text", "<white>empty</white>");
            String type = t.getString("type", "chat");
            int time = t.getInt("time", 4);
            boolean enabled = t.getBoolean("enabled", true);
            String audience = t.getString("audience", "all");
            templates.put(key.toLowerCase(Locale.ROOT), new Template(key.toLowerCase(Locale.ROOT), text, type, time, enabled, audience));
        }
        plugin.getLogger().info("Загружено шаблонов: " + templates.size());
    }

    public Collection<Template> list() { return templates.values(); }
    public Template get(String id) { return templates.get(id.toLowerCase(Locale.ROOT)); }

    public void addOrUpdate(String id, String text, String type, int time, boolean enabled, String audience) {
        templates.put(id.toLowerCase(Locale.ROOT), new Template(id.toLowerCase(Locale.ROOT), text, type, time, enabled, audience));
        saveToConfig();
    }

    public void remove(String id) { templates.remove(id.toLowerCase(Locale.ROOT)); saveToConfig(); }

    public void saveToConfig() {
        plugin.getConfig().set("templates", null);
        for (Template t : templates.values()) {
            ConfigurationSection s = plugin.getConfig().createSection("templates." + t.id());
            s.set("text", t.text());
            s.set("type", t.type());
            s.set("time", t.titleTime());
            s.set("enabled", t.enabled());
            s.set("audience", t.audience());
        }
        plugin.saveConfig();
    }

    // broadcast with audience filtering
    public void broadcastTemplate(String id, String mode, Player actor) {
        Template tpl = get(id);
        if (tpl == null || !tpl.enabled()) return;
        String raw = tpl.text();
        raw = raw.replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()));
        if (actor != null) raw = raw.replace("{player}", actor.getName());

        Component comp = mm.deserialize(raw);

        List<Player> recipients = computeRecipients(tpl.audience(), actor);
        if (recipients.isEmpty()) return;

        String useMode = (mode == null || mode.isEmpty()) ? tpl.type() : mode;
        if (useMode.equalsIgnoreCase("chat") || useMode.equalsIgnoreCase("all")) {
            for (Player p : recipients) plugin.audiences().player(p).sendMessage(comp);
        }
        if (useMode.equalsIgnoreCase("actionbar") || useMode.equalsIgnoreCase("all")) {
            for (Player p : recipients) plugin.audiences().player(p).sendActionBar(comp);
        }
        if (useMode.equalsIgnoreCase("title") || useMode.equalsIgnoreCase("all")) {
            Title title = Title.title(comp, Component.empty(), Title.Times.of(Duration.ofMillis(200), Duration.ofSeconds(tpl.titleTime()), Duration.ofMillis(200)));
            for (Player p : recipients) plugin.audiences().player(p).showTitle(title);
        }
    }

    private List<Player> computeRecipients(String audience, Player actor) {
        if (audience == null || audience.isEmpty() || audience.equalsIgnoreCase("all")) {
            return new ArrayList<>(Bukkit.getOnlinePlayers());
        }
        if (audience.equalsIgnoreCase("ops")) {
            return Bukkit.getOnlinePlayers().stream().filter(Player::isOp).collect(Collectors.toList());
        }
        if (audience.equalsIgnoreCase("player")) {
            return actor == null ? Collections.emptyList() : Collections.singletonList(actor);
        }
        if (audience.startsWith("permission:")) {
            String perm = audience.substring("permission:".length());
            return Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission(perm)).collect(Collectors.toList());
        }
        if (audience.startsWith("world:")) {
            String world = audience.substring("world:".length());
            return Bukkit.getOnlinePlayers().stream().filter(p -> p.getWorld().getName().equalsIgnoreCase(world)).collect(Collectors.toList());
        }
        if (audience.startsWith("near:")) {
            try {
                double r = Double.parseDouble(audience.substring("near:".length()));
                if (actor == null) return Collections.emptyList();
                return Bukkit.getOnlinePlayers().stream().filter(p -> p.getLocation().distance(actor.getLocation()) <= r).collect(Collectors.toList());
            } catch (Exception ex) { return Collections.emptyList(); }
        }
        // fallback: empty
        return Collections.emptyList();
    }
}
