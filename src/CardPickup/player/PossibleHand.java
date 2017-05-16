/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CardPickup.player;
import CardPickup.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zachary J Bell
 * @since 4/28/2017
 */

public class PossibleHand implements Comparable<PossibleHand> {

    private float handValue;   // the value of the hand before probability, ect. is applied
    private List<Card> cardsInHand;
    private List<CardContainer> possibleCards;
    private int numCardsInHand;

    public PossibleHand(Card c1, Card c2) {
        numCardsInHand = 2;
        cardsInHand = new ArrayList<>(numCardsInHand);
    }

    public PossibleHand(Card c1, Card c2, Card c3) {
        numCardsInHand = 3;
        cardsInHand = new ArrayList<>(numCardsInHand);
    }
    public PossibleHand(Card c1, Card c2, Card c3, Card c4) {
        numCardsInHand = 4;
        cardsInHand = new ArrayList<>(numCardsInHand);
    }
    public PossibleHand(Card c1, Card c2, Card c3, Card c4, Card c5) {
        numCardsInHand = 5;
        cardsInHand = new ArrayList<>(numCardsInHand);
    }

    public PossibleHand(ArrayList<CardContainer> list) {
        possibleCards = list;
    }

    public void setHandValue(float handValue) {
        this.handValue = handValue;
    }
    
    public List<CardContainer> getPossibleCards() {
        return this.possibleCards;
    }

    public double getHandValue() {
        return handValue;
    }

    @Override
    public int compareTo(PossibleHand o) {
        return (int) (o.getHandValue() - handValue);
    }
}