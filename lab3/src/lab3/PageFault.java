/* It is in this file, specifically the replacePage function that will
   be called by MemoryManagement when there is a page fault.  The 
   users of this program should rewrite PageFault to implement the 
   page replacement algorithm.
*/

// This PageFault file is an example of the FIFO Page Replacement
// Algorithm as described in the Memory Management section.
package lab3;

import java.util.*;

public class PageFault {
  private static int clockHand = 0;

  // clock algorithm
  public static void replacePage ( Vector<Page> memory, int virtPageCount, int replacingPageIdx, ControlPanel controlPanel ) {
    int evictedPageIdx;

    while (true) {
      Page page = memory.get(clockHand);

      if (page.physical != -1) {
        if (page.R == 1) {
          page.R = 0;
        } else {
          evictedPageIdx = clockHand;
          break;
        }
      }

      clockHand++;
      if (clockHand == virtPageCount) {
        clockHand = 0;
      }
    }

    Page evictedPage = memory.get(evictedPageIdx);
    Page replacingPage = memory.get(replacingPageIdx);

    replacingPage.physical = evictedPage.physical;
    evictedPage.inMemTime = 0;
    evictedPage.lastTouchTime = 0;
    evictedPage.R = 0;
    evictedPage.M = 0;
    evictedPage.physical = -1;

    controlPanel.removePhysicalPage(evictedPageIdx);
    controlPanel.addPhysicalPage(replacingPage.physical, replacingPageIdx);
  }
}
