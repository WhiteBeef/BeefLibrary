package ru.whitebeef.beeflibrarytest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.whitebeef.beeflibrary.utils.FlagSetting;

import java.util.BitSet;

public class FlagSettingTest {

    @Test
    public void testFlagSetting() {
        FlagSetting flagSetting = FlagSetting.builder()
                .addSetting("firstsetting")
                .addSetting("secondsetting")
                .addSetting("FirstSetting")
                .addSetting("SecondSetting")
                .addSetting("FIRSTSETTING")
                .addSetting("SECONDSETTING")
                .build();

        flagSetting.setSetting("firstsetting", false);
        flagSetting.setSetting("secondsetting", true);
        flagSetting.setSetting("FirstSetting", false);
        flagSetting.setSetting("SecondSetting", true);
        flagSetting.setSetting("FIRSTSETTING", true);

        Assertions.assertFalse(flagSetting.getSetting("firstsetting"));
        Assertions.assertTrue(flagSetting.getSetting("secondsetting"));
        Assertions.assertFalse(flagSetting.getSetting("FirstSetting"));
        Assertions.assertTrue(flagSetting.getSetting("SecondSetting"));
        Assertions.assertTrue(flagSetting.getSetting("FIRSTSETTING"));
        Assertions.assertFalse(flagSetting.getSetting("SECONDSETTING"));

    }
}
