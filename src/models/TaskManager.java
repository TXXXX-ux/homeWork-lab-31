package models;

import utils.FileUtil;
import exceptions.InvalidMenuChoiceException;
import exceptions.InvalidStringInputException;
import utils.LocalizedLabels;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class TaskManager {
    private List<Task> tasks = new ArrayList<>();
    private Comparator<Task> currentComparator = Comparator.comparing(Task::getPriority).reversed();
    private Scanner scanner = new Scanner(System.in);

    private void showMenu() {
        System.out.println("Меню");
        System.out.println("1.Показать все задачи");
        System.out.println("2.Добавить задачу");
        System.out.println("3.Изменить статус");
        System.out.println("4.Изменить описание");
        System.out.println("5.Удалить задачу");
        System.out.println("6.Сортировать");
        System.out.println("7.Фильтровать");
        System.out.println("8.Поиск по критериям");
        System.out.println("9.Выйти");
    }

    public void run() {
        loadTasks();

        while (true) {
            showMenu();

            int choice = enterInt("Выберите действие: ", 1, 9);

            switch (choice) {
                case 1 -> displayTasks();
                case 2 -> addTask();
                case 3 -> changeStatus();
                case 4 -> changeDescription();
                case 5 -> deleteTask();
                case 6 -> sortTasks();
                case 7 -> filterTasks();
                case 8 -> searchTasks();
                case 9 -> {
                    saveTasks();
                    return;
                }
            }
        }
    }

    private boolean enterConfirmation(String message) {
        while (true) {
            try {
                System.out.print(message + " (да/нет): ");
                String input = scanner.nextLine().trim().toLowerCase();

                if (input.equals("да")) {
                    return true;
                } else if (input.equals("нет")) {
                    return false;
                } else {
                    throw new InvalidStringInputException("Ошибка: введите 'да' или 'нет'!\n");
                }
            } catch (InvalidStringInputException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private LocalDate enterAnyDate(String message) {
        while (true) {
            try {
                System.out.print(message);
                String input = scanner.nextLine();
                return LocalDate.parse(input, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            } catch (DateTimeParseException e) {
                System.out.println("Ошибка: неверный формат даты! Используйте дд.мм.гггг\n");
            }
        }
    }

    private LocalDate enterDate(String message) {
        while (true) {
            try {
                System.out.print(message);
                String input = scanner.nextLine();
                LocalDate date = LocalDate.parse(input, DateTimeFormatter.ofPattern("dd.MM.yyyy"));

                if (date.isBefore(LocalDate.now())) {
                    throw new IllegalArgumentException("Ошибка: дата в прошлом!\n");
                }
                return date;

            } catch (DateTimeParseException e) {
                System.out.println("Ошибка: неверный формат даты! Используйте дд.мм.гггг\n");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private String enterString(String message) {
        while (true) {
            try {
                System.out.println(message);
                String input = scanner.nextLine().strip();
                if (input.isEmpty()) {
                    throw new InvalidStringInputException("Ошибка: вы ничего не ввели!\n");
                }
                return input;

            } catch (InvalidStringInputException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private Task.Priority enterPriority() {
        while (true) {
            try {
                System.out.print("Введите приоритет (низкий/средний/высокий): ");
                String input = scanner.nextLine().strip().toLowerCase();
                Task.Priority priority = LocalizedLabels.PRIORITY_MAP.get(input);

                if (priority == null) throw new InvalidStringInputException("Ошибка: допустимые значения: низкий, средний, высокий\n");
                return priority;
            } catch (InvalidStringInputException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private Task.Status enterStatus() {
        while (true) {
            try {
                System.out.print("Введите статус (новая/в работе/сделано): ");
                String input = scanner.nextLine().strip().toLowerCase();
                Task.Status status = LocalizedLabels.STATUS_MAP.get(input);

                if (status == null) throw new InvalidStringInputException("Ошибка: допустимые значения: новая, в работе, сделано\n");
                return status;
            } catch (InvalidStringInputException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private int enterInt(String message, int min, int max) {
        while (true) {
            try {
                System.out.print(message);
                int value = Integer.parseInt(scanner.nextLine());

                if (value < min || value > max) {
                    throw new InvalidMenuChoiceException("Ошибка: число должно быть от " + min + " до " + max + "!\n");
                }
                return value;

            } catch (InvalidMenuChoiceException e) {
                System.out.println(e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число!\n");
            }
        }
    }

    private List<Task> getSortedTasks() {
        return tasks.stream()
                .sorted(currentComparator)
                .toList();
    }

    private void loadTasks() {
        tasks = FileUtil.readTasks();
        if (tasks.isEmpty()) {
            System.out.println("Файл задач пуст или все задачи некорректны!\nСоздайте новую задачу.\n");
            addTask();
        } else {
            System.out.println("Загружено задач: " + tasks.size());
        }
    }

    private void saveTasks() {
        FileUtil.writeTasks(tasks);
    }

    private void displayTasks(List<Task> taskList) {
        if (taskList.isEmpty()) {
            System.out.println("Список пуст");
            return;
        }
        taskList.forEach(System.out::print);
    }

    private void displayTasks() {
        List<Task> sortedTask = getSortedTasks();
        if (sortedTask.isEmpty()) {
            System.out.println("Список пуст");
            return;
        }
        sortedTask.forEach(System.out::print);
    }

    private void addTask() {
        System.out.println("=== Добавление новой задачи ===");

        String title = enterString("Введите название задачи: ");
        String desc = enterString("Введите описание задачи: ");

        LocalDate date = enterDate("Введите дату завершения задачи: ");

        Task.Priority priority = enterPriority();

        tasks.add(new Task(title, desc, date, priority));
        saveTasks();
        System.out.println("Задача добавлена!\nОбновленный список задач: ");

        currentComparator = Comparator.comparing(Task::getPriority).reversed();
        displayTasks();
    }

    private void changeStatus() {
        displayTasks();
        int id = enterInt("Введите ID задачи: ", 1, getMaxId());

        tasks.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .ifPresentOrElse(task -> {
                    try {
                        task.changeStatus();
                        saveTasks();
                        System.out.println("Статус изменён");
                    } catch (IllegalStateException e) {
                        System.out.println(e.getMessage());
                    }
                }, () -> System.out.println("Задача не найдена!"));
    }

    private void changeDescription() {
        displayTasks();
        int id = enterInt("Введите ID задачи: ", 1, getMaxId());

        tasks.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .ifPresentOrElse(task -> {
                    try {
                        System.out.print("Новое описание: ");
                        task.changeDescriptionState(scanner.nextLine());
                        saveTasks();
                        System.out.println("Описание обновлено");
                    } catch (IllegalStateException e) {
                        System.out.println(e.getMessage());
                    }
                }, () -> System.out.println("Задача не найдена!"));
    }

    private void deleteTask() {
        displayTasks();
        int id = enterInt("Введите ID задачи: ", 1, getMaxId());

        tasks.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .ifPresentOrElse(task -> {
                    try {
                        task.deleteState();
                        tasks.removeIf(Task::isDeleted);
                        saveTasks();
                        System.out.println("Задача удалена");
                    } catch (IllegalStateException e) {
                        System.out.println(e.getMessage());
                    }
                }, () -> System.out.println("Задача не найдена!"));
    }

    private int getMaxId() {
        return tasks.stream()
                .mapToInt(Task::getId)
                .max()
                .orElse(0);
    }

    private void sortTasks() {
        System.out.println("\t1. Приоритет " +
                "\n\t2. Дата создания " +
                "\n\t3. Название " +
                "\n\t4. Дата завершения");

        int choice = enterInt("Выберите сортировку: ", 1, 4);

        currentComparator = switch (choice) {
            case 2 -> Comparator.comparing(Task::getCreateDate);
            case 3 -> Comparator.comparing(Task::getTitle);
            case 4 -> Comparator.comparing(Task::getCompletionDate);
            default -> Comparator.comparing(Task::getPriority).reversed();
        };

        System.out.println("Сортировка изменена!");
        displayTasks();
    }

    private void searchTasks() {
        System.out.println("=== Поиск задач ===" +
                "\n\t1. По ключевому слову в названии/описании" +
                "\n\t2. По дате (или диапазону дат/месяцу)" +
                "\n\t3. По приоритету");

        int choice = enterInt("Выберите критерий поиска: ", 1, 3);
        List<Task> results = new ArrayList<>();

        switch (choice) {
            case 1 -> {
                String keyword = enterString("Введите ключевое слово: ").toLowerCase();
                results = tasks.stream()
                        .filter(task -> task.getTitle().toLowerCase().contains(keyword)
                                || task.getDescription().toLowerCase().contains(keyword))
                        .toList();
                if (results.isEmpty()) {
                    System.out.println("Нет задач, содержащих это слово!");
                }
            }
            case 2 -> {
                System.out.println("1. По конкретной дате" +
                        "\n2. По диапазону дат" +
                        "\n3. По месяцу");
                int dateChoice = enterInt("Выберите вариант: ", 1, 3);
                switch (dateChoice) {
                    case 1 -> {
                        LocalDate date = enterAnyDate("Введите дату (дд.мм.гггг): ");
                        results = tasks.stream()
                                .filter(task -> task.getCompletionDate().isEqual(date))
                                .toList();
                    }
                    case 2 -> {
                        LocalDate start = enterAnyDate("Введите начальную дату (дд.мм.гггг): ");
                        LocalDate end = enterAnyDate("Введите конечную дату (дд.мм.гггг): ");
                        results = tasks.stream()
                                .filter(task -> !task.getCompletionDate().isBefore(start) &&
                                        !task.getCompletionDate().isAfter(end))
                                .toList();
                    }
                    case 3 -> {
                        int month = enterInt("Введите месяц (1-12): ", 1, 12);
                        int year = enterInt("Введите год: ", 1900, 3000);
                        results = tasks.stream()
                                .filter(t -> t.getCompletionDate().getMonthValue() == month &&
                                        t.getCompletionDate().getYear() == year)
                                .toList();
                    }
                }
                if (results.isEmpty()) System.out.println("Задачи за выбранный период не найдены!");
            }
            case 3 -> {
                Task.Priority priority = enterPriority();
                results = tasks.stream()
                        .filter(task -> task.getPriority() == priority)
                        .toList();
                if (results.isEmpty()) {
                    System.out.println("Нет задач с таким приоритетом!");
                }
            }
        }
        if (!results.isEmpty()) {
            System.out.println("Найденные задачи:");
            displayTasks(results.stream()
                    .sorted(currentComparator)
                    .toList());
        }
    }

    private void filterTasks() {
        System.out.println("\t1. По приоритету " +
                "\n\t2. По статусу " +
                "\n\t3. Просроченные");

        int choice = enterInt("Выберите фильтр: ", 1, 3);

        List<Task> filtered = new ArrayList<>();

        switch (choice) {
            case 1 -> {
                Task.Priority priority = enterPriority();
                filtered = tasks.stream()
                        .filter(t -> t.getPriority() == priority)
                        .toList();
                if (filtered.isEmpty()) {
                    System.out.println("Нет задач с таким приоритетом!");
                    return;
                }
            }
            case 2 -> {
                Task.Status status = enterStatus();
                filtered = tasks.stream()
                        .filter(t -> t.getStatus() == status)
                        .toList();
                if (filtered.isEmpty()) {
                    System.out.println("Нет задач с таким статусом!");
                    return;
                }
            }
            case 3 -> {
                filtered = tasks.stream()
                        .filter(Task::isOverdue)
                        .toList();
                if (filtered.isEmpty()) {
                    System.out.println("Нет просроченных задач!");
                    return;
                }
            }
            default -> System.out.println("Неверный выбор фильтра");
        };

        displayTasks(filtered.stream()
                .sorted(currentComparator)
                .toList());
    }

}