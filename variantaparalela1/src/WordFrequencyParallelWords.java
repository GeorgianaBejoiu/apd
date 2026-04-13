import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class WordFrequencyParallelWords {

    private static final int NUM_THREADS = 4;

    public static void main(String[] args) throws Exception {

        String filePath = "data/test1.txt";

        // citire fișier
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        // transformăm tot textul într-un singur array de cuvinte
        String text = String.join(" ", lines);
        String[] allWords = text.toLowerCase().split("\\W+");

        long startTime = System.nanoTime();

        // împărțire în bucăți de cuvinte
        List<List<String>> chunks = splitWordsIntoChunks(allWords, NUM_THREADS);

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        List<Future<Map<String, Integer>>> futures = new ArrayList<>();

        // trimitere task-uri
        for (List<String> chunk : chunks) {
            futures.add(executor.submit(new WordCountTask(chunk)));
        }

        // map final
        Map<String, Integer> finalMap = new HashMap<>();

        // combinare rezultate
        for (Future<Map<String, Integer>> future : futures) {
            Map<String, Integer> localMap = future.get();

            for (Map.Entry<String, Integer> entry : localMap.entrySet()) {
                finalMap.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }

        long endTime = System.nanoTime();

        executor.shutdown();

        // afișare rezultate
        System.out.println("Frecvența cuvintelor:");
        for (Map.Entry<String, Integer> entry : finalMap.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }

        System.out.println("\nTimp execuție: " + (endTime - startTime) / 1_000_000 + " ms");
    }

    // împărțire array de cuvinte în bucăți
    private static List<List<String>> splitWordsIntoChunks(String[] words, int numChunks) {
        List<List<String>> chunks = new ArrayList<>();

        int chunkSize = (int) Math.ceil((double) words.length / numChunks);

        for (int i = 0; i < words.length; i += chunkSize) {
            chunks.add(Arrays.asList(Arrays.copyOfRange(words, i,
                    Math.min(i + chunkSize, words.length))));
        }

        return chunks;
    }

    // task paralel
    static class WordCountTask implements Callable<Map<String, Integer>> {

        private final List<String> words;

        public WordCountTask(List<String> words) {
            this.words = words;
        }

        @Override
        public Map<String, Integer> call() {
            Map<String, Integer> localMap = new HashMap<>();

            for (String word : words) {
                if (!word.isEmpty()) {
                    localMap.put(word, localMap.getOrDefault(word, 0) + 1);
                }
            }

            return localMap;
        }
    }
}