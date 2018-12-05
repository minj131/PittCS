import java.util.ArrayList;
import java.util.HashSet;

public class DLBDict {

    private Node root;

    private final char WORD_FLAG = '^'; //flag used to indicate when word is found

    // constructor to set root to new node
    DLBDict(){
        root = new Node();
    }

    // combined search and autocomplete function and returns a boolean true if word is found and autocompleted or false if word is not found
    public boolean autoComplete(StringBuilder word, ArrayList<String> list, HashSet<String> ref) {

        Node curr = root;

        for (int i = 0; i < word.length(); i++) {

            char letter = word.charAt(i);
            curr = getChild(curr, letter);

            if (curr == null) {
                // Not a word, not a prefix
                return false;
            }
        }

        // base case, if list is full return
        if (list.size() == 5) return true;

        // If method gets here, then word has not been found yet
        // but has reached last char of the word.
        // So check child for flag to indicate
        // if word has been found
        Node lastNode = getChild(curr, WORD_FLAG);

        if (lastNode == null) {
            // nodes surrounding does not contain word_flag
            // but does contain more nodes
            // is a prefix, but not a word
            word.append(curr.childNode.letter);

            autoComplete(word, list, ref);

            while (curr.hasSideNode()) {


                if (word.length() >= 3)
                    word.setLength(word.length()-2);

                autoComplete(word.append(curr.sideNode.letter), list, ref);

                curr = curr.sideNode;
            }

        } else if (lastNode.sideNode == null) {
            // side node does anything, stop here
            // is a word, but not a prefix
            if (!list.contains(word.toString())) {
                list.add(word.toString());
            }

            if (curr.hasSideNode()) {
                word.setLength(word.length()-1);
                autoComplete(word.append(curr.sideNode.letter), list, ref);
            }

        } else {
            // is a word and a prefix
            if (!list.contains(word.toString())) {
                list.add(word.toString());
            }

            word.append(curr.childNode.sideNode.letter);
            autoComplete(word, list, ref);

        }
        return true;
    }

    boolean isBiggestPrefix(StringBuilder key, String ref) {
//        int min = Math.min(key.length(), ref.length());
//        for (int i=0; i<min; i++) {
//            if (key.charAt(i) != ref.charAt(i)) {
//                return key.substring(0, i);
//            }
//        }
//        return key.substring(0, min);
        return ref.startsWith(key.toString());
    }

    public boolean add(StringBuilder word) {

        word.append(WORD_FLAG);
        Node curr = root;

        for (int i=0; i<word.length(); i++) {
            char letter = word.charAt(i);
            curr = addChild(curr, letter);
        }

        // if reached here, add was successful and return true
        return true;
    }

    private Node addChild(Node parent, char letter) {

        if (parent.childNode == null) {
            // if parent does not have child node, create new child node and return node
            parent.childNode = new Node(letter);
            return parent.childNode;
        } else {
            // else child node is already taken, create a new side node and add letter
            return addSide(parent.childNode, letter);
        }
    }

    private Node addSide(Node curr, char letter) {

        if (curr == null) {
            curr = new Node(letter);
            return curr;


        } else {

            Node newSide = curr;

            // if side node is not null, go right until either
            // l. letter is found (break out of loop) or
            // 2. null node is found
            while (newSide.sideNode != null) {
                if (newSide.letter == letter) break;
                newSide = newSide.sideNode;
            }

            // if current node contains letter, return that node. no need to add the letter again
            // else create a new side node with that letter and return node
            if (newSide.letter == letter) {
                return newSide;

            } else {

                newSide.sideNode = new Node(letter);
                return newSide.sideNode;
            }
        }
    }

    // returns side node
    private Node getSide(Node curr, char letter) {

        Node nextNode = curr;
        while (nextNode != null) {
            if (nextNode.letter == letter) break;
            nextNode = nextNode.sideNode;
        }   return nextNode;
    }

    // returns child node
    private Node getChild(Node parent, char letter) {
        //return the node at the child
        return getSide(parent.childNode, letter);
    }

    private class Node {

        private Node sideNode;
        private Node childNode;
        private char letter;


        private Node() {}

        private Node(char letter) {
            this(letter, null, null);
        }

        private Node(char letter, Node sideNode, Node childNode) {
            this.letter = letter;
            this.sideNode = sideNode;
            this.childNode = childNode;
        }

        private boolean hasSideNode() {return sideNode != null;}
        private boolean hasChildNode() {return childNode != null;}
    }
}

