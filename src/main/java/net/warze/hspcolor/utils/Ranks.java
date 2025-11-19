package net.warze.hspcolor.utils;

import java.util.Map;

/**
 * @author Warze
 */
public class Ranks {
    public static final Map<String, String> Old = Map.of(
        "owner",       "󏿿󏿿󏿿󏿿󏿿󏿿󏿠",
        "chief",       "󏿿󏿿󏿿󏿿󏿿󏿿󏿢",
        "strategist",  "󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿄",
        "captain",     "󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿖",
        "recruiter",   "󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿊",
        "recruit",     "󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿖"
    );

    public static final Map<String, String> New = Map.of(
        "owner",       "󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿠",
        "chief",       "󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿢",
        "strategist",  "󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿄",
        "captain",     "󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿖",
        "recruiter",   "󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿊",
        "recruit",     "󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿖"
    );

    public static final Map<String, Integer> RoleColor = Map.of(
        "system",     0x279F2C,
        "owner",      0xE91E63,
        "chief",      0xE67E22,
        "strategist", 0x8F4CE2,
        "captain",    0x2ECC71,
        "recruiter",  0x206694,
        "recruit",    0x9EB4BE
    );

    public static final Map<String, Integer> NameColor = Map.of(
        "system",     0x1FB427,
        "owner",      0xE9387B,
        "chief",      0xE6993C,
        "strategist", 0xA66DEC,
        "captain",    0x48CE87,
        "recruiter",  0x508DD8,
        "recruit",    0xB3BDC5
    );
}

