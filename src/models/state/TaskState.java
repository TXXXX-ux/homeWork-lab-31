package models.state;

import models.Task;

public interface TaskState {
    void changeStatus(Task task);
    void changeDescription(Task task, String description);
    void delete(Task task);
    String getName();
}

