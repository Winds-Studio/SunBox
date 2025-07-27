package cn.dreeam.sunbox.util;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Objects;

/**
 * Modified version of `net.minecraft.network.chat.TextColor` for benchmark usage.
 */
public final class TextColor {
    private final int value;
    @Nullable
    public final String name;
    // CraftBukkit start
    @Nullable
    public final ChatFormatting format;

    private TextColor(int value, String name, ChatFormatting format) {
        this.value = value & 16777215;
        this.name = name;
        this.format = format;
    }
    // CraftBukkit end

    public static TextColor of(int value, String name, ChatFormatting format) {
        return new TextColor(value, name, format);
    }

    public String serialize() {
        return this.name != null ? this.name : this.formatValue();
    }

    private String formatValue() {
        return String.format(Locale.ROOT, "#%06X", this.value);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other != null && this.getClass() == other.getClass()) {
            TextColor textColor = (TextColor) other;
            return this.value == textColor.value;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value, this.name);
    }

    @Override
    public String toString() {
        return this.serialize();
    }
}
