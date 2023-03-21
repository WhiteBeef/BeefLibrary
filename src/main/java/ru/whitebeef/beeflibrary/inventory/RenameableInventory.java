package ru.whitebeef.beeflibrary.inventory;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface RenameableInventory {

    void rename(@NotNull Component newName);

}
