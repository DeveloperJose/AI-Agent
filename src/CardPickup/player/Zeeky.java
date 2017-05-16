/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CardPickup.player;
import CardPickup.*;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Little Zeeky Boy extends the Player class so that he can play some poker.
 *
 * @author Zachary J Bell && Anthony Ortiz
 * @since 4/27/2017
 */

public class Zeeky extends Player {
    protected final String newName = "Zeeky";
    private HandEvaluator evaluator;
    private BabySitter babySitter;
    private boolean firstTime = true;
    private Queue<PossibleHand> bestHands;
    private int k;

    public Zeeky() {
        super();
        playerName = newName;
        evaluator = new HandEvaluator();
        bestHands = new PriorityQueue<>();
        
        //initialize();
    }

    @Override
    public void initialize() {
        babySitter = new BabySitter(this.graph, 3);
        List<Card> cardsOnHand = new ArrayList<Card>();
        cardsOnHand.add(this.hand.getHoleCard(0));
        cardsOnHand.add(this.hand.getHoleCard(1));
        babySitter.setcardsOnHand(cardsOnHand);
        babySitter.setTurnsRemaining(this.turnsRemaining);
        this.k = (this.graph[this.currentNode].getPossibleCards().size() - 1) * (this.graph.length) * 5 + 1;
    }

    @Override
    public Action makeAction() {
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
        else if (bestHands.size()>0){
            for(int i = 0; i<bestHands.size(); i++){
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

    // Used to record opponent actions

    @Override
    protected void opponentAction(int opponentNode, boolean opponentPickedUp, Card c) {
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
    
    @Override
    protected void actionResult(int currentNode, Card c){
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


}