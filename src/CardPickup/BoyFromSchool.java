package CardPickup;

import java.util.*;

/**
 * Breadth First Search based Agent
 * Attempts to create pairs based on their own hand and neighbors
 * @author Jose Perez, Tomas Chagoya, Brandon Delgado
 * @version 04/27/20XX
 */
public class BoyFromSchool extends Player {
    protected final String newName = "BoyFromSchool";

    // For printing debugging stuff
    private static final boolean isVerbose = true;

    // From my own testing of 20, 30, and 50 games with different uncertainty
    // You seem to only win a handful more games in exchange for a lower hand
    // rank
    private static boolean shouldSabotage = false;

    // Used for random number generation
    private Random rand;

    // Used for sabotage
    private List<Card> opponentCards;

    public void initialize() {
        rand = new Random(System.nanoTime());
        opponentCards = new ArrayList<>();
        // isCertain = graph[0].getPossibleCards().size() == 1;
    }

    /**
     * Do not alter this constructor as nothing has been initialized yet. Please
     * use initialize() instead
     */
    public BoyFromSchool() {
        super();
        playerName = newName;
    }

    /**
     * Print method used for debugging
     * Will only print if isVerbose is enabled
     * @param format String format to use. Same as the one in String.format()
     * @param args Object arguments to use for String formatting
     */
    private void println(String format, Object... args) {
        if (!isVerbose)
            return;

        System.out.printf("[BS]" + format + "\n", args);
    }

    /**
     * THIS METHOD SHOULD BE OVERRIDDEN if you wish to make computations off of
     * the opponent's moves.
     * GameMaster will call this to update your player on the opponent's
     * actions. This method is called
     * after the opponent has made a move.
     * 
     * @param opponentNode Opponent's current location
     * @param opponentPickedUp Notifies if the opponent picked up a card last
     *            turn
     * @param c The card that the opponent picked up, if any (null if the
     *            opponent did not pick up a card)
     */
    protected void opponentAction(int opponentNode, boolean opponentPickedUp, Card c) {
        oppNode = opponentNode;
        if (opponentPickedUp) {
            oppLastCard = c;
            // Update our memory of opponent cards
            opponentCards.add(c);
            // We can't pick a card there anymore so clear the node in the graph
            graph[opponentNode].clearPossibleCards();
        } else
            oppLastCard = null;
    }

    /**
     * THIS METHOD SHOULD BE OVERRIDDEN if you wish to make computations off of
     * your results.
     * GameMaster will call this to update you on your actions.
     *
     * @param currentNode Opponent's current location
     * @param c The card that you picked up, if any (null if you did not pick up
     *            a card)
     */
    protected void actionResult(int currentNode, Card c) {
        this.currentNode = currentNode;
        if (c != null) {
            // Update our hand
            addCardToHand(c);
            // We can't pick a card here anymore so clear the node in the graph
            graph[currentNode].clearPossibleCards();
        }

    }

    public int getRank(Card card) {
        if (card.getRank() == 1) // Ace
            return 14;
        return card.getRank();
    }

    /**
     * Measures the strength of a list of cards
     * Higher strength means better cards
     * @param possibleCards List of cards to evaluate
     * @return Double representing strength of cards
     */
    public double getPairStrength(List<Card> possibleCards) {
        double result = 0;

        for (Card card : possibleCards) {
            if (canFormPair(card))
                result += (getRank(card) * 20);
            else if (shouldSabotage && canFormPairOpponent(card))
                result += getRank(card) * 10;
            else
                result += getRank(card) * 2;
        }

        return result;
    }

    /**
     * Checks if we can form a pair with the given card with what we have in our
     * hand
     * @param card Card to check
     * @return True if we can form a pair with it
     */
    public boolean canFormPair(Card card) {
        for (int i = 0; i < hand.getNumHole(); i++) {
            Card cardInHand = hand.getHoleCard(i);

            if (card.getRank() == cardInHand.getRank())
                return true;
        }
        return false;
    }

    /**
     * Checks if the opponent can form a pair with the given card based on what
     * we know they have
     * @param card Card to check
     * @return True if the opponent can form a pair with it
     */
    public boolean canFormPairOpponent(Card card) {
        for (Card opp : opponentCards) {
            if (opp.getRank() == card.getRank())
                return true;
        }
        return false;
    }

    /**
     * Gets the list of possible cards at the specified node
     * Removes cards that we know are in our hand and the opponent's
     * @param nodeID Node to check
     * @return Curated list of possible cards at the specified node
     */
    public List<Card> getPossibleCards(int nodeID) {
        Node node = graph[nodeID];
        List<Card> possible = node.getPossibleCards();

        // Remove the cards we already have
        for (int i = 0; i < hand.getNumHole(); i++) {
            Card cardInHand = hand.getHoleCard(i);
            possible.remove(cardInHand);
        }

        // Remove the cards we know the opponent has
        for (int i = 0; i < opponentCards.size(); i++) {
            Card cardInOpponent = opponentCards.get(i);
            possible.remove(cardInOpponent);
        }
        return possible;
    }

    public Action getActionRandom() {
        int loopCount = 0;
        int randomIndex;
        int neighborID;
        do {
            randomIndex = rand.nextInt(graph[currentNode].getNeighborAmount());
            neighborID = graph[currentNode].getNeighbor(randomIndex).getNodeID();

            if (getPossibleCards(neighborID).size() > 0) {
                println("Random choosing");
                return new Action(ActionType.PICKUP, neighborID);
            }

            loopCount++;
        } while (loopCount < 30);

        println("Avoided infinite loop");
        return new Action(ActionType.MOVE, neighborID);
    }
    ArrayList<Integer> visited = new ArrayList<>();
    public Action makeAction() {
        // Check the strength of our current node and neighbors.
        // Pick the one with the highest pair strength
        double highestStrength = getPairStrength(getPossibleCards(currentNode));
        int nodeID = currentNode;

        
        visited.add(nodeID);
        LinkedList<Integer> queue = new LinkedList<Integer>();
        queue.add(currentNode);
        int temp = 0;

        while (queue.size() != 0) {
            temp = queue.poll();

            for (Node neighbor : graph[temp].getNeighborList()) {
                if (!visited.contains(neighbor.getNodeID())) {
                    queue.add(neighbor.getNodeID());
                    visited.add(neighbor.getNodeID());

                    double strength = getPairStrength(getPossibleCards(neighbor.getNodeID()));
                    if (strength > highestStrength) {
                        highestStrength = strength;
                        nodeID = neighbor.getNodeID();
                    }
                }
            }
        }

        // Everything around us has been taken?!?
        // Pick a random one to go to
        if (highestStrength == 0)
            return getActionRandom();
        else
            return new Action(ActionType.PICKUP, nodeID);
    }
}