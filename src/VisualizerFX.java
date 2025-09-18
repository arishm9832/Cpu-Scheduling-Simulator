// src/VisualizerFX.java
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class VisualizerFX {

    public static void show(Stage stage, String title, Result result) {
        Platform.runLater(() -> {
            stage.setTitle("CPU Scheduling - " + title);
            VBox root = new VBox(8);
            root.setPadding(new Insets(10));

            // top controls
            HBox controls = new HBox(8);
            Label speedLbl = new Label("Speed (ms):");
            Slider speed = new Slider(50, 2000, 700);
            speed.setShowTickLabels(false);
            speed.setShowTickMarks(false);

            Button play = new Button("Play");
            Button step = new Button("Step");
            Button reset = new Button("Reset");

            controls.getChildren().addAll(speedLbl, speed, play, step, reset);

            // canvas area for Gantt
            Canvas canvas = new Canvas(1000, 220);
            GraphicsContext gc = canvas.getGraphicsContext2D();

            // statistics area
            TextArea stats = new TextArea();
            stats.setEditable(false);
            stats.setPrefRowCount(6);

            root.getChildren().addAll(controls, canvas, stats);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setWidth(1100);
            stage.setHeight(420);
            stage.show();

            // prepare drawing data
            List<String> names = result.names;
            List<Integer> starts = result.starts;
            List<Integer> ends = result.ends;

            // compute canvas scale
            int maxTime = 0;
            for (int e : ends) if (e > maxTime) maxTime = e;
            int scale = Math.max(10, 900 / Math.max(1, maxTime)); // px per time unit
            int y = 40, h = 40;

            // colors
            Color[] colors = new Color[names.size()];
            Random rnd = new Random(42);
            for (int i = 0; i < colors.length; i++)
                colors[i] = Color.rgb(rnd.nextInt(180), rnd.nextInt(180), rnd.nextInt(180));

            // animation timeline
            Timeline timeline = new Timeline();
            timeline.setCycleCount(Timeline.INDEFINITE);
            final int[] idx = {0};

            KeyFrame frame = new KeyFrame(Duration.millis(speed.getValue()), e -> {
                drawSegments(gc, names, starts, ends, colors, scale, y, h, idx[0]);
                idx[0]++;
                if (idx[0] > names.size()) {
                    timeline.stop();
                }
            });
            timeline.getKeyFrames().add(frame);

            // update animation speed live
            speed.valueProperty().addListener((obs, oldV, newV) -> {
                timeline.stop();
                if (!timeline.getKeyFrames().isEmpty()) {
                    timeline.getKeyFrames().set(0, new KeyFrame(Duration.millis(newV.doubleValue()), frame.getOnFinished()));
                }
            });

            play.setOnAction(a -> {
                timeline.stop();
                timeline.getKeyFrames().set(0, new KeyFrame(Duration.millis(speed.getValue()), frame.getOnFinished()));
                timeline.play();
            });

            step.setOnAction(a -> {
                if (idx[0] <= names.size()) {
                    drawSegments(gc, names, starts, ends, colors, scale, y, h, idx[0]);
                    idx[0]++;
                }
            });

            reset.setOnAction(a -> {
                idx[0] = 0;
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            });

            // show stats
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Avg TAT: %.2f\nAvg WT: %.2f\nAvg RT: %.2f\nContext Switches: %d\n",
                    result.avgTAT, result.avgWT, result.avgRT, result.contextSwitches));
            stats.setText(sb.toString());
        });
    }

    private static void drawSegments(GraphicsContext gc, List<String> names, List<Integer> starts,
                                     List<Integer> ends, Color[] colors, int scale, int y, int h, int upto) {
        gc.clearRect(0, 0, 1000, 220);
        int x = 50;
        for (int i = 0; i < Math.min(upto, names.size()); i++) {
            int w = Math.max(1, (ends.get(i) - starts.get(i)) * scale);
            gc.setFill(colors[i % colors.length]);
            gc.fillRect(x, y, w, h);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(x, y, w, h);
            gc.setFill(Color.BLACK);
            gc.fillText(names.get(i), x + Math.max(2, w / 2) - 6, y + 25);
            gc.fillText(String.valueOf(starts.get(i)), x, y + h + 12);
            x += w;
            gc.fillText(String.valueOf(ends.get(i)), x, y + h + 12);
        }
    }

    public static void showComparison(Stage stage, String title, Map<String, Result> two) {
        Platform.runLater(() -> {
            stage.setTitle("Comparison - " + title);
            HBox root = new HBox(8);
            root.setPadding(new Insets(10));

            VBox left = new VBox(6);
            VBox right = new VBox(6);

            // Left display
            Result r1 = two.values().stream().toList().get(0);
            Result r2 = two.values().stream().toList().get(1);

            Canvas c1 = new Canvas(520, 180);
            Canvas c2 = new Canvas(520, 180);
            drawStatic(c1.getGraphicsContext2D(), r1);
            drawStatic(c2.getGraphicsContext2D(), r2);

            TextArea stats = new TextArea();
            stats.setEditable(false);
            stats.setPrefRowCount(8);

            StringBuilder sb = new StringBuilder();
            sb.append("Algorithm A stats:\n");
            sb.append(String.format("Avg TAT: %.2f  Avg WT: %.2f  Avg RT: %.2f  CS: %d\n\n",
                    r1.avgTAT, r1.avgWT, r1.avgRT, r1.contextSwitches));
            sb.append("Algorithm B stats:\n");
            sb.append(String.format("Avg TAT: %.2f  Avg WT: %.2f  Avg RT: %.2f  CS: %d\n\n",
                    r2.avgTAT, r2.avgWT, r2.avgRT, r2.contextSwitches));

            // Simple judgment
            sb.append("Summary / Recommendation:\n");
            if (r1.avgWT < r2.avgWT) sb.append("- A has lower average waiting time.\n"); else sb.append("- B has lower average waiting time.\n");
            if (r1.contextSwitches > r2.contextSwitches) sb.append("- A has more context switches (more overhead).\n");
            // starvation check
            boolean s1 = checkStarvation(r1), s2 = checkStarvation(r2);
            sb.append(String.format("- Starvation: A=%b, B=%b\n", s1, s2));
            stats.setText(sb.toString());

            left.getChildren().addAll(new Label("Algorithm A"), c1);
            right.getChildren().addAll(new Label("Algorithm B"), c2);

            VBox mid = new VBox(6, new Label("Comparison"), stats);
            root.getChildren().addAll(left, mid, right);

            Scene sc = new Scene(root);
            stage.setScene(sc);
            stage.setWidth(1200);
            stage.setHeight(380);
            stage.show();
        });
    }

    private static void drawStatic(javafx.scene.canvas.GraphicsContext gc, Result r) {
        gc.clearRect(0,0,520,180);
        List<String> names = r.names;
        List<Integer> starts = r.starts;
        List<Integer> ends = r.ends;
        int max = 0;
        for (int e : ends) if (e > max) max = e;
        int scale = Math.max(6, 480 / Math.max(1, max));
        Color[] colors = new Color[names.size()];
        Random rnd = new Random(42);
        for (int i=0;i<colors.length;i++) colors[i] = Color.rgb(rnd.nextInt(200), rnd.nextInt(200), rnd.nextInt(200));
        int x = 20, y = 40, h = 30;
        for (int i=0;i<names.size(); i++) {
            int w = Math.max(1, (ends.get(i) - starts.get(i))*scale);
            gc.setFill(colors[i%colors.length]);
            gc.fillRect(x,y,w,h);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(x,y,w,h);
            gc.setFill(Color.BLACK);
            gc.fillText(names.get(i), x + Math.max(2, w/2)-6, y+18);
            x += w;
        }
    }

    private static boolean checkStarvation(Result r) {
        // crude check: any process waiting > 3 * avg waiting
        double avg = r.avgWT;
        for (Process p : r.finished) {
            if (p.waitingTime > 3 * avg) return true;
        }
        return false;
    }
}
