// Run() is called from Scheduling.main() and is where
// the scheduling algorithm written by the user resides.
// User modification should occur within the Run() function.

import java.util.Vector;
import java.io.*;

public class SchedulingAlgorithm {

  public static Results Run(int runtime, Vector<Process> processVector, Results result) {
    int i;
    int comptime = 0;
    int currentProcess = 0;
    int previousProcess;
    int size = processVector.size();
    int completed = 0;
    String resultsFile = "Summary-Processes";

    result.schedulingType = "Batch (Nonpreemptive)";
    result.schedulingName = "First-Come First-Served";

    try {
      //BufferedWriter out = new BufferedWriter(new FileWriter(resultsFile));
      //OutputStream out = new FileOutputStream(resultsFile);
      PrintStream out = new PrintStream(new FileOutputStream(resultsFile));
      Process process = processVector.elementAt(currentProcess);
      writeProcessState(currentProcess, out, process, "registered");

      while (comptime < runtime) {
        if (process.cpudone == process.cputime) {
          completed++;
          writeProcessState(currentProcess, out, process, "completed");

          if (completed == size) {
            result.compuTime = comptime;
            out.close();
            return result;
          }

          for (i = size - 1; i >= 0; i--) {
            process = processVector.elementAt(i);
            if (process.cpudone < process.cputime) { 
              currentProcess = i;
            }
          }

          process = processVector.elementAt(currentProcess);
          writeProcessState(currentProcess, out, process, "registered");
        }

        if (process.ioblocking == process.ionext) {
          writeProcessState(currentProcess, out, process, "I/O blocked");
          process.numblocked++;
          process.ionext = 0; 
          previousProcess = currentProcess;

          for (i = size - 1; i >= 0; i--) {
            process = processVector.elementAt(i);
            if (process.cpudone < process.cputime && previousProcess != i) { 
              currentProcess = i;
            }
          }

          process = processVector.elementAt(currentProcess);
          writeProcessState(currentProcess, out, process, "registered");
        }

        process.cpudone++;

        if (process.ioblocking > 0) {
          process.ionext++;
        }

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
    out.println("Process: " + currentProcess + " " + state + "... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.ionext + ")");
  }
}
