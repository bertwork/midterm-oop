package utils;

/**
 * Small collection of console-formatting helper methods used to print
 * consistent headers, separator lines throughout the application.
 */
public class ConsoleUI {
    public static void printHeader(String title) {
        int lineCount = 50;

        System.out.println("\n" + headerLine(lineCount, '='));

        int padding = (lineCount - title.length()) / 2;
        System.out.println(" ".repeat(Math.max(0, padding)) + title);

        System.out.println(headerLine(lineCount, '='));
    }

    public static String headerLine(int count, char symbol) {
        return String.valueOf(symbol).repeat(count);
    }
}
