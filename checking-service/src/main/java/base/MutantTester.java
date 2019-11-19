package base;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MutantTester {

    private static final Integer MINIMAL_LENGTH = 4;
    private static final Integer MINIMAL_MUTATION = 2;

    public static boolean isMutant(final List<String> dna) {

        if (!canBeMutant(dna)) return false;

        final Integer size = dna.size();

        SynchronizedCounter counter = new SynchronizedCounter();

        ExecutorService executorService = Executors.newCachedThreadPool();

        for (Integer i = 0; i < size; i++) {
            Integer j = i;

            try {
                executorService
                        .submit(() -> searchForMutations(executorService, counter, dna, j,0, horizontal));

                executorService
                        .submit(() -> searchForMutations(executorService, counter, dna, 0, j, vertical));

                if (j + MINIMAL_LENGTH <= size) {
                    executorService
                            .submit(() -> searchForMutations(executorService, counter, dna, 0, j, diagonalAsc));
                }

                if (j + 1 >= MINIMAL_LENGTH) {
                    executorService
                            .submit(() -> searchForMutations(executorService, counter, dna, 0, j, diagonalDes));
                }

                if (j > 0 && j + MINIMAL_LENGTH <= size) {
                    executorService
                            .submit(() -> searchForMutations(executorService, counter, dna, j, 0, diagonalAsc));

                    executorService
                            .submit(() -> searchForMutations(executorService, counter, dna, j, size - 1, diagonalDes));
                }

            } catch (RejectedExecutionException e) {
            }
        }

        executorService.shutdown();

        try {
            executorService.awaitTermination(15, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
        }

        return counter.getCount() >= MINIMAL_MUTATION;
    }

    private static void searchForMutations(ExecutorService executor, SynchronizedCounter counter, List<String> dna,
                                           Integer row, Integer col, Consumer<Integer[]> direction) {
        Integer localRow = row;
        Integer localCol = col;

        final Integer size = dna.size();
        Integer count = 0;

        char target = dna.get(localRow).charAt(localCol);

        Integer[] position = {localRow, localCol};

        /////////////////////////////////////

        do {
            char c = dna.get(position[0]).charAt(position[1]);

            if (c == target) {
                count++;
            } else {
                target = c;
                count = 1;
            }

            if (count >= MINIMAL_LENGTH) {
                counter.increment();
                count = 0;
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            direction.accept(position);

        } while (isPositionValid(size, position));

        if (counter.getCount() >= MINIMAL_MUTATION) {
            executor.shutdownNow();
        }
    }

    private static Consumer<Integer[]> horizontal = position -> {
        position[1] = position[1] + 1;
    };

    private static Consumer<Integer[]> vertical = position -> {
        position[0] = position[0] + 1;
    };

    private static Consumer<Integer[]> diagonalAsc = position -> {
        position[0] = position[0] + 1;
        position[1] = position[1] + 1;
    };

    private static Consumer<Integer[]> diagonalDes = position -> {
        position[0] = position[0] + 1;
        position[1] = position[1] - 1;
    };

    private static boolean isPositionValid(Integer size, Integer[] position) {

        if (position[0] < 0) return false;
        if (position[1] < 0) return false;

        if (position[0] >= size) return false;
        if (position[1] >= size) return false;

        return true;
    }

    private static boolean canBeMutant(final List<String> dna) {

        if (dna == null || dna.size() < MINIMAL_LENGTH) {
            return false;
        }

        final Integer size = dna.size();

        for (Integer i = 0; i < size; i++) {
            if (dna.get(i).length() != size) return false;
        }

        return true;
    }

}

