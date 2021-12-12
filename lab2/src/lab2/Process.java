package lab2;

public class Process implements Comparable<Process> {
  public int cpuTime;
  public int ioBlocking;
  public int cpuDone;
  public int ioNext;
  public int numBlocked;

  public Process(int cpuTime, int ioBlocking, int cpuDone, int ioNext, int numBlocked) {
    // CPU (burst) time for process to be completed
    this.cpuTime = cpuTime;

    // delay before process blocking for I/O
    this.ioBlocking = ioBlocking;

    // how long the process has been executed
    this.cpuDone = cpuDone;

    // how long the process has been executed before it is gone to I/O blocking" state
    this.ioNext = ioNext;

    // how many times the process has gone to "I/O blocking" state
    this.numBlocked = numBlocked;
  }

  public boolean isDone() {
    return cpuDone >= cpuTime;
  }

  @Override
  public int compareTo(Process other) {
    // cpuTime is considered as job time
    if (this.cpuTime == other.cpuTime) {
      return Integer.compare(this.ioBlocking, other.ioBlocking);
    }

    return Integer.compare(this.cpuTime, other.cpuTime);
  }
}
