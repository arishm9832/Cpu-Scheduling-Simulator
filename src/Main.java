// src/Main.java
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main extends Application {

    // store last input for reuse in compare mode
    private static List<Process> baseProcesses = new ArrayList<>();
    private static List<Process> agingProcesses = new ArrayList<>();

    public static void main(String[] args) {
        // Pre-demo: ask user for processes (interactive CLI)
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter number of processes:");
        int n = sc.nextInt();
        baseProcesses.clear();
        for (int i = 0; i < n; i++) {
            System.out.printf("Process %d name (e.g., P1): ", i+1);
            String name = sc.next();
            System.out.printf("Arrival time: ");
            int at = sc.nextInt();
            System.out.printf("Service time: ");
            int st = sc.nextInt();
            baseProcesses.add(new Process(name, at, st));
        }
        // For Aging (if user wants), we copy base but allow priority override
        agingProcesses.clear();
        for (Process p : baseProcesses) {
            Process ap = p.clone();
            ap.priority = 0; // default; user can override later in UI
            agingProcesses.add(ap);
        }

        // Launch JavaFX UI
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Build a menu (simple) using dialog choices
        String[] algos = {"FCFS","RR","SPN","SRT","HRRN","FB-1","FB-2^i","Aging","Compare two algorithms","Exit"};
        ChoiceDialog<String> dialog = new ChoiceDialog<>(algos[0], algos);
        dialog.setTitle("Choose Algorithm");
        dialog.setHeaderText("CPU Scheduling Simulator");
        dialog.setContentText("Select algorithm:");

        dialog.showAndWait().ifPresent(choice -> {
            if (choice.equals("Exit")) { Platform.exit(); return; }
            try {
                if (choice.equals("Compare two algorithms")) {
                    // ask both algorithms and their params via simple dialogs
                    ChoiceDialog<String> d1 = new ChoiceDialog<>(algos[0], algos);
                    d1.setTitle("Compare - Algorithm A");
                    d1.setContentText("Select algorithm A:");
                    String a1 = d1.showAndWait().orElse(algos[0]);
                    ChoiceDialog<String> d2 = new ChoiceDialog<>(algos[1], algos);
                    d2.setTitle("Compare - Algorithm B");
                    d2.setContentText("Select algorithm B:");
                    String a2 = d2.showAndWait().orElse(algos[1]);

                    Map<String, Result> two = new LinkedHashMap<>();
                    two.put(a1, computeForChoice(a1));
                    two.put(a2, computeForChoice(a2));
                    VisualizerFX.showComparison(new Stage(), "Compare", two);
                } else {
                    Result res = computeForChoice(choice);
                    res.printStats(choice);
                    VisualizerFX.show(new Stage(), choice, res);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
    

    private Result computeForChoice(String choice) {
        // defaults
        int rrQ = 2;
        int agingQ = 1;
        int fbLevels = 5;

        // For simplicity we ask small dialogs for parameters when needed
        if (choice.startsWith("RR")) {
            TextInputDialog d = new TextInputDialog("2");
            d.setHeaderText("Round Robin Quantum");
            d.setContentText("Enter quantum:");
            String v = d.showAndWait().orElse("2");
            rrQ = Integer.parseInt(v);
            return RRScheduler.run(baseProcesses, rrQ);
        } else if (choice.startsWith("FB-1")) {
            TextInputDialog d = new TextInputDialog("5");
            d.setHeaderText("Feedback Levels");
            d.setContentText("Enter number of levels:");
            fbLevels = Integer.parseInt(d.showAndWait().orElse("5"));
            return FeedbackScheduler.run(baseProcesses, fbLevels, "FB1");
        } else if (choice.startsWith("FB-2")) {
            TextInputDialog d = new TextInputDialog("5");
            d.setHeaderText("Feedback Levels");
            d.setContentText("Enter number of levels:");
            fbLevels = Integer.parseInt(d.showAndWait().orElse("5"));
            return FeedbackScheduler.run(baseProcesses, fbLevels, "FB2I");
        } else if (choice.startsWith("Aging")) {
            TextInputDialog d = new TextInputDialog("1");
            d.setHeaderText("Aging Quantum");
            d.setContentText("Enter quantum:");
            agingQ = Integer.parseInt(d.showAndWait().orElse("1"));
            // use agingProcesses (copy of base); optionally prompt for priorities
            // For simplicity we use baseProcesses but with default priorities
            return AgingScheduler.run(agingProcesses, agingQ);
        } else if (choice.equals("FCFS")) {
            return FcfsScheduler.run(baseProcesses);
        } else if (choice.equals("SPN")) {
            return SpnScheduler.run(baseProcesses);
        } else if (choice.equals("SRT")) {
            return SrtScheduler.run(baseProcesses);
        } else if (choice.equals("HRRN")) {
            return HrrnScheduler.run(baseProcesses);
        }
        // fallback
        return FcfsScheduler.run(baseProcesses);
    }
}
