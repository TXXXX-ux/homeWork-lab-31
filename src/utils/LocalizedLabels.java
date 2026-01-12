package utils;

import models.Task;

import java.util.Map;

public class LocalizedLabels {
    public static final Map<String, Task.Priority> PRIORITY_MAP = Map.of(
            "низкий", Task.Priority.LOW,
            "средний", Task.Priority.MEDIUM,
            "высокий", Task.Priority.HIGH
    );

    public static final Map<String, Task.Status> STATUS_MAP = Map.of(
            "новая", Task.Status.NEW,
            "в работе", Task.Status.IN_PROGRESS,
            "сделано", Task.Status.DONE
    );

    public static final Map<Task.Priority, String> PRIORITY_REVERSE = Map.of(
            Task.Priority.LOW, "низкий",
            Task.Priority.MEDIUM, "средний",
            Task.Priority.HIGH, "высокий"
    );

    public static final Map<Task.Status, String> STATUS_REVERSE = Map.of(
            Task.Status.NEW, "новая",
            Task.Status.IN_PROGRESS, "в работе",
            Task.Status.DONE, "сделано"
    );
}
