import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class Assig5 {

    public Assig5(String textFile) throws FileNotFoundException {

        Scanner treeIn = new Scanner(new File(textFile));

        // Assertion: root = treeIn.nextLine() which will always be an Interior node represented as 'I'
        BinaryNode<Character> root = new BinaryNode<>('\0');
        treeIn.nextLine();

        // Recursively populate binary tree using the text file
        // Takes root and scanner text file as its arguments
        root.setLeftChild(setNextNode(root, treeIn));
        root.setRightChild(setNextNode(root, treeIn));

        // Builds Huffman Encoded table
        // Initialize a SB to store encoded values
        // Initialize a map to pair the values to the Character index
        StringBuilder encode = new StringBuilder();
        HashMap<Character, String> huffTable = new HashMap<>();
        buildHuffTable(huffTable, root, encode);


        // loads main program
        System.out.println("\nThe Huffman Tree has been restored.");

        boolean quit = false;
        Scanner user = new Scanner(System.in);

        while (!quit) {

            System.out.println("\nPlease choose from the following:");
            System.out.println("1) Encode a text string");
            System.out.println("2) Decode a Huffman string");
            System.out.println("3) Quit");

            int input = user.nextInt();

            if (input == 1) {
                System.out.println("Enter a string from the following characters: ");

                for (HashMap.Entry<Character, String> entry : huffTable.entrySet())
                    System.out.print(entry.getKey());

                System.out.println();

                boolean stringError = false;

                String stringIn = user.next();
                stringIn = stringIn.toUpperCase();

                for (int i = 0; i < stringIn.length(); i++) {
                    if (!huffTable.containsKey(stringIn.charAt(i))) {
                        System.out.println("There was an error in your text string");
                        stringError = true;
                        break;
                    }
                }

                if (!stringError) {
                    System.out.println("Huffman String:");
                    for (int i = 0; i < stringIn.length(); i++) {
                        for (HashMap.Entry<Character, String> entry : huffTable.entrySet()) {
                            if (stringIn.charAt(i) == entry.getKey())
                                System.out.println(entry.getValue());
                        }
                    }
                }
            }

            else if (input == 2) {
                System.out.println("Here is the encoding table:");
                for (HashMap.Entry<Character, String> entry : huffTable.entrySet())
                    System.out.println(entry.getKey() + " : " + entry.getValue());

                System.out.println("Please enter a Huffman string (one line, no spaces)");
                String stringIn = user.next();

                System.out.println("Text string:");

                System.out.println(decodeHuff(stringIn, root));

            }

            else if (input == 3) {
                System.out.println("Goodbye!");
                quit = true;
            }

            else
                System.out.println("Please enter a valid number! (1, 2, or 3)\n");
        }
        treeIn.close();
        user.close();
    }

    BinaryNode<Character> setNextNode(BinaryNode<Character> node, Scanner treeFile) {

        BinaryNode<Character> newRoot = null;
        String nextLine = treeFile.nextLine();

        // recurse while file has more lines
        if (nextLine != null) {

            if (nextLine.charAt(0) == 'I') {

                newRoot = new BinaryNode<>('\0');

                if (!node.hasLeftChild())
                    node.setLeftChild(newRoot);

                else if (!node.hasRightChild())
                    node.setRightChild(newRoot);

                // recurse until leaf node is found propagating each interior node
                newRoot.setLeftChild(setNextNode(newRoot, treeFile));
                newRoot.setRightChild(setNextNode(newRoot, treeFile));

            } else if (nextLine.charAt(0) == 'L') { // else node is leaf, propagate left then right leaf nodes

                BinaryNode<Character> newLeaf = new BinaryNode<>(nextLine.charAt(2));

                if (!node.hasLeftChild()) {
                    node.setLeftChild(newLeaf);
                    return newLeaf;

                } else if (!node.hasRightChild()) {
                    node.setRightChild(newLeaf);
                    return newLeaf;
                }

            }
        }   return newRoot;
    }

    void buildHuffTable(HashMap<Character, String> huffTable, BinaryNode<Character> node, StringBuilder encode) {

        if (node.hasLeftChild()) {
            encode.append("0");
            buildHuffTable(huffTable, (BinaryNode<Character>)node.getLeftChild(), encode);
            encode.deleteCharAt(encode.length() - 1);
        }

        if (node.hasRightChild()) {
            encode.append("1");
            buildHuffTable(huffTable, (BinaryNode<Character>)node.getRightChild(), encode);
            encode.deleteCharAt(encode.length() - 1);
        }

        // inserts letter and Huff encoded string to HuffTable
        if (node.isLeaf())
            huffTable.put(node.getData(), encode.toString());
    }

    // takes encoded input, decodes, and returns decoded string
    String decodeHuff (String encode, BinaryNode node) {

        StringBuilder encodedInput = new StringBuilder(encode);
        StringBuilder decodedInput = new StringBuilder();
        BinaryNode temp = node;

        while (encodedInput.length() > 0) {

            if (encodedInput.charAt(0) == '0') {
                encodedInput.deleteCharAt(0);
                temp = (BinaryNode)temp.getLeftChild();

                if (temp.isLeaf()) {
                    decodedInput.append(temp.getData());
                    temp = node;
                }

            } else if (encodedInput.charAt(0) == '1') {
                encodedInput.deleteCharAt(0);
                temp = (BinaryNode)temp.getRightChild();

                if (temp.isLeaf()) {
                    decodedInput.append(temp.getData());
                    temp = node;
                }
            }
        }   return decodedInput.toString();
    }

    public static void main(String[] args) throws FileNotFoundException {
        new Assig5(args[0]);
    }
}