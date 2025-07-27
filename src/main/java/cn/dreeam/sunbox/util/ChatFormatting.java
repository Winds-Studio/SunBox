package cn.dreeam.sunbox.util;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Locale;

/**
 * Modified version of `net.minecraft.network.ChatFormatting` for benchmark usage.
 */
public enum ChatFormatting {
    BLACK("BLACK", '0', false, 0, 0),
    DARK_BLUE("DARK_BLUE", '1', false, 1, 170),
    DARK_GREEN("DARK_GREEN", '2', false, 2, 43520),
    DARK_AQUA("DARK_AQUA", '3', false, 3, 43690),
    DARK_RED("DARK_RED", '4', false, 4, 11141120),
    DARK_PURPLE("DARK_PURPLE", '5', false, 5, 11141290),
    GOLD("GOLD", '6', false, 6, 16755200),
    GRAY("GRAY", '7', false, 7, 11184810),
    DARK_GRAY("DARK_GRAY", '8', false, 8, 5592405),
    BLUE("BLUE", '9', false, 9, 5592575),
    GREEN("GREEN", 'a', false, 10, 5635925),
    AQUA("AQUA", 'b', false, 11, 5636095),
    RED("RED", 'c', false, 12, 16733525),
    LIGHT_PURPLE("LIGHT_PURPLE", 'd', false, 13, 16733695),
    YELLOW("YELLOW", 'e', false, 14, 16777045),
    WHITE("WHITE", 'f', false, 15, 16777215),
    OBFUSCATED("OBFUSCATED", 'k', true, -1, null),
    BOLD("BOLD", 'l', true, -1, null),
    STRIKETHROUGH("STRIKETHROUGH", 'm', true, -1, null),
    UNDERLINE("UNDERLINE", 'n', true, -1, null),
    ITALIC("ITALIC", 'o', true, -1, null),
    RESET("RESET", 'r', false, -1, null);

    public static final ChatFormatting[] COLOR_VALUES = Arrays.stream(ChatFormatting.values())
            .filter(ChatFormatting::isColor)
            .toArray(ChatFormatting[]::new);
    private final String name;
    public final char code;
    private final boolean isFormat;
    private final String toString;
    private final int id;
    @Nullable
    private final Integer color;
    private final TextColor textColor;

    ChatFormatting(final String name, final char code, final boolean isFormat, final int id, @Nullable final Integer color) {
        this.name = name;
        this.code = code;
        this.isFormat = isFormat;
        this.id = id;
        this.color = color;
        this.toString = "§" + code;
        if (!isFormat && !name.equals("RESET")) {
            this.textColor = TextColor.of(color, this.name().toLowerCase(Locale.ROOT), this);
        } else {
            this.textColor = null;
        }
    }

    public boolean isColor() {
        return !this.isFormat && this != RESET;
    }

    @Nullable
    public Integer getColor() {
        return this.color;
    }

    public String getName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public TextColor getTextColor() {
        return textColor;
    }

    @Override
    public String toString() {
        return this.toString;
    }
}
