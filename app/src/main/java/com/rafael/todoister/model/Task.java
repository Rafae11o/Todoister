package com.rafael.todoister.model;

import java.util.Date;

public class Task {

    private String id;

    private String task;

    private Priority priority;

    private Date dueDate;

    private Date dateCreated;

    private boolean isDone;

    public Task(String id, String task, Priority priority, Date dueDate, Date dateCreated, boolean isDone) {
        setId(id);
        setTask(task);
        setPriority(priority);
        setDueDate(dueDate);
        setDateCreated(dateCreated);
        setDone(isDone);
    }

    public Task(String task, Priority priority, Date dueDate, Date dateCreated, boolean isDone) {
        setTask(task);
        setPriority(priority);
        setDueDate(dueDate);
        setDateCreated(dateCreated);
        setDone(isDone);
    }

    public Task() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) { this.id = id; }

    public String getTask() { return task; }

    public void setTask(String task) {
        this.task = task;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", task='" + task + '\'' +
                ", priority=" + priority +
                ", dueDate=" + dueDate +
                ", dateCreated=" + dateCreated +
                ", isDone=" + isDone +
                '}';
    }
}
