package CardPickup;

import java.util.*;

/**
 * This agent picks the best permutations and travels using A*
 * @author Jose Perez, Tomas Chagoya, Brandon Delgado
 * @version 04/27/20XX
 */
public class DreamDestroyer extends Player {
    protected final String newName = "DreamDestroyer";
    protected static final boolean isVerbose = false;

    // A* frontier
    private PriorityQueue<ABox> frontier;

    // All cards available in the game
    private Set<Card> allAvailableCards;

    // All the cards the opponent has picked up
    private List<Card> opponentCards;

    // Used for evaluation of hands
    private HandEvaluator handEvaluator;

    // Nodes we have visited
    private boolean[] visited;

    public void initialize() {
        long startTime = System.currentTimeMillis();

        // Get all available cards in the game
        allAvailableCards = new TreeSet<>();
        for (int i = 0; i < graph.length; i++)
            for (Node neighbor : graph[i].getNeighborList())
                for (Card card : neighbor.getPossibleCards())
                    allAvailableCards.add(card);

        // Remove cards we have in our hand
        for (int i = 0; i < hand.getNumHole(); i++) {
            allAvailableCards.remove(hand.getHoleCard(i));
        }

        visited = new boolean[graph.length];
        frontier = new PriorityQueue<>();
        opponentCards = new ArrayList<>();
        handEvaluator = new HandEvaluator();

        long duration = System.currentTimeMillis() - startTime;
        println("Init took %s out of %s", duration, Parameters.INIT_TIME);
    }

    /**
     * Do not alter this constructor as nothing has been initialized yet. Please
     * use initialize() instead
     */
    public DreamDestroyer() {
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

        System.out.printf("[DD]" + format + "\n", args);
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

            allAvailableCards.remove(oppLastCard);

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

            // Update our list of cards
            allAvailableCards.remove(c);

            // We can't pick a card here anymore so clear the node in the graph
            graph[currentNode].clearPossibleCards();
        }

    }

    public Hand copyCurrentHand() {
        Hand h = new Hand();
        for (int i = 0; i < hand.getNumHole(); i++)
            h.addHoleCard(hand.getHoleCard(i));

        return h;
    }

    /**
     * Measures the strength of a list of cards
     * Higher strength means better cards
     * @param possibleCards List of cards to evaluate
     * @return Double representing strength of cards
     */
    public float getPairStrength(List<Card> possibleCards) {
        if (possibleCards.size() == 0)
            return -1;

        float bestRank = -1;

        int remainingCards = 5 - hand.size();

        // We can only fit one more card into our hand
        if (remainingCards == 1) {
            // Find which one is the best to pick
            for (Card card : possibleCards) {
                Hand h2 = copyCurrentHand();
                h2.addHoleCard(card);
                bestRank = Math.max(bestRank, handEvaluator.rankHand(h2));

            }
        } else if (remainingCards == 2) {
            for (Card card : possibleCards) {
                for (Card other : allAvailableCards) {
                    // No duplicates
                    if (other.compareTo(card) == 0)
                        continue;

                    Hand h2 = copyCurrentHand();
                    h2.addHoleCard(card);
                    h2.addHoleCard(other);
                    bestRank = Math.max(bestRank, handEvaluator.rankHand(h2));
                }
            }
        } else if (remainingCards == 3) {
            for (Card card : possibleCards) {
                for (Card other : allAvailableCards) {
                    // No duplicates
                    if (other.compareTo(card) == 0)
                        continue;

                    for (Card other2 : allAvailableCards) {
                        // No duplicates
                        if (other2.compareTo(card) == 0)
                            continue;

                        Hand h2 = copyCurrentHand();
                        h2.addHoleCard(card);
                        h2.addHoleCard(other);
                        h2.addHoleCard(other2);
                        bestRank = Math.max(bestRank, handEvaluator.rankHand(h2));
                    }
                }
            }
        }

        return bestRank;
    }

    /**
     * Gets the list of possible cards at the specified node
     * Removes cards that we know are in our hand and the opponent's
     * @param nodeID Node to check
     * @return Curated list of possible cards at the specified node
     */
    public List<Card> getPossibleCards(int nodeID) {
        Node node = graph[nodeID];
        ArrayList<Card> possible = node.getPossibleCards();

        // Remove the cards we already have
        for (int i = 0; i < hand.getNumHole(); i++) {
            Card cardInHand = hand.getHoleCard(i);
            possible.remove(cardInHand);
        }

        // Remove the cards we know the opponent has
        for (Card cardInOpponent : opponentCards) {
            possible.remove(cardInOpponent);
        }

        return possible;
    }

    public Action makeAction() {
        // Begin timing
        long startTime = System.currentTimeMillis();

        println("%s/%s turns remain (%s cards required)", turnsRemaining, Parameters.NUM_TURNS, 5 - hand.size());
        println("%s depth allowed", maxDepth());

        // println("We are currently at: %s", currentNode);
        // for (Node neighbor : graph[currentNode].getNeighborList()) {
        // println("We are next to %s", neighbor.getNodeID());
        // }

        // Predict our best move
        updateFrontier();

        // Get the best move
        ABox bestHand = frontier.poll();

        // Debug
        // println("We need to get to %s, depth = %s",
        // bestHand.parentNode.getNodeID(), bestHand.g);

        // println("Frontier size: %s", frontier.size());
        // println("The best hand right now is: %s, %s, %s",
        // HandEvaluator.nameHand(bestHand.f), bestHand.f, bestHand.g);

        // What should we do?!?
        Action action = bestHand.getAction(graph[currentNode]);
        // println("Best hand wants to %s to %s", action.move, action.nodeID);

        // Clear all of our A* frontier
        frontier.clear();

        // Spend one of our turns
        turnsRemaining--;

        println("Action took %s out of %s", System.currentTimeMillis() - startTime, Parameters.ACTION_TIME);
        return action;
    }

    /**
     * Create a node for A*
     */
    private ABox fromNode(Node n, int depth) {
        ABox abox = new ABox(n);
        abox.g = depth;
        abox.f = getPairStrength(getPossibleCards(n.getNodeID()));
        return abox;
    }

    /**
     * How far can we go into the search?
     */
    public int maxDepth() {
        return turnsRemaining - (5 - hand.size());
    }

    private void updateFrontierRecursive(int nodeID, int depth) {
        // We cannot consider nodes that would take too long to get to
        // Or nodes that we already considered
        if (depth > maxDepth() || visited[nodeID])
            return;

        visited[nodeID] = true;
        ABox parentBox = fromNode(graph[nodeID], depth);

        // Check neighbors first. They are the same depth as the parent
        for (Node neighbor : graph[nodeID].getNeighborList()) {
            if (!visited[neighbor.getNodeID()]) {
                visited[neighbor.getNodeID()] = true;

                // Neighbors are the same depth
                ABox neighborBox = fromNode(neighbor, depth);
                frontier.add(neighborBox);
            }
        }

        // Check neighbors of neighbors.
        // It takes 1 turn to go there so increase depth
        for (Node neighbor : graph[nodeID].getNeighborList())
            for (Node neighbor2 : neighbor.getNeighborList())
                updateFrontierRecursive(neighbor2.getNodeID(), depth + 1);

        frontier.add(parentBox);
    }

    private void updateFrontier() {
        visited = new boolean[graph.length];
        updateFrontierRecursive(currentNode, 0);
    }

    /**
     * Nodes for A* search
     */
    private class ABox implements Comparator<ABox>, Comparable<ABox> {
        public float f = -1;
        public int g = -1;

        public Node parentNode;

        public ABox(Node parentNode) {
            this.parentNode = parentNode;
        }

        public Action getAction(Node currentNode) {
            // We can pick it up if this box is the current node
            if (parentNode.getNodeID() == currentNode.getNodeID())
                return new Action(ActionType.PICKUP, parentNode.getNodeID());

            // We can pick it up if this box is a neighbor of the current node
            for (Node neighbor : currentNode.getNeighborList()) {
                if (parentNode.getNodeID() == neighbor.getNodeID())
                    return new Action(ActionType.PICKUP, parentNode.getNodeID());
            }

            // We need to move closer to pick this card up
            return getMoveAction(currentNode, parentNode, 0);
        }

        public Action getMoveAction(Node currentNode, Node targetNode, int depth) {
            if (depth > maxDepth())
                return null;

            // Check if we can move there by a shared neighbor
            for (Node neighbor : targetNode.getNeighborList()) {
                for (Node neighbor2 : currentNode.getNeighborList()) {
                    if (neighbor.getNodeID() == neighbor2.getNodeID()) {
                        println("We share: %s", neighbor.getNodeID());
                        return new Action(ActionType.MOVE, neighbor.getNodeID());
                    }
                }
            }

            // We don't share any neighbors
            for (Node neighbor : targetNode.getNeighborList()) {
                Action action = getMoveAction(currentNode, neighbor, depth + 1);
                if (action != null)
                    return action;
            }
            return null;
        }

        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;

            ABox other = (ABox) obj;
            return other.parentNode.getNodeID() == parentNode.getNodeID();
        }

        public int compareTo(ABox otherNode) {
            if (Float.floatToIntBits(f) == Float.floatToIntBits(otherNode.f))
                return 0;
            else if (f > otherNode.f)
                return -1;
            else
                return 1;
        }

        @Override
        public int compare(ABox obj1, ABox obj2) {
            return obj1.compareTo(obj2);
        }
    }
}