package CardPickup;

import java.util.*;

/**
 * @author Jose Perez, Tomas Chagoya, Brandon Delgado
 * @version 04/27/20XX
 */
public class Player20XX extends Player {
    protected final String newName = "20XX"; // Overwrite this variable in your
                                             // player subclass
    
    private static final boolean isVerbose = false;

    /**
     * Do not alter this constructor as nothing has been initialized yet. Please
     * use initialize() instead
     */
    public Player20XX() {
        super();
        playerName = newName;
    }

    private boolean[] visited;
    private int visitedCount;
    public void initialize() {
        visited = new boolean[this.graph.length];
        visitedCount = 0;
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
            addCardToHand(c);
            graph[currentNode].clearPossibleCards();
        }

    }

    public boolean canFormPair(Card card) {
        for (int i = 0; i < hand.getNumHole(); i++) {
            Card cardInHand = hand.getHoleCard(i);
            if(isVerbose){
                System.out.printf("20XX {%s, %s}\n", card.getRank(), cardInHand.getRank());
                System.out.printf("Card: " + cardInHand.shortName() + "\n");
            }
            if(card.getRank() == cardInHand.getRank())
                return true;
            
        }
        return false;
    }
    
    public List<Card> getPossibleCards(int nodeID){
        Node node = graph[nodeID];
        List<Card> possible = node.getPossibleCards();
        
        // Remove the cards we already have
        for (int i = 0; i < hand.getNumHole(); i++) {
            Card cardInHand = hand.getHoleCard(i);
            possible.remove(cardInHand);
        }
        return possible;
    }
    
    public Action checkNode(int nodeID){
        if(visited[nodeID])
            return null;
        
        List<Card> possibleCards = getPossibleCards(nodeID);
        
        // No uncertainty
        if(possibleCards.size() == 1){
            visited[nodeID] = true;
            visitedCount++;
            Card card = possibleCards.get(0);
            if(isVerbose){
                System.out.println("Card in node: " + card.shortName() + ", Node ID: " + nodeID);
            }
            if (canFormPair(card)){
                return new Action(ActionType.PICKUP, nodeID);
            }
        }
        
        return null;
    }
    
    public Action getRandomAction(){
        Random r = new Random();
        
        int count = 0;
        
        while(true){
           int randomIndex = r.nextInt(graph[currentNode].getNeighborAmount());
           Node neighbor = graph[currentNode].getNeighbor(randomIndex);
           int neighborID = neighbor.getNodeID();
           
           if(getPossibleCards(neighborID).size() > 0){
               if(isVerbose){
                   System.out.println("Picked randomly");
               }
               return new Action(ActionType.PICKUP, neighborID);
           }
           
           count++;
           
           if(count > 50){
               if(isVerbose){
                   System.out.println("Avoided infinite loop");
               }
               return new Action(ActionType.MOVE, neighbor.getNodeID());
           }
        }
       
    }
    
    public Action makeAction() {
        // We visited all nodes
        if(visitedCount >= graph.length)
            return getRandomAction();
        
        // *************** Case 1: Certainty
        Action action = checkNode(currentNode);
        
        if(action != null)
            return action;
        
        // No cards in ours. Check neighbors
        for (int i = 0; i < graph[currentNode].getNeighborAmount(); i++) {
            int neighborID = graph[currentNode].getNeighbor(i).getNodeID();
            action = checkNode(neighborID);
            
            if(action != null)
                return action;
        }
        
        return getRandomAction();
    }

}