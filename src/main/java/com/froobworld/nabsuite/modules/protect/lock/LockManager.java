package com.froobworld.nabsuite.modules.protect.lock;

import com.destroystokyo.paper.MaterialTags;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.player.PlayerDataManager;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.*;
import org.bukkit.block.data.type.Bed.Part;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.block.data.type.Door.Hinge;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

// TODO - Dear god, start over
public class LockManager {
    public static final String PERM_BYPASS_LOCKS = "nabsuite.bypasslocks";
    public static final String LOCK_HEADER = "[Private]";
    public static final String MORE_HEADER = "[More users]";
    public static final String FRIENDS = "[Friends]";
    public static final String EVERYONE = "[Everyone]";
    public static final String ERROR_HEADER = "[?]";
    private static final long CACHE_ITEM_LIFE = 60000L;
    public Map<Player, Location> selectedSigns = new HashMap<>();
    private final Map<Location, CacheItem> lockCache = new HashMap<>();
    private final PlayerDataManager playerDataManager;
    private final ProtectModule protectModule;

    public LockManager(ProtectModule protectModule) {
        this.protectModule = protectModule;
        playerDataManager = protectModule.getPlugin().getModule(BasicsModule.class).getPlayerDataManager();
        Bukkit.getPluginManager().registerEvents(new LockListener(this), protectModule.getPlugin());
    }

    public UUID getOwner(Location location, boolean useCache) {
        if (useCache) {
            CacheItem cacheItem = lockCache.get(location);
            if (cacheItem == null || System.currentTimeMillis() > cacheItem.getCreated() + cacheItem.getLife()) {
                UUID owner = getOwner(location, false);
                lockCache.put(location, new CacheItem(owner, CACHE_ITEM_LIFE));
                return owner;
            } else {
                return cacheItem.getOwner();
            }
        }

        ArrayList<Sign> relevantSigns = getAllRelevantSigns(location.getBlock());
        for (Sign sign : relevantSigns) {
            if (SignUtils.getLine(sign, 0).equalsIgnoreCase(LOCK_HEADER)) {
                UUID owner = SignUtils.decodePlayer(sign.getSide(Side.FRONT).line(1));
                if (owner == null) {
                    // Sign might have been rolled back, try to populate uuids from names again for all signs
                    for (Sign sign2 : relevantSigns) {
                        if (isLockSign(sign2)) {
                            restoreSignUuids(sign2);
                        }
                    }
                    owner = SignUtils.decodePlayer(sign.getSide(Side.FRONT).line(1));
                }
                return owner;
            }
        }

        return null;
    }

    private void restoreSignUuids(Sign sign) {
        boolean save = false;
        for (int i = 1; i <= 3; i++) {
            TextComponent row = (TextComponent)sign.getSide(Side.FRONT).line(i);
            if (row.content().equalsIgnoreCase(FRIENDS) || row.content().equalsIgnoreCase(EVERYONE)) {
                continue;
            }
            HoverEvent<?> hoverEvent = row.hoverEvent();
            if (hoverEvent != null && hoverEvent.value() instanceof HoverEvent.ShowEntity) {
                continue;
            }
            Set<PlayerIdentity> players = protectModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(row.content());
            if (players.size() == 1) {
                PlayerIdentity player = players.stream().findFirst().get();
                sign.getSide(Side.FRONT).line(i, SignUtils.encodePlayer(player));
                save = true;
            }
        }
        if (save) {
            sign.update(true);
        }
    }

    public boolean isUser(Location location, Player player) {
        ArrayList<Sign> relevantSigns = getAllRelevantSigns(location.getBlock());
        UUID owner = getOwner(location, false);
        if (owner == null) {
            return true;
        }
        for (Sign sign : relevantSigns) {
            if (isLockSign(sign)) {
                for (int i = 1; i <= 3; i++) {
                    if (SignUtils.getLine(sign, i).equalsIgnoreCase(EVERYONE)) {
                        return true;
                    }
                    if (SignUtils.getLine(sign, i).equalsIgnoreCase(FRIENDS)) {
                        if (playerDataManager.getFriendManager().areFriends(player, owner)) {
                            return true;
                        }
                    }
                    String name = SignUtils.getLine(sign, i);
                    UUID uuid = SignUtils.decodePlayer(sign.getSide(Side.FRONT).line(i));
                    if (player.getUniqueId().equals(uuid)) {
                        if (!Objects.equals(name, player.getName())) {
                            sign.getSide(Side.FRONT).line(i, SignUtils.encodePlayer(player));
                            sign.update(true);
                        }
                        return true;
                    }
                    if (uuid == null && name.equalsIgnoreCase(player.getName())) {
                        sign.getSide(Side.FRONT).line(i, SignUtils.encodePlayer(player));
                        sign.update(true);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public ArrayList<Sign> getAllRelevantSigns(Block block) {
        ArrayList<Sign> list = new ArrayList<>();
        for (Block b : getRelevantBlocks(block)) {
            list.addAll(getSigns(b));
        }

        return list;
    }

    public ArrayList<Sign> getSigns(Block block) {
        ArrayList<Sign> list = new ArrayList<>();
        Block[] faces = SignUtils.getAdjacentBlocks(block);

        for (Block face : faces) {
            if (face.getBlockData() instanceof WallSign) {
                if (SignUtils.getAttachedBlock(face).equals(block)) {
                    list.add((Sign) face.getState());
                }
            }
        }

        return list;
    }

    public ArrayList<Block> getRelevantBlocks(Block block) {
        ArrayList<Block> blocks = new ArrayList<>();
        blocks.add(block);
        if (block.getBlockData() instanceof Chest) {
            Chest chest = (Chest) block.getBlockData();
            if (chest.getType() != Type.SINGLE) {
                blocks.add(SignUtils.getLR(block, chest.getFacing(), !(chest.getType() == Type.LEFT ? true : false)));
            }
        }
        if (block.getBlockData() instanceof Door) {
            blocks = new ArrayList<>();
            Door door = (Door) block.getBlockData();
            Door doorTop;
            if (door.getHalf() == Half.TOP) {
                doorTop = door;
                door = (Door) block.getRelative(0, -1, 0).getBlockData();
                block = block.getRelative(0, -1, 0);
            } else {
                doorTop = (Door) block.getRelative(0, 1, 0).getBlockData();
            }
            for (int i = -1; i <= 2; i++) {
                blocks.add(block.getRelative(0, i, 0));
            }

            Block nextTo = SignUtils.getLR(block, door.getFacing(), !(doorTop.getHinge() == Hinge.LEFT ? true : false));
            if (nextTo.getBlockData() instanceof Door) {
                Door door2 = (Door) nextTo.getBlockData();
                Door door2Top = (Door) nextTo.getRelative(0, 1, 0).getBlockData();
                if (!(door2.getHalf() == Half.TOP) && door2.getFacing() == door.getFacing()
                        && door2Top.getHinge() != doorTop.getHinge()) {
                    for (int i = -1; i <= 2; i++) {
                        blocks.add(nextTo.getRelative(0, i, 0));
                    }
                }
            }

        }
        if (block.getBlockData() instanceof TrapDoor) {
            TrapDoor door = (TrapDoor) block.getBlockData();
            blocks.add(block.getRelative(door.getFacing().getOppositeFace()));
        }
        if (block.getBlockData() instanceof Bed) {
            Bed bed = (Bed) block.getBlockData();
            if (bed.getPart() == Part.HEAD) {
                blocks.add(block.getRelative(bed.getFacing().getOppositeFace()));
            } else {
                blocks.add(block.getRelative(bed.getFacing()));
            }
        }
        if (block.getType() == Material.ANVIL) {
            blocks.add(block.getRelative(0, -1, 0));
        }

        return blocks;
    }

    public ArrayList<Block> getAttachedLockables(Block block) {
        ArrayList<Block> attached = new ArrayList<>();
        if (isLockable(block.getType())) {
            attached.add(block);
        }
        for (Block adj : SignUtils.getAdjacentBlocks(block)) {
            if (adj.getBlockData() instanceof TrapDoor) {
                TrapDoor door = (TrapDoor) adj.getBlockData();
                if (adj.getRelative(door.getFacing().getOppositeFace()).getLocation().equals(block.getLocation())) {
                    attached.add(adj);
                }
            }
        }
        Block up = block.getRelative(0, 1, 0);
        Block down = block.getRelative(0, -1, 0);

        if (down.getBlockData() instanceof Door) {
            attached.add(down);
        }
        if (up.getBlockData() instanceof Door || up.getType() == Material.ANVIL) {
            attached.add(up);
        }

        return attached;
    }

    public boolean isLockable(Material material) {

        return material == Material.CHEST ||
                material == Material.TRAPPED_CHEST ||
                MaterialTags.DOORS.isTagged(material) ||
                MaterialTags.TRAPDOORS.isTagged(material) ||
                MaterialTags.SHULKER_BOXES.isTagged(material) ||
                MaterialTags.BEDS.isTagged(material) ||
                MaterialTags.FENCE_GATES.isTagged(material) ||
                material == Material.HOPPER ||
                material == Material.ANVIL ||
                material == Material.BEACON ||
                material == Material.BREWING_STAND ||
                material == Material.FURNACE ||
                material == Material.DROPPER ||
                material == Material.DISPENSER ||
                material == Material.JUKEBOX ||
                material == Material.ENDER_CHEST ||
                material == Material.ENCHANTING_TABLE ||
                material == Material.BARREL;
    }

    public boolean isLockSign(Sign sign) {
        return SignUtils.getLine(sign, 0).equalsIgnoreCase(LOCK_HEADER) || SignUtils.getLine(sign, 0).equalsIgnoreCase(MORE_HEADER);
    }

    public boolean isLockSign(SignChangeEvent sign) {
        return SignUtils.getLine(sign, 0).equalsIgnoreCase(LOCK_HEADER) || SignUtils.getLine(sign, 0).equalsIgnoreCase(MORE_HEADER);
    }

    public void updateUser(Sign sign, int index, String name, UUID uuid) {
        sign.getSide(Side.FRONT).line(index, SignUtils.encodePlayer(name, uuid));
        sign.update();
    }

    public void onBlockBreak(BlockBreakEvent event) {
        for (Block block : getAttachedLockables(event.getBlock())) {
            UUID owner = getOwner(block.getLocation(), false);
            if (!event.getPlayer().hasPermission(PERM_BYPASS_LOCKS) && owner != null && !event.getPlayer().getUniqueId().equals(owner)) {
                event.setCancelled(true);
                return;
            }
        }
        if (event.getBlock().getBlockData() instanceof WallSign) {
            Sign sign = (Sign) event.getBlock().getState();
            WallSign wallSign = (WallSign) event.getBlock().getBlockData();
            Block connected = event.getBlock().getRelative(wallSign.getFacing().getOppositeFace());

            if (getAttachedLockables(connected).isEmpty()) {
                return;
            }
            if (isLockSign(sign)) {
                UUID owner = getOwner(connected.getLocation(), false);
                if (!event.getPlayer().hasPermission(PERM_BYPASS_LOCKS) && !event.getPlayer().getUniqueId().equals(owner)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getBlockData() instanceof WallSign) {
            Sign sign = (Sign) event.getClickedBlock().getState();
            if (isLockSign(sign)) {
                if (!selectedSigns.containsKey(event.getPlayer()) || selectedSigns.get(event.getPlayer()) != event.getClickedBlock().getLocation()) {
                    selectedSigns.put(event.getPlayer(), event.getClickedBlock().getLocation());
                    event.getPlayer().sendMessage(Component.text("Sign selected. Use /lock to edit it.").color(NamedTextColor.YELLOW));
                }

            }
        }
        for (Block block : getAttachedLockables(event.getClickedBlock())) {
            if (!event.getPlayer().hasPermission(PERM_BYPASS_LOCKS) && !isUser(block.getLocation(), event.getPlayer())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    public void onBlockExplode(BlockExplodeEvent event) {
        ArrayList<Block> toRemove = new ArrayList<>();
        for (Block b : event.blockList()) {
            if (b.getBlockData() instanceof WallSign) {
                Sign sign = (Sign) b.getState();
                if (isLockSign(sign)) {
                    toRemove.add(b);
                }
                continue;
            }
            for (Block block : getAttachedLockables(b)) {
                if (getOwner(block.getLocation(), false) != null) {
                    toRemove.add(b);
                    break;
                }
            }
        }
        event.blockList().removeAll(toRemove);
    }

    public void onEntityExplode(EntityExplodeEvent event) {
        ArrayList<Block> toRemove = new ArrayList<>();
        for (Block b : event.blockList()) {
            if (b.getBlockData() instanceof WallSign) {
                Sign sign = (Sign) b.getState();
                if (isLockSign(sign)) {
                    toRemove.add(b);
                }
                continue;
            }
            for (Block block : getAttachedLockables(b)) {
                if (getOwner(block.getLocation(), false) != null) {
                    toRemove.add(b);
                    break;
                }
            }
        }
        event.blockList().removeAll(toRemove);
    }

    public void onBlockBurn(BlockBurnEvent event) {
        for (Block block : getAttachedLockables(event.getBlock())) {
            if (getOwner(block.getLocation(), false) != null) {
                event.setCancelled(true);
                break;
            }
        }
    }

    public void onSignChange(SignChangeEvent event) {
        if (!(event.getBlock().getBlockData() instanceof WallSign)) {
            return;
        }
        if (!isLockSign(event) && !(SignUtils.getLine(event, 0).isEmpty() && SignUtils.getLine(event, 1).isEmpty() && SignUtils.getLine(event, 2).isEmpty() && SignUtils.getLine(event, 3).isEmpty())) {
            return;
        }
        WallSign wallSign = (WallSign) event.getBlock().getBlockData();
        Block connected = event.getBlock().getRelative(wallSign.getFacing().getOppositeFace());

        if (getAttachedLockables(connected).isEmpty()) {
            if (isLockSign(event)) {
                event.line(0, Component.text(ERROR_HEADER));
                event.getPlayer().sendMessage(Component.text("This cannot be locked.").color(NamedTextColor.RED));
            }
            return;
        }

        UUID owner = getOwner(connected.getLocation(), false);
        if (owner != null && !owner.equals(event.getPlayer().getUniqueId())) {
            event.line(0, Component.text(ERROR_HEADER));
            event.getPlayer().sendMessage(Component.text("This is already locked.").color(NamedTextColor.RED));
            return;
        }
        if (owner == null) {
            event.line(0, Component.text(LOCK_HEADER));
            event.line(1, SignUtils.encodePlayer(event.getPlayer().getName(), event.getPlayer().getUniqueId()));
        } else {
            event.line(0, Component.text(MORE_HEADER));
            event.line(1, Component.text(FRIENDS));
        }
    }

    private static class CacheItem {
        private final UUID owner;
        private final long created;
        private final long life;

        public CacheItem(UUID owner, long life) {
            this.owner = owner;
            this.created = System.currentTimeMillis();
            this.life = life;
        }

        public UUID getOwner() {

            return owner;
        }

        public long getCreated() {

            return created;
        }

        public long getLife() {

            return life;
        }
    }

}
