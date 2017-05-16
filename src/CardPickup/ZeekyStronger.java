/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CardPickup;
import CardPickup.player.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Little ZeekyStronger Boy extends the Player class so that he can play some poker.
 *
 * @author Zachary J Bell && Anthony Ortiz
 * @since 4/27/2017
 */

public class ZeekyStronger extends Player {
    protected final String newName = "ZeekyStronger";
    private HandEvaluator evaluator;
    private BabySitter babySitter;
    private boolean firstTime = true;
    private Queue<PossibleHand> bestHands;
    private int k;
    private boolean zeeky = false;
    //protected final String newName = "BedThoughts";
    protected BabySitter pillow;
    protected Path[][] distTable;
    protected double[] rewardTable;
    protected ArrayList<Card> opponentCards;
    protected ArrayList<Card> myHand;
    protected int staticNode; // serves as index for rewardTable and distTable
    protected int goalNode; // serves as index for rewardTable and distTable
    protected boolean goFlush;
    protected boolean goStraight; //still in progress
    protected int index;
    protected boolean firstTurn;

    public ZeekyStronger() {
        super();
        playerName = newName;
        evaluator = new HandEvaluator();
        bestHands = new PriorityQueue<>();
        
        //initialize();
    }

    @Override
    public void initialize() {
        if (this.graph[0].getPossibleCards().size() == 1)
            this.zeeky = true;
        if(this.zeeky){
        babySitter = new BabySitter(this.graph, 3);
        List<Card> cardsOnHand = new ArrayList<Card>();
        cardsOnHand.add(this.hand.getHoleCard(0));
        cardsOnHand.add(this.hand.getHoleCard(1));
        babySitter.setcardsOnHand(cardsOnHand);
        babySitter.setTurnsRemaining(this.turnsRemaining);
        this.k = (this.graph[this.currentNode].getPossibleCards().size() - 1) * (this.graph.length) *5 + 1;
        }
        else{
            firstTurn = true;
        index = 1;
        myHand = new ArrayList<Card>();
        myHand.add(hand.getHoleCard(0));
        myHand.add(hand.getHoleCard(1));
        
        if (myHand.get(0).getSuit() == myHand.get(1).getSuit())
            goFlush = true;
        // Finished Setting Up Cases for Straight and Flushes

        pillow = new BabySitter(graph, graph.length);
        //pillow.initPathTable(graph);
        distTable = pillow.getPathTable();
        rewardTable = new double[graph.length];
        opponentCards = new ArrayList<>();
        goalNode = currentNode;
        double bestReward;
        bestReward = assessCards(graph[currentNode].getPossibleCards());
        // to be finished: STILL NEEDS EDITS
        // This is the First Heuristic: Greedy Search
        for (int i = 0; i < distTable.length; i++) {
            if (distTable[currentNode][i].getDistance() >= (turnsRemaining / 1.5)) {
                //distTable[currentNode][i].setDistance(Integer.MAX_VALUE);
                continue;
            } else {
                rewardTable[i] = assessCards(graph[i].getPossibleCards());
                if (rewardTable[i] > bestReward) {
                    bestReward = rewardTable[i];
                    goalNode = i;
                }
            }
        }
        staticNode = currentNode;
        //for(int i = 0; i < )
        //pillow.printDistances();
            
        }
    }
    
    public double assessCards(ArrayList<Card> cards) {
        double reward = 0;
        double uncertainty = cards.size();
        boolean opponentHas;
        for (Card card : cards) {
            //my hand assessment
            for (int i = 0; i < myHand.size(); i++) {
                opponentHas = false;
                if (opponentCards.size() > 0) {
                    for (int j = 0; j < opponentCards.size(); j++) {
                        if (card.getRank() == opponentCards.get(j).getRank() &&
                                card.getSuit() == opponentCards.get(j).getSuit()) {
                            uncertainty -= 1.0;
                            opponentHas = true;
                        }
                    }
                }
                if (card.getRank() == myHand.get(i).getRank() && !opponentHas) {
                    if (card.getSuit() == myHand.get(i).getSuit())
                        uncertainty -= 1.0;
                    else {
                        if (myHand.size() == 3) {
                            if (goFlush)
                                reward += 0.75;
                            else
                                ++reward;
                        } else if (myHand.size() == 4) {
                            if (goFlush)
                                reward += 0.5;
                            else
                                reward += 1;
                        } else
                            reward += 1;
                    }
                } else if (!opponentHas && card.getRank() != myHand.get(i).getRank()) {
                        if (card.getSuit() == myHand.get(i).getSuit() && goFlush)
                            reward += 1;
                            // fix goStraight
                    //Case for The Ace Card
                    if (card.getRank() == 1)
                        reward += (double) 14 / 1000.0;
                    else {
                        reward += (double) card.getRank() / 1000.0;
                        //System.out.println((double)card.getRank() / 100.0);
                    }
                    //System.out.println("Reward is: " + reward);
                }
            }
        }
        if (uncertainty <= 0)
            return reward;
        return reward / uncertainty;
    }
    
    public int evaluate() {
        double bestReward = -1;
        goalNode = currentNode;
        for (int i = 0; i < distTable.length; i++) {
            if (distTable[currentNode][i].getDistance() >= (turnsRemaining /(5.0 - myHand.size()))) {
                distTable[i][currentNode].setDistance(Integer.MAX_VALUE);
            } else {
                rewardTable[i] = assessCards(graph[i].getPossibleCards());
                if (rewardTable[i] > bestReward) {
                    bestReward = rewardTable[i];
                    goalNode = i;
                }
            }
        }

        staticNode = currentNode;
        index = 1;
        return goalNode;
    }

    @Override
    public Action makeAction() {
        if(this.zeeky){
        if(this.firstTime){
            this.initialize();
            this.firstTime = false;
        }        
        bestHands = babySitter.getTopHands();
        int nodeToVisit = 0;
        if(bestHands.size()>k){
            for(int i = 0; i<k; i++){
                PossibleHand temp = bestHands.poll();
                babySitter.setNodeValue(temp.getPossibleCards().get(0).getNode().getNodeID(),temp.getHandValue() );
                //temp.getPossibleCards().get(j).getNode(). bestHands.poll().getHandValue();  
            }
            for(int i = 0; i<babySitter.getPathTable().length; i++){
                if(babySitter.getPathTable()[i][i].getNodeValue()> babySitter.getPathTable()[nodeToVisit][nodeToVisit].getNodeValue() ){
                    nodeToVisit = i;
                } 
            }
            
            babySitter.resetNodeValues();
            if(this.graph[this.currentNode].neighbor.contains(babySitter.getPathTable()[nodeToVisit][nodeToVisit].getNodes().get(0))){
                return new Action(ActionType.PICKUP, nodeToVisit);
            }
            if(this.graph[this.currentNode].getNodeID() == nodeToVisit){
                return new Action(ActionType.PICKUP, nodeToVisit);
            }
            else{
                int neighbor = babySitter.getPathTable()[this.currentNode][nodeToVisit].getNodes().get(1).getNodeID();
                return new Action(ActionType.MOVE, neighbor);
            }
        }
        else if (bestHands.size()>0){
            for(int i = 0; i<(int)(bestHands.size()); i++){
                PossibleHand temp = bestHands.poll();
                babySitter.setNodeValue(temp.getPossibleCards().get(0).getNode().getNodeID(),temp.getHandValue() );
                //temp.getPossibleCards().get(j).getNode(). bestHands.poll().getHandValue();  
            }
            for(int i = 1; i<babySitter.getPathTable().length; i++){
                if(babySitter.getPathTable()[i][i].getNodeValue()> babySitter.getPathTable()[nodeToVisit][nodeToVisit].getNodeValue() ){
                    nodeToVisit = i;
                } 
            }
            
            babySitter.resetNodeValues();
            if(this.graph[this.currentNode].neighbor.contains(babySitter.getPathTable()[nodeToVisit][nodeToVisit].getNodes().get(0))){
                return new Action(ActionType.PICKUP, nodeToVisit);
            }
            if(this.graph[this.currentNode].getNodeID() == nodeToVisit){
                return new Action(ActionType.PICKUP, nodeToVisit);
            }
            else{
                int neighbor = babySitter.getPathTable()[this.currentNode][nodeToVisit].getNodes().get(1).getNodeID();
                return new Action(ActionType.MOVE, neighbor);
            }
            
            
        }
        /*
        if(bestHands.size()>0){
            for(int i = 0; i< bestHands.peek().getPossibleCards().size(); i++){
                if( this.graph[this.currentNode].neighbor.contains(bestHands.peek().getPossibleCards().get(i).getNode()) ){
                    int neighbor = bestHands.peek().getPossibleCards().get(i).getNode().getNodeID();
                    return new Action(ActionType.PICKUP, neighbor);
                }
            }
            //dest =bestHands.peek().getPossibleCards().get(0).getNode()
            if(babySitter.getPathTable()[this.currentNode][bestHands.peek().getPossibleCards().get(0).getNode().getNodeID()].getNodes().size()>1 &&this.graph[this.currentNode].neighbor.contains(babySitter.getPathTable()[this.currentNode][bestHands.peek().getPossibleCards().get(0).getNode().getNodeID()].getNodes().get(1)) ){
                    int neighbor = babySitter.getPathTable()[this.currentNode][bestHands.peek().getPossibleCards().get(0).getNode().getNodeID()].getNodes().get(1).getNodeID();
                    return new Action(ActionType.MOVE, neighbor);
            }
        }
        
        if(bestHands.size()>0 && this.currentNode == (bestHands.peek().getPossibleCards().get(0).getNode().getNodeID()) ){
            return new Action(ActionType.PICKUP, this.currentNode);
        }
        if(bestHands.size()>0){
            //int neighbor = babySitter.getPathTable()[this.currentNode][bestHands.peek().getPossibleCards().get(0).getNode().getNodeID()].getNodes().get(0).getNodeID();
           // return new Action(ActionType.PICKUP, neighbor);
        }
        */
        return new Action(ActionType.PICKUP, this.graph[this.currentNode].neighbor.get(0).getNodeID());
        }
        else{
            
        if (goalNode == -1) {
            goalNode = evaluate();
            //System.out.println("Current node is: " + currentNode);
            //System.out.println("Goal node is: " + goalNode);
        }

        // currently performs an invalid action.
        if (distTable[currentNode][goalNode].getNodes().size() <= 1) {
            if (goalNode == currentNode) {
                currentNode = goalNode;
                return new Action(ActionType.PICKUP, currentNode);
            }
            currentNode = goalNode;
            return new Action(ActionType.PICKUP, goalNode);
        }

        if (goalNode == currentNode || goalNode == distTable[currentNode][goalNode].getNodes().get(1).getNodeID()) {
            currentNode = goalNode;
            return new Action(ActionType.PICKUP, currentNode);
        }

        int next = distTable[currentNode][goalNode].getNodes().get(1).getNodeID();
        currentNode = next;
        return new Action(ActionType.MOVE, next);
    
        }
    }

    // Used to record opponent actions

    @Override
    protected void opponentAction(int opponentNode, boolean opponentPickedUp, Card c) {
        if(zeeky){
        super.opponentAction(opponentNode, opponentPickedUp, c);
        if(c != null && opponentPickedUp){
            // If opponent picked up a card in a node, all the possible cards in
            // the node are remove from the list of possible cards.
            babySitter.removePossibleCards(this.graph[this.oppNode]);
            //Every card can trully be iin only one node, if the card picked up
            //by the opponent is appearing in other nodes is removed as well
            babySitter.removePossibleCards(c);
            babySitter.updatePossibleCards(currentNode, this.turnsRemaining);
        }
}
        else{
            oppNode = opponentNode;
        if (opponentPickedUp) {
            //System.out.println("Node that I am at: " + currentNode);
            //System.out.println("Goal node during opponent's action is: " + goalNode);
            //System.out.println("Opponent's node is: " + opponentNode);
            if (goalNode == opponentNode) {
                goalNode = -1;
                //System.out.println("GoalNode is now -1. It should re-evaluate.");
            }
            opponentCards.add(c);
            rewardTable[opponentNode] = -1;
            for (int i = 0; i < graph.length; i++) {
                distTable[opponentNode][i].setDistance(Integer.MAX_VALUE);
                //distTable[i][opponentNode].setDistance(Integer.MAX_VALUE);
            }
            graph[opponentNode].clearPossibleCards();
            //System.out.println("Graph should have cleared possible cards here.");
            //System.out.println(graph[opponentNode].getPossibleCards().size());
        }
        }

    }
    
    @Override
    protected void actionResult(int currentNode, Card c){
        if(zeeky){
        super.actionResult(currentNode, c);
        /*After every action taken by the agent the number of turns remaining is 
        is reduced by one. */
        this.turnsRemaining = this.turnsRemaining - 1;
        babySitter.setTurnsRemaining(this.turnsRemaining);
        
        /*If our agent picked up a card successfully, the card is added to the 
        agent's hand. All the cards in that node are removed from possible 
        cards. If the card picked up is also in other nodes is also removed */
        if(c != null){
            babySitter.addCardsOnHand(c);
            babySitter.removePossibleCards(this.graph[this.currentNode]);
            babySitter.setCardsLeft(5-this.hand.size()); 
            babySitter.removePossibleCards(c);
        }
        /*After every action of the agent the number of turns remaining decrease
        If any node become unreachable, all the possible cards in that node 
        are removed */
        babySitter.updatePossibleCards(currentNode, this.turnsRemaining);

    }
    
    else{
    this.currentNode = currentNode;

        --turnsRemaining;
       //System.out.println("Node that I am at: " + currentNode);
        if (c != null) {
            this.currentNode = goalNode;
            addCardToHand(c);
            myHand.add(c);
            graph[this.currentNode].clearPossibleCards();
            if (goFlush) {
                if (c.getSuit() != myHand.get(0).getSuit())
                    goFlush = false;
            }
            for (int i = 0; i < graph.length; i++) {
                distTable[i][goalNode].setDistance(Integer.MAX_VALUE);
                //distTable[goalNode][i].setDistance(Integer.MAX_VALUE);
            }
            goalNode = -1;
            //goalNode = evaluate();
            //System.out.println("Hello!!!");
        }
}
}


}