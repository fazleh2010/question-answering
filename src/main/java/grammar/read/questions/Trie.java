/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grammar.read.questions;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author elahi
 */
public class Trie {
    private TrieNode rootNode;
 
    public Trie() {
        rootNode = new TrieNode(' '); 
    }
 
    public void insertNode(String word) {
        if (searchNode(word) == true) 
            return;    
        
        TrieNode current = rootNode; 
        TrieNode pre ;
        for (char ch : word.toCharArray()) {
        	pre = current;
            TrieNode child = current.getChild(ch);
            if (child != null) {
                current = child;
                child.parent = pre;
            } else {
                 current.children.add(new TrieNode(ch));
                 current = current.getChild(ch);
                 current.parent = pre;
            }
        }
        current.isEnd = true;
    }
    
    public boolean searchNode(String word) {
        TrieNode current = rootNode;      
        for (char ch : word.toCharArray()) {
            if (current.getChild(ch) == null)
                return false;
            else {
                current = current.getChild(ch);    
            }
        }      
        if (current.isEnd == true) {       
            return true;
        }
        return false;
    }
    
    public List performAutocomplete(String prefix) {     
       TrieNode lastNode = rootNode;
       for (int i = 0; i< prefix.length(); i++) {
	       lastNode = lastNode.getChild(prefix.charAt(i));	     
	       if (lastNode == null) 
	    	   return new ArrayList();      
       }
       
       return lastNode.getWords();
    }
}    
 