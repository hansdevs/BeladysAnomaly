import java.util.*;

public final class TaskLRU implements Runnable {

    private final int[] sequence;
    private final int   frames;
    @SuppressWarnings("unused")
    private final int   maxPageReference;
    private final int[] pageFaultsOut;

    public TaskLRU(int[] sequence, int frames, int maxPageReference, int[] out) {
        this.sequence = sequence;
        this.frames = frames;
        this.maxPageReference = maxPageReference;
        this.pageFaultsOut = out;
    }

    @Override
    public void run() {

        if (frames == 0) return;

        LinkedList<Integer> list = new LinkedList<>();
        int faults = 0;

        for (int p : sequence) {
            if (list.remove((Integer) p)) {
                list.addFirst(p);
            } else {
                faults++;
                if (list.size() == frames) list.removeLast();
                list.addFirst(p);
            }
        }
        pageFaultsOut[frames] = faults;
    }
}
