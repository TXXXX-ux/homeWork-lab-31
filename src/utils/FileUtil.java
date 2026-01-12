package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import models.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static models.Task.DATE_FORMAT;

public class FileUtil {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, (com.google.gson.JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                    new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
            )
            .registerTypeAdapter(LocalDate.class, (com.google.gson.JsonDeserializer<LocalDate>) (json, typeOfT, context) ->
                    LocalDate.parse(json.getAsString(), DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            )
            .create();

    private static final Path PATH = Paths.get("src/data/tasks.json");


    private static String readFile(Path path) {
        try {
            if (!Files.exists(path)) {
                return "[]";
            }
            return Files.readString(path);
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла!");
        }
        return "";
    }

    private static void writeToFile(Path path, String json) {
        byte[] strToBytes = json.getBytes();

        try {
            Files.write(path, strToBytes);
        } catch (IOException e) {
            System.out.println("Ошибка записи в файл!");
        }
    }

    public static List<Task> readTasks() {
        Task[] tasksArr;
        try {
            tasksArr = GSON.fromJson(readFile(PATH), Task[].class);
        } catch (JsonSyntaxException e) {
            System.out.println("Ошибка формата JSON!");
            return new ArrayList<>();
        }

        if (tasksArr == null) {
            return new ArrayList<>();
        }

        List<Task> validTasks = new ArrayList<>();
        for (Task t : tasksArr) {
            if (t != null && t.getCompletionDate() != null && t.getCreateDate() != null) {
                validTasks.add(t);
            } else if (t != null) {
                System.out.println("Пропущена задача с некорректными датами: " + t.getTitle());
            }
        }
        return validTasks;
    }

    public static void writeTasks(List<Task> tasks) {
        Task[] tasksArr = tasks.toArray(new Task[0]);
        writeToFile(PATH, GSON.toJson(tasksArr));
    }
}
