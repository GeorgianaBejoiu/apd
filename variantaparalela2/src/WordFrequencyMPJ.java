import mpi.*;
import java.nio.file.*;
import java.util.*;

public class WordFrequencyMPJ {

    public static void main(String[] args) throws Exception {

        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        byte[] data = null;
        int[] lengths = new int[size];

        long startTime = 0;

        // =========================
        // ROOT citește fișierul
        // =========================
        if (rank == 0) {

            data = Files.readAllBytes(Paths.get("data/test1.txt"));

            startTime = System.currentTimeMillis();

            int chunk = data.length / size;

            for (int i = 0; i < size; i++) {
                lengths[i] = (i == size - 1)
                        ? data.length - i * chunk
                        : chunk;
            }
        }

        // =========================
        // BROADCAST lungimi
        // =========================
        MPI.COMM_WORLD.Bcast(lengths, 0, size, MPI.INT, 0);

        byte[] localData = new byte[lengths[rank]];

        // =========================
        // SCATTER bytes
        // =========================
        MPI.COMM_WORLD.Scatter(
                data, 0, lengths[rank], MPI.BYTE,
                localData, 0, lengths[rank], MPI.BYTE,
                0
        );

        // =========================
        // PARSARE LOCALĂ (FAST)
        // =========================
        Map<String, Integer> localMap = new HashMap<>();

        StringBuilder sb = new StringBuilder();

        for (byte b : localData) {

            char c = (char) b;

            if (Character.isLetterOrDigit(c)) {
                sb.append(Character.toLowerCase(c));
            } else {
                if (sb.length() > 0) {
                    localMap.merge(sb.toString(), 1, Integer::sum);
                    sb.setLength(0);
                }
            }
        }

        if (sb.length() > 0) {
            localMap.merge(sb.toString(), 1, Integer::sum);
        }

        // =========================
        // GATHER (mic, nu masiv)
        // =========================
        Object[] send = new Object[]{localMap};
        Object[] recv = null;

        if (rank == 0) {
            recv = new Object[size];
        }

        MPI.COMM_WORLD.Gather(
                send, 0, 1, MPI.OBJECT,
                recv, 0, 1, MPI.OBJECT,
                0
        );

        // =========================
        // ROOT combină
        // =========================
        if (rank == 0) {

            Map<String, Integer> finalMap = new HashMap<>();

            for (Object obj : recv) {
                Map<String, Integer> m = (Map<String, Integer>) obj;

                for (var e : m.entrySet()) {
                    finalMap.merge(e.getKey(), e.getValue(), Integer::sum);
                }
            }

            System.out.println("\nFREQUENCY RESULT:");

            finalMap.entrySet()
                    .stream()
                    .sorted((a, b) -> b.getValue() - a.getValue())
                    .limit(50)
                    .forEach(e ->
                            System.out.println(e.getKey() + " -> " + e.getValue())
                    );

            long endTime = System.currentTimeMillis();
            System.out.println("\nTIME: " + (endTime - startTime) + " ms");
        }

        MPI.Finalize();
    }
}