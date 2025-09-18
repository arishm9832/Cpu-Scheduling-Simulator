// src/SchedulerUtils.java
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SchedulerUtils {

    public static List<Process> deepCopy(List<Process> src) {
        List<Process> dst = new ArrayList<>();
        for (Process p : src) dst.add(p.clone());
        return dst;
    }

    public static void finalizeAndAdd(Result r, Process p, int now) {
        p.completionTime = now;
        p.turnaroundTime = p.completionTime - p.arrivalTime;
        p.waitingTime = p.turnaroundTime - p.serviceTime;
        if (p.startTime >= 0) p.responseTime = p.startTime - p.arrivalTime;
        r.finished.add(p);
    }

    public static void addSegment(Result r, String name, int start, int end) {
        if (start == end) return;
        int m = r.names.size();
        if (m > 0 && r.names.get(m - 1).equals(name) && r.ends.get(m - 1) == start) {
            r.ends.set(m - 1, end);
        } else {
            r.names.add(name);
            r.starts.add(start);
            r.ends.add(end);
        }
    }

    public static int nextArrivalTime(List<Process> procs, int after) {
        return procs.stream()
                .filter(p -> p.arrivalTime > after)
                .map(p -> p.arrivalTime)
                .min(Integer::compareTo)
                .orElse(after);
    }

    public static void sortByArrivalThenName(List<Process> ps) {
        ps.sort(Comparator.<Process>comparingInt(p -> p.arrivalTime)
                .thenComparing(p -> p.name));
    }
}
