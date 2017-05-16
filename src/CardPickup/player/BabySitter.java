/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CardPickup.player;
import CardPickup.*;
import java.util.*;

/**
 * @author Zachary J Bell and Anthony Ortiz
 * @since 4/28/2017
 */

public class BabySitter {

    private Path[][] pathTable;
    private List<CardContainer> possibleCards;
    private List<Node> nodeList;
    private int cardsLeft;
    private int turnsRemaining;
    private List<Card> cardsOnHand;

    public BabySitter(Node[] graph, int cardsLeft) {
        this.cardsLeft = cardsLeft;
        initGraph(graph);
        initPathTable(graph);
        initPossibleCards(graph);
    }
    
    public Path[][] getPathTable(){
        return this.pathTable;
    }

    public void initGraph(Node[] graph) {
        //System.out.println(graph.length);
        this.nodeList = new ArrayList<>(graph.length);
        Collections.addAll(nodeList, graph);
    } 
    
    public void setNodeValue(int node, double value){
        this.pathTable[node][node].setNodeValue(this.pathTable[node][node].getNodeValue() + value);
    }
    
    public void resetNodeValues(){
        for(int i = 0 ; i<this.pathTable.length; i++){
            this.pathTable[i][i].setNodeValue(0.0);
        }
    }
    
    public void setTurnsRemaining(int turnsRemaining){
        this.turnsRemaining = turnsRemaining;
    }
    
    public void setCardsLeft(int cardsLeft){
        this.cardsLeft = cardsLeft;
    }
    
     public void setcardsOnHand(List<Card> cardsOnHand){
        this.cardsOnHand = cardsOnHand;
    }

    /**
     * Builds a path table that contains the minimum distance between the node at the index of the row and the node at
     * the index of the column. The Path contains a minimum distance as well as a linked list directing travel between
     * the minimum path.
     *
     * @param graph the initial graph provided by the Player class
     */

    @SuppressWarnings("unchecked")
    public void initPathTable(Node[] graph) {

        Queue<PathPair> queue = new PriorityQueue<>();
        Set<Node> visited = new HashSet<>();
        LinkedList<Node> pathLL = new LinkedList<>();
        ArrayList<Node> children;
        PathPair pair;

        pathTable = new Path[graph.length][graph.length];

        for (Node node : graph) {
            visited.clear();
            pathLL.clear();

            visited.add(node);
            queue.add(new PathPair(0, node, pathLL));

            pathTable[node.getNodeID()][node.getNodeID()] = new Path(0, null);

            while(!queue.isEmpty()) {

                pair = queue.poll();

                pathLL = pair.getNodeLL();

                visited.add(pair.getNode());

                children = pair.getNode().getNeighborList();

                pathTable[node.getNodeID()][pair.getNode().getNodeID()] = new Path(pair.getDistance(), ((LinkedList<Node>)pathLL.clone()));

                for (Node child : children) {
                    if (!visited.contains(child)) {
                        visited.add(child);
                        queue.add(new PathPair(pair.getDistance() + 1, child, ((LinkedList<Node>)pathLL.clone())));
                    }
                }
            }
        }
    }

    /**
     * Initializes the possible card set. The possible card set starts by initializing all cards contained on the graph
     * with pointers to the nodes they belong to. As the agent progresses in the game this set can be pruned by searching
     * algorithms to reduce complexity when certain cards are deemed unreachable or cards are picked up by the opponent
     * or the player.
     *
     * @param graph the initial graph provided by the Player class
     */

    public void initPossibleCards(Node[] graph) {

        possibleCards = new ArrayList<CardContainer>();

        ArrayList<Card> cards;

        for (Node node : graph) {
            cards = node.getPossibleCards();
            for (Card card : cards) {
                possibleCards.add(new CardContainer(card, node));
            }
        }
    }

    /**
     * Used when all the cards in a node are removed from play by a player or opponent pickup.
     *
     * @param node the node that the card pickup took place within
     */

    public void removePossibleCards(Node node) {
        Iterator<CardContainer> iter = possibleCards.iterator(); 
        while (iter.hasNext()) {
            CardContainer possibleCard = iter.next();
            if (possibleCard.getNode().equals(node)) {
                iter.remove();
            }
        }

    }
    
    public void removePossibleCards(Card cardRemovee) {
        Iterator<CardContainer> iter = possibleCards.iterator(); 
        while (iter.hasNext()) {
            CardContainer possibleCard = iter.next();
            if (possibleCard.getCard().equals(cardRemovee)) {
                iter.remove();
            }
        }

    }

    /**
     * Gets the minimum distance between the source node and the destinations
     *
     * @param source the source node
     * @param destination the destination node
     * @return the distance between the source and destination nodes
     */

    public int getDistance(Node source, Node destination) {
        return pathTable[source.getNodeID()][destination.getNodeID()].getDistance();
    }
    
    /**
     * Gets the total path distance of a possible Hand
     *
     * @param source the source node
     * @param destination the destination node
     * @return the distance between the source and destination nodes
     */
    public int getTotalPathDistance(ArrayList<CardContainer> possibleHand) {
        if(possibleHand == null){
            System.out.println("Possible hand to evaluate is null");
            return -1;
        }
        int totalDistance = 0;
        while(totalDistance< (possibleHand.size()-1)){
            totalDistance += getDistance(possibleHand.get(totalDistance).getNode(), possibleHand.get(totalDistance+1).getNode());
            totalDistance++;
        }
        return totalDistance;
    }

    /**
     * Prunes the possible cards set and removes any card that is unfeasible to be reached within the number of turns
     * available.
     *
     * @param currentNode the index of the node that the player is currently at
     * @param turnsRemaining the number of turns remaining in the game
     */

    public void updatePossibleCards(int currentNode, int turnsRemaining) {

        Node node = nodeList.get(currentNode);
        
        Iterator<CardContainer> iter = possibleCards.iterator(); 
        while (iter.hasNext()) {
            CardContainer possibleCard = iter.next();
            if (getDistance(node, possibleCard.getNode()) > turnsRemaining - cardsLeft) {
                iter.remove();
            }
        }
    }
    
    public void addCardsOnHand(Card cardToAdd) {
        this.cardsOnHand.add(cardToAdd);
    }

    public Queue<PossibleHand> getTopHands() {
        Queue<PossibleHand> bestHands = new PriorityQueue<>();
        CardContainer[] possibleHand = new CardContainer[this.cardsLeft];
        computeHandValuesRec((ArrayList)possibleCards, possibleHand, bestHands, 0, possibleCards.size() - 1, 0, this.cardsLeft, null );
        return bestHands;
    }

    private void computeHandValuesRec(ArrayList<CardContainer> possibleCards, CardContainer[] possibleHand, Queue<PossibleHand> bestHands,
                                      int start, int end, int index, int r, Set<Node> nodeSet) {
        if (nodeSet == null) nodeSet = new HashSet<>(r);
        if (index == r) {
            permute(possibleHand, bestHands);
            nodeSet = null;       
            return;
        }
        for (int i = start; (i <= end) && ((end - i + 1) >= (r - index)); i++) {
            CardContainer container = possibleCards.get(i);
            if(nodeSet.contains(container)) continue;
            possibleHand[index]= container;
            computeHandValuesRec(possibleCards, possibleHand, bestHands, i+1, end, index+1, r, nodeSet);
        }
    }

    public void permute(CardContainer[] possibleHand, Queue<PossibleHand> bestHands) {
        permute(possibleHand, bestHands, 0);
    }


    private void permute(CardContainer[] possibleHand, Queue<PossibleHand> bestHands, int index) {
        if (index >= (possibleHand.length - 1)) {
            ArrayList<CardContainer> temp = new ArrayList<>();
            for (int i = 0; i < possibleHand.length - 1; i++) {
                temp.add(possibleHand[i]);
            }
            if (possibleHand.length > 0) {
                temp.add(possibleHand[possibleHand.length - 1]);
            }
            if (isPossible(temp)) {
                PossibleHand tempHand = new PossibleHand(temp);
                float handValue = evaluateHandWithProbabilities(temp);
                tempHand.setHandValue(handValue);
                bestHands.add(tempHand);
            }
            return;
        }
        for (int i = index; i < possibleHand.length; i++) {
            CardContainer temp = possibleHand[index];
            possibleHand[index] = possibleHand[i];
            possibleHand[i] = temp;

            permute(possibleHand, bestHands, index + 1);

            temp = possibleHand[index];
            possibleHand[index] = possibleHand[i];
            possibleHand[i] = temp;
        }
    }

    private boolean isPossible(ArrayList<CardContainer> cardContainers) {
        if(getTotalPathDistance(cardContainers) < (this.turnsRemaining ))
            return true;
        return false; 
    }
    
    private float evaluateHandWithProbabilities(ArrayList<CardContainer> possibleHand){
        Hand tempHandToEvaluate = new Hand();
        for (Card cardOnHand : this.cardsOnHand){
            tempHandToEvaluate.addUpCard(cardOnHand);
            //System.out.print(cardOnHand.shortName() + " ");
        }
        for (CardContainer possibleCard : possibleHand) {
            tempHandToEvaluate.addSharedCard(possibleCard.getCard());
           // System.out.print(possibleCard.getCard().shortName()+ " ");
        }
        
        //TODO: FIx probabilities to something more sensible
        HandEvaluator eval = new HandEvaluator();
        float value = eval.rankHand(tempHandToEvaluate);
        float prob =  1.0f/tempHandToEvaluate.getNumShared();
        //System.out.print("Value: " + value);
        //System.out.println("Value Prob: " + value*prob);
        return value *prob; 
    }

    // used for testing

    public void printDistances() {

        List<Node> nodes;

        for (int i = 0; i < pathTable.length; i++) {
            for (int j = 0; j < pathTable[i].length; j++) {
                System.out.print(pathTable[i][j].getDistance() + " ");
            }
            System.out.println();
        }
        for (int i = 0; i < pathTable.length; i++) {
            for (int j = 0; j < pathTable[i].length; j++) {
                nodes = pathTable[i][j].getNodes();
                if(nodes != null) {
                    for (Node node : nodes) {
                        System.out.print(node.getNodeID() + " -> ");
                    }
                    System.out.println();
                }
            }
        }

    }
}