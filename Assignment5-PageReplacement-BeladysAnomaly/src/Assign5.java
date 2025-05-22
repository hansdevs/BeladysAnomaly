import java.util.*;
import java.util.concurrent.*;

public class Assign5 {

    private static final int NUM_SIMULATIONS    = 1_000;
    private static final int SEQUENCE_LENGTH    = 1_000;
    private static final int MAX_PAGE_REFERENCE = 250;
    private static final int MAX_MEMORY_FRAMES  = 100;
    private static final long RNG_SEED          = 0L;

    private static final class AnomalyStats {
        int count, maxDiff;
        final StringBuilder details = new StringBuilder();
    }

    public static void main(String[] args) {

        long start = System.currentTimeMillis();
        ExecutorService pool = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors());

        long fifoWins = 0, lruWins = 0, mruWins = 0;
        AnomalyStats fifoAnom = new AnomalyStats();
        AnomalyStats lruAnom  = new AnomalyStats();
        AnomalyStats mruAnom  = new AnomalyStats();

        Random rng = new Random(RNG_SEED);

        try {
            for (int sim = 0; sim < NUM_SIMULATIONS; sim++) {

                int[] sequence = new int[SEQUENCE_LENGTH];
                for (int i = 0; i < SEQUENCE_LENGTH; i++)
                    sequence[i] = 1 + rng.nextInt(MAX_PAGE_REFERENCE);

                int[] fifoPF = new int[MAX_MEMORY_FRAMES + 1];
                int[] lruPF  = new int[MAX_MEMORY_FRAMES + 1];
                int[] mruPF  = new int[MAX_MEMORY_FRAMES + 1];

                List<Future<?>> futures = new ArrayList<>(MAX_MEMORY_FRAMES * 3);

                for (int frames = 1; frames <= MAX_MEMORY_FRAMES; frames++) {
                    futures.add(pool.submit(new TaskFIFO(sequence, frames,
                            MAX_PAGE_REFERENCE, fifoPF)));
                    futures.add(pool.submit(new TaskLRU(sequence, frames,
                            MAX_PAGE_REFERENCE, lruPF)));
                    futures.add(pool.submit(new TaskMRU(sequence, frames,
                            MAX_PAGE_REFERENCE, mruPF)));
                }
                for (Future<?> f : futures) f.get();

                for (int frames = 1; frames <= MAX_MEMORY_FRAMES; frames++) {

                    int f = fifoPF[frames];
                    int l = lruPF[frames];
                    int m = mruPF[frames];
                    int min = Math.min(f, Math.min(l, m));

                    if (f == min) fifoWins++;
                    if (l == min) lruWins++;
                    if (m == min) mruWins++;

                    if (frames > 1) {
                        checkAnomaly(fifoPF[frames - 1], f, fifoAnom);
                        checkAnomaly(lruPF[frames - 1],  l, lruAnom);
                        checkAnomaly(mruPF[frames - 1],  m, mruAnom);
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }

        long elapsed = System.currentTimeMillis() - start;

        System.out.printf("Simulation took %d ms%n%n", elapsed);
        System.out.printf("FIFO min PF : %d%n", fifoWins);
        System.out.printf("LRU  min PF : %d%n", lruWins);
        System.out.printf("MRU  min PF : %d%n%n", mruWins);

        printAnomalyReport("FIFO", fifoAnom);
        printAnomalyReport("LRU",  lruAnom);
        printAnomalyReport("MRU",  mruAnom);
    }

    private static void checkAnomaly(int prev, int curr, AnomalyStats s) {
        if (curr > prev) {
            int diff = curr - prev;
            s.count++;
            s.maxDiff = Math.max(s.maxDiff, diff);
            s.details.append(String.format(
                    "\tdetected - Previous %d : Current %d (%d)%n", prev, curr, diff));
        }
    }

    private static void printAnomalyReport(String name, AnomalyStats s) {
        System.out.printf("Belady's Anomaly Report for %s%n", name);
        if (s.count > 0) System.out.print(s.details);
        System.out.printf("\t Anomaly detected %d times with a max difference of %d%n%n",
                s.count, s.maxDiff);
    }
}
