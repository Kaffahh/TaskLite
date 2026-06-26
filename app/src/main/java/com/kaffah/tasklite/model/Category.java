package com.kaffah.tasklite.model;

public class Category {
    public long id;
    public String name;
    public int color;
    public long createdAt;
    public long updatedAt;

    public Category() {
    }

    public Category(long id, String name, int color, long createdAt, long updatedAt) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
