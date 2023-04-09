package ru.whitebeef.beeflibrarytest;

import org.bukkit.Bukkit;
import org.junit.jupiter.api.Test;
import ru.whitebeef.beeflibrary.chat.MessageFormatter;
import ru.whitebeef.beeflibrary.utils.Color;


public class TextTest {
    @Test
    public void textTest() {
        System.out.println(Color.colorize("#75bfffПрогресс: #bfbfbf5 4 #75bfff/#bfbfbfMAX"));
        System.out.println(MessageFormatter.of("#75bfffПрогресс: #bfbfbf5 4 #75bfff/#bfbfbfMAX").toComponent(null));
    }

}
