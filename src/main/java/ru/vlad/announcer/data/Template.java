package ru.vlad.announcer.data;

import java.util.Objects;

public class Template {
    private String id;
    private String text; // MiniMessage
    private String type; // chat | actionbar | title | all
    private int titleTime;
    private boolean enabled;
    private String audience; // all | player | ops | permission:node | world:worldname | near:radius

    public Template(String id, String text, String type, int titleTime, boolean enabled, String audience) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.titleTime = titleTime;
        this.enabled = enabled;
        this.audience = audience == null ? "all" : audience;
    }

    // getters/setters
    public String id() { return id; }
    public String text() { return text; }
    public String type() { return type; }
    public int titleTime() { return titleTime; }
    public boolean enabled() { return enabled; }
    public String audience() { return audience; }

    public void setText(String t) { this.text = t; }
    public void setType(String t) { this.type = t; }
    public void setTitleTime(int s) { this.titleTime = s; }
    public void setEnabled(boolean b) { this.enabled = b; }
    public void setAudience(String a) { this.audience = a; }

    @Override
    public boolean equals(Object o) { if (this==o) return true; if (!(o instanceof Template)) return false; return Objects.equals(id, ((Template)o).id); }
    @Override
    public int hashCode() { return id.hashCode(); }
}
