// src/HrrnScheduler.java
import java.util.ArrayList;
import java.util.List;

public class HrrnScheduler {
    public static Result run(List<Process> input) {
        List<Process> procs = SchedulerUtils.deepCopy(input);
        procs.sort((a,b)->Integer.compare(a.arrivalTime,b.arrivalTime));

        Result r = new Result();
        int now = 0;
        List<Process> remaining = new ArrayList<>(procs);
        String last = null;

        while (!remaining.isEmpty()) {
            Process best = null;
            double bestRR = -1.0;
            for (Process p : remaining) {
                if (p.arrivalTime <= now) {
                    int waiting = Math.max(0, now - p.arrivalTime);
                    double rr = (waiting + p.serviceTime) / (double) p.serviceTime;
                    if (rr > bestRR) {
                        bestRR = rr;
                        best = p;
                    }
                }
            }
            if (best == null) {
                int next = SchedulerUtils.nextArrivalTime(remaining, now);
                SchedulerUtils.addSegment(r, "idle", now, next);
                now = next;
                continue;
            }
            if (last != null && !last.equals(best.name)) r.contextSwitches++;
            best.startTime = now;
            SchedulerUtils.addSegment(r, best.name, now, now + best.serviceTime);
            now += best.serviceTime;
            SchedulerUtils.finalizeAndAdd(r, best, now);
            remaining.remove(best);
            last = best.name;
        }
        r.computeAverages();
        return r;
    }
}
