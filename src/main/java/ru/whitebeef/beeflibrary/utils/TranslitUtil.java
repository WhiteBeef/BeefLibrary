package ru.whitebeef.beeflibrary.utils;

import java.util.HashMap;
import java.util.Map;

public class TranslitUtil {

    private static final Map<Character, String> letters = new HashMap<>() {{
        put('а', "a");
        put('б', "b");
        put('в', "v");
        put('г', "g");
        put('д', "d");
        put('е', "e");
        put('ё', "e");
        put('ж', "zh");
        put('з', "z");
        put('и', "i");
        put('й', "i");
        put('к', "k");
        put('л', "l");
        put('м', "m");
        put('н', "n");
        put('о', "o");
        put('п', "p");
        put('р', "r");
        put('с', "s");
        put('т', "t");
        put('у', "u");
        put('ф', "f");
        put('х', "h");
        put('ц', "c");
        put('ч', "ch");
        put('ш', "sh");
        put('щ', "sch");
        put('ы', "y");
        put('ъ', "");
        put('ь', "");
        put('э', "e");
        put('ю', "yu");
        put('я', "ya");
    }};


    public static String translit(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (char ch : text.toCharArray()) {
            sb.append(letters.getOrDefault(Character.toLowerCase(ch), String.valueOf(ch)));
        }
        return sb.toString();
    }
}
