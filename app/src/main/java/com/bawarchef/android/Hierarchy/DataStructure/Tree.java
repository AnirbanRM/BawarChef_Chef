package com.bawarchef.android.Hierarchy.DataStructure;

import java.io.Serializable;

public class Tree implements Serializable {

    Node root = null;
    public Tree(String rootString){
        root = new Node(null,rootString,true);
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }
}
