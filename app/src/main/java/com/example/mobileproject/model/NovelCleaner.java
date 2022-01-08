package com.example.mobileproject.model;

import java.util.Objects;

public class NovelCleaner {
    public int position;

    private int cleanerId;
    private int connectionId;

    private boolean isActive = true;

    private String name;
    private String flag;
    private String replacement;

    private int type;

    public NovelCleaner() {
    }

    public NovelCleaner(String name, String flag, String replacement, int type) {
        this.name = name;
        this.flag = flag;
        this.replacement = replacement;
        this.type = type;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getReplacement() {
        return replacement;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCleanerId() {
        return cleanerId;
    }

    public void setCleanerId(int cleanerId) {
        this.cleanerId = cleanerId;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NovelCleaner cleaner = (NovelCleaner) o;
        return isActive == cleaner.isActive && type == cleaner.type && name.equals(cleaner.name) && flag.equals(cleaner.flag) && replacement.equals(cleaner.replacement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isActive, name, flag, replacement, type);
    }
}
