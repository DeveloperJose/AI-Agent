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

class PathPair implements Comparable<PathPair> {

    private int distance;
    private Node node;
    private LinkedList<Node> nodeLL;

    public PathPair(int distance, Node node) {
        this.distance = distance;
        this.node = node;
    }

    public PathPair(int distance, Node node, LinkedList<Node> nodeLL) {
        this.distance = distance;
        this.node = node;
        this.nodeLL = nodeLL;
        this.nodeLL.add(node);
    }

    public int getDistance() {
        return distance;
    }

    public Node getNode() {
        return node;
    }

    public LinkedList<Node> getNodeLL() {
        return nodeLL;
    }

    @Override
    public int compareTo(PathPair pathPair) {
        return distance - pathPair.getDistance();
    }
}