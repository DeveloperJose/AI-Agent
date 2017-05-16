/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CardPickup.player;
import CardPickup.*;


/**
 * @author Zachary J Bell
 * @since 4/28/2017
 */

public class CardContainer {

    private Card card;
    private Node node;

    public CardContainer(Card card, Node node) {
        this.card = card;
        this.node = node;
    }

    public Card getCard() {
        return card;
    }

    public Node getNode() {
        return node;
    }
}
