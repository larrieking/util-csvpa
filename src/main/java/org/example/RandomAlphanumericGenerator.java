package org.example;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RandomAlphanumericGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int STRING_LENGTH = 30;
    private static final int COUNT = 21;

    public static void main(String[] args) {
        Set<String> uniqueStrings = new HashSet<>();
        Random random = new Random();

        while (uniqueStrings.size() < COUNT) {
            StringBuilder sb = new StringBuilder(STRING_LENGTH);
            for (int i = 0; i < STRING_LENGTH; i++) {
                int index = random.nextInt(CHARACTERS.length());
                sb.append(CHARACTERS.charAt(index));
            }
            uniqueStrings.add(sb.toString());
        }

        uniqueStrings.forEach(System.out::println);
    }
}
