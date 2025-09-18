// src/Result.java
import java.util.ArrayList;
import java.util.List;

public class Result {
    public List<String> names = new ArrayList<>();
    public List<Integer> starts = new ArrayList<>();
    public List<Integer> ends = new ArrayList<>();
    public List<Process> finished = new ArrayList<>();

    public double avgWT, avgTAT, avgRT;
    public int contextSwitches = 0;
    public boolean starvation = false;

    // Compute averages for waiting time, turnaround time, response time
    public void computeAverages() {
        int n = finished.size();
        if (n == 0) {
            avgWT = avgTAT = avgRT = 0;
            return;
        }
        long swt = 0, stat = 0, srt = 0;
        for (Process p : finished) {
            swt += p.waitingTime;
            stat += p.turnaroundTime;
            srt += p.responseTime;
        }
        avgWT = swt * 1.0 / n;
        avgTAT = stat * 1.0 / n;
        avgRT = srt * 1.0 / n;
    }

    // Print statistics with algorithm title
    public void printStats(String title) {
        computeAverages(); // ensure averages are up-to-date
        System.out.println("=====================================");
        System.out.println("Algorithm: " + title);
        System.out.println("-------------------------------------");
        System.out.printf("%-5s %-5s %-5s %-5s\n", "PID", "WT", "TAT", "RT");
        for (Process p : finished) {
            System.out.printf("%-5s %-5d %-5d %-5d\n", p.name, p.waitingTime, p.turnaroundTime, p.responseTime);
        }
        System.out.println("-------------------------------------");
        System.out.printf("Average Waiting Time   : %.2f\n", avgWT);
        System.out.printf("Average Turnaround Time: %.2f\n", avgTAT);
        System.out.printf("Average Response Time  : %.2f\n", avgRT);
        System.out.println("Context Switches       : " + contextSwitches);
        System.out.println("Starvation Occurred    : " + (starvation ? "Yes" : "No"));
        System.out.println("=====================================\n");
    }
}
