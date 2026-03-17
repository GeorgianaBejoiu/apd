import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        String fileName = "data/test4.txt";
        Map<String, Integer> wordCount = new HashMap<>();//cheia-cuvantul, valoarea-nr de aparitii
        long startTime = System.currentTimeMillis();

        //citim din fisier, linie cu linie
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            //citim fiecare linie pana la final
            while ((line = br.readLine()) != null) {
                //toate literele devin mici
                //sparge linia în cuvinte, folosind orice caracter care nu este litera sau cifra ca delimitator (\W)
                String[] words = line.toLowerCase().split("\\W+");
                for (String word : words) {
                    //verf daca exista cuvantul in hashmap
                    if (!word.isEmpty()) {
                        wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);//Daca da, ia valoarea; daca nu, ia 0
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Fișierul nu a fost găsit: " + fileName);
            return;
        } catch (IOException e) {
            System.out.println("Eroare la citirea fișierului: " + e.getMessage());
            return;
        }

        //incheiem cronometrarea si afișam rezultatele
        long endTime = System.currentTimeMillis();
        System.out.println("Frecvența cuvintelor în fișier:");
        wordCount.forEach((k, v) -> System.out.println(k + ": " + v));
        System.out.println("\nTimp de rulare secvențial: " + (endTime - startTime) + " ms");
    }
}