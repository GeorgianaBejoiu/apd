import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class PVMSimulator {

    static class Worker implements Callable<Map<String, Integer>> {
        private final String[] words;
        private final int start, end;

        Worker(String[] words, int start, int end) {
            this.words = words;
            this.start = start;
            this.end = end;
        }

        @Override
        public Map<String, Integer> call() {
            Map<String, Integer> map = new HashMap<>();

            for (int i = start; i < end; i++) {
                String w = words[i];
                if (w != null && !w.isEmpty()) {
                    map.merge(w, 1, Integer::sum);
                }
            }

            return map;
        }
    }

    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis();

        String text = Files.readString(Paths.get("data/test4.txt"))
                .toLowerCase();

        String[] words = text.split("\\W+");

        int workers = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(workers);

        List<Future<Map<String, Integer>>> futures = new ArrayList<>();

        int chunk = words.length / workers;

        for (int i = 0; i < workers; i++) {
            int start = i * chunk;
            int end = (i == workers - 1) ? words.length : start + chunk;

            futures.add(pool.submit(new Worker(words, start, end)));
        }

        Map<String, Integer> finalMap = new HashMap<>();

        for (Future<Map<String, Integer>> f : futures) {
            Map<String, Integer> m = f.get();

            for (var e : m.entrySet()) {
                finalMap.merge(e.getKey(), e.getValue(), Integer::sum);
            }
        }

        pool.shutdown();

        finalMap.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(50)
                .forEach(e -> System.out.println(e.getKey() + " -> " + e.getValue()));

        long endTime = System.currentTimeMillis();

        System.out.println("\nTIME: " + (endTime - startTime) + " ms");
    }
}