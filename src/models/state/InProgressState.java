package models.state;

import models.Task;

public class InProgressState implements TaskState {

    @Override
    public void changeStatus(Task task) {
        task.setState(new DoneState());
    }

    @Override
    public void changeDescription(Task task, String description) {
        throw new IllegalStateException("Нельзя менять описание задачи в работе");
    }

    @Override
    public void delete(Task task) {
        throw new IllegalStateException("Нельзя удалить задачу в работе");
    }

    @Override
    public String getName() {
        return "IN_PROGRESS";
    }
}
