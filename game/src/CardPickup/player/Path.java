/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CardPickup.player;
import CardPickup.*;

import java.util.LinkedList;

/**
 * @author Zachary J Bell
 * @since 4/28/2017
 */

public class Path {

    private int distance;
    private double nodeValue = 0.0;
    private LinkedList<Node> nodes;

    Path(int distance, LinkedList<Node> nodes) {
        this.distance = distance;
        this.nodes = nodes;
    }

    public int getDistance() {
        return distance;
    }
    
    public void setDistance(int distance) {
        this.distance = distance;
    }
    
    public double getNodeValue() {
        return this.nodeValue;
    }
    
    public void setNodeValue(double nodeValue) {
        this.nodeValue = nodeValue;
    }

    public LinkedList<Node> getNodes() {
        return nodes;
    }
}