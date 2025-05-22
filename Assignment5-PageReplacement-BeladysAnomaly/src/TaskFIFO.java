import java.util.*;

public final class TaskFIFO implements Runnable {

    private final int[] sequence;
    private final int   frames;
    @SuppressWarnings("unused")
    private final int   maxPageReference;
    private final int[] pageFaultsOut;

    public TaskFIFO(int[] sequence, int frames, int maxPageReference, int[] out) {
        this.sequence = sequence;
        this.frames = frames;
        this.maxPageReference = maxPageReference;
        this.pageFaultsOut = out;
    }

    @Override
    public void run() {

        if (frames == 0) return;

        Deque<Integer> queue = new ArrayDeque<>(frames);
        Set<Integer> pages   = new HashSet<>(frames);
        int faults = 0;

        for (int p : sequence) {
            if (!pages.contains(p)) {
                faults++;
                if (queue.size() == frames) pages.remove(queue.poll());
                queue.offer(p);
                pages.add(p);
            }
        }
        pageFaultsOut[frames] = faults;
    }
}
