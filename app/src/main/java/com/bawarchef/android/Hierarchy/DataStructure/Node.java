package com.bawarchef.android.Hierarchy.DataStructure;

import java.io.Serializable;
import java.util.ArrayList;

public class Node implements Serializable {
    Node parent=null;
    String nodeText="";
    ArrayList<Node> children=null;

    public Node(Node parent,String nodeText,boolean expandable){
        this.parent = parent;
        this.nodeText = nodeText;
        if(expandable)
            children = new ArrayList<Node>();
    }

    public int childrenCount(){
        return children.size();
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public String getNodeText() {
        return nodeText;
    }

    public void setNodeText(String nodeText) {
        this.nodeText = nodeText;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<Node> children) {
        this.children = children;
    }

    public void add(Node node){
        children.add(node);
    }
}
