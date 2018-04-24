
public class War {

    private static MultiDS<Card> deck = new MultiDS<>(52);

    private static MultiDS<Card> hand1 = new MultiDS<>(52);
    private static MultiDS<Card> hand2 = new MultiDS<>(52);

    private static MultiDS<Card> discard1 = new MultiDS<>(52);
    private static MultiDS<Card> discard2 = new MultiDS<>(52);

    //this structure contains number of cards currently in game pool to be added to the winning player's discard pile
    private static MultiDS<Card> pool = new MultiDS<>(52);

    public static void main(String[] args) throws Exception {

        int numOfRounds = 0;
        int initRound = 0;

        try {
            numOfRounds = Integer.parseInt(args[0]);
        } catch (Exception e){
            System.out.println("Please enter a valid number.");
            System.exit(0);
        }

        System.out.println("Welcome to the Game of War!\n");
        System.out.println("Now dealing the cards to the players...\n");

        build();

        System.out.println("Here is Player 0's Hand:\n" + hand1.toString());
        System.out.println("\nHere is Player 1's Hand:\n" + hand2.toString());

        System.out.println("\nStarting the WAR!\n");

        play(numOfRounds, initRound);
    }

    static void build(){

        for(Card.Suits suit : Card.Suits.values()) {
            for(Card.Ranks rank: Card.Ranks.values()) {
                deck.addItem(new Card(suit, rank));
            }
        }

        deck.shuffle();

        while (!deck.empty()){
            hand1.addItem(deck.removeItem());
            hand2.addItem(deck.removeItem());
        }
    }

    static void play(int rounds, int curRound){

        curRound++;

        if (curRound == rounds){

            System.out.println("\nAfter " + rounds + " rounds here is the status:");

            int score1 = hand1.size() + discard1.size();
            int score2 = hand2.size() + discard2.size();

            System.out.println("\tPlayer 0 has: " + score1);
            System.out.println("\tPlayer 1 has: " + score2);

            if (score1 > score2) System.out.println("Player 0 is the WINNER!");
            else if (score2 > score1) System.out.println("Player 1 is the WINNER!");
            else System.out.println("It is a STALEMATE.");

            return;
        }

        shuffleCheck();

        compareCards(hand1.removeItem(), hand2.removeItem(), curRound);

        //simple recursive method that loops until condition is met where current round is equal to the argument given at call
        play(rounds, curRound);
    }

    //this method checks whether or not hand is empty then shuffles discard pile into hand and finally checks whether game should be over
    //this method must be run before evaluating the compare to incase both hands are empty.
    static void shuffleCheck() {
        if (hand1.empty()) {
            System.out.println("\tGetting and shuffling the pile for player 0");
            while (!discard1.empty()) hand1.addItem(discard1.removeItem());
        }
        if (hand2.empty()) {
            System.out.println("\tGetting and shuffling the pile for player 1");
            while (!discard2.empty()) hand2.addItem(discard2.removeItem());
        }
        if (hand1.empty() && discard1.empty()) {
            System.out.println("\nPlayer 0 is out of cards!");
            System.out.println("\nPlayer 1 is the winner");
            System.exit(0);
        }
        if (hand2.empty() && discard2.empty()) {
            System.out.println("\nPlayer 1 is out of cards!");
            System.out.println("\nPlayer 0 is the winner");
            System.exit(0);
        }
    }

    static void compareCards(Card x, Card y, int round) {

        shuffleCheck();
        int result = x.compareTo(y);
        pool.addItem(x);
        pool.addItem(y);

        if (result > 0) {
            System.out.println("Player 0 Wins Rnd " + round + ": " + x + " beats " + y + " : " + pool.size() + " cards");
            discard1.addItem(x);
            discard1.addItem(y);
            pool.clear();
        }
        else if (result < 0) {
            System.out.println("Player 1 Wins Rnd " + round + ": " + x + " loses to " + y + " : " + pool.size() + " cards");
            discard2.addItem(x);
            discard2.addItem(y);
            pool.clear();
        }
        else {
            System.out.println("\tWAR: " + x + " ties " + y);

            Card risk1 = hand1.removeItem();
            Card risk2 = hand2.removeItem();

            pool.addItem(risk1);
            pool.addItem(risk2);

            System.out.println("\tPlayer 0: " + risk1 + " and Player 1: " + risk2 + " are at risk!");

            shuffleCheck();

//            debug check to see where NPE was happening
//            if(hand1.empty()) System.out.println("HAND 1 IS EMPTY");
//            if(hand2.empty()) System.out.println("HAND 2 IS EMPTY");
//
//            if(discard1.empty()) System.out.println("DISC 1 IS EMPTY");
//            if(discard2.empty()) System.out.println("DISC 2 IS EMPTY");

            Card newX = hand1.removeItem();
            Card newY = hand2.removeItem();

            int newResult = newX.compareTo(newY); //compares war scenario winner deciding card

            if (newResult > 0) {
                discard1.addItem(x);
                discard1.addItem(y);
                discard1.addItem(risk1);
                discard1.addItem(risk2);
            }
            else if (newResult < 0) {
                discard2.addItem(x);
                discard2.addItem(y);
                discard2.addItem(risk1);
                discard2.addItem(risk2);
            }
            //recursively uses previous cards to make sure those cards are added into the pool and subsequent winner discard pile
            compareCards(newX, newY, round);
        }
    }
}
