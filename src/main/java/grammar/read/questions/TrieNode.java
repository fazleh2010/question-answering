/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grammar.read.questions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author elahi
 */
public class TrieNode {

    char data;
    LinkedList<TrieNode> children;
    TrieNode parent;
    boolean isEnd;

    public TrieNode(char c) {
        data = c;
        this.children = new LinkedList<TrieNode>();
        isEnd = false;
    }

    public TrieNode getChild(char c) {
        if (children != null) {
            for (TrieNode eachChild : children) {
                if (eachChild.data == c) {
                    return eachChild;
                }
            }
        }
        return null;
    }

    protected List getWords() {
        List list = new ArrayList();
        if (isEnd) {
            list.add(toString());
        }

        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                if (children.get(i) != null) {
                    list.addAll(children.get(i).getWords());
                }
            }
        }
        return list;
    }

    public String toString() {
        if (parent == null) {
            return "";
        } else {
            return parent.toString() + new String(new char[]{data});
        }
    }
}
