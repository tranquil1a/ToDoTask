
package main.java;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        Task task1 = new Task(1, "Complete Project", "Finish the Java project", LocalDate.of(2024, 12, 20), 1, "Pending");
        Task task2 = new Task(2, "Read Book", "Read 'Clean Code'", LocalDate.of(2024, 12, 15), 2, "Pending");

        manager.addTask(task1);
        manager.addTask(task2);

        System.out.println("All Tasks:");
        manager.getAllTasks().forEach(System.out::println);

        System.out.println("\nUpdating Task 1...");
        task1.setStatus("Completed");
        manager.updateTask(1, task1);

        System.out.println("\nTask with ID 1:");
        System.out.println(manager.getTaskById(1));

        System.out.println("\nDeleting Task 2...");
        manager.deleteTask(2);

        System.out.println("\nAll Tasks After Deletion:");
        manager.getAllTasks().forEach(System.out::println);
    }
}
