package com.froobworld.nabsuite.modules.protect.area.visualiser;

import com.froobworld.nabsuite.modules.protect.area.Area;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AreaVisualisation {
    private final Area area;
    private final List<Line> edgeLines = new ArrayList<>();
    private final List<Line> wallLines = new ArrayList<>();
    private Vector previousCorner1;
    private Vector previousCorner2;

    public AreaVisualisation(Area area) {
        this.area = area;
    }

    public Area getArea() {
        return area;
    }

    private boolean needToCalculateLines() {
        return !area.getCorner1().equals(previousCorner1) || !area.getCorner2().equals(previousCorner2);
    }

    private void calculateLines() {
        edgeLines.clear();
        wallLines.clear();
        int minX = Math.min(area.getCorner1().getBlockX(), area.getCorner2().getBlockX());
        int minY = Math.min(-64, Math.min(area.getCorner1().getBlockY(), area.getCorner2().getBlockY()));
        int minZ = Math.min(area.getCorner1().getBlockZ(), area.getCorner2().getBlockZ());
        int maxX = Math.max(area.getCorner1().getBlockX(), area.getCorner2().getBlockX()) + 1;
        int maxY = Math.max(320, Math.max(area.getCorner1().getBlockY(), area.getCorner2().getBlockY()) + 1);
        int maxZ = Math.max(area.getCorner1().getBlockZ(), area.getCorner2().getBlockZ()) + 1;

        edgeLines.addAll(List.of(
                new Line(Line.LineType.EDGE, new Vector(minX, minY, minZ), new Vector(maxX, minY, minZ)),
                new Line(Line.LineType.EDGE, new Vector(minX, minY, minZ), new Vector(minX, maxY, minZ)),
                new Line(Line.LineType.EDGE, new Vector(minX, minY, minZ), new Vector(minX, minY, maxZ)),

                new Line(Line.LineType.EDGE, new Vector(maxX, maxY, maxZ), new Vector(minX, maxY, maxZ)),
                new Line(Line.LineType.EDGE, new Vector(maxX, maxY, maxZ), new Vector(maxX, minY, maxZ)),
                new Line(Line.LineType.EDGE, new Vector(maxX, maxY, maxZ), new Vector(maxX, maxY, minZ)),

                new Line(Line.LineType.EDGE, new Vector(maxX, minY, minZ), new Vector(maxX, maxY, minZ)),
                new Line(Line.LineType.EDGE, new Vector(maxX, minY, minZ), new Vector(maxX, minY, maxZ)),

                new Line(Line.LineType.EDGE, new Vector(minX, maxY, minZ), new Vector(maxX, maxY, minZ)),
                new Line(Line.LineType.EDGE, new Vector(minX, maxY, minZ), new Vector(minX, maxY, maxZ)),

                new Line(Line.LineType.EDGE, new Vector(minX, minY, maxZ), new Vector(maxX, minY, maxZ)),
                new Line(Line.LineType.EDGE, new Vector(minX, minY, maxZ), new Vector(minX, maxY, maxZ))
        ));

        for (int x = minX + 1; x < maxX; x++) {
            wallLines.add(new Line(Line.LineType.WALL, new Vector(x, minY, minZ), new Vector(x, maxY, minZ)));
            wallLines.add(new Line(Line.LineType.WALL, new Vector(x, minY, maxZ), new Vector(x, maxY, maxZ)));
        }
        for (int z = minZ + 1; z < maxZ; z++) {
            wallLines.add(new Line(Line.LineType.WALL, new Vector(minX, minY, z), new Vector(minX, maxY, z)));
            wallLines.add(new Line(Line.LineType.WALL, new Vector(maxX, minY, z), new Vector(maxX, maxY, z)));
        }

        previousCorner1 = area.getCorner1();
        previousCorner2 = area.getCorner2();
    }

    public void sendToPlayer(Player player) {
        if (!player.getWorld().equals(area.getWorld())) {
            return;
        }
        if (needToCalculateLines()) {
            calculateLines();
        }
        edgeLines.forEach(line -> line.sendToPlayer(player));
        wallLines.forEach(line -> line.sendToPlayer(player));
    }

    private static final class Line {
        private final LineType type;
        private final Vector start;
        private final Vector end;
        private long lastSend = -1;

        private Line(LineType type, Vector start, Vector end) {
            this.type = type;
            this.start = start;
            this.end = end;
        }

        public void sendToPlayer(Player player) {
            if (System.currentTimeMillis() - lastSend < type.rateLimit) {
                return;
            }
            if (!shouldRenderLine(player)) {
                return;
            }
            int steps = (int) Math.floor(end.clone().subtract(start).length()) * type.stepSize;
            Vector unit = end.clone().subtract(start).normalize().multiply(1.0 / (double) type.stepSize);

            for (int i = 0; i <= steps; i++) {
                Vector nextPoint = start.clone().add(unit.clone().multiply(i));
                if (!shouldRenderPoint(player, nextPoint)) {
                    continue;
                }
                int particleIndex = i % type.particle.length;
                player.spawnParticle(type.particle[particleIndex], nextPoint.toLocation(player.getWorld()), 1, 0, 0, 0, 0, type.particleData[particleIndex]);
            }
            lastSend = System.currentTimeMillis();
        }

        private boolean shouldRenderLine(Player player) {
            if (type == LineType.EDGE) {
                return true;
            }
            double distance = Math.max(Math.abs(player.getLocation().getBlockX() - start.getBlockX()), Math.abs(player.getLocation().getBlockZ() - start.getBlockZ()));
            return distance < 20;
        }

        private boolean shouldRenderPoint(Player player, Vector vector) {
            if (type == LineType.EDGE) {
                return true;
            }
            double distance = Math.max(Math.abs(player.getLocation().getBlockX() - vector.getBlockX()), Math.abs(player.getLocation().getBlockZ() - vector.getBlockZ()));
            distance = Math.max(distance, Math.abs(player.getLocation().getBlockY() - vector.getBlockY()));
            return distance < 20;
        }

        public LineType type() {
            return type;
        }

        public Vector start() {
            return start;
        }

        public Vector end() {
            return end;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Line) obj;
            return Objects.equals(this.type, that.type) &&
                    Objects.equals(this.start, that.start) &&
                    Objects.equals(this.end, that.end);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, start, end);
        }

        @Override
        public String toString() {
            return "Line[" +
                    "type=" + type + ", " +
                    "start=" + start + ", " +
                    "end=" + end + ']';
        }


        public enum LineType {
            EDGE(new Particle[]{Particle.GLOW_SQUID_INK, Particle.BLOCK_MARKER}, new Object[]{null, Material.BARRIER.createBlockData()}, 1, TimeUnit.MILLISECONDS.toMillis(200)),
            WALL(new Particle[]{Particle.FLAME}, new Object[]{null}, 1, TimeUnit.MILLISECONDS.toMillis(400));

            public final Particle[] particle;
            public final Object[] particleData;
            public final int stepSize;
            public final long rateLimit;

            LineType(Particle[] particle, Object[] particleData, int stepSize, long rateLimit) {
                this.particle = particle;
                this.particleData = particleData;
                this.stepSize = stepSize;
                this.rateLimit = rateLimit;
            }
        }

    }

}
