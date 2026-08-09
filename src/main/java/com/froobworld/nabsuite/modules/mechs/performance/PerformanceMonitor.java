package com.froobworld.nabsuite.modules.mechs.performance;

import com.froobworld.nabsuite.modules.mechs.MechsModule;
import me.lucko.spark.api.Spark;
import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.misc.DoubleAverageInfo;
import me.lucko.spark.api.statistic.types.GenericStatistic;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class PerformanceMonitor {
    private static final long INIT_DELAY = 2400;
    private static final long CHECK_FREQ = 1200;
    private static final int NUM_OBS = 60;
    private static final int MIN_OBS = 10;

    private Spark spark;

    private final Deque<Double> tickTimes = new ArrayDeque<>();
    private final Deque<Double> entities = new ArrayDeque<>();
    private final Deque<Double> tileEntities = new ArrayDeque<>();

    public void enable(MechsModule mechsModule) {
        spark = mechsModule.getPlugin().getHookManager().getSparkHook().getSpark();
        if (spark != null) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(mechsModule.getPlugin(), this::tick, INIT_DELAY, CHECK_FREQ);
        }
    }

    public boolean enabled() {
        return spark != null;
    }

    private void tick() {
        GenericStatistic<DoubleAverageInfo, StatisticWindow.MillisPerTick> mspt = spark.mspt();
        if (mspt == null) {
            return;
        }

        // record mspt, entity count and tile entity count
        tickTimes.add(mspt.poll(StatisticWindow.MillisPerTick.MINUTES_1).median());
        entities.add(Bukkit.getWorlds().stream().mapToDouble(World::getEntityCount).sum());
        tileEntities.add(Bukkit.getWorlds().stream().mapToDouble(World::getTileEntityCount).sum());

        if (tickTimes.size() > NUM_OBS) {
            tickTimes.remove();
            entities.remove();
            tileEntities.remove();
        }
    }

    public ContributionEstimateResult getEstimatedMsptContributions() {
        if (tickTimes.size() < MIN_OBS) {
            return null;
        }

        double[] y = tickTimes.stream().mapToDouble(Double::doubleValue).toArray();
        double[][] xBoth = new double[tickTimes.size()][2];
        double[][] xEntitiesOnly = new double[tickTimes.size()][1];
        double[][] xTileEntitiesOnly = new double[tickTimes.size()][1];
        for (int i = 0; i < tickTimes.size(); i++) {
            xBoth[i][0] = (double) entities.toArray()[i];
            xBoth[i][1] = (double) tileEntities.toArray()[i];
            xEntitiesOnly[i][0] = (double) entities.toArray()[i];
            xTileEntitiesOnly[i][0] = (double) tileEntities.toArray()[i];
        }

        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.newSampleData(y, xBoth);

        double[] params = regression.estimateRegressionParameters();
        double c = params[0]; // constant
        double e = params[1]; // entities coefficient
        double t = params[2]; // tile entities coefficient

        // neither coefficient should be negative - if they are, just use the best predictor
        if (e < 0 || t < 0) {
            OLSMultipleLinearRegression regressionEntities = new OLSMultipleLinearRegression();
            OLSMultipleLinearRegression regressionTileEntities = new OLSMultipleLinearRegression();
            regressionEntities.newSampleData(y, xEntitiesOnly);
            regressionTileEntities.newSampleData(y, xTileEntitiesOnly);

            if (regressionEntities.calculateAdjustedRSquared() > regressionTileEntities.calculateAdjustedRSquared()
                    && regressionEntities.estimateRegressionParameters()[1] > 0
                    || regressionTileEntities.estimateRegressionParameters()[1] < 0) {
                e = regressionEntities.estimateRegressionParameters()[1];
                t = 0;
                regression = regressionEntities;
            } else {
                e = 0;
                t = regressionEntities.estimateRegressionParameters()[1];
                regression = regressionTileEntities;
            }
        }

        // estimate player MSPT contributions
        Map<Player, Double> contributions = new HashMap<>();
        for (Map.Entry<Player, double[]> entry : getPlayerEntities().entrySet()) {
            double[] entities = entry.getValue();
            double nEntities = entities[0];
            double nTileEntities = entities[1];

            double msptContribution = e * nEntities + t * nTileEntities;
            contributions.put(entry.getKey(), msptContribution);
        }

        return new ContributionEstimateResult(contributions, c, e, t, regression.calculateAdjustedRSquared());
    }

    private Map<Player, double[]> getPlayerEntities() {
        Map<Player, double[]> entitiesMap = new HashMap<>();


        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                Collection<Player> loadedBy = chunk.getPlayersSeeingChunk();
                for (Player player : loadedBy) {
                    // entities[0] = entities
                    // entities[1] = tile entities
                    double[] entities = entitiesMap.computeIfAbsent(player, p -> new double[2]);

                    entities[0] += (double) chunk.getEntities().length / loadedBy.size();
                    entities[1] += (double) chunk.getTileEntities().length / loadedBy.size();
                }
            }
        }

        return entitiesMap;
    }

    public record ContributionEstimateResult(Map<Player, Double> contribs, double c, double e, double t, double adjRsq) {

    }

}
