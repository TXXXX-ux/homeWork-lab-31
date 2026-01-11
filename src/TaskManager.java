import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import exceptions.InvalidMenuChoiceException;

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

            try {
                int choice = Integer.parseInt(scanner.nextLine());

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
                    default -> System.out.println("Введите число от 1 до 8.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Введите число от 1 до 8.");
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
        try {
            System.out.print("Название: ");
            String title = scanner.nextLine();

            System.out.print("Описание: ");
            String desc = scanner.nextLine();

            LocalDate date;
            while (true) {
                System.out.print("Дата завершения (дд.мм.гггг): ");
                String input = scanner.nextLine();
                try {
                    date = LocalDate.parse(input, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    if (date.isBefore(LocalDate.now())) {
                        System.out.println("Дата в прошлом! Повторите.");
                        continue;
                    }
                    break;
                } catch (DateTimeParseException e) {
                    System.out.println("Неверный формат!");
                }
            }

            Task.Priority priority;
            while (true) {
                System.out.print("Приоритет (низкий/средний/высокий): ");
                String input = scanner.nextLine().toUpperCase();
                try {
                    priority = Task.Priority.valueOf(input);
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println("Неверный приоритет!");
                }
            }

            tasks.add(new Task(title, desc, date, priority));
            tasks.sort((a,b) -> b.getPriority().ordinal() - a.getPriority().ordinal());
            saveTasks();
            System.out.println("Задача добавлена!");

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void changeStatus() {
        displayTasks();
        System.out.print("ID задачи: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            tasks.stream()
                    .filter(t -> t.getId() == id)
                    .findFirst()
                    .ifPresentOrElse(task -> {
                        System.out.println("Текущий статус: " + task.getStatus());
                        switch (task.getStatus()) {
                            case NEW -> {
                                System.out.print("Перевести в работу? (да/нет): ");
                                if (scanner.nextLine().equalsIgnoreCase("да")) {
                                    task.setStatus(Task.Status.IN_PROGRESS);
                                    saveTasks();
                                    System.out.println("Статус обновлен!");
                                }
                            }
                            case IN_PROGRESS -> {
                                System.out.print("Отметить выполненной? (да/нет): ");
                                if (scanner.nextLine().equalsIgnoreCase("да")) {
                                    task.setStatus(Task.Status.DONE);
                                    saveTasks();
                                    System.out.println("Задача выполнена!");
                                }
                            }
                            case DONE -> System.out.println("Задача уже выполнена!");
                        }
                    }, () -> System.out.println("Задача не найдена"));
        } catch (NumberFormatException e) {
            System.out.println("Неверный ID!");
        }
    }

    private void changeDescription() {
        displayTasks();
        System.out.print("ID задачи: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
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
        } catch (NumberFormatException e) {
            System.out.println("Неверный ID!");
        }
    }

    private void deleteTask() {
        displayTasks();
        System.out.print("ID задачи для удаления: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
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
        } catch (NumberFormatException e) {
            System.out.println("Неверный ID!");
        }
    }

    private void sortTasks() {
        System.out.println("\t1. Приоритет " +
                "\n\t2. Дата создания " +
                "\n\t3. Название " +
                "\n\t4. Дата завершения");
        boolean sortingChosen = false;
        while (!sortingChosen) {
            try {
                int choice = Integer.parseInt(scanner.nextLine());

                currentComparator = switch (choice) {
                    case 1 -> Comparator.comparing(Task::getPriority).reversed();
                    case 2 -> Comparator.comparing(Task::getCreateDate);
                    case 3 -> Comparator.comparing(Task::getTitle);
                    case 4 -> Comparator.comparing(Task::getCompletionDate);
                    default -> throw new InvalidMenuChoiceException("Ошибка: номер сортировки должен быть от 1 до 4!");
                };

                System.out.println("Сортировка изменена!");
                displayTasks();
                sortingChosen = true;

            } catch (NumberFormatException e) {
                System.out.println("Неверный ввод!");
            } catch (InvalidMenuChoiceException e) {
                System.out.println(e.getMessage());
            }
        }

    }

    private void filterTasks() {
        System.out.println("\t1. По приоритету " +
                "\n\t2. По статусу " +
                "\n\t3. Просроченные");
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            List<Task> filtered = switch (choice) {
                case 1 -> {
                    System.out.print("Приоритет (низкий/средний/высокий): ");
                    Task.Priority p = Task.Priority.valueOf(scanner.nextLine().toUpperCase());
                    yield tasks.stream()
                            .filter(t -> t.getPriority() == p)
                            .collect(Collectors.toList());
                }
                case 2 -> {
                    System.out.print("Статус (new/in_progress/done): ");
                    Task.Status s = Task.Status.valueOf(scanner.nextLine().toUpperCase());
                    yield tasks.stream()
                            .filter(t -> t.getStatus() == s)
                            .collect(Collectors.toList());
                }
                case 3 -> tasks.stream()
                        .filter(Task::isOverdue)
                        .collect(Collectors.toList());
                default -> new ArrayList<>();
            };
            displayTasks(filtered);
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}