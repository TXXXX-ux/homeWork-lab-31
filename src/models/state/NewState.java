package models.state;

import models.Task;

public class NewState implements TaskState {

    @Override
    public void changeStatus(Task task) {
        task.setState(new InProgressState());
    }

    @Override
    public void changeDescription(Task task, String description) {
        task.setDescription(description);
    }

    @Override
    public void delete(Task task) {
        task.markDeleted();
    }

    @Override
    public String getName() {
        return "NEW";
    }
}
