package CardPickup;

import java.util.Random;

/**
 * @author Jose Perez, Tomas Chagoya, Brandon Delgado
 * @version 04/27/20XX
 */
public class Player20XX extends Player {
    protected final String newName = "20XX"; // Overwrite this variable in your
                                             // player subclass

    /**
     * Do not alter this constructor as nothing has been initialized yet. Please
     * use initialize() instead
     */
    public Player20XX() {
        super();
        playerName = newName;
    }

    public void initialize() {
        // WRITE ANY INITIALIZATION COMPUTATIONS HERE
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
        if (opponentPickedUp)
            oppLastCard = c;
        else
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
        if (c != null)
            addCardToHand(c);
    }

    /**
     * Player logic goes here
     */
    public Action makeAction() {
        Random r = new Random();
        int neighbor;
        if (graph[currentNode].getNeighborAmount() == 1)
            neighbor = graph[currentNode].getNeighbor(0).getNodeID();
        else
            neighbor = graph[currentNode].getNeighbor(r.nextInt(graph[currentNode].getNeighborAmount())).getNodeID();
        return new Action(ActionType.PICKUP, neighbor);
    }

}