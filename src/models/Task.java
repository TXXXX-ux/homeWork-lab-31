package models;

import utils.LocalizedLabels;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import models.state.*;

public class Task {
    public enum Status { NEW, IN_PROGRESS, DONE }
    public enum Priority { LOW, MEDIUM, HIGH }

    private static int nextId = 1;
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private TaskState state;
    private boolean deleted = false;

    private int id;
    private String title;
    private String description;
    private LocalDate completionDate;
    private LocalDate createDate;
    private Priority priority;
    private Status status;

    public Task(String title, String description, LocalDate completionDate, Priority priority) {
        this.id = nextId++;
        this.title = title;
        this.description = description;
        this.completionDate = completionDate;
        this.createDate = LocalDate.now();
        this.priority = priority;
        this.status = Status.NEW;
        this.state = new NewState();
    }

    public Task(int id, String title, String description, LocalDate completionDate,
                LocalDate createDate, Priority priority, Status status) {
        this.id = id;
        nextId = Math.max(nextId, id + 1);
        this.title = title;
        this.description = description;
        this.completionDate = completionDate;
        this.createDate = createDate;
        this.priority = priority;
        this.status = status;
        this.state = new NewState();
    }

    public boolean isOverdue() {
        return LocalDate.now().isAfter(completionDate) && status != Status.DONE;
    }

    @Override
    public String toString() {
        return String.format(
                "ID: %d | %s%s\n   Приоритет: %s | Статус: %s\n   Создана: %s | Завершить до: %s\n   Описание: %s\n%s\n",
                id, title, isOverdue() ? "просрочено" : "",
                LocalizedLabels.PRIORITY_REVERSE.get(priority), LocalizedLabels.STATUS_REVERSE.get(status),
                createDate.format(DATE_FORMAT), completionDate.format(DATE_FORMAT),
                description, "-".repeat(50)
        );
    }

    public void changeStatus() {
        state.changeStatus(this);
    }

    public void changeDescriptionState(String desc) {
        state.changeDescription(this, desc);
    }

    public void deleteState() {
        state.delete(this);
    }

    public void setState(TaskState state) {
        this.state = state;
        this.status = Status.valueOf(state.getName());
    }

    public void markDeleted() {
        this.deleted = true;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getCompletionDate() { return completionDate; }
    public LocalDate getCreateDate() { return createDate; }
    public Priority getPriority() { return priority; }
    public Status getStatus() { return status; }

    public void setDescription(String description) {
        if (status == Status.NEW) this.description = description;
    }
    public void setStatus(Status status) { this.status = status; }
}