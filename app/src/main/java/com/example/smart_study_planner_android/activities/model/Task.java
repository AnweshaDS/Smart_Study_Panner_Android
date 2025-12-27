package com.example.smart_study_planner_android.activities.model;

public class Task {
    private int id;
    private String title;
    private int status;

    public Task(int id, String title, int status) {
        this.id = id;
        this.title = title;
        this.status = status;
    }

    public Task(String title, int status) {
        this(-1, title, status);
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public int getStatus() { return status; }
}
