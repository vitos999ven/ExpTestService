package taskmanager;

import controllers.RequestParams;
import entities.alternatives.ExpParams;
import entities.alternatives.SelectionParams;
import entities.exceptions.ActionException;
import entities.exceptions.actionreasons.NoDependentData;
import java.util.TreeMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import logic.tests.ExpQuantilesModulator;
import logic.tests.PowerModulator;
import logic.tests.QuantilePair;
import hibernate.logic.IterationsCount;
import hibernate.logic.Power;
import hibernate.logic.Quantile;
import hibernate.logic.SignificanceLevel;
import hibernate.logic.TestType;
import hibernate.util.Factory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import taskmanager.tasks.MultiplePowerModulatorTask;
import taskmanager.tasks.MultipleQuantileModulatorTask;
import taskmanager.tasks.PowerModulatorTask;
import taskmanager.tasks.ProcessTask;
import taskmanager.tasks.QuantilesModulatorTask;

public class TaskManager {

    private final ExecutorService service;
    private final Map<ProcessTask, ProcessObserver> tasks;

    private static class TaskManagerHolder {

        private final static TaskManager instance = new TaskManager();
    }

    public static TaskManager getInstance() {
        return TaskManagerHolder.instance;
    }

    private TaskManager() {
        service = Executors.newFixedThreadPool(10);
        tasks = new TreeMap<>();
    }

    public ProcessObserver<Quantile> doQuantilesModulation(
            final TestType type,
            final SignificanceLevel signLevel,
            final int iterCount,
            final int selSize) throws ActionException {
        QuantilesModulatorTask task = new QuantilesModulatorTask(type, signLevel, iterCount, selSize);

        final ProcessObserver<Quantile> observer;

        synchronized (tasks) {
            if (tasks.containsKey(task)) {
                observer = tasks.get(task);
                if (observer == null || observer.isFinished()) {
                    tasks.remove(task);
                }

                return observer;
            }

            observer = new ProcessObserver<>(task, iterCount);

            observer.setFuture(service.submit(() -> {
                Quantile result = null;
                try {
                    QuantilePair pair = ExpQuantilesModulator.modulateQuantiles(type, signLevel, iterCount, selSize, observer);

                    Factory factory = Factory.getInstance();

                    IterationsCount count = Factory.getInstance().getIterationsCountsDAO().getIterationsCount(type, iterCount);

                    if (count == null) {
                        throw new ActionException(new NoDependentData(RequestParams.ITERATIONS_COUNT));
                    }

                    Quantile quantile = factory.getQuantilesDAO().getQuantile(type, signLevel, count, selSize);
                    if (quantile == null) {
                        factory.getQuantilesDAO().addQuantile(
                                new Quantile(type, signLevel, count, selSize, pair.first, pair.second)
                        );
                        result = factory.getQuantilesDAO().getQuantile(type, signLevel, count, selSize);
                    } else {
                        quantile.setFirstValue(pair.first);
                        quantile.setSecondValue(pair.second);
                        factory.getQuantilesDAO().updateQuantile(quantile);
                        result = quantile;
                    }
                } finally {
                    observer.finish();
                }

                return result;
            }));

            tasks.put(task, observer);
        }

        return observer;
    }
    
    public ProcessObserver<Map<String, Map<Integer, QuantilePair> > > doQuantilesModulation(
            final Map<TestType, Set<SignificanceLevel> > typesForQuantiles,
            final int iterCount,
            final int selSize) throws ActionException {
        MultipleQuantileModulatorTask task = new MultipleQuantileModulatorTask(iterCount, selSize);

        final ProcessObserver<Map<String, Map<Integer, QuantilePair> > > observer;

        synchronized (tasks) {
            if (tasks.containsKey(task)) {
                observer = tasks.get(task);
                if (observer == null || observer.isFinished()) {
                    tasks.remove(task);
                }

                return observer;
            }

            observer = new ProcessObserver<>(task, iterCount);

            observer.setFuture(service.submit(() -> {
                Map<String, Map<Integer, QuantilePair> > result = null;
                try {
                    Map<TestType, Map<SignificanceLevel, QuantilePair> >  pairs = ExpQuantilesModulator.modulateQuantiles(typesForQuantiles, iterCount, selSize, observer);
                    
                    result = new TreeMap<>();
                    
                    Factory factory = Factory.getInstance();

                    for (Map.Entry<TestType, Map<SignificanceLevel, QuantilePair> > typeEntry : pairs.entrySet()) {
                        TestType type = typeEntry.getKey();
                        IterationsCount count = Factory.getInstance().getIterationsCountsDAO().getIterationsCount(type, iterCount);

                        if (count == null) {
                            throw new ActionException(new NoDependentData(RequestParams.ITERATIONS_COUNT));
                        }
                        
                        Map<Integer, QuantilePair> signResults = new TreeMap<>();
                        result.put(type.getType(), signResults);
                        
                        for (Map.Entry<SignificanceLevel, QuantilePair> levelEntry : typeEntry.getValue().entrySet()) {
                            SignificanceLevel level = levelEntry.getKey();
                            QuantilePair pair = levelEntry.getValue();
                            
                            Quantile quantile = factory.getQuantilesDAO().getQuantile(type, level, count, selSize);
                            if (quantile == null) {
                                factory.getQuantilesDAO().addQuantile(
                                        new Quantile(type, level, count, selSize, pair.first, pair.second)
                                );
                            } else {
                                quantile.setFirstValue(pair.first);
                                quantile.setSecondValue(pair.second);
                                factory.getQuantilesDAO().updateQuantile(quantile);
                            }
                            
                            signResults.put(level.getLevel(), pair);
                        }
                        
                    }
                } finally {
                    observer.finish();
                }

                return result;
            }));

            tasks.put(task, observer);
        }

        return observer;
    }

    public ProcessObserver<Power> doPowerModulation(
            final Quantile quantile,
            final SelectionParams params,
            final int powerIterCount) throws ActionException {
        PowerModulatorTask task = new PowerModulatorTask(quantile, params, powerIterCount);

        final ProcessObserver<Power> observer;

        synchronized (tasks) {
            if (tasks.containsKey(task)) {
                observer = tasks.get(task);
                if (observer == null || observer.isFinished()) {
                    tasks.remove(task);
                }

                return observer;
            }

            observer = new ProcessObserver<>(task, powerIterCount);

            observer.setFuture(service.submit(() -> {
                Power result = null;
                try {
                    Double value = PowerModulator.modulatePower(quantile, params, powerIterCount, observer);
                    
                    Factory factory = Factory.getInstance();

                    Power power = factory.getPowersDAO().getPower(quantile, params);
                    if (power == null) {
                        factory.getPowersDAO().addPower(
                                new Power(quantile, params, value)
                        );
                        result = factory.getPowersDAO().getPower(quantile, params);
                    } else {
                        power.setPower(value);
                        factory.getPowersDAO().updatePower(power);
                        result = power;
                    }
                } finally {
                    observer.finish();
                }

                return result;
            }));

            tasks.put(task, observer);
        }

        return observer;
    }

    public ProcessObserver<Map<Quantile, Map<SelectionParams, Double> > > doPowerModulation(
            final int selSize,
            final String testType,
            final List<Quantile> quantiles,
            final List<SelectionParams> params,
            final int powerIterCount) throws ActionException {
        int currIterCount = powerIterCount * params.size();
        MultiplePowerModulatorTask task = new MultiplePowerModulatorTask(selSize, testType, quantiles, params, currIterCount);

        final ProcessObserver<Map<Quantile, Map<SelectionParams, Double> > > observer;

        synchronized (tasks) {
            if (tasks.containsKey(task)) {
                observer = tasks.get(task);
                if (observer == null || observer.isFinished()) {
                    tasks.remove(task);
                }
                
                return observer;
            }

            observer = new ProcessObserver<>(task, currIterCount);

            observer.setFuture(service.submit(() -> {
                Map<Quantile, Map<SelectionParams, Double> > result = null;
                try {
                    result = PowerModulator.modulatePower(selSize, quantiles, params, powerIterCount, observer);

                    Factory factory = Factory.getInstance();
                    
                    for (Entry<Quantile, Map<SelectionParams, Double> > quantentry : result.entrySet()) {
                        Quantile quantile = quantentry.getKey();
                        
                        for (Entry<SelectionParams, Double> entry : quantentry.getValue().entrySet()) {
                            SelectionParams altParams = entry.getKey();
                            Double value = entry.getValue();
                            
                            Power power = factory.getPowersDAO().getPower(quantile, altParams);
                            if (power == null) {
                                factory.getPowersDAO().addPower(
                                        new Power(quantile, altParams, value)
                                );
                            } else {
                                power.setPower(value);
                                factory.getPowersDAO().updatePower(power);
                            }
                        }
                    }
                } finally {
                    observer.finish();
                }

                return result;
            }));

            tasks.put(task, observer);
        }

        return observer;
    }

}
