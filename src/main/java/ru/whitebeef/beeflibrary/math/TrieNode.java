package ru.whitebeef.beeflibrary.math;

import java.util.HashMap;
import java.util.Map;

public class TrieNode {
    private boolean isEndOfWord = false;
    private final Map<Character, TrieNode> children = new HashMap<>();

    public TrieNode() {
    }

    public boolean isEndOfWord() {
        return isEndOfWord;
    }

    public void setEndOfWord(boolean endOfWord) {
        isEndOfWord = endOfWord;
    }

    public int getChildrenCount() {
        return children.size();
    }

    public void removeChild(Character character) {
        children.remove(character);
    }

    public void setChild(Character children, TrieNode trieNode) {
        this.children.put(children, trieNode);
    }

    public TrieNode getChild(Character character) {
        return children.get(character);
    }
}

