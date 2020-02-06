package CardPickup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Jose Perez, Tomas Chagoya, Brandon Delgado
 * @version 04/27/20XX
 */
public class Yamcha extends Player {
    protected final String newName = "Yamcha";

    private List<Card> opponentCards;
    private Random rand;

    public Yamcha() {
        super();
        this.playerName = newName;
    }

    @Override
    public void initialize() {
        rand = new Random(System.nanoTime());
        opponentCards = new ArrayList<>();
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
            opponentCards.add(c);
        } else {
            oppLastCard = null;
        }
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
            addCardToHand(c);
        }
        graph[currentNode].clearPossibleCards();
    }

    /**
     * Player logic goes here
     */
    public Action makeAction() {

        double highestStrength = getPairStrength(getPossibleCards(currentNode));
        int nodeID = currentNode;

        for (Node neighbor : graph[currentNode].getNeighborList()) {
            double strength = getPairStrength(getPossibleCards(neighbor.getNodeID()));

            if (strength > highestStrength) {
                highestStrength = strength;
                nodeID = neighbor.getNodeID();
            }
        }

        int randNum = rand.nextInt(graph.length);
        int chance = rand.nextInt(100);

        if (chance > 80) {
            nodeID = graph[randNum].getNodeID();
        }

        return new Action(ActionType.PICKUP, nodeID);
    }

    public double getPairStrength(List<Card> possibleCards) {
        double result = 0;

        for (Card card : possibleCards) {
            if (hasInHand(card))
                result += (getRank(card) * 20);
            else
                result += getRank(card) * 2;
        }

        return result;
    }

    public int getRank(Card card) {
        if (card.getRank() == 1) // Ace
            return 14;
        return card.getRank();
    }

    public double LimitedIDS(int depth) {
        // random start
        rand = new Random(graph.length);
        int randIndex = rand.nextInt();

        return LimitedDFS(depth, graph[randIndex]);
    }

    public double LimitedDFS(int depth, Node curr) {
        double price = 0;

        if (depth == 0)
            return price;

        for (int i = 0; i < curr.getNeighborAmount() - 1; i++) {
            List<Card> cards = getPossibleCards(curr.getNodeID());
            price = Math.max(price, LimitedDFS(depth - 1, curr.getNeighbor(i)));
        }

        return price;
    }

    public boolean hasInHand(Card card) {
        for (int i = 0; i < hand.getNumHole(); i++) {
            Card cardInHand = hand.getHoleCard(i);

            if (card.getRank() == cardInHand.getRank())
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

}