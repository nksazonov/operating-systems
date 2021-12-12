package lab2;
// Run() is called from Scheduling.main() and is where
// the scheduling algorithm written by the user resides.
// User modification should occur within the Run() function.

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

public class SchedulingAlgorithm {

  public static Results Run(int runtime, final Vector<Process> processVector, Results result) {
    int i;
    int comptime = 0;
    int currProcessIdx = 0;
    int prevProcessIdx;
    int processNum = processVector.size();
    int completedProcessNum = 0;
    String resultsFile = "Summary-Processes.txt";

    result.schedulingType = "Batch (Nonpreemptive)";
    result.schedulingName = "Shortest Job First (Static)";

    try {
      PrintStream out = new PrintStream(new FileOutputStream(resultsFile));

      // sorting processes by cpuTime in ascending order
      processVector.sort(Process::compareTo);

      Process process = processVector.elementAt(currProcessIdx);
      writeProcessState(currProcessIdx, out, process, "registered");

      // execute processes until "runtime" exceeded
      while (comptime < runtime) {
        // current process execution ended
        if (process.cpuDone == process.cpuTime) {
          completedProcessNum++;
          writeProcessState(currProcessIdx, out, process, "completed");

          // current process was the last process to execute
          if (completedProcessNum == processNum) {
            result.compuTime = comptime;
            out.close();
            return result;
          }

          // pick next process
          for (i = processNum - 1; i >= 0; i--) {
            process = processVector.elementAt(i);
            if (!process.isDone()) {
              currProcessIdx = i;
            }
          }
          process = processVector.elementAt(currProcessIdx);
          writeProcessState(currProcessIdx, out, process, "registered");
        }

        // current process will go to "blocked" state
        if (process.ioBlocking == process.ioNext) {
          writeProcessState(currProcessIdx, out, process, "I/O blocked");
          process.numBlocked++;
          process.ioNext = 0;
          prevProcessIdx = currProcessIdx;

          // pick next process
          for (i = processNum - 1; i >= 0; i--) {
            process = processVector.elementAt(i);
            if (!process.isDone() && prevProcessIdx != i) {
              currProcessIdx = i;
            }
          }
          process = processVector.elementAt(currProcessIdx);
          writeProcessState(currProcessIdx, out, process, "registered");
        }

        // mark that process is being executed
        process.cpuDone++;

        // mark for how long current process is being executed to know when to go to "I/O Blocking state"
        if (process.ioBlocking > 0) {
          process.ioNext++;
        }

        // mark that process pool is being executed
        comptime++;
      }

      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    result.compuTime = comptime;
    return result;
  }

  private static void writeProcessState(int currentProcess, PrintStream out, Process process, String state) {
    out.println("Process: " + currentProcess + " " + state + "... (" + process.cpuTime + " " + process.ioBlocking + " " + process.cpuDone + " " + process.ioNext + ")");
  }
}
