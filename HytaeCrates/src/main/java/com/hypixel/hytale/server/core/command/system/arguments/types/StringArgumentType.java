package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.server.core.command.system.ParseResult;

/**
 * Argument type for parsing string arguments.
 * Extends SingleArgumentType from the Hytale Server API.
 */
public class StringArgumentType extends SingleArgumentType<String> {

    private final StringType type;

    private StringArgumentType(StringType type) {
        super("<string>", "A text value");
        this.type = type;
    }

    /**
     * Creates an argument type that matches a single word (no spaces).
     */
    public static StringArgumentType word() {
        return new StringArgumentType(StringType.WORD);
    }

    /**
     * Creates an argument type that matches a quoted string.
     */
    public static StringArgumentType string() {
        return new StringArgumentType(StringType.STRING);
    }

    /**
     * Creates an argument type that matches the rest of the input (greedy).
     */
    public static StringArgumentType greedyString() {
        return new StringArgumentType(StringType.GREEDY);
    }

    @Override
    public String parse(String input, ParseResult result) {
        return input;
    }

    public StringType getStringType() {
        return type;
    }

    public enum StringType {
        WORD,
        STRING,
        GREEDY
    }
}

