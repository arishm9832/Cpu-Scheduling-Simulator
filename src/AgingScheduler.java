// src/AgingScheduler.java
import java.util.List;

public class AgingScheduler {
    public static Result run(List<Process> input, int quantum) {
        List<Process> procs = SchedulerUtils.deepCopy(input);
        for (Process p : procs) {
            if (p.serviceTime <= 0) { p.serviceTime = Math.max(1, p.priority + 3); p.remainingTime = p.serviceTime; }
        }
        procs.sort((a,b)->Integer.compare(a.arrivalTime,b.arrivalTime));
        Result r = new Result();
        int now = 0, completed = 0, n = procs.size();
        String last = null;
        while (completed < n) {
            Process best = null;
            for (Process p : procs) {
                if (p.remainingTime > 0 && p.arrivalTime <= now) {
                    if (best == null || p.priority > best.priority ||
                       (p.priority == best.priority && p.arrivalTime < best.arrivalTime)) {
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
            if (last != null && !last.equals(best.name)) r.contextSwitches++;
            if (best.startTime < 0) best.startTime = now;
            int exec = Math.min(quantum, best.remainingTime);
            SchedulerUtils.addSegment(r, best.name, now, now + exec);
            // age others
            for (Process p : procs) {
                if (p != best && p.remainingTime > 0 && p.arrivalTime <= now) p.priority += exec;
            }
            now += exec;
            best.remainingTime -= exec;
            if (best.remainingTime == 0) { SchedulerUtils.finalizeAndAdd(r, best, now); completed++; }
            last = best.name;
        }
        r.computeAverages();
        return r;
    }
}
