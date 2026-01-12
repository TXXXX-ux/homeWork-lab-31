package models.state;

import models.Task;

public class DoneState implements TaskState {

    @Override
    public void changeStatus(Task task) {
        throw new IllegalStateException("Задача уже завершена");
    }

    @Override
    public void changeDescription(Task task, String description) {
        throw new IllegalStateException("Нельзя менять описание завершенной задачи");
    }

    @Override
    public void delete(Task task) {
        throw new IllegalStateException("Нельзя удалить завершенную задачу");
    }

    @Override
    public String getName() {
        return "DONE";
    }
}
