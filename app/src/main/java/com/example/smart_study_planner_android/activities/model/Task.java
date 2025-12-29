package com.example.smart_study_planner_android.activities.model;

public class Task {

    private int id;
    private String title;
    private int status;

    private long targetSeconds;
    private long studySeconds;
    private long breakSeconds;

    private long spentSeconds;
    private long lastStartTime;

    public static String formatSeconds(long sec) {
        long h = sec / 3600;
        long m = (sec % 3600) / 60;
        long s = sec % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public String getSpentTimeFormatted() {
        return formatSeconds(spentSeconds);
    }

    public String getRemainingTimeFormatted() {
        long remaining = Math.max(0, targetSeconds - spentSeconds);
        return formatSeconds(remaining);
    }

    public String getStudyTimeFormatted() {
        return formatSeconds(studySeconds);
    }


    public Task(int id, String title, int status,
                long targetSeconds, long studySeconds,
                long breakSeconds, long spentSeconds,
                long lastStartTime) {

        this.id = id;
        this.title = title;
        this.status = status;
        this.targetSeconds = targetSeconds;
        this.studySeconds = studySeconds;
        this.breakSeconds = breakSeconds;
        this.spentSeconds = spentSeconds;
        this.lastStartTime = lastStartTime;
    }

    // getters & setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTargetSeconds() {
        return targetSeconds;
    }

    public void setTargetSeconds(long targetSeconds) {
        this.targetSeconds = targetSeconds;
    }

    public long getStudySeconds() {
        return studySeconds;
    }

    public void setStudySeconds(long studySeconds) {
        this.studySeconds = studySeconds;
    }

    public long getBreakSeconds() {
        return breakSeconds;
    }

    public void setBreakSeconds(long breakSeconds) {
        this.breakSeconds = breakSeconds;
    }

    public long getSpentSeconds() {
        return spentSeconds;
    }

    public void setSpentSeconds(long spentSeconds) {
        this.spentSeconds = spentSeconds;
    }

    public long getLastStartTime() {
        return lastStartTime;
    }

    public void setLastStartTime(long lastStartTime) {
        this.lastStartTime = lastStartTime;
    }
}
