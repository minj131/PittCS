import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Stack;

public class Assig3 {

    String[] words;
    Stack<String> stack = new Stack<>();

    boolean wordFound;

    public Assig3() {

        Scanner sc = new Scanner(System.in);
        Scanner fReader;
        File fName;
        String fString, phrase;

        // Make sure the file name is valid
        while (true) {
            try {
                System.out.println("Please enter grid filename:");
                fString = sc.nextLine();
                fName = new File(fString);
                fReader = new Scanner(fName);

                break;
            } catch (IOException e) {
                System.out.println("Problem " + e);
            }
        }

        // Parse input file to create 2-d grid of characters
        String [] dims = (fReader.nextLine()).split(" ");
        int row = Integer.parseInt(dims[0]);
        int col = Integer.parseInt(dims[1]);

        char [][] board = new char[row][col];

        for (int i = 0; i < row; i++) {
            String rowString = fReader.nextLine();
            for (int j = 0; j < rowString.length(); j++) {
                board[i][j] = Character.toLowerCase(rowString.charAt(j));
            }
        }

        // Show user the grid
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                System.out.print(board[i][j] + " ");
            }   System.out.println();
        }
        System.out.println("Please enter the phrase (separated by single spaces):");
        phrase = (sc.nextLine()).toLowerCase();

        while (!phrase.equals("")) {

            System.out.println("Looking for: " + phrase);

            words = phrase.split(" ");
            System.out.println("Containing: " + words.length + " word(s)...");

            String list = phrase.replace(" ", "");

            boolean isFound = false;

            for (int r = 0; (r < row && !isFound); r++) {
                for (int c = 0; (c < col && !isFound); c++) {
                    // Start search for each position at index 0 of the word
                    isFound = findWord(r, c, list, "", "", 0, board, 0);
                }
            }

            if (isFound) {
                for (int i = 0; i < row; i++) {
                    for (int j = 0; j < col; j++) {
                        System.out.print(board[i][j] + " ");
                        board[i][j] = Character.toLowerCase(board[i][j]);
                    }   System.out.println();
                }
            } else System.out.println("The phrase: " + phrase + "\n was not found...");

            System.out.println("\nPlease enter the phrase (separated by single spaces):");
            phrase = (sc.nextLine()).toLowerCase();
        }
    }

    /** Recursive method to handle individual words and chained word phrases
     *  Backtracks char when next char is not found, or word if word is found
     *  but next char is not found
     * @param r = x coordinate
     * @param c = y coordinate
     * @param word = concatenated phrase ex: (hello world) -> (helloworld)
     * @param builder = initialized to "", when char is found, appends to String builder,
     *                  then continually checks if it matches any of the words in the phrase
     * @param ans = initialized to "", when word is found, appends word and coordinates to String ans
     * @param loc = location index of char in String word
     * @param board = game board
     * @param direction = direction marker, 0 = right, 1 = down, 2 = left, 3 = up, for EC: see commented section
     * @return false if word/char is not false or true if word is found
     */

    public boolean findWord(int r, int c, String word, String builder, String ans, int loc, char [][] board, int direction) {
//        System.out.println("findWord: " + r + ":" + c + " " + word + ": " + loc); // trace code

        // boundary conditions
        if (r >= board.length || r < 0 || c >= board[0].length || c < 0)
            return false;
        else if (board[r][c] != word.charAt(loc)) { // char does not match
            return false;
        }
        else {

            builder += board[r][c];
            board[r][c] = Character.toUpperCase(board[r][c]);

            // uses stack to keep track of coordinates. first and last letter coordinates are accessed using .firstElement() and .lastElement()
            stack.push("(" + r + "," + c + ")");

            // continually checks if string matches words in a phrase
            // if match, reinitialize String builder to "", append answer to String ans, and reset stack
            for (String s : words) {
                if (builder.equals(s)) {
                    wordFound = true;
                    direction = 0;
                    ans += (s + ": " + stack.firstElement() + " to " + stack.lastElement() +"\n");
                    builder = "";
                    stack.clear();
                }
            }

            boolean answer;
            if (loc == word.length() - 1) {        // base case - word found and we
                answer = true;                // are done!
                System.out.println(ans);
            } else {    // Still have more letters to match, so recurse.
                // Try all four directions if necessary (but only if necessary)
                answer = false;
                if (direction == 0) {
                    answer = findWord(r, c + 1, word, builder, ans, loc + 1, board, 0);  // Right
                    if (!answer) direction = 1;
                }
                if (!answer && direction == 1) {
                    answer = findWord(r + 1, c, word, builder, ans, loc + 1, board, 1);  // Down
                    if (!answer) direction = 2;
                }
                if (!answer && direction == 2) {
                    answer = findWord(r, c - 1, word, builder, ans, loc + 1, board, 2);  // Left
                    if (!answer) direction = 3;
                }
                if (!answer && direction == 3) {
                    answer = findWord(r - 1, c, word, builder, ans, loc + 1, board, 3);  // Up
                    if (!answer) direction = 4;
                }

                // uncomment for diagonal search, and use 'ecsample.txt' use "hello world this is not fair"

               // if (!answer && direction == 4) {
                   // answer = findWord(r - 1, c + 1, word, builder, ans, loc + 1, board, 4);  // NE
                   // if (!answer) direction = 5;
               // }
               // if (!answer && direction == 5) {
                   // answer = findWord(r + 1, c + 1, word, builder, ans, loc + 1, board, 5);  // SE
                   // if (!answer) direction = 6;
               // }
               // if (!answer && direction == 6) {
                   // answer = findWord(r + 1, c - 1, word, builder, ans, loc + 1, board, 6);  // SW
                   // if (!answer) direction = 7;
               // }
               // if (!answer && direction == 7) {
                   // answer = findWord(r - 1, c - 1, word, builder, ans, loc + 1, board, 7);  // NW
               // }

                if (!answer) {
                    board[r][c] = Character.toLowerCase(board[r][c]);
                    if (!stack.empty()) stack.pop();
                    if (wordFound) return false;
                    // backtrack case:
                    // if wordFound is false, and char is not found, backtrack one char
                    // else if wordFound is true, and char is not found, backtrack one word
                }
            }   return answer;
        }
    }
    public static void main(String[] args) {new Assig3();}
}