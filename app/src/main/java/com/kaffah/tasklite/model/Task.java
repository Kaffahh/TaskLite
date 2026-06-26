package com.kaffah.tasklite.model;

public class Task {
    public long id;
    public String title;
    public String description;
    public Long categoryId;
    public String categoryName;
    public int categoryColor;
    public int priority;
    public Long dueAt;
    public boolean reminderEnabled;
    public Long reminderAt;
    public boolean completed;
    public Long completedAt;
    public long createdAt;
    public long updatedAt;
}
