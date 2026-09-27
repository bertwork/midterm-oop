package utils;

import java.util.Scanner;

/**
 * Utility class for reading user input from the console.
 * <p>
 * Wraps a single shared {@link Scanner} on {@code System.in} and
 * provides convenience methods for prompting the user, reading
 * validated integers, and pausing until the user presses Enter.
 */
public class InputReader {

    /**
     * Shared scanner used to read from standard input for the lifetime of the app.
     */
    private static Scanner scanner = new Scanner(System.in);

    /**
     * Prints a prompt and reads a single line of trimmed text input.
     *
     * @param prompt the message to display before reading input
     * @return the trimmed line of text entered by the user
     */
    public static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /**
     * Prints a prompt and reads an integer from the console, re-prompting
     * repeatedly until the user enters valid, parsable integer text.
     *
     * @param prompt the message to display before reading input
     * @return the integer entered by the user
     */
    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);

            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    /**
     * Prints a "press Enter to continue" message and blocks until the
     * user presses Enter, giving them a chance to read console output
     * before the screen moves on.
     */
    public static void pauseScreen() {
        System.out.println("\nPress Enter to continue....");
        scanner.nextLine();
    }
}