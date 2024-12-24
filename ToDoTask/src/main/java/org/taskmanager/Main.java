package org.taskmanager;
///tima
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main extends Application {

    private boolean isPomodoroRunning = false;

    private Label pomodoroLabel = new Label("25:00");
    private Timeline pomodoroTimer;
    private int pomodoroTimeLeft = 25 * 60; // 25 minutes in seconds

    private Connection connection;
    private String loggedInUser = null;

    private ListView<String> toDoList = new ListView<>();
    private ListView<String> inProgressList = new ListView<>();
    private ListView<String> doneList = new ListView<>();

    private void startPomodoroTimer() {
        if (!isPomodoroRunning) {
            isPomodoroRunning = true;
            pomodoroTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                pomodoroTimeLeft--;
                updatePomodoroLabel();
                if (pomodoroTimeLeft <= 0) {
                    pomodoroTimer.stop();
                    pomodoroLabel.setText("Time's up!");
                }
            }));
            pomodoroTimer.setCycleCount(Animation.INDEFINITE);
            pomodoroTimer.play();
        }
    }
    private void pausePomodoroTimer() {
        if (pomodoroTimer != null) {
            pomodoroTimer.pause();
            isPomodoroRunning = false;
        }
    }

    private void resetPomodoroTimer() {
        if (pomodoroTimer != null) {
            pomodoroTimer.stop();
        }
        pomodoroTimeLeft = 25 * 60; // Reset to 25 minutes
        updatePomodoroLabel();
    }

    private void updatePomodoroLabel() {
        int minutes = pomodoroTimeLeft / 60;
        int seconds = pomodoroTimeLeft % 60;
        pomodoroLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Enhanced ToDoTask Manager");

        connectToDatabase();
        showLoginScreen(primaryStage);
    }

    //a
    private void showLoginScreen(Stage primaryStage) {
        VBox loginLayout = new VBox(10);
        loginLayout.setPadding(new Insets(20));

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Button loginButton = new Button("Sign In");
        Button signUpButton = new Button("Sign Up");
        Label loginMessage = new Label();

        loginButton.setStyle("-fx-background-color: pink; -fx-text-fill: black; -fx-font-weight: bold;");
        signUpButton.setStyle("-fx-background-color: pink; -fx-text-fill: black; -fx-font-weight: bold;");

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (authenticateUser(username, password)) {
                loggedInUser = username;
                loginMessage.setText("Login successful!");
                showTaskManagerScreen(primaryStage);
            } else {
                loginMessage.setText("Invalid username or password.");
            }
        });

        signUpButton.setOnAction(e -> showSignUpScreen(primaryStage));

        loginLayout.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField, loginButton, signUpButton, loginMessage);

        primaryStage.setScene(new Scene(loginLayout, 300, 250));
        primaryStage.show();
    }
///Arnur
    private void showSignUpScreen(Stage primaryStage) {
        VBox signUpLayout = new VBox(10);
        signUpLayout.setPadding(new Insets(20));

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Button signUpButton = new Button("Create Account");
        Label signUpMessage = new Label();

        signUpButton.setStyle("-fx-background-color: pink; -fx-text-fill: black; -fx-font-weight: bold;");

        signUpButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (createUser(username, password)) {
                signUpMessage.setText("Account created successfully!");
                showLoginScreen(primaryStage);
            } else {
                signUpMessage.setText("Username already exists.");
            }
        });

        signUpLayout.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField, signUpButton, signUpMessage);

        primaryStage.setScene(new Scene(signUpLayout, 300, 250));
    }

    private void showTaskManagerScreen(Stage primaryStage) {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));

        pomodoroLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Button startButton = new Button("Start");
        Button pauseButton = new Button("Pause");
        Button resetButton = new Button("Reset");

        startButton.setStyle("-fx-background-color: pink; -fx-text-fill: black; -fx-font-weight: bold;");
        pauseButton.setStyle("-fx-background-color: pink; -fx-text-fill: black; -fx-font-weight: bold;");
        resetButton.setStyle("-fx-background-color: pink; -fx-text-fill: black; -fx-font-weight: bold;");

        startButton.setOnAction(e -> startPomodoroTimer());
        pauseButton.setOnAction(e -> pausePomodoroTimer());
        resetButton.setOnAction(e -> resetPomodoroTimer());

        HBox pomodoroControls = new HBox(10, pomodoroLabel, startButton, pauseButton, resetButton);
        pomodoroControls.setPadding(new Insets(10));

        setupDragAndDrop(toDoList, inProgressList, doneList);

        VBox toDoBox = createTaskSection("To Do", toDoList);
        VBox inProgressBox = createTaskSection("In Progress", inProgressList);
        VBox doneBox = createTaskSection("Done", doneList);

        HBox taskSections = new HBox(20, toDoBox, inProgressBox, doneBox);
        taskSections.setPadding(new Insets(20));

        mainLayout.getChildren().addAll(pomodoroControls, taskSections);

        primaryStage.setScene(new Scene(mainLayout, 1000, 600));
        primaryStage.show();
    }

    private VBox createTaskSection(String sectionTitle, ListView<String> taskList) {
        Label sectionLabel = new Label(sectionTitle);

        TextField taskInput = new TextField();
        taskInput.setPromptText("Enter task...");

        Button addButton = new Button("Add to " + sectionTitle);
        Button removeButton = new Button("Remove Selected");

        addButton.setStyle("-fx-background-color: pink; -fx-text-fill: black; -fx-font-weight: bold;");
        removeButton.setStyle("-fx-background-color: pink; -fx-text-fill: black; -fx-font-weight: bold;");

        addButton.setOnAction(e -> {
            String task = taskInput.getText();
            if (!task.isEmpty()) {
                taskList.getItems().add(task);
                taskInput.clear();
            }
        });

        removeButton.setOnAction(e -> {
            String selectedTask = taskList.getSelectionModel().getSelectedItem();
            if (selectedTask != null) {
                taskList.getItems().remove(selectedTask);
            }
        });

        VBox taskBox = new VBox(10, sectionLabel, taskList, taskInput, addButton, removeButton);
        taskBox.setPadding(new Insets(10));
        taskBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px;");
        return taskBox;
    }

///Zhakha
    private void setupDragAndDrop(ListView<String>... lists) {
        for (ListView<String> list : lists) {
            list.setOnDragDetected(event -> {
                if (!list.getSelectionModel().isEmpty()) {
                    String selected = list.getSelectionModel().getSelectedItem();
                    Dragboard dragboard = list.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(selected);
                    dragboard.setContent(content);
                    list.getItems().remove(selected);
                }
                event.consume();
            });

            list.setOnDragOver(event -> {
                if (event.getGestureSource() != list && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            list.setOnDragDropped(event -> {
                Dragboard dragboard = event.getDragboard();
                if (dragboard.hasString()) {
                    list.getItems().add(dragboard.getString());
                    event.setDropCompleted(true);
                } else {
                    event.setDropCompleted(false);
                }
                event.consume();
            });
        }
    }

    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_manager", "root", "Akbulak33");
            System.out.println("Connected to the database!");
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }

    private boolean authenticateUser(String username, String password) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Authentication failed: " + e.getMessage());
            return false;
        }
    }

    private boolean createUser(String username, String password) {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("User creation failed: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
