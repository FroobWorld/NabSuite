package com.froobworld.nabsuite.modules.protect.lock;

import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

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

    public static TextComponent encodePlayer(String name, UUID uuid) {
        TextComponent data = Component.text(name);
        if (uuid != null) {
            data = data.hoverEvent(HoverEvent.showEntity(Key.key("player"), uuid, Component.text(name)));
        }
        return data;
    }

    public static TextComponent encodePlayer(Player player) {
        return encodePlayer(player.getName(), player.getUniqueId());
    }

    public static TextComponent encodePlayer(PlayerIdentity player) {
        return encodePlayer(player.getLastName(), player.getUuid());
    }

    public static UUID decodePlayer(Component row) {
        HoverEvent<?> hoverEvent = row.hoverEvent();
        if (hoverEvent != null && hoverEvent.value() instanceof HoverEvent.ShowEntity showEntity) {
            return showEntity.id();
        }
        return null;
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

    public static String getLine(Sign sign, int index) {
        Component line = sign.getSide(Side.FRONT).line(index);
        if (line instanceof TextComponent textLine) {
            return textLine.content();
        }
        return "";
    }

    public static String getLine(SignChangeEvent event, int index) {
        Component line = event.line(index);
        if (line instanceof TextComponent textLine) {
            return textLine.content();
        }
        return "";
    }

}
