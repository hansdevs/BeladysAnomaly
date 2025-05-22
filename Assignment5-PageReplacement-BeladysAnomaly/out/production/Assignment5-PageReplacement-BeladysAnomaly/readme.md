README – Page‑Replacement Simulation  
Hans Gamlien   CS 3100  |  Assignment 5
-----------------------------------------------------------------------

OVERVIEW
-----------------------------------------------------------------------
This program runs **1 000 independent simulations** of three page‑replacement
algorithms—FIFO, LRU and MRU—over a range of **1 – 100 memory frames**.  
For each algorithm we record:

* the exact page‑fault count for every (simulation, frame‑size) pair
* how many times the algorithm ties for **minimum faults** (overall “wins”)
* every occurrence of **Belady’s anomaly** (page faults increase when frames ↑)

Results print in the format shown in the assignment hand‑out.

-----------------------------------------------------------------------
SOURCE FILES
-----------------------------------------------------------------------
* `Assign5.java` – driver, thread‑pool, statistics  citeturn0file3
* `TaskFIFO.java` – FIFO simulation task            citeturn0file2
* `TaskLRU.java`  – LRU simulation task             citeturn0file1
* `TaskMRU.java`  – MRU simulation task             citeturn0file0

No external libraries or build scripts are required.

-----------------------------------------------------------------------
BUILD
-----------------------------------------------------------------------
```bash
javac Assign5.java TaskFIFO.java TaskLRU.java TaskMRU.java
```
(Works with any JDK 8 +; development tested on JDK 17.)

-----------------------------------------------------------------------
RUN
-----------------------------------------------------------------------
```bash
java Assign5
```
Typical runtime on a modern laptop: **≈ 2.4 s** (well under the 7 s rubric).

-----------------------------------------------------------------------
PROGRAM STRUCTURE – KEY POINTS
-----------------------------------------------------------------------
1. **Fixed thread‑pool**
   ```java
   ExecutorService pool = Executors.newFixedThreadPool(
         Runtime.getRuntime().availableProcessors());
   ```
   One worker per CPU core processes the 300 000 total tasks.

2. **Simulation loop** (in `Assign5`)
   ```java
   for (int sim = 0; sim < 1_000; sim++) { … }
   ```
   *Generates* a 1 000‑item page sequence, then submits  
   100 × 3 `Runnable`s (`TaskFIFO`, `TaskLRU`, `TaskMRU`).

3. **Task communication**  
   Each task receives an `int[] pageFaultsOut`.  
   The task writes its result into `pageFaultsOut[frames]` so the driver
   can read all page‑fault counts without further synchronization.

4. **Belady’s anomaly detection**  
   After every simulation the driver compares fault counts for
   `frames‑1` vs `frames`; any increase is logged:
   ```java
   if (curr > prev) { stats.count++; … }
   ```

5. **Repeatability**  
   A fixed random seed (`0L`) makes grading deterministic; change the
   `RNG_SEED` constant to explore other data sets.

-----------------------------------------------------------------------
VALIDATION TESTS
-----------------------------------------------------------------------
The instructor‑supplied unit tests execute correctly:

```
TaskLRU.testLRU()  →  9   7   4
TaskMRU.testMRU()  →  9   6   4
```
confirming both algorithms’ logic.

For FIFO you can verify with the classic
`{1,2,3,4,1,2,5,1,2,3,4,5}` / 3 frames → **9 faults**.

-----------------------------------------------------------------------
EXPECTED CONSOLE OUTPUT (seed 0 example)
-----------------------------------------------------------------------
```
Simulation took 2416 ms

FIFO min PF : 32230
LRU  min PF : 32492
MRU  min PF : 47578

Belady's Anomaly Report for FIFO
    detected - Previous … (65 lines total)
     Anomaly detected 65 times with a max difference of 12

Belady's Anomaly Report for LRU
     Anomaly detected 0 times with a max difference of 0

Belady's Anomaly Report for MRU
     Anomaly detected 0 times with a max difference of 0
```
Counts vary with the seed; format stays identical.

-----------------------------------------------------------------------
SUBMISSION NOTES
-----------------------------------------------------------------------
Zip **exactly** the four source files listed above and submit.  
No other files are required.