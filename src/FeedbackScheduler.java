// src/FeedbackScheduler.java
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class FeedbackScheduler {
    public static Result run(List<Process> input, int levels, String mode) {
        List<Process> procs = SchedulerUtils.deepCopy(input);
        procs.sort((a,b)->Integer.compare(a.arrivalTime,b.arrivalTime));

        Result r = new Result();
        @SuppressWarnings("unchecked")
        Deque<Process>[] queues = new Deque[levels];
        for (int i=0;i<levels;i++) queues[i] = new ArrayDeque<>();

        int now = 0, i = 0;
        String last = null;

        while (true) {
            while (i < procs.size() && procs.get(i).arrivalTime <= now) {
                queues[0].addLast(procs.get(i)); i++;
            }
            int qlvl = -1;
            for (int q = 0; q < levels; q++) if (!queues[q].isEmpty()) { qlvl = q; break; }
            if (qlvl == -1) {
                if (i >= procs.size()) break;
                int next = procs.get(i).arrivalTime;
                SchedulerUtils.addSegment(r, "idle", now, next);
                now = next;
                continue;
            }
            Process p = queues[qlvl].pollFirst();
            if (last != null && !last.equals(p.name)) r.contextSwitches++;
            if (p.startTime < 0) p.startTime = now;

            int quantum = 1;
            if ("FB2I".equalsIgnoreCase(mode)) quantum = (int) Math.pow(2, qlvl);
            int exec = Math.min(quantum, p.remainingTime);
            int endPlanned = now + exec;

            while (i < procs.size() && procs.get(i).arrivalTime < endPlanned) {
                int slice = procs.get(i).arrivalTime - now;
                if (slice > 0) {
                    SchedulerUtils.addSegment(r, p.name, now, now + slice);
                    now += slice;
                    p.remainingTime -= slice;
                    exec -= slice;
                }
                queues[0].addLast(procs.get(i)); i++;
                endPlanned = now + exec;
            }

            if (exec > 0) {
                SchedulerUtils.addSegment(r, p.name, now, now + exec);
                now += exec;
                p.remainingTime -= exec;
            }

            if (p.remainingTime == 0) SchedulerUtils.finalizeAndAdd(r, p, now);
            else {
                int nextLevel = Math.min(levels - 1, qlvl + 1);
                queues[nextLevel].addLast(p);
            }
            last = p.name;
        }
        r.computeAverages();
        return r;
    }
}
