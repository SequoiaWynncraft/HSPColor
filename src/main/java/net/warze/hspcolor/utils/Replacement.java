package net.warze.hspcolor.utils;

import java.util.regex.Pattern;

/**
 * @author Warze
 */
public class Replacement {
    public final Pattern pattern;
    public final String rolepill;
    public final int rolecolor;
    public final int namecolor;

    public Replacement(Pattern pattern, String rolepill, int rolecolor, int namecolor) {
        this.pattern = pattern;
        this.rolepill = rolepill;
        this.rolecolor = rolecolor;
        this.namecolor = namecolor;
    }
}