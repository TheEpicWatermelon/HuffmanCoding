package com.company;

/**
 * Node Class
 * Version 1.0
 * @author Sasha Maximovitch
 * @date October 5th, 2017
 * Stores information about each node in the binary tree
 * Contains:
 * Byte - Byte of the node, well be null if it is not a leaf
 * Frequency - the frequency value of the node, if not a leaf it will be the sum of all the nodes before
 * Left - the node that goes to the left to continue the branch, will be null if it is a leaf
 * Right - the node that goes to the right to continue the branch, will be null if it is a leaf
 */


public class Node {
    // The Byte of the node
    Byte b;
    // the frequency value of the node
    int frequency;
    // node to the left and down of this node
    Node left;
    // node to the right and down of this node
    Node right;

    // Make it able to print out the Node, prints out the character and the frequency value
    @Override
    public String toString() {
        return "'"+b+"':"+frequency;
    }// end toString

}// end Node Class