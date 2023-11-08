package ru.whitebeef.beeflibrary.math;

import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class Trie {
    private final TrieNode root = new TrieNode();

    private static final Set<Character> specialCharacters = new HashSet<>() {{
        for (char ch : "/*!@#$%^&*()\"{}_[]|\\?/<>,+-=.".toCharArray()) {
            add(ch);
        }
    }};


    public Trie() {
    }

    public void insert(String word) {
        TrieNode current = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            TrieNode node = current.getChild(c);
            if (node == null) {
                node = new TrieNode();
                current.setChild(c, node);
            }
            current = node;
        }
        current.setEndOfWord(true);
    }

    public boolean search(String word, double matchPercent, boolean skipSimilarLiterals) {
        int maxSpecialCharsNum = 0;
        for (int j = 0; j < word.length(); j++) {
            TrieNode current = root;
            int findIndex = (int) -1e7;
            int specialCharsNum = 0;
            for (int i = j; i < word.length(); i++) {
                char ch = word.charAt(i);
                TrieNode node = current.getChild(ch);

                if (specialCharacters.contains(ch)) {
                    specialCharsNum++;
                    continue;
                }
                if (node == null) {
                    if (skipSimilarLiterals && ch == word.charAt(Math.max(i - 1, 0))) {
                        node = current;
                    } else if (current.isEndOfWord()) {
                        findIndex = i;
                        break;
                    } else {
                        break;
                    }
                }
                if (node.isEndOfWord()) {
                    findIndex = i;
                }
                current = node;
            }
            maxSpecialCharsNum = Math.max(maxSpecialCharsNum, specialCharsNum);
            if (1 - ((double) (word.length() - maxSpecialCharsNum) - (findIndex - j) - 1) / (word.length() - maxSpecialCharsNum) >= matchPercent) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public String searchMaximumPrefix(String word) {
        StringBuilder ret = new StringBuilder();
        TrieNode current = root;
        boolean find = false;
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            TrieNode node = current.getChild(ch);

            if (node == null) {
                if (current.isEndOfWord()) {
                    find = true;
                    ret = new StringBuilder().append(word, 0, i);
                }
                break;
            }
            if (node.isEndOfWord()) {
                find = true;
                ret = new StringBuilder().append(word, 0, i);
            }
            current = node;
        }
        return !find ? null : ret.toString();
    }

    public void remove(String word) {
        remove(root, word, 0);
    }

    private boolean remove(TrieNode node, String word, int index) {
        if (index == word.length()) {
            if (!node.isEndOfWord()) {
                return false;
            }
            node.setEndOfWord(false);
            return node.getChildrenCount() == 0;
        }
        char c = word.charAt(index);
        TrieNode child = node.getChild(c);
        if (child == null) {
            return false;
        }
        boolean shouldRemoveChild = remove(child, word, index + 1);
        if (shouldRemoveChild) {
            node.removeChild(c);
            return node.getChildrenCount() == 0 && !node.isEndOfWord();
        }
        return false;
    }

}