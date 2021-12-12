package lab2;

public class Process {
  public int cputime;
  public int ioblocking;
  public int cpudone;
  public int ionext;
  public int numblocked;

  public Process(int cputime, int ioblocking, int cpudone, int ionext, int numblocked) {
    // CPU (burst) time for process to be completed
    this.cputime = cputime;

    // delay before process blocking for I/O
    this.ioblocking = ioblocking;

    // how long the process has been executed
    this.cpudone = cpudone;

    // how long the process has been executed before it is gone to I/O blocking" state
    this.ionext = ionext;

    // how many times the process has gone to "I/O blocking" state
    this.numblocked = numblocked;
  } 	
}
