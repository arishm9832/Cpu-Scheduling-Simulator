// src/FcfsScheduler.java
import java.util.List;

public class FcfsScheduler {
    public static Result run(List<Process> input) {
        List<Process> procs = SchedulerUtils.deepCopy(input);
        SchedulerUtils.sortByArrivalThenName(procs);

        Result r = new Result();
        int now = 0;
        String last = null;
        for (Process p : procs) {
            if (now < p.arrivalTime) {
                SchedulerUtils.addSegment(r, "idle", now, p.arrivalTime);
                now = p.arrivalTime;
            }
            if (last != null && !last.equals(p.name)) r.contextSwitches++;
            p.startTime = now;
            SchedulerUtils.addSegment(r, p.name, now, now + p.serviceTime);
            now += p.serviceTime;
            SchedulerUtils.finalizeAndAdd(r, p, now);
            last = p.name;
        }
        r.computeAverages();
        return r;
    }
}
