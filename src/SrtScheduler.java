// src/SrtScheduler.java
import java.util.List;

public class SrtScheduler {
    public static Result run(List<Process> input) {
        List<Process> procs = SchedulerUtils.deepCopy(input);
        procs.sort((a,b)->Integer.compare(a.arrivalTime,b.arrivalTime));
        Result r = new Result();

        int now = 0;
        int completed = 0;
        int n = procs.size();
        String last = null;

        while (completed < n) {
            Process best = null;
            for (Process p : procs) {
                if (p.remainingTime > 0 && p.arrivalTime <= now) {
                    if (best == null || p.remainingTime < best.remainingTime ||
                        (p.remainingTime == best.remainingTime && p.arrivalTime < best.arrivalTime)) {
                        best = p;
                    }
                }
            }
            if (best == null) {
                int next = SchedulerUtils.nextArrivalTime(procs, now);
                SchedulerUtils.addSegment(r, "idle", now, next);
                now = next;
                continue;
            }
            if (last != null && !last.equals(best.name) && !last.equals("idle")) r.contextSwitches++;
            if (best.startTime < 0) best.startTime = now;
            SchedulerUtils.addSegment(r, best.name, now, now + 1);
            now += 1;
            best.remainingTime -= 1;
            if (best.remainingTime == 0) {
                SchedulerUtils.finalizeAndAdd(r, best, now);
                completed++;
            }
            last = best.name;
        }
        r.computeAverages();
        return r;
    }
}
