// src/RRScheduler.java
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class RRScheduler {
    public static Result run(List<Process> input, int quantum) {
        List<Process> procs = SchedulerUtils.deepCopy(input);
        procs.sort((a,b) -> Integer.compare(a.arrivalTime, b.arrivalTime));

        Result r = new Result();
        Queue<Process> q = new ArrayDeque<>();
        int now = 0, i = 0;
        String last = null;

        while (i < procs.size() || !q.isEmpty()) {
            while (i < procs.size() && procs.get(i).arrivalTime <= now) q.add(procs.get(i++));
            if (q.isEmpty()) {
                int next = (i < procs.size()) ? procs.get(i).arrivalTime : now;
                SchedulerUtils.addSegment(r, "idle", now, next);
                now = next;
                continue;
            }
            Process p = q.poll();
            if (last != null && !last.equals(p.name)) r.contextSwitches++;
            if (p.startTime < 0) p.startTime = now;
            int exec = Math.min(quantum, p.remainingTime);
            SchedulerUtils.addSegment(r, p.name, now, now + exec);
            now += exec;
            p.remainingTime -= exec;
            while (i < procs.size() && procs.get(i).arrivalTime <= now) q.add(procs.get(i++));
            if (p.remainingTime > 0) q.add(p);
            else SchedulerUtils.finalizeAndAdd(r, p, now);
            last = p.name;
        }
        r.computeAverages();
        return r;
    }
}
