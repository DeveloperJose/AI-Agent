package CardPickup;

import java.util.*;

/**
 * Simple Functional Agent
 * Attempts to create pairs based on their own hand and neighbors
 * @author Jose Perez, Tomas Chagoya, Brandon Delgado
 * @version 04/27/20XX
 */
public class Player20XX extends Player {
    protected final String newName = "20XX";
    
    // For printing debugging stuff
    private static final boolean isVerbose = false;
    private static final boolean isShowingResults = false;
    private static int pairCount = 0;
    private static int randomCount = 0;
    private static int sabotageCount = 0;
    
    // {Certain} No Sabotage (30 games): 102 wins, 5276.782 score
    // {Certain} Sabotage (30 games): 97 wins, 5102.937 score
    // {Uncertain} No Sabotage (20 games): 82 wins, 3678.437 score
    // {Uncertain} Sabotage (20 games): 75 wins, 3279.3298 score
    // It seems sabotage isn't as good as it sounds
    private static boolean shouldSabotage = false;
    
    // Used for random number generation
    private Random rand;
    
    // Is the game of certainty?
    private boolean isCertain;
    
    // Used by the player during certainty pairing
    private boolean[] pairVisited;
    private int pairVisitedCount;
    
    // Used for sabotage
    private List<Card> opponentCards;

    public void initialize() {
        rand = new Random(System.nanoTime());
        isCertain = graph[0].getPossibleCards().size() == 1;
        pairVisited = new boolean[this.graph.length];
        pairVisitedCount = 0;
        opponentCards = new ArrayList<>();
    }

    /**
     * Do not alter this constructor as nothing has been initialized yet. Please
     * use initialize() instead
     */
    public Player20XX() {
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

        System.out.printf(format + "\n", args);
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
    
    public int getRank(Card card){
        if(card.getRank() == 1) // Ace
            return 14;
        return card.getRank();
    }

    /**
     * Measures the strength of a list of cards
     * Higher strength means better cards
     * @param possibleCards List of cards to evaluate
     * @return Double representing strength of cards
     */
    public double getPairStrength(List<Card> possibleCards){
        double result = 0;
        
        for(Card card : possibleCards){
            if(canFormPair(card))
                result += (getRank(card) * 20);
            else if(shouldSabotage && canFormPairOpponent(card))
                result += getRank(card) * 10;
            else
                result += getRank(card) * 2;
        }
        
        return result;
    }

    /**
     * Checks if we can form a pair with the given card with what we have in our hand
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
     * Checks if the opponent can form a pair with the given card based on what we know they have
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
        for(int i = 0; i < opponentCards.size(); i++){
            Card cardInOpponent = opponentCards.get(i);
            possible.remove(cardInOpponent);
        }
        return possible;
    }

    public Action getActionCertain(int nodeID) {
        if (pairVisited[nodeID])
            return null;

        List<Card> possibleCards = getPossibleCards(nodeID);
        if (possibleCards.size() != 1)
            return null;

        pairVisited[nodeID] = true;
        pairVisitedCount++;
        Card card = possibleCards.get(0);

        if (canFormPair(card)) {
            println("Making a pair with %s", card.shortName());
            pairCount++;
            return new Action(ActionType.PICKUP, nodeID);
        }

        return null;
    }
    
    public Action getActionCertainSabotage(int nodeID) {
        List<Card> possibleCards = getPossibleCards(nodeID);

        if (possibleCards.size() != 1)
            return null;

        Card card = possibleCards.get(0);

        if (canFormPairOpponent(card)) {
            println("Sabotaging by taking %s", card.shortName());
            sabotageCount++;
            return new Action(ActionType.PICKUP, nodeID);
        }

        return null;
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
                randomCount++;
                return new Action(ActionType.PICKUP, neighborID);
            }

            loopCount++;
        } while (loopCount < 15);

        println("Avoided infinite loop");
        return new Action(ActionType.MOVE, neighborID);
    }
    
    public Action makeActionCertain(){
        // We haven't visited all nodes yet so attempt to find pairs
        if (pairVisitedCount < graph.length) {
            // Check for a pair in our current node
            Action action = getActionCertain(currentNode);

            // We found a pair in our current node
            if (action != null)
                return action;

            // No pairs in ours. Check neighbors
            for (int i = 0; i < graph[currentNode].getNeighborAmount(); i++) {
                int neighborID = graph[currentNode].getNeighbor(i).getNodeID();

                action = getActionCertain(neighborID);

                if (action != null)
                    return action;
            }
        }
        // We finished exploring. Try sabotage
        else if (shouldSabotage) {
            // No pairs anywhere. Sabotage the opponent.
            Action action = getActionCertainSabotage(currentNode);

            if (action != null)
                return action;

            for (int i = 0; i < graph[currentNode].getNeighborAmount(); i++) {
                int neighborID = graph[currentNode].getNeighbor(i).getNodeID();
                action = getActionCertainSabotage(neighborID);

                if (action != null)
                    return action;
            }
        }
        // Nothing is able to be done. Pick randomly
        return getActionRandom();
    }
    
    public Action makeActionUncertain(){
        // Check the strength of our current node and neighbors.
        // Pick the one with the highest pair strength
        double highestStrength = getPairStrength(getPossibleCards(currentNode));
        int nodeID = currentNode;
        
        
        for(Node neighbor : graph[currentNode].getNeighborList()){
            double strength = getPairStrength(getPossibleCards(neighbor.getNodeID()));
            
            if(strength > highestStrength){
                highestStrength = strength;
                nodeID = neighbor.getNodeID();
            }
        }
        
        // Everything around us has been taken?!?
        if(highestStrength == 0)
            return getActionRandom();
        else
            return new Action(ActionType.PICKUP, nodeID);
    }

    public Action makeAction() {
        if(getHandSize() == 4 && isShowingResults)
            System.out.printf("Pair %s, Sabotage %s, Random %s\n", pairCount, sabotageCount, randomCount);
        
        //return makeActionUncertain();
        
        return (isCertain) ? makeActionCertain() : makeActionUncertain();
    }
}