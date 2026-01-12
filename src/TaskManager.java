import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import exceptions.InvalidMenuChoiceException;
import exceptions.InvalidStringInputException;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class TaskManager {
    private List<Task> tasks = new ArrayList<>();
    private Comparator<Task> currentComparator = Comparator.comparing(Task::getPriority).reversed();
    private Scanner scanner = new Scanner(System.in);
    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new com.google.gson.JsonSerializer<LocalDate>() {
                public com.google.gson.JsonElement serialize(LocalDate date, java.lang.reflect.Type type,
                                                             com.google.gson.JsonSerializationContext context) {
                    return new com.google.gson.JsonPrimitive(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                }
            })
            .registerTypeAdapter(LocalDate.class, new com.google.gson.JsonDeserializer<LocalDate>() {
                public LocalDate deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type type,
                                             com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {
                    return LocalDate.parse(json.getAsString(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                }
            })
            .setPrettyPrinting()
            .create();

    private void showMenu() {
        System.out.println("Меню");
        System.out.println("1.Показать все задачи");
        System.out.println("2.Добавить задачу");
        System.out.println("3.Изменить статус");
        System.out.println("4.Изменить описание");
        System.out.println("5.Удалить задачу");
        System.out.println("6.Сортировать");
        System.out.println("7.Фильтровать");
        System.out.println("8.Выйти");
    }

    public void run() {
        loadTasks();

        while (true) {
            showMenu();

            int choice = enterInt("Выберите действие: ", 1, 8);

            switch (choice) {
                case 1 -> displayTasks();
                case 2 -> addTask();
                case 3 -> changeStatus();
                case 4 -> changeDescription();
                case 5 -> deleteTask();
                case 6 -> sortTasks();
                case 7 -> filterTasks();
                case 8 -> {
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
                String input = scanner.nextLine().toUpperCase();
                return Task.Priority.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: допустимые значения: НИЗКИЙ, СРЕДНИЙ, ВЫСОКИЙ\n");
            }
        }
    }

    private Task.Status enterStatus() {
        while (true) {
            try {
                System.out.print("Введите статус (new/in_progress/done): ");
                String input = scanner.nextLine().toUpperCase();
                return Task.Status.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: допустимые значения: NEW, IN_PROGRESS, DONE\n");
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
        try (Reader reader = new FileReader("tasks.json")) {
            Type listType = new TypeToken<List<Task>>(){}.getType();
            tasks = gson.fromJson(reader, listType);
            if (tasks == null) tasks = new ArrayList<>();
            System.out.println("Загружено задач: " + tasks.size());
        } catch (IOException e) {
        }
    }

    private void saveTasks() {
        try (Writer writer = new FileWriter("tasks.json")) {
            gson.toJson(tasks, writer);
        } catch (IOException e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }
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
        System.out.println("Добавление новой задачи:\n");

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
        System.out.print("Введите ID задачи: ");

        int id = enterInt("Введите ID задачи: ", 1, getMaxId());

        tasks.stream()
                    .filter(t -> t.getId() == id)
                    .findFirst()
                    .ifPresentOrElse(task -> {
                        System.out.println("Текущий статус: " + task.getStatus());
                        switch (task.getStatus()) {
                            case NEW -> {
                                if (enterConfirmation("Перевести в работу?")) {
                                    task.setStatus(Task.Status.IN_PROGRESS);
                                    saveTasks();
                                    System.out.println("Статус обновлен!");
                                }
                            }
                            case IN_PROGRESS -> {
                                if (enterConfirmation("Отметить выполненной?")) {
                                    task.setStatus(Task.Status.DONE);
                                    saveTasks();
                                    System.out.println("Задача выполнена!");
                                }
                            }
                            case DONE -> System.out.println("Задача уже выполнена!");
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
                    if (task.getStatus() != Task.Status.NEW) {
                        System.out.println("Можно менять только у новых задач!");
                        return;
                    }
                    System.out.print("Новое описание: ");
                    task.setDescription(scanner.nextLine());
                    saveTasks();
                    System.out.println("Описание обновлено!");
                }, () -> System.out.println("Задача не найдена"));
    }

    private void deleteTask() {
        displayTasks();
        int id = enterInt("Введите ID задачи: ", 1, getMaxId());

        Optional<Task> taskOpt = tasks.stream()
                .filter(t -> t.getId() == id)
                .findFirst();

        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            if (task.getStatus() != Task.Status.NEW) {
                System.out.println("Можно удалять только новые задачи!");
                return;
            }
            tasks.remove(task);
            saveTasks();
            System.out.println("Задача удалена!");
        } else {
            System.out.println("Задача не найдена!");
        }
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

    private void filterTasks() {
        System.out.println("\t1. По приоритету " +
                "\n\t2. По статусу " +
                "\n\t3. Просроченные");

        int choice = enterInt("Выберите фильтр: ", 1, 3);
        List<Task> filtered = switch (choice) {
            case 1 -> tasks.stream()
                        .filter(t -> t.getPriority() == enterPriority())
                        .toList();
            case 2 -> tasks.stream()
                        .filter(t -> t.getStatus() == enterStatus())
                        .toList();
            case 3 -> tasks.stream()
                        .filter(Task::isOverdue)
                        .toList();
            default -> new ArrayList<>();
        };

        displayTasks(filtered.stream()
                .sorted(currentComparator)
                .toList());
    }

}