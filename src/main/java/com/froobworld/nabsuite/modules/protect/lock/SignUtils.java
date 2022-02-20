package com.froobworld.nabsuite.modules.protect.lock;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.WallSign;

import java.util.UUID;

public final class SignUtils {

    private SignUtils() {}

    public static Block[] getAdjacentBlocks(Block block) {
        Block[] faces = {
                block.getRelative(1, 0, 0),
                block.getRelative(0, 0, 1),
                block.getRelative(-1, 0, 0),
                block.getRelative(0, 0, -1)
        };

        return faces;
    }

    public static Block getAttachedBlock(Block sb) {
        if (sb.getBlockData() instanceof WallSign) {
            return sb.getRelative(((WallSign) sb.getBlockData()).getFacing().getOppositeFace());
        } else {
            return null;
        }
    }

    public static Block getLR(Block block, BlockFace facing, boolean left) {
        if (facing == BlockFace.NORTH) {
            return left ? block.getRelative(BlockFace.WEST) : block.getRelative(BlockFace.EAST);
        }
        if (facing == BlockFace.SOUTH) {
            return left ? block.getRelative(BlockFace.EAST) : block.getRelative(BlockFace.WEST);
        }
        if (facing == BlockFace.WEST) {
            return left ? block.getRelative(BlockFace.SOUTH) : block.getRelative(BlockFace.NORTH);
        }
        if (facing == BlockFace.EAST) {
            return left ? block.getRelative(BlockFace.NORTH) : block.getRelative(BlockFace.SOUTH);
        }

        return null;
    }

    public static String encodeUUID(UUID uuid) {
        String str = uuid.toString();
        String concat = "";
        for (char c : str.toCharArray()) {
            concat += "§" + getEncoding(c);
        }

        return concat;
    }

    public static UUID decodeUUID(String line) {
        String[] split = line.split("§=");

        if (split.length < 2) {
            return null;
        }
        String decoded = "";
        for (String s : split[1].replaceFirst("§", "").split("§")) {
            decoded += getDecoded(s);
        }

        return UUID.fromString(decoded);
    }

    public static String decodeName(String line) {
        String[] split = line.split("§=");

        return split[0];
    }

    public static String encodeName(String name, UUID uuid) {

        return name + "§=" + encodeUUID(uuid);
    }

    private static char getEncoding(char c) {
        switch (c) {
            case '0':
                return ')';
            case '1':
                return '!';
            case '2':
                return '@';
            case '3':
                return '#';
            case '4':
                return '$';
            case '5':
                return '%';
            case '6':
                return '^';
            case '7':
                return '&';
            case '8':
                return '*';
            case '9':
                return '(';
            case 'a':
                return '_';
            case 'b':
                return '+';
            case 'c':
                return '[';
            case 'd':
                return ']';
            case 'e':
                return '{';
            case 'f':
                return '}';
            case '-':
                return '-';
        }

        return c;
    }

    private static char getDecoded(String s) {
        switch (s) {
            case ")":
                return '0';
            case "!":
                return '1';
            case "@":
                return '2';
            case "#":
                return '3';
            case "$":
                return '4';
            case "%":
                return '5';
            case "^":
                return '6';
            case "&":
                return '7';
            case "*":
                return '8';
            case "(":
                return '9';
            case "_":
                return 'a';
            case "+":
                return 'b';
            case "[":
                return 'c';
            case "]":
                return 'd';
            case "{":
                return 'e';
            case "}":
                return 'f';
            case "-":
                return '-';
        }

        return 'x';
    }

}
