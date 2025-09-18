// src/Process.java
public class Process implements Cloneable {
    public String name;
    public int arrivalTime;
    public int serviceTime;    // burst time
    public int priority;       // for aging input if used
    public int remainingTime;

    // stats
    public int startTime = -1;
    public int completionTime = 0;
    public int turnaroundTime = 0;
    public int waitingTime = 0;
    public int responseTime = 0;

    public Process(String name, int arrivalTime, int serviceTime) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.serviceTime = serviceTime;
        this.remainingTime = serviceTime;
        this.priority = 0;
    }

    public static Process fromAging(String name, int arrivalTime, int priority) {
        Process p = new Process(name, arrivalTime, 0);
        p.priority = priority;
        return p;
    }

    @Override
    public Process clone() {
        Process p = new Process(this.name, this.arrivalTime, this.serviceTime);
        p.priority = this.priority;
        p.remainingTime = this.remainingTime;
        p.startTime = this.startTime;
        p.completionTime = this.completionTime;
        p.turnaroundTime = this.turnaroundTime;
        p.waitingTime = this.waitingTime;
        p.responseTime = this.responseTime;
        return p;
    }
}
