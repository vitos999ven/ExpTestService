package services;

import controllers.GsonHolder;
import com.google.gson.JsonSyntaxException;
import controllers.RequestParams;
import entities.ChartArray;
import entities.IterationsCountsSet;
import entities.QuantilesMap;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.springframework.stereotype.Service;
import entities.SelectionData;
import entities.SelectionInfo;
import entities.TestResult;
import entities.TestResultsMap;
import entities.WeibullAlternativeResult;
import entities.actionresults.ActionResult;
import entities.actionresults.FailedActionResult;
import entities.actionresults.SucceededActionResult;
import entities.alternatives.ExpParams;
import entities.alternatives.SelectionParams;
import entities.alternatives.SelectionsEnum;
import entities.alternatives.WeibullParams;
import entities.exceptions.ActionException;
import entities.exceptions.UnknownTestTypeException;
import entities.exceptions.WrongSelectionSizeException;
import entities.exceptions.actionreasons.DataAlreadyExists;
import entities.exceptions.actionreasons.DataNotReadyYet;
import entities.exceptions.actionreasons.ProblemsWithDatabase;
import entities.exceptions.actionreasons.InvalidParameter;
import entities.exceptions.actionreasons.InternalError;
import entities.exceptions.actionreasons.NoData;
import entities.exceptions.actionreasons.NoDependentData;
import entities.exceptions.actionreasons.WrongDBData;
import hibernate.DAO.SelectionsDAO;
import hibernate.DAO.SignificanceLevelsDAO;
import hibernate.DAO.TestTypesDAO;
import hibernate.logic.IterationsCount;
import hibernate.logic.Power;
import hibernate.logic.Quantile;
import hibernate.logic.Selection;
import hibernate.logic.SignificanceLevel;
import hibernate.logic.TestType;
import hibernate.util.Factory;
import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import javax.servlet.http.HttpServletRequest;
import logic.MathFunctionsHandler;
import logic.selections.SelectionsModulator;
import logic.tests.ExpTestsExecutor;
import logic.tests.PowerModulator;
import logic.tests.QuantilePair;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.exception.ConstraintViolationException;
import taskmanager.ProcessObserver;
import taskmanager.TaskManager;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final Logger logger = Logger.getLogger(ApplicationServiceImpl.class);

    private final LocksMap<String> selectionsLocks = new LocksMap<>();
    private final SelectionsMap selectionsCache = new SelectionsMap();

    @Override
    public List<TestType> getTestTypes() throws ActionException {
        List<TestType> list = null;
        TestTypesDAO dao = Factory.getInstance().getTestTypesDAO();

        try {
            list = dao.getAllTestTypes();
        } catch (HibernateException ex) {
            logger.error(ex);
            throw new ActionException(new ProblemsWithDatabase());
        }

        return list;
    }

    @Override
    public void addTestType(String type, String typeName, boolean forSorted) throws ActionException {
        if (type == null) {
            throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
        }

        if (typeName == null) {
            throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE_NAME));
        }

        TestType testType = new TestType(type, typeName, forSorted);

        try {
            if (!Factory.getInstance().getTestTypesDAO().addTestType(testType)) {
                throw new ActionException(new DataAlreadyExists());
            }
        } catch (ConstraintViolationException ex) {
            logger.error(ex);
            throw new ActionException(new DataAlreadyExists());
        } catch (HibernateException ex) {
            logger.error(ex);
            throw new ActionException(new ProblemsWithDatabase());
        }

        try {
            Factory.getInstance().getIterationsCountsDAO().addIterationsCount(
                    new IterationsCount(testType, testType.getDefaultIterCount())
            );
        } catch (HibernateException ex) {
            logger.error(
                    new StringBuilder().append("Can't add default iterations count for new test type \'")
                    .append(type).append("\':").append(ex.getLocalizedMessage()),
                    ex
            );
        }
    }

    @Override
    public void removeTestType(String type) throws ActionException {
        if (type == null) {
            throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
        }

        try {
            if (!Factory.getInstance().getTestTypesDAO()
                    .removeTestType(new TestType(type))) {
                throw new ActionException(new NoData());
            }
        } catch (HibernateException ex) {
            logger.error(ex.getLocalizedMessage(), ex);
            throw new ActionException(new ProblemsWithDatabase());
        }
    }

    @Override
    public IterationsCountsSet getIterationsCounts(String testType) throws ActionException {
        if (testType == null) {
            throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
        }

        IterationsCountsSet iterSet = null;
        Factory factory = Factory.getInstance();

        try {
            TestType type = factory.getTestTypesDAO().getTestType(testType);
            if (type == null) {
                throw new ActionException(new NoDependentData(RequestParams.TEST_TYPE));
            }

            List<IterationsCount> list = factory.getIterationsCountsDAO().getIterationsCounts(type);

            SortedSet<Integer> set = new TreeSet<>();

            list.stream().forEach((iterCount) -> {
                set.add(iterCount.getCount());
            });

            iterSet = new IterationsCountsSet(type.getType(), type.getDefaultIterCount(), set);
        } catch (HibernateException ex) {
            logger.error(ex);
            throw new ActionException(new ProblemsWithDatabase());
        }

        return iterSet;
    }

    @Override
    public void addIterationsCount(String testType, int count) throws ActionException {
        if (testType == null) {
            throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
        }

        if (count <= 0) {
            throw new ActionException(new InvalidParameter(RequestParams.ITERATIONS_COUNT));
        }

        Factory factory = Factory.getInstance();

        TestType type = new TestType(testType);

        try {
            if (!factory.getTestTypesDAO().containsTestType(testType)) {
                throw new ActionException(new NoDependentData(RequestParams.TEST_TYPE));
            }

            if (!factory.getIterationsCountsDAO().addIterationsCount(new IterationsCount(type, count))) {
                throw new ActionException(new DataAlreadyExists());
            }
        } catch (ConstraintViolationException ex) {
            logger.error(ex);
            throw new ActionException(new DataAlreadyExists());
        } catch (HibernateException ex) {
            logger.error(ex);
            throw new ActionException(new ProblemsWithDatabase());
        }
    }

    @Override
    public void removeIterationsCount(String testType, int count) throws ActionException {
        if (testType == null) {
            throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
        }

        Factory factory = Factory.getInstance();

        TestType type = new TestType(testType);

        try {
            if (!factory.getTestTypesDAO().containsTestType(testType)) {
                throw new ActionException(new NoDependentData(RequestParams.TEST_TYPE));
            }

            if (!factory.getIterationsCountsDAO().removeIterationsCount(type, count)) {
                throw new ActionException(new NoData());
            }
        } catch (HibernateException ex) {
            logger.error(ex);
            throw new ActionException(new ProblemsWithDatabase());
        }
    }

    @Override
    public List<SignificanceLevel> getSignificanceLevels() throws ActionException {
        List<SignificanceLevel> list = null;
        SignificanceLevelsDAO dao = Factory.getInstance().getSignificanceLevelsDAO();

        try {
            list = dao.getAllSignificanceLevels();
        } catch (HibernateException ex) {
            logger.error(ex);
            throw new ActionException(new ProblemsWithDatabase());
        }

        return list;
    }

    @Override
    public void addSignificanceLevel(int level) throws ActionException {
        if (level <= 0 || level >= SignificanceLevel.getMaxLevel()) {
            throw new ActionException(new InvalidParameter(RequestParams.SIGNIFICANCE_LEVEL));
        }

        SignificanceLevel signLevel = new SignificanceLevel(level);

        try {
            if (!Factory.getInstance().getSignificanceLevelsDAO().addSignificanceLevel(signLevel)) {
                throw new ActionException(new DataAlreadyExists());
            }
        } catch (ConstraintViolationException ex) {
            logger.error(ex);
            throw new ActionException(new DataAlreadyExists());
        } catch (HibernateException ex) {
            logger.error(ex);
            throw new ActionException(new ProblemsWithDatabase());
        }
    }

    @Override
    public void removeSignificanceLevel(int level) throws ActionException {
        try {
            if (!Factory.getInstance().getSignificanceLevelsDAO()
                    .removeSignificanceLevel(new SignificanceLevel(level))) {
                throw new ActionException(new NoData());
            }
        } catch (HibernateException ex) {
            logger.error(ex);
            throw new ActionException(new ProblemsWithDatabase());
        }
    }

    @Override
    public Quantile modulateQuantile(
            String testType,
            int iterCount,
            int signLevel,
            int selSize) throws ActionException {
        if (testType == null) {
            throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
        }

        Factory factory = Factory.getInstance();

        SignificanceLevel level = new SignificanceLevel(signLevel);
        Quantile quantile = null;

        try {
            TestType type = factory.getTestTypesDAO().getTestType(testType);
            if (type == null) {
                throw new ActionException(new NoDependentData(RequestParams.TEST_TYPE));
            }

            if (!factory.getSignificanceLevelsDAO().containsSignificanceLevel(level)) {
                throw new ActionException(new NoDependentData(RequestParams.SIGNIFICANCE_LEVEL));
            }

            IterationsCount count = factory.getIterationsCountsDAO().getIterationsCount(type, iterCount);
            if (count == null) {
                throw new ActionException(new NoDependentData(RequestParams.ITERATIONS_COUNT));
            }

            ProcessObserver<Quantile> observer = TaskManager.getInstance().doQuantilesModulation(
                    type,
                    level,
                    iterCount,
                    selSize);

            if (observer == null) {
                logger.error(
                        new StringBuilder().append("No observer for quantile modulation (")
                        .append(RequestParams.TEST_TYPE).append(": ").append(testType).append(", ")
                        .append(RequestParams.SIGNIFICANCE_LEVEL).append(": ").append(signLevel).append(", ")
                        .append(RequestParams.ITERATIONS_COUNT).append(": ").append(iterCount).append(", ")
                        .append(RequestParams.SELECTION_SIZE).append(": ").append(selSize).append(")")
                );
                throw new ActionException(new InternalError("No observer for quantile modulation"));
            }

            try {
                quantile = observer.get();

                if (quantile == null) {
                    logger.error(
                            new StringBuilder().append("No quantile after quantile modulation (")
                            .append(RequestParams.TEST_TYPE).append(": ").append(testType).append(", ")
                            .append(RequestParams.SIGNIFICANCE_LEVEL).append(": ").append(signLevel).append(", ")
                            .append(RequestParams.ITERATIONS_COUNT).append(": ").append(iterCount).append(", ")
                            .append(RequestParams.SELECTION_SIZE).append(": ").append(selSize).append(")")
                    );
                    throw new ActionException(new InternalError("No quantile after quantile modulation"));
                }
            } catch (TimeoutException ex) {
                logger.error(ex);
                throw new ActionException(new DataNotReadyYet(observer.status()));
            } catch (ExecutionException ex) {
                logger.error(ex.getCause());
                if (ex.getCause() instanceof WrongSelectionSizeException) {
                    throw new ActionException(new InvalidParameter(RequestParams.SELECTION_SIZE));
                } else if (ex.getCause() instanceof UnknownTestTypeException) {
                    throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
                }
                throw new ActionException(new InternalError((ex.getCause() != null) ? ex.getCause().toString() : ex.toString()));
            } catch (InterruptedException ex) {
                logger.error(ex);
                throw new ActionException(new InternalError(ex.toString()));
            }
        } catch (HibernateException ex) {
            logger.error(ex);
            throw new ActionException(new ProblemsWithDatabase());
        }

        return quantile;
    }

    @Override
    public Quantile getOrModulateQuantile(
            String testType,
            int iterCount,
            int signLevel,
            int selSize) throws ActionException {
        if (testType == null) {
            throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
        }

        Factory factory = Factory.getInstance();

        SignificanceLevel level = new SignificanceLevel(signLevel);
        Quantile quantile = null;

        try {
            TestType type = factory.getTestTypesDAO().getTestType(testType);
            if (type == null) {
                throw new ActionException(new NoDependentData(RequestParams.TEST_TYPE));
            }

            if (!factory.getSignificanceLevelsDAO().containsSignificanceLevel(level)) {
                throw new ActionException(new NoDependentData(RequestParams.SIGNIFICANCE_LEVEL));
            }

            IterationsCount count = factory.getIterationsCountsDAO().getIterationsCount(type, iterCount);
            if (count == null) {
                throw new ActionException(new NoDependentData(RequestParams.ITERATIONS_COUNT));
            }

            quantile = factory.getQuantilesDAO().getQuantile(type, level, count, selSize);

            if (quantile != null) {
                return quantile;
            }

            ProcessObserver<Quantile> observer = TaskManager.getInstance().doQuantilesModulation(
                    type,
                    level,
                    iterCount,
                    selSize);

            if (observer == null) {
                logger.error(
                        new StringBuilder().append("No observer for quantile modulation (")
                        .append(RequestParams.TEST_TYPE).append(": ").append(testType).append(", ")
                        .append(RequestParams.SIGNIFICANCE_LEVEL).append(": ").append(signLevel).append(", ")
                        .append(RequestParams.ITERATIONS_COUNT).append(": ").append(iterCount).append(", ")
                        .append(RequestParams.SELECTION_SIZE).append(": ").append(selSize).append(")")
                );
                throw new ActionException(new InternalError("No observer for quantile modulation"));
            }

            try {
                quantile = observer.get();

                if (quantile == null) {
                    logger.error(
                            new StringBuilder().append("No quantile after quantile modulation (")
                            .append(RequestParams.TEST_TYPE).append(": ").append(testType).append(", ")
                            .append(RequestParams.SIGNIFICANCE_LEVEL).append(": ").append(signLevel).append(", ")
                            .append(RequestParams.ITERATIONS_COUNT).append(": ").append(iterCount).append(", ")
                            .append(RequestParams.SELECTION_SIZE).append(": ").append(selSize).append(")")
                    );
                    throw new ActionException(new InternalError("No quantile after quantile modulation"));
                }
            } catch (TimeoutException ex) {
                throw new ActionException(new DataNotReadyYet(observer.status()));
            } catch (ExecutionException ex) {
                logger.error(ex.getCause());
                if (ex.getCause() instanceof WrongSelectionSizeException) {
                    throw new ActionException(new InvalidParameter(RequestParams.SELECTION_SIZE));
                } else if (ex.getCause() instanceof UnknownTestTypeException) {
                    throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
                }
                throw new ActionException(new InternalError((ex.getCause() != null) ? ex.getCause().toString() : ex.toString()));
            } catch (InterruptedException ex) {
                logger.error(ex);
                throw new ActionException(new InternalError(ex.toString()));
            }
        } catch (HibernateException ex) {
            logger.error(ex);
            throw new ActionException(new ProblemsWithDatabase());
        }

        return quantile;
    }

    @Override
    public List<Quantile> getQuantiles(String testType, int iterCount, int signLevel) throws ActionException {
        if (testType == null) {
            throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
        }

        Factory factory = Factory.getInstance();

        SignificanceLevel level = new SignificanceLevel(signLevel);

        List<Quantile> list = null;

        try {
            TestType type = factory.getTestTypesDAO().getTestType(testType);
            if (type == null) {
                throw new ActionException(new NoDependentData(RequestParams.TEST_TYPE));
            }

            if (!factory.getSignificanceLevelsDAO().containsSignificanceLevel(level)) {
                throw new ActionException(new NoDependentData(RequestParams.SIGNIFICANCE_LEVEL));
            }

            IterationsCount count = factory.getIterationsCountsDAO().getIterationsCount(type, iterCount);
            if (count == null) {
                throw new ActionException(new NoDependentData(RequestParams.ITERATIONS_COUNT));
            }

            list = factory.getQuantilesDAO().getQuantiles(type, level, count);

            if (list == null) {
                throw new ActionException(new NoData());
            }
        } catch (HibernateException ex) {
            logger.error(ex);
            throw new ActionException(new ProblemsWithDatabase());
        }

        return list;
    }

    @Override
    public List<Quantile> getQuantiles(
            String testType,
            int iterCount,
            int signLevel,
            int minSelSize,
            int maxSelSize) throws ActionException {
        if (testType == null) {
            throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
        }

        Factory factory = Factory.getInstance();

        SignificanceLevel level = new SignificanceLevel(signLevel);

        List<Quantile> list = null;

        try {
            TestType type = factory.getTestTypesDAO().getTestType(testType);
            if (type == null) {
                throw new ActionException(new NoDependentData(RequestParams.TEST_TYPE));
            }

            if (!factory.getSignificanceLevelsDAO().containsSignificanceLevel(level)) {
                throw new ActionException(new NoDependentData(RequestParams.SIGNIFICANCE_LEVEL));
            }

            IterationsCount count = factory.getIterationsCountsDAO().getIterationsCount(type, iterCount);
            if (count == null) {
                throw new ActionException(new NoDependentData(RequestParams.ITERATIONS_COUNT));
            }

            list = factory.getQuantilesDAO().getQuantiles(type, level, count, minSelSize, maxSelSize);

            if (list == null) {
                throw new ActionException(new NoData());
            }
        } catch (HibernateException ex) {
            logger.error(ex);
            throw new ActionException(new ProblemsWithDatabase());
        }

        return list;
    }

    @Override
    public Quantile modulateDefaultQuantile(
            String testType,
            int signLevel,
            int selSize) throws ActionException {
        if (testType == null) {
            throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
        }

        Factory factory = Factory.getInstance();

        SignificanceLevel level = new SignificanceLevel(signLevel);
        Quantile quantile = null;

        try {
            TestType type = factory.getTestTypesDAO().getTestType(testType);
            if (type == null) {
                throw new ActionException(new NoDependentData(RequestParams.TEST_TYPE));
            }

            if (!factory.getSignificanceLevelsDAO().containsSignificanceLevel(level)) {
                throw new ActionException(new NoDependentData(RequestParams.SIGNIFICANCE_LEVEL));
            }

            int defaultIterCount = type.getDefaultIterCount();
            IterationsCount count = factory.getIterationsCountsDAO().getIterationsCount(type, defaultIterCount);
            if (count == null) {
                throw new ActionException(new NoDependentData(RequestParams.ITERATIONS_COUNT));
            }

            ProcessObserver<Quantile> observer = TaskManager.getInstance().doQuantilesModulation(
                    type,
                    level,
                    defaultIterCount,
                    selSize);

            if (observer == null) {
                logger.error(
                        new StringBuilder().append("No observer for default quantile modulation (")
                        .append(RequestParams.TEST_TYPE).append(": ").append(testType).append(", ")
                        .append(RequestParams.SIGNIFICANCE_LEVEL).append(": ").append(signLevel).append(", ")
                        .append(RequestParams.ITERATIONS_COUNT).append(": ").append(defaultIterCount).append(", ")
                        .append(RequestParams.SELECTION_SIZE).append(": ").append(selSize).append(")")
                );
                throw new ActionException(new InternalError("No observer for default quantile modulation"));
            }

            try {
                quantile = observer.get();

                if (quantile == null) {
                    logger.error(
                            new StringBuilder().append("No quantile after default quantile modulation (")
                            .append(RequestParams.TEST_TYPE).append(": ").append(testType).append(", ")
                            .append(RequestParams.SIGNIFICANCE_LEVEL).append(": ").append(signLevel).append(", ")
                            .append(RequestParams.ITERATIONS_COUNT).append(": ").append(defaultIterCount).append(", ")
                            .append(RequestParams.SELECTION_SIZE).append(": ").append(selSize).append(")")
                    );
                    throw new ActionException(new InternalError("No quantile after default quantile modulation"));
                }
            } catch (TimeoutException ex) {
                throw new ActionException(new DataNotReadyYet(observer.status()));
            } catch (ExecutionException ex) {
                logger.error(ex.getCause());
                if (ex.getCause() instanceof WrongSelectionSizeException) {
                    throw new ActionException(new InvalidParameter(RequestParams.SELECTION_SIZE));
                } else if (ex.getCause() instanceof UnknownTestTypeException) {
                    throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
                }
                throw new ActionException(new InternalError((ex.getCause() != null) ? ex.getCause().toString() : ex.toString()));
            } catch (InterruptedException ex) {
                logger.error(ex);
                throw new ActionException(new InternalError(ex.toString()));
            }
        } catch (HibernateException ex) {
            logger.error(ex);
            throw new ActionException(new ProblemsWithDatabase());
        }

        return quantile;
    }

    @Override
    public Quantile getOrModulateDefaultQuantile(
            String testType,
            int signLevel,
            int selSize) throws ActionException {
        if (testType == null) {
            throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
        }

        Factory factory = Factory.getInstance();

        SignificanceLevel level = new SignificanceLevel(signLevel);
        Quantile quantile = null;

        try {
            TestType type = factory.getTestTypesDAO().getTestType(testType);
            if (type == null) {
                throw new ActionException(new NoDependentData(RequestParams.TEST_TYPE));
            }

            if (!factory.getSignificanceLevelsDAO().containsSignificanceLevel(level)) {
                throw new ActionException(new NoDependentData(RequestParams.SIGNIFICANCE_LEVEL));
            }

            int defaultIterCount = type.getDefaultIterCount();
            IterationsCount count = factory.getIterationsCountsDAO().getIterationsCount(type, defaultIterCount);
            if (count == null) {
                throw new ActionException(new NoDependentData(RequestParams.ITERATIONS_COUNT));
            }

            quantile = factory.getQuantilesDAO().getQuantile(type, level, count, selSize);

            if (quantile != null) {
                return quantile;
            }

            ProcessObserver<Quantile> observer = TaskManager.getInstance().doQuantilesModulation(
                    type,
                    level,
                    defaultIterCount,
                    selSize);

            if (observer == null) {
                logger.error(
                        new StringBuilder().append("No observer for default quantile modulation (")
                        .append(RequestParams.TEST_TYPE).append(": ").append(testType).append(", ")
                        .append(RequestParams.SIGNIFICANCE_LEVEL).append(": ").append(signLevel).append(", ")
                        .append(RequestParams.ITERATIONS_COUNT).append(": ").append(defaultIterCount).append(", ")
                        .append(RequestParams.SELECTION_SIZE).append(": ").append(selSize).append(")")
                );
                throw new ActionException(new InternalError("No observer for default quantile modulation"));
            }

            try {
                quantile = observer.get();

                if (quantile == null) {
                    logger.error(
                            new StringBuilder().append("No quantile after default quantile modulation (")
                            .append(RequestParams.TEST_TYPE).append(": ").append(testType).append(", ")
                            .append(RequestParams.SIGNIFICANCE_LEVEL).append(": ").append(signLevel).append(", ")
                            .append(RequestParams.ITERATIONS_COUNT).append(": ").append(defaultIterCount).append(", ")
                            .append(RequestParams.SELECTION_SIZE).append(": ").append(selSize).append(")")
                    );
                    throw new ActionException(new InternalError("No quantile pair after default quantile modulation"));
                }
            } catch (TimeoutException ex) {
                throw new ActionException(new DataNotReadyYet(observer.status()));
            } catch (ExecutionException ex) {
                logger.error(ex.getCause());
                if (ex.getCause() instanceof WrongSelectionSizeException) {
                    throw new ActionException(new InvalidParameter(RequestParams.SELECTION_SIZE));
                } else if (ex.getCause() instanceof UnknownTestTypeException) {
                    throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
                }
                throw new ActionException(new InternalError((ex.getCause() != null) ? ex.getCause().toString() : ex.toString()));
            } catch (InterruptedException ex) {
                logger.error(ex);
                throw new ActionException(new InternalError(ex.toString()));
            }
        } catch (HibernateException ex) {
            logger.error(ex);
            throw new ActionException(new ProblemsWithDatabase());
        }

        return quantile;
    }
    
    
    @Override
    public Map<String, Map<Integer, QuantilePair> > getOrModulateMultipleQuantiles(
            String[] testTypes,
            int iterCount,
            int selSize,
            boolean ifNotExists) throws ActionException {
        if (testTypes == null) {
            throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPES));
        }

        Factory factory = Factory.getInstance();

        Map<String, Map<Integer, QuantilePair> > result = new TreeMap<>();

        Map<TestType, Set<SignificanceLevel> > typesForQuantiles = new TreeMap<>();
        try {
            List<SignificanceLevel> levels = factory.getSignificanceLevelsDAO().getAllSignificanceLevels();
            
            for (int i = 0; i < testTypes.length; ++i) {
                String testType = testTypes[i];
                TestType type = factory.getTestTypesDAO().getTestType(testType);
                if (type == null) {
                    throw new ActionException(new NoDependentData(RequestParams.TEST_TYPE));
                }

                IterationsCount count = factory.getIterationsCountsDAO().getIterationsCount(type, iterCount);
                if (count == null) {
                    throw new ActionException(new NoDependentData(RequestParams.ITERATIONS_COUNT));
                }

                Map<Integer, QuantilePair> quantiles = new TreeMap<>();
                Set<SignificanceLevel> levelsForType = null;
                
                result.put(testType, quantiles);

                if (!ifNotExists) {
                    levelsForType  = new TreeSet<>(levels);
                    typesForQuantiles.put(type, levelsForType);
                    continue;
                }
                
                for (SignificanceLevel level : levels) {
                    Quantile quantile = factory.getQuantilesDAO().getQuantile(type, level, count, selSize);
                    if (quantile == null) {
                        if (levelsForType == null) {
                            levelsForType  = new TreeSet<>();
                            typesForQuantiles.put(type, levelsForType);
                        }
                        levelsForType.add(level);
                        continue;
                    }

                    quantiles.put(level.getLevel(), new QuantilePair(quantile.getFirstValue(), quantile.getSecondValue()));
                }
            }
            

            if (typesForQuantiles.isEmpty()) {
                return result;
            }
            
            ProcessObserver<Map<String, Map<Integer, QuantilePair> > > observer = TaskManager.getInstance().doQuantilesModulation(
                    typesForQuantiles,
                    iterCount,
                    selSize);

            if (observer == null) {
                logger.error(
                        new StringBuilder().append("No observer for multiple quantiles modulation (")
                        .append(RequestParams.TEST_TYPE).append(": ").append(testTypes).append(", ")
                        .append(RequestParams.ITERATIONS_COUNT).append(": ").append(iterCount).append(", ")
                        .append(RequestParams.SELECTION_SIZE).append(": ").append(selSize).append(")")
                );
                throw new ActionException(new InternalError("No observer for quantiles modulation"));
            }

            try {
                Map<String, Map<Integer, QuantilePair> > modulatorResult = observer.get();

                if (modulatorResult == null) {
                    logger.error(
                            new StringBuilder().append("No powers after multiple quantiles modulation (")
                            .append(RequestParams.TEST_TYPE).append(": ").append(testTypes).append(", ")
                            .append(RequestParams.ITERATIONS_COUNT).append(": ").append(iterCount).append(", ")
                            .append(RequestParams.SELECTION_SIZE).append(": ").append(selSize).append(")")
                    );
                    throw new ActionException(new InternalError("No powers after quantiles modulation"));
                }
                
                for (Entry<String, Map<Integer, QuantilePair> > entry : modulatorResult.entrySet()) {
                    String testType = entry.getKey();
                    
                    result.get(testType).putAll(entry.getValue());
                }
            } catch (TimeoutException ex) {
                throw new ActionException(new DataNotReadyYet(observer.status()));
            } catch (ExecutionException ex) {
                logger.error("ExecutionException was thrown while modulating power", ex.getCause());
                if (ex.getCause() instanceof WrongSelectionSizeException) {
                    throw new ActionException(new InvalidParameter(RequestParams.SELECTION_SIZE));
                } else if (ex.getCause() instanceof UnknownTestTypeException) {
                    throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
                }
                throw new ActionException(new InternalError((ex.getCause() != null) ? ex.getCause().getLocalizedMessage() : ex.getLocalizedMessage()));
            } catch (InterruptedException ex) {
                logger.error(ex);
                throw new ActionException(new InternalError(ex.toString()));
            }
        } catch (HibernateException ex) {
            logger.error("Problems with database", ex);
            throw new ActionException(new ProblemsWithDatabase());
        }

        return result;
    }

    @Override
    public QuantilesMap getQuantiles(
            String testType,
            int iterCount,
            int minSelSize,
            int quantilesCount) throws ActionException {
        if (testType == null) {
            throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
        }

        if (iterCount <= 0) {
            throw new ActionException(new InvalidParameter(RequestParams.ITERATIONS_COUNT));
        }

        if (minSelSize < 10 || minSelSize > 1000) {
            throw new ActionException(new InvalidParameter(RequestParams.MIN_SELECTION_SIZE));
        }

        if (quantilesCount <= 0) {
            throw new ActionException(new InvalidParameter(RequestParams.QUANTILES_COUNT));
        }

        Factory factory = Factory.getInstance();

        QuantilesMap map = new QuantilesMap(testType, iterCount, minSelSize, quantilesCount);

        try {
            TestType type = factory.getTestTypesDAO().getTestType(testType);
            if (type == null) {
                throw new ActionException(new NoDependentData(RequestParams.TEST_TYPE));
            }

            IterationsCount count = factory.getIterationsCountsDAO().getIterationsCount(type, iterCount);
            if (count == null) {
                throw new ActionException(new NoDependentData(RequestParams.ITERATIONS_COUNT));
            }

            List<SignificanceLevel> levels = factory.getSignificanceLevelsDAO().getAllSignificanceLevels();
            if (levels == null || levels.isEmpty()) {
                throw new ActionException(new NoDependentData(RequestParams.SIGNIFICANCE_LEVEL));
            }

            levels.stream().forEach((level) -> {
                List<Quantile> list = factory.getQuantilesDAO().getQuantiles(
                        type,
                        level,
                        count,
                        minSelSize,
                        minSelSize + quantilesCount);

                SortedMap<Integer, QuantilePair> quantilesMap = new TreeMap<>();

                if (list != null) {
                    list.stream().forEach((quantile) -> {
                        quantilesMap.put(
                                quantile.getSelectionSize(),
                                new QuantilePair(quantile.getFirstValue(), quantile.getSecondValue()));
                    });
                }

                map.quantiles.put(level.getLevel(), quantilesMap);
            });
        } catch (HibernateException ex) {
            logger.error(ex);
            throw new ActionException(new ProblemsWithDatabase());
        }

        return map;
    }

    @Override
    public Double getOrModulatePower(
            String testType,
            int iterCount,
            int signLevel,
            int selSize,
            String alternativeType,
            HttpServletRequest request,
            boolean ifNotExists) throws ActionException {
        if (testType == null) {
            throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
        }

        SelectionsEnum alternative;
        SelectionParams params;
        switch (alternativeType) {
            case "exp":
                alternative = SelectionsEnum.EXP;
                params = new ExpParams();
                break;
            case "weibull":
                alternative = SelectionsEnum.WEIBULL;

                BigDecimal weibullParam;
                try {
                    String weibullStr = request.getParameter(RequestParams.WEIBULL_PARAMETER);

                    if (weibullStr == null || weibullStr.equals("")) {
                        throw new ActionException(new InvalidParameter(RequestParams.WEIBULL_PARAMETER));
                    }

                    weibullParam = new BigDecimal(weibullStr);
                } catch (Exception ex) {
                    logger.error(ex);
                    throw new ActionException(new InvalidParameter(RequestParams.WEIBULL_PARAMETER));
                }

                params = new WeibullParams(weibullParam);
                break;
            default:
                throw new ActionException(new InvalidParameter(RequestParams.ALTERNATIVE));
        }

        Factory factory = Factory.getInstance();

        SignificanceLevel level = new SignificanceLevel(signLevel);
        Double result = null;

        try {
            TestType type = factory.getTestTypesDAO().getTestType(testType);
            if (type == null) {
                throw new ActionException(new NoDependentData(RequestParams.TEST_TYPE));
            }

            if (!factory.getSignificanceLevelsDAO().containsSignificanceLevel(level)) {
                throw new ActionException(new NoDependentData(RequestParams.SIGNIFICANCE_LEVEL));
            }

            IterationsCount count = factory.getIterationsCountsDAO().getIterationsCount(type, iterCount);
            if (count == null) {
                throw new ActionException(new NoDependentData(RequestParams.ITERATIONS_COUNT));
            }

            Quantile quantile = factory.getQuantilesDAO().getQuantile(type, level, count, selSize);
            if (quantile == null) {
                throw new ActionException(new NoDependentData(RequestParams.QUANTILE));
            }

            Power power = null;
            if (ifNotExists) {
                power = factory.getPowersDAO().getPower(quantile, params);
                if (power != null) {
                    return power.getPower();
                }
            }

            ProcessObserver<Power> observer = TaskManager.getInstance().doPowerModulation(
                    quantile,
                    params,
                    10000);

            if (observer == null) {
                logger.error(
                        new StringBuilder().append("No observer for power modulation (")
                        .append(RequestParams.TEST_TYPE).append(": ").append(testType).append(", ")
                        .append(RequestParams.SIGNIFICANCE_LEVEL).append(": ").append(signLevel).append(", ")
                        .append(RequestParams.ITERATIONS_COUNT).append(": ").append(iterCount).append(", ")
                        .append(RequestParams.SELECTION_SIZE).append(": ").append(selSize).append(", ")
                        .append(RequestParams.ALTERNATIVE).append(": ").append(alternativeType).append(")")
                );
                throw new ActionException(new InternalError("No observer for power modulation"));
            }

            try {
                power = observer.get();

                if (power == null) {
                    logger.error(
                            new StringBuilder().append("No power after power modulation (")
                            .append(RequestParams.TEST_TYPE).append(": ").append(testType).append(", ")
                            .append(RequestParams.SIGNIFICANCE_LEVEL).append(": ").append(signLevel).append(", ")
                            .append(RequestParams.ITERATIONS_COUNT).append(": ").append(iterCount).append(", ")
                            .append(RequestParams.SELECTION_SIZE).append(": ").append(selSize).append(", ")
                            .append(RequestParams.ALTERNATIVE).append(": ").append(alternativeType).append(")")
                    );
                    throw new ActionException(new InternalError("No power after power modulation"));
                }

                result = power.getPower();
            } catch (TimeoutException ex) {
                throw new ActionException(new DataNotReadyYet(observer.status()));
            } catch (ExecutionException ex) {
                logger.error("ExecutionException was thrown while modulating power", ex.getCause());
                if (ex.getCause() instanceof WrongSelectionSizeException) {
                    throw new ActionException(new InvalidParameter(RequestParams.SELECTION_SIZE));
                } else if (ex.getCause() instanceof UnknownTestTypeException) {
                    throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
                }
                throw new ActionException(new InternalError((ex.getCause() != null) ? ex.getCause().getLocalizedMessage() : ex.getLocalizedMessage()));
            } catch (InterruptedException ex) {
                logger.error(ex);
                throw new ActionException(new InternalError(ex.toString()));
            }
        } catch (HibernateException ex) {
            logger.error(ex.getLocalizedMessage(), ex);
            throw new ActionException(new ProblemsWithDatabase());
        }

        return result;
    }

    @Override
    public Map<Integer, ChartArray> getOrModulateWeibullPower(
            String testType,
            int iterCount,
            int selSize,
            boolean ifNotExists) throws ActionException {
        if (testType == null) {
            throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
        }

        SelectionsEnum alternative;
        List<SelectionParams> paramsList = PowerModulator.weibulParamsList;

        Factory factory = Factory.getInstance();

        Map<Integer, ChartArray> result = new TreeMap<>();

        try {
            TestType type = factory.getTestTypesDAO().getTestType(testType);
            if (type == null) {
                throw new ActionException(new NoDependentData(RequestParams.TEST_TYPE));
            }
            
            IterationsCount count = factory.getIterationsCountsDAO().getIterationsCount(type, iterCount);
            if (count == null) {
                throw new ActionException(new NoDependentData(RequestParams.ITERATIONS_COUNT));
            }

            List<SignificanceLevel> levels = factory.getSignificanceLevelsDAO().getAllSignificanceLevels();
            List<Quantile> quantiles = new LinkedList<>();

            Map<BigDecimal, Double> powers = null;

            for (SignificanceLevel level : levels) {
                Quantile quantile = factory.getQuantilesDAO().getQuantile(type, level, count, selSize);
                if (quantile == null) {
                    throw new ActionException(new NoDependentData(RequestParams.QUANTILE));
                }
                
                if (!ifNotExists) {
                    quantiles.add(quantile);
                    continue;
                }

                boolean containsAll = true;
                powers = new TreeMap<>();

                for (SelectionParams params : paramsList) {
                    Power power = factory.getPowersDAO().getPower(quantile, params);
                    if (power == null) {
                        containsAll = false;
                        break;
                    }
                    WeibullParams wParams = (WeibullParams) params;
                    powers.put(wParams.getWeibullParam(), power.getPower());
                }

                if (!containsAll) {
                    quantiles.add(quantile);
                    continue;
                }
                
                result.put(level.getLevel(), new ChartArray(powers));
            }

            if (quantiles.isEmpty()) {
                return result;
            }
            
            ProcessObserver<Map<Quantile, Map<SelectionParams, Double> > > observer = TaskManager.getInstance().doPowerModulation(
                    selSize,
                    testType,
                    quantiles,
                    paramsList,
                    1000);

            if (observer == null) {
                logger.error(
                        new StringBuilder().append("No observer for weibull powers modulation (")
                        .append(RequestParams.TEST_TYPE).append(": ").append(testType).append(", ")
                        .append(RequestParams.ITERATIONS_COUNT).append(": ").append(iterCount).append(", ")
                        .append(RequestParams.SELECTION_SIZE).append(": ").append(selSize).append(")")
                );
                throw new ActionException(new InternalError("No observer for weibull powers modulation"));
            }

            try {
                Map<Quantile, Map<SelectionParams, Double> > modulatorResult = observer.get();

                if (modulatorResult == null) {
                    logger.error(
                            new StringBuilder().append("No powers after weibull powers modulation (")
                            .append(RequestParams.TEST_TYPE).append(": ").append(testType).append(", ")
                            .append(RequestParams.ITERATIONS_COUNT).append(": ").append(iterCount).append(", ")
                            .append(RequestParams.SELECTION_SIZE).append(": ").append(selSize).append(")")
                    );
                    throw new ActionException(new InternalError("No powers after weibull powers modulation"));
                }
                
                for (Entry<Quantile, Map<SelectionParams, Double> > entry : modulatorResult.entrySet()) {
                    Integer level = entry.getKey().getSignificanceLevel().getLevel();
                            
                    Map<BigDecimal, Double> paramsMap = new TreeMap<>();
                    for (Entry<SelectionParams, Double> paramsEntry : entry.getValue().entrySet()) {
                        WeibullParams p = (WeibullParams) paramsEntry.getKey();
                        
                        paramsMap.put(p.getWeibullParam(), paramsEntry.getValue());
                    }
                    
                    result.put(level, new ChartArray(paramsMap));
                }
            } catch (TimeoutException ex) {
                throw new ActionException(new DataNotReadyYet(observer.status()));
            } catch (ExecutionException ex) {
                logger.error("ExecutionException was thrown while modulating power", ex.getCause());
                if (ex.getCause() instanceof WrongSelectionSizeException) {
                    throw new ActionException(new InvalidParameter(RequestParams.SELECTION_SIZE));
                } else if (ex.getCause() instanceof UnknownTestTypeException) {
                    throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
                }
                throw new ActionException(new InternalError((ex.getCause() != null) ? ex.getCause().getLocalizedMessage() : ex.getLocalizedMessage()));
            } catch (InterruptedException ex) {
                logger.error(ex);
                throw new ActionException(new InternalError(ex.toString()));
            }
        } catch (HibernateException ex) {
            logger.error("Problems with database", ex);
            throw new ActionException(new ProblemsWithDatabase());
        }

        return result;
    }

    @Override
    public Map<String, Map <Integer, ChartArray> > getOrModulateWeibullPower(
            String[] testTypes,
            int iterCount,
            int selSize,
            boolean ifNotExists) throws ActionException {
        if (testTypes == null) {
            throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPES));
        }
        
        List<SelectionParams> paramsList = PowerModulator.weibulParamsList;

        Factory factory = Factory.getInstance();

        Map<String, Map <Integer, ChartArray> > result = new TreeMap<>();

        List<Quantile> quantiles = new LinkedList<>();
        try {
            List<SignificanceLevel> levels = factory.getSignificanceLevelsDAO().getAllSignificanceLevels();
            
            for (int i = 0; i < testTypes.length; ++i) {
                String testType = testTypes[i];
                TestType type = factory.getTestTypesDAO().getTestType(testType);
                if (type == null) {
                    throw new ActionException(new NoDependentData(RequestParams.TEST_TYPE));
                }

                IterationsCount count = factory.getIterationsCountsDAO().getIterationsCount(type, iterCount);
                if (count == null) {
                    throw new ActionException(new NoDependentData(RequestParams.ITERATIONS_COUNT));
                }
                
                Map <Integer, ChartArray> charts = new TreeMap<>();
                result.put(testType, charts);

                Map<BigDecimal, Double> powers = null;

                for (SignificanceLevel level : levels) {
                    Quantile quantile = factory.getQuantilesDAO().getQuantile(type, level, count, selSize);
                    if (quantile == null) {
                        throw new ActionException(new NoDependentData(RequestParams.QUANTILE));
                    }

                    if (!ifNotExists) {
                        quantiles.add(quantile);
                        continue;
                    }

                    boolean containsAll = true;
                    powers = new TreeMap<>();

                    for (SelectionParams params : paramsList) {
                        Power power = factory.getPowersDAO().getPower(quantile, params);
                        if (power == null) {
                            containsAll = false;
                            break;
                        }
                        WeibullParams wParams = (WeibullParams) params;
                        powers.put(wParams.getWeibullParam(), power.getPower());
                    }

                    if (!containsAll) {
                        quantiles.add(quantile);
                        continue;
                    }

                    charts.put(level.getLevel(), new ChartArray(powers));
                }
            }
            

            if (quantiles.isEmpty()) {
                return result;
            }
            
            ProcessObserver<Map<Quantile, Map<SelectionParams, Double> > > observer = TaskManager.getInstance().doPowerModulation(
                    selSize,
                    "weibull",
                    quantiles,
                    paramsList,
                    1000);

            if (observer == null) {
                logger.error(
                        new StringBuilder().append("No observer for multiple weibull powers modulation (")
                        .append(RequestParams.TEST_TYPE).append(": ").append(testTypes).append(", ")
                        .append(RequestParams.ITERATIONS_COUNT).append(": ").append(iterCount).append(", ")
                        .append(RequestParams.SELECTION_SIZE).append(": ").append(selSize).append(")")
                );
                throw new ActionException(new InternalError("No observer for weibull powers modulation"));
            }

            try {
                Map<Quantile, Map<SelectionParams, Double> > modulatorResult = observer.get();

                if (modulatorResult == null) {
                    logger.error(
                            new StringBuilder().append("No powers after multiple weibull powers modulation (")
                            .append(RequestParams.TEST_TYPE).append(": ").append(testTypes).append(", ")
                            .append(RequestParams.ITERATIONS_COUNT).append(": ").append(iterCount).append(", ")
                            .append(RequestParams.SELECTION_SIZE).append(": ").append(selSize).append(")")
                    );
                    throw new ActionException(new InternalError("No powers after multiple weibull powers modulation"));
                }
                
                for (Entry<Quantile, Map<SelectionParams, Double> > entry : modulatorResult.entrySet()) {
                    String testType = entry.getKey().getTestType().getType();
                    Integer level = entry.getKey().getSignificanceLevel().getLevel();
                            
                    Map<BigDecimal, Double> paramsMap = new TreeMap<>();
                    for (Entry<SelectionParams, Double> paramsEntry : entry.getValue().entrySet()) {
                        WeibullParams p = (WeibullParams) paramsEntry.getKey();
                        
                        paramsMap.put(p.getWeibullParam(), paramsEntry.getValue());
                    }
                    
                    result.get(testType).put(level, new ChartArray(paramsMap));
                }
            } catch (TimeoutException ex) {
                throw new ActionException(new DataNotReadyYet(observer.status()));
            } catch (ExecutionException ex) {
                logger.error("ExecutionException was thrown while modulating power", ex.getCause());
                if (ex.getCause() instanceof WrongSelectionSizeException) {
                    throw new ActionException(new InvalidParameter(RequestParams.SELECTION_SIZE));
                } else if (ex.getCause() instanceof UnknownTestTypeException) {
                    throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
                }
                throw new ActionException(new InternalError((ex.getCause() != null) ? ex.getCause().getLocalizedMessage() : ex.getLocalizedMessage()));
            } catch (InterruptedException ex) {
                logger.error(ex);
                throw new ActionException(new InternalError(ex.toString()));
            }
        } catch (HibernateException ex) {
            logger.error("Problems with database", ex);
            throw new ActionException(new ProblemsWithDatabase());
        }

        return result;
    }
    
    @Override
    public Map<String, Map<Integer, Double> > getOrModulateTrueAcceptancePower(
            String[] testTypes,
            int iterCount,
            int selSize,
            boolean ifNotExists) throws ActionException {
        if (testTypes == null) {
            throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPES));
        }
        
        SelectionParams params = ExpParams.getInstance();
        List<SelectionParams> paramsList = Arrays.asList(params);

        Factory factory = Factory.getInstance();

        Map<String, Map<Integer, Double> > result = new TreeMap<>();

        List<Quantile> quantiles = new LinkedList<>();
        try {
            List<SignificanceLevel> levels = factory.getSignificanceLevelsDAO().getAllSignificanceLevels();
            
            for (int i = 0; i < testTypes.length; ++i) {
                String testType = testTypes[i];
                TestType type = factory.getTestTypesDAO().getTestType(testType);
                if (type == null) {
                    throw new ActionException(new NoDependentData(RequestParams.TEST_TYPE));
                }

                IterationsCount count = factory.getIterationsCountsDAO().getIterationsCount(type, iterCount);
                if (count == null) {
                    throw new ActionException(new NoDependentData(RequestParams.ITERATIONS_COUNT));
                }

                Map<Integer, Double> powers = new TreeMap<>();
                result.put(testType, powers);

                for (SignificanceLevel level : levels) {
                    Quantile quantile = factory.getQuantilesDAO().getQuantile(type, level, count, selSize);
                    if (quantile == null) {
                        throw new ActionException(new NoDependentData(RequestParams.QUANTILE));
                    }
                    
                    
                    if (!ifNotExists) {
                        quantiles.add(quantile);
                        continue;
                    }


                    Power power = factory.getPowersDAO().getPower(quantile, params);
                    if (power == null) {
                        quantiles.add(quantile);
                        continue;
                    }
                    powers.put(level.getLevel(), power.getPower());
                }
            }
            

            if (quantiles.isEmpty()) {
                return result;
            }
            
            ProcessObserver<Map<Quantile, Map<SelectionParams, Double> > > observer = TaskManager.getInstance().doPowerModulation(
                    selSize,
                    "exp",
                    quantiles,
                    paramsList,
                    1000);

            if (observer == null) {
                logger.error(
                        new StringBuilder().append("No observer for multiple true acceptance powers modulation (")
                        .append(RequestParams.TEST_TYPE).append(": ").append(testTypes).append(", ")
                        .append(RequestParams.ITERATIONS_COUNT).append(": ").append(iterCount).append(", ")
                        .append(RequestParams.SELECTION_SIZE).append(": ").append(selSize).append(")")
                );
                throw new ActionException(new InternalError("No observer for true acceptance powers modulation"));
            }

            try {
                Map<Quantile, Map<SelectionParams, Double> > modulatorResult = observer.get();

                if (modulatorResult == null) {
                    logger.error(
                            new StringBuilder().append("No powers after multiple true acceptance powers modulation (")
                            .append(RequestParams.TEST_TYPE).append(": ").append(testTypes).append(", ")
                            .append(RequestParams.ITERATIONS_COUNT).append(": ").append(iterCount).append(", ")
                            .append(RequestParams.SELECTION_SIZE).append(": ").append(selSize).append(")")
                    );
                    throw new ActionException(new InternalError("No powers after multiple true acceptance powers modulation"));
                }
                
                for (Entry<Quantile, Map<SelectionParams, Double> > entry : modulatorResult.entrySet()) {
                    String testType = entry.getKey().getTestType().getType();
                    Integer level = entry.getKey().getSignificanceLevel().getLevel();
                            
                    Double power = entry.getValue().get(params);
                    
                    result.get(testType).put(level, power);
                }
            } catch (TimeoutException ex) {
                throw new ActionException(new DataNotReadyYet(observer.status()));
            } catch (ExecutionException ex) {
                logger.error("ExecutionException was thrown while modulating power", ex.getCause());
                if (ex.getCause() instanceof WrongSelectionSizeException) {
                    throw new ActionException(new InvalidParameter(RequestParams.SELECTION_SIZE));
                } else if (ex.getCause() instanceof UnknownTestTypeException) {
                    throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
                }
                throw new ActionException(new InternalError((ex.getCause() != null) ? ex.getCause().getLocalizedMessage() : ex.getLocalizedMessage()));
            } catch (InterruptedException ex) {
                logger.error(ex);
                throw new ActionException(new InternalError(ex.toString()));
            }
        } catch (HibernateException ex) {
            logger.error("Problems with database", ex);
            throw new ActionException(new ProblemsWithDatabase());
        }

        return result;
    }

    @Override
    public SortedSet<SelectionInfo> getSelectionsInfo() throws ActionException {
        SelectionsDAO dao = Factory.getInstance().getSelectionsDAO();
        SortedSet<SelectionInfo> selecions = new TreeSet<>();

        try {
            List<String> names = dao.getAllSelectionsNames();

            if (names == null || names.isEmpty()) {
                return selecions;
            }

            names.stream().forEach((name) -> {
                Integer size = dao.getSelectionSize(name);
                Integer hash = dao.getSelectionHash(name);
                selecions.add(new SelectionInfo(name, size, hash));
            });
        } catch (HibernateException ex) {
            logger.error(ex);
            throw new ActionException(new ProblemsWithDatabase());
        }

        return selecions;
    }

    @Override
    public SelectionData getSelection(String name) throws ActionException {
        if (name == null) {
            throw new ActionException(new InvalidParameter(RequestParams.SELECTION_NAME));
        }

        Lock lock = selectionsLocks.get(name);

        try {
            if (lock == null || !lock.tryLock(500, TimeUnit.MILLISECONDS)) {
                throw new ActionException(new InternalError("Can't lock selection"));
            }
        } catch (InterruptedException ex) {
            logger.error(ex);
            throw new ActionException(new InternalError(ex.toString()));
        }

        try {

            SelectionData cache = selectionsCache.get(name);

            if (cache != null) {
                return cache.clone();
            }

            Selection selection = null;

            try {
                selection = Factory.getInstance().getSelectionsDAO().getSelection(name);
            } catch (HibernateException ex) {
                logger.error(ex);
                throw new ActionException(new ProblemsWithDatabase());
            }

            if (selection == null) {
                throw new ActionException(new NoData());
            }

            List<BigDecimal> values;

            try {
                values = GsonHolder.parseSelectionValues(selection.getValues());
            } catch (JsonSyntaxException ex) {
                logger.error(ex);
                throw new ActionException(new WrongDBData(RequestParams.SELECTION_VALUES));
            }

            int hash = selection.getHash();

            cache = new SelectionData(name, values, hash);

            selectionsCache.put(name, cache);

            return cache.clone();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public SelectionInfo addSelection(String name, String values) throws ActionException {
        if (name == null) {
            throw new ActionException(new InvalidParameter(RequestParams.SELECTION_NAME));
        }

        if (values == null) {
            throw new ActionException(new InvalidParameter(RequestParams.SELECTION_VALUES));
        }

        Lock lock = selectionsLocks.get(name);

        try {
            if (lock == null || !lock.tryLock(500, TimeUnit.MILLISECONDS)) {
                throw new ActionException(new InternalError("Can't lock selection"));
            }
        } catch (InterruptedException ex) {
            logger.error(ex);
            throw new ActionException(new InternalError(ex.toString()));
        }

        try {

            SelectionData cache = selectionsCache.get(name);

            if (cache != null) {
                throw new ActionException(new DataAlreadyExists());
            }

            SelectionsDAO dao = Factory.getInstance().getSelectionsDAO();

            List<BigDecimal> list;

            try {
                list = GsonHolder.parseSelectionValues(values);
            } catch (JsonSyntaxException ex) {
                logger.error(ex);
                throw new ActionException(new InvalidParameter(RequestParams.SELECTION_VALUES));
            }

            SelectionData data = new SelectionData(name, list);
            int size = list.size();
            Selection selection = new Selection(name, size, values);

            try {
                if (!dao.addSelection(selection)) {
                    throw new ActionException(new DataAlreadyExists());
                }
            } catch (ConstraintViolationException ex) {
                logger.error(ex);
                throw new ActionException(new DataAlreadyExists());
            } catch (HibernateException ex) {
                logger.error(ex);
                throw new ActionException(new ProblemsWithDatabase());
            }

            selectionsCache.put(name, data);
            return new SelectionInfo(name, size, data.hash);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public SelectionInfo modulateSelection(
            String name,
            int size,
            String selType,
            boolean sorted,
            HttpServletRequest request) throws ActionException {
        if (name == null) {
            throw new ActionException(new InvalidParameter(RequestParams.SELECTION_NAME));
        }

        if (size < 10 || size > 1000) {
            throw new ActionException(new InvalidParameter(RequestParams.SELECTION_SIZE));
        }

        if (selType == null) {
            throw new ActionException(new InvalidParameter(RequestParams.SELECTION_TYPE));
        }

        Lock lock = selectionsLocks.get(name);

        try {
            if (lock == null || !lock.tryLock(500, TimeUnit.MILLISECONDS)) {
                throw new ActionException(new InternalError("Can't lock selection"));
            }
        } catch (InterruptedException ex) {
            logger.error(ex);
            throw new ActionException(new InternalError(ex.toString()));
        }

        try {

            SelectionData cache = selectionsCache.get(name);

            if (cache != null) {
                throw new ActionException(new DataAlreadyExists());
            }

            SelectionsDAO dao = Factory.getInstance().getSelectionsDAO();

            List<BigDecimal> list;

            switch (selType) {
                case "exp":
                    list = SelectionsModulator.createExpSelection(size, sorted);
                    break;
                case "weibull":
                    BigDecimal weibullParam;

                    try {
                        String weibullStr = request.getParameter(RequestParams.WEIBULL_PARAMETER);

                        if (weibullStr == null || weibullStr.equals("")) {
                            throw new ActionException(new InvalidParameter(RequestParams.WEIBULL_PARAMETER));
                        }

                        weibullParam = new BigDecimal(weibullStr);
                    } catch (Exception ex) {
                        logger.error(ex);
                        throw new ActionException(new InvalidParameter(RequestParams.WEIBULL_PARAMETER));
                    }

                    try {
                        list = SelectionsModulator.createWeibullSelection(size, sorted, weibullParam);
                    } catch (InvalidParameterException ex) {
                        logger.error(ex);
                        throw new ActionException(new InvalidParameter(RequestParams.WEIBULL_PARAMETER));
                    }
                    break;
                default:
                    throw new ActionException(new InvalidParameter(RequestParams.SELECTION_TYPE));
            }

            SelectionData data = new SelectionData(name, list);
            Selection selection = new Selection(name, size, GsonHolder.getGson().toJson(list));

            try {
                if (!dao.addSelection(selection)) {
                    throw new ActionException(new DataAlreadyExists());
                }
            } catch (ConstraintViolationException ex) {
                logger.error(ex);
                throw new ActionException(new DataAlreadyExists());
            } catch (HibernateException ex) {
                logger.error(ex);
                throw new ActionException(new ProblemsWithDatabase());
            }

            selectionsCache.put(name, data);
            return new SelectionInfo(name, size, data.hash);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public SelectionInfo appendToSelection(String name, String values) throws ActionException {
        if (name == null) {
            throw new ActionException(new InvalidParameter(RequestParams.SELECTION_NAME));
        }

        Lock lock = selectionsLocks.get(name);

        try {
            if (lock == null || !lock.tryLock(500, TimeUnit.MILLISECONDS)) {
                throw new ActionException(new InternalError("Can't lock selection"));
            }
        } catch (InterruptedException ex) {
            logger.error(ex);
            throw new ActionException(new InternalError(ex.toString()));
        }

        try {
            SelectionsDAO dao = Factory.getInstance().getSelectionsDAO();

            SelectionData cache = selectionsCache.get(name);

            Selection selection = null;
            if (cache == null) {
                try {
                    selection = dao.getSelection(name);
                } catch (HibernateException ex) {
                    logger.error(ex);
                    throw new ActionException(new ProblemsWithDatabase());
                }

                if (selection == null) {
                    throw new ActionException(new NoData());
                }

                List<BigDecimal> list;
                try {
                    list = GsonHolder.parseSelectionValues(selection.getValues());
                } catch (JsonSyntaxException ex) {
                    logger.error(ex);
                    throw new ActionException(new WrongDBData(RequestParams.SELECTION_VALUES));
                }

                cache = new SelectionData(name, list, selection.getHash());
                selectionsCache.put(name, cache);
            }

            List<BigDecimal> newValues;

            try {
                newValues = GsonHolder.parseSelectionValues(values);
            } catch (JsonSyntaxException ex) {
                logger.error(ex);
                throw new ActionException(new InvalidParameter(RequestParams.SELECTION_VALUES));
            }

            cache.values.addAll(newValues);
            cache.hash++;

            int size = cache.values.size();
            if (selection == null) {
                selection = new Selection(
                        name,
                        size,
                        GsonHolder.getGson().toJson(cache.values),
                        cache.hash
                );
            } else {
                selection.setSize(size);
                selection.setValues(GsonHolder.getGson().toJson(cache.values));
                selection.setHash(cache.hash);
            }

            try {
                if (!dao.updateSelection(selection)) {
                    throw new ActionException(new NoData());
                }
            } catch (HibernateException ex) {
                logger.error(ex);
                throw new ActionException(new ProblemsWithDatabase());
            }

            return new SelectionInfo(name, size, cache.hash);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeSelection(String name) throws ActionException {
        if (name == null) {
            throw new ActionException(new InvalidParameter(RequestParams.SELECTION_NAME));
        }

        Lock lock = selectionsLocks.get(name);

        try {
            if (lock == null || !lock.tryLock(500, TimeUnit.MILLISECONDS)) {
                throw new ActionException(new InternalError("Can't lock selection"));
            }
        } catch (InterruptedException ex) {
            logger.error(ex);
            throw new ActionException(new InternalError(ex.toString()));
        }

        try {
            SelectionData cache = selectionsCache.get(name);

            if (cache != null) {
                selectionsCache.remove(name);
            }

            try {
                if (!Factory.getInstance().getSelectionsDAO().removeSelection(name)) {
                    throw new ActionException(new NoData());
                }
            } catch (HibernateException ex) {
                logger.error(ex);
                throw new ActionException(new ProblemsWithDatabase());
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public SelectionInfo sortSelection(String name) throws ActionException {
        if (name == null) {
            throw new ActionException(new InvalidParameter(RequestParams.SELECTION_NAME));
        }

        Lock lock = selectionsLocks.get(name);

        try {
            if (lock == null || !lock.tryLock(500, TimeUnit.MILLISECONDS)) {
                throw new ActionException(new InternalError("Can't lock selection"));
            }
        } catch (InterruptedException ex) {
            logger.error(ex);
            throw new ActionException(new InternalError(ex.toString()));
        }

        try {
            SelectionsDAO dao = Factory.getInstance().getSelectionsDAO();

            SelectionData cache = selectionsCache.get(name);

            Selection selection = null;
            if (cache == null) {
                try {
                    selection = dao.getSelection(name);
                } catch (HibernateException ex) {
                    logger.error(ex);
                    throw new ActionException(new ProblemsWithDatabase());
                }

                if (selection == null) {
                    throw new ActionException(new NoData());
                }

                List<BigDecimal> list;
                try {
                    list = GsonHolder.parseSelectionValues(selection.getValues());
                } catch (JsonSyntaxException ex) {
                    logger.error(ex);
                    throw new ActionException(new WrongDBData(RequestParams.SELECTION_VALUES));
                }

                cache = new SelectionData(name, list, selection.getHash());
                selectionsCache.put(name, cache);
            }

            Collections.sort(cache.values);
            cache.hash++;

            int size = cache.values.size();
            if (selection == null) {
                selection = new Selection(
                        name,
                        size,
                        GsonHolder.getGson().toJson(cache.values),
                        cache.hash
                );
            } else {
                selection.setSize(size);
                selection.setValues(GsonHolder.getGson().toJson(cache.values));
                selection.setHash(cache.hash);
            }

            try {
                if (!dao.updateSelection(selection)) {
                    throw new ActionException(new NoData());
                }
            } catch (HibernateException ex) {
                logger.error(ex);
                throw new ActionException(new ProblemsWithDatabase());
            }

            return new SelectionInfo(name, size, cache.hash);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public TestResultsMap checkSelectionByTests(
            final String selectionName,
            final SortedSet<String> testTypes,
            final int signLevel) throws ActionException {
        if (selectionName == null) {
            throw new ActionException(new InvalidParameter(RequestParams.SELECTION_NAME));
        }

        if (testTypes == null) {
            throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPES));
        }

        Lock lock = selectionsLocks.get(selectionName);

        try {
            if (lock == null || !lock.tryLock(500, TimeUnit.MILLISECONDS)) {
                throw new ActionException(new InternalError("Can't lock selection"));
            }
        } catch (InterruptedException ex) {
            logger.error(ex);
            throw new ActionException(new InternalError(ex.toString()));
        }

        Factory factory = Factory.getInstance();

        try {
            SelectionData cache = selectionsCache.get(selectionName);

            if (cache == null) {
                Selection selection = factory.getSelectionsDAO().getSelection(selectionName);

                if (selection == null) {
                    throw new ActionException(new NoData());
                }

                List<BigDecimal> list;
                try {
                    list = GsonHolder.parseSelectionValues(selection.getValues());
                } catch (JsonSyntaxException ex) {
                    logger.error(ex);
                    throw new ActionException(new WrongDBData(RequestParams.SELECTION_VALUES));
                }

                cache = new SelectionData(selectionName, list, selection.getHash());
                selectionsCache.put(selectionName, cache);
            }

            SignificanceLevel level = new SignificanceLevel(signLevel);

            if (!factory.getSignificanceLevelsDAO().containsSignificanceLevel(level)) {
                throw new ActionException(new NoDependentData(RequestParams.SIGNIFICANCE_LEVEL));
            }

            TestResultsMap resultsMap = new TestResultsMap(
                    new SelectionInfo(
                            cache.name,
                            cache.values.size(),
                            cache.hash
                    ));

            final List<BigDecimal> values = cache.values;
            testTypes.stream().forEach((testType) -> {
                ActionResult actionResult;
                try {
                    TestResult testResult = checkSelectionByTest(
                            values,
                            testType,
                            level);
                    actionResult = new SucceededActionResult(testResult);
                } catch (ActionException ex) {
                    logger.error(
                            new StringBuilder().append("Failed to test selection \"")
                            .append(selectionName).append("\"").append(" by test type \"")
                            .append(testType).append("\": ").append(ex.getReason().toString())
                    );
                    actionResult = new FailedActionResult(ex.getReason());
                }

                resultsMap.test_results.put(testType, actionResult);
            });

            return resultsMap;
        } catch (HibernateException ex) {
            logger.error(ex);
            throw new ActionException(new ProblemsWithDatabase());
        } finally {
            lock.unlock();
        }
    }

    private TestResult checkSelectionByTest(
            final List<BigDecimal> selectionValues,
            final String testType,
            final SignificanceLevel signLevel) throws ActionException {
        if (testType == null) {
            throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
        }

        Factory factory = Factory.getInstance();

        try {
            TestType type = factory.getTestTypesDAO().getTestType(testType);
            if (type == null) {
                throw new ActionException(new NoDependentData(RequestParams.TEST_TYPE));
            }

            return ExpTestsExecutor.makeTestForSelection(selectionValues, type, signLevel);
        } catch (HibernateException ex) {
            logger.error(ex);
            throw new ActionException(new ProblemsWithDatabase());
        }
    }

    public WeibullAlternativeResult createResultsArrayAgainstWeibull(
            final int iterCount,
            final int signLevel,
            final int selSize,
            final String resultsJson
            ) throws ActionException {
        if (resultsJson == null) {
            throw new ActionException(new InvalidParameter("results"));
        }
        
        SortedMap<String, Boolean> testResults;
        try {
            testResults = GsonHolder.parseTestResults(resultsJson);
        } catch (JsonSyntaxException ex) {
            logger.error(ex);
            throw new ActionException(new InvalidParameter("results"));
        }
        
        Factory factory = Factory.getInstance();

        WeibullAlternativeResult result = new WeibullAlternativeResult();
        
        try {
            SignificanceLevel level = new SignificanceLevel(signLevel);

            if (!factory.getSignificanceLevelsDAO().containsSignificanceLevel(level)) {
                throw new ActionException(new NoDependentData(RequestParams.SIGNIFICANCE_LEVEL));
            }
            
            List<String> typesWithoutPowers = new LinkedList<>();
            Map<String, ResultsArrayInfo> powersMap = new TreeMap<>();
            for (SortedMap.Entry<String, Boolean> entry : testResults.entrySet()) {
                String testName = entry.getKey();
                
                TestType type = factory.getTestTypesDAO().getTestType(testName);
                if (type == null) {
                    throw new ActionException(new NoDependentData(RequestParams.TEST_TYPE));
                }
                
                IterationsCount count = factory.getIterationsCountsDAO().getIterationsCount(type, iterCount);
                if (count == null) {
                    throw new ActionException(new NoDependentData(RequestParams.ITERATIONS_COUNT));
                }
                
                Quantile quantile = factory.getQuantilesDAO().getQuantile(type, level, count, selSize);
                if (quantile == null) {
                    throw new ActionException(new NoDependentData(RequestParams.QUANTILE));
                }
                
                Power trueAccuracyPower = factory.getPowersDAO().getPower(quantile, ExpParams.getInstance());
                if (trueAccuracyPower == null) {
                    typesWithoutPowers.add(type.getName());
                    continue;
                }
                
                
                List<Power> powers = factory.getPowersDAO().getPowers(quantile, SelectionsEnum.WEIBULL);
                
                if (powers == null || powers.isEmpty()) {
                    typesWithoutPowers.add(testName);
                    continue;
                }
                
                powers.sort((Power first, Power second) -> {
                    WeibullParams paramsFirst = (WeibullParams)first.getAlternativeParams();
                    WeibullParams paramsSecond = (WeibullParams)second.getAlternativeParams();
                    return paramsFirst.getWeibullParam().compareTo(paramsSecond.getWeibullParam());
                });
                
                powersMap.put(testName, new ResultsArrayInfo(
                        entry.getValue(),
                        trueAccuracyPower.getPower(),
                        powers.listIterator()));
            }
            
            if (powersMap.isEmpty()) {
                throw new ActionException(new NoData());
            }
            
            if (!typesWithoutPowers.isEmpty()) {
                result.comment = new StringBuilder()
                        .append("Powers for ").append(typesWithoutPowers).append(" dont exist").toString();
            }
            
            double totalSum = 0.0;
            double totalCount = 0;
            for (SelectionParams params : PowerModulator.weibulParamsList) {
                WeibullParams wParams = (WeibullParams)params;
                double sum = 0.0;
                int count = 0;
                for (Map.Entry<String, ResultsArrayInfo> powerEntry : powersMap.entrySet()) {
                    ResultsArrayInfo info = powerEntry.getValue();
                    while(info.alternativePowers.hasNext()) {
                        Power power = info.alternativePowers.next();
                        int compare = power.getAlternativeParams().compareTo(params);
                        if (compare < 0) {
                            continue;
                        }
                        if (compare > 0) {
                            break;
                        }
                        
                        double coef = (info.succeded) ? 
                                (info.trueAcceptancePower / (info.trueAcceptancePower + 1.0 - power.getPower())) :
                                ((1.0 - info.trueAcceptancePower) / (1.0 - info.trueAcceptancePower + power.getPower()));
                        sum += coef;
                        ++count;
                        break;
                    }
                }
                
                if (sum < 0.00001) {
                    continue;
                }
                
                sum /= count;
                totalSum += sum;
                ++totalCount;
                result.result.put(wParams.getWeibullParam(), sum);
            }
            
            result.total = totalSum / totalCount;
            
        } catch (HibernateException ex) {
            logger.error(ex);
            throw new ActionException(new ProblemsWithDatabase());
        }
        
        return result;
    }
}
