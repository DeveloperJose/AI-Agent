/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CardPickup;

import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import CardPickup.player.*;
/**
 * @author Gerardo Uranga
 * 5/12/2017 : Needs to be fixed. There are still a lot of edits to be made.
 */

public class BedThoughts extends Player {

    protected final String newName = "BedThoughts";
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
    //If Straight, the following variables are used:
    protected int missingRank;
    protected int max;
    protected int max2; // Ace case scenario to treat as rank 15
    protected int min;

    public BedThoughts() {
        super();
        playerName = newName;
    }

    public void initialize() {
        firstTurn = true;
        index = 1;
        myHand = new ArrayList<Card>();
        myHand.add(hand.getHoleCard(0));
        myHand.add(hand.getHoleCard(1));
        max2 = 100;
        // Set up Cases for Straight & Flush Possibilities
        if (myHand.get(0).getRank() > myHand.get(1).getRank()) {
            max = myHand.get(0).getRank();
            min = myHand.get(1).getRank();
        } else {
            max = myHand.get(1).getRank();
            min = myHand.get(0).getRank();
        }
        if (min == 1) { // Ace Case
            max2 = 14;
            min = max;
        }
        if (max - min < 5 && (max - min) != 0) {
            goStraight = true;
            int temp = max;
            max = min + 4;
            min = temp - 4;
        }
        if (max2 - max < 5 && max2 - max != 0) {
            goStraight = true;
            max = max2;
            min = 10;
        }
        goStraight = false; // REMOVE THIS TO ADD GOSTRAIGHT AGAIN ONCE FIXED.
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

    /*
        Rewards need to be tested and calculated. (WIP)
        Idea: Base the rewards on (2x) their current rank
        -> All Numbers below 7 that are pairs are counted as + 0.014
        -> All Numbers 7 and above that are pairs are counted as x2. (0.0rank) * 2
        -> (This allows for there to be a priority on high cards or nodes with lots of pairs)
        -> NOTE: This strategy does not take advantage of permutations, and is naive.

     */
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
                            if (goStraight)
                                reward += 0.75;
                            else if (goFlush)
                                reward += 0.75;
                            else
                                ++reward;
                        } else if (myHand.size() == 4) {
                            if (goStraight)
                                reward += 0.5;
                            else if (goFlush)
                                reward += 0.5;
                            else
                                reward += 1;
                        } else
                            reward += 1;
                    }
                } else if (!opponentHas && card.getRank() != myHand.get(i).getRank()) {
                    if (myHand.size() == 4 && goFlush && goStraight) {
                        if (card.getRank() == missingRank && card.getSuit() == myHand.get(i).getSuit())
                            reward += 2.0;
                    } else {
                        if (card.getSuit() == myHand.get(i).getSuit() && goFlush)
                            reward += 1;
                            // fix goStraight
                        else if (goStraight) {
                            if (max2 != 1000) {
                                if (card.getRank() > min && card.getRank() < 14) {
                                    reward += 1.35;
                                }
                            } else if (card.getRank() > min && card.getRank() < max)
                                ++reward;
                            else if (myHand.size() == 4 && card.getRank() == missingRank)
                                reward += 2.5;
                        } else if (goStraight && card.getSuit() == myHand.get(i).getSuit() && goFlush) {
                            reward += 1.35;
                        }
                    }
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

    /*
        For some reason this isn't correctly updating the goalNode or the graph.
         Double check to make sure nothing dumb is happening.
     */
    protected void opponentAction(int opponentNode, boolean opponentPickedUp, Card c) {
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

    /*
        For some reason this also doesn't correctly update the graph when a card
        is picked.
     */

    protected void actionResult(int currentNode, Card c) {
        this.currentNode = currentNode;
        --turnsRemaining;
       //System.out.println("Node that I am at: " + currentNode);
        if (c != null) {
            this.currentNode = goalNode;
            addCardToHand(c);
            myHand.add(c);
            graph[this.currentNode].clearPossibleCards();
            if (goStraight) { // NEEDS TO UPDATE MAX-MIN VALUES TOO.
                if (c.getRank() <= min && c.getRank() >= max)
                    goStraight = false;
                else {
                    if (myHand.size() == 4) {
                        ArrayList<Integer> ranks = new ArrayList<>();
                        for (int i = 0; i < myHand.size(); i++)
                            ranks.add(myHand.get(i).getRank());
                        Collections.sort(ranks);
                        int j = 0;
                        for (int i = min; i < min + 5; i++) {
                            if (ranks.get(j) != i)
                                missingRank = i;
                            ++j;
                        }
                    } else {
                        for (int i = 0; i < myHand.size(); i++) {
                            if (myHand.get(i).getRank() == c.getRank())
                                goStraight = false;
                        }
                    }
                }

            }
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

    /*
        heuristic to change is the turnsRemaining / myHand part. Perhaps more searching should be allowed
        NOTE: Make sure the assessCards testing is correct for the "getPossibleCards" method I didn't code.
     */

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

    // turnsRemaining at 1 = last move allowed
    //

    // Testing basically everything. Still falls short. RIP.
    // WIP just like the GoStraight function.
    public Action makeAction() {

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

