package controllers;


import com.google.gson.Gson;
import entities.ChartArray;
import entities.IterationsCountsSet;
import entities.QuantilesMap;
import entities.SelectionData;
import entities.SelectionInfo;
import entities.TestResultsMap;
import entities.WeibullAlternativeResult;
import java.io.IOException;
import java.util.SortedSet;
import entities.actionresults.ActionResult;
import entities.actionresults.FailedActionResult;
import entities.actionresults.SucceededActionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import services.ApplicationService;
import entities.exceptions.ActionException;
import hibernate.logic.IterationsCount;
import hibernate.logic.Power;
import hibernate.logic.Quantile;
import hibernate.logic.SignificanceLevel;
import hibernate.logic.TestType;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;
import logic.tests.QuantilePair;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ApplicationController {

    private final Gson gson = GsonHolder.getGson();
    
    private static final Logger logger = Logger.getLogger(ApplicationController.class);
    
    @Autowired
    private ApplicationService appService; 
    
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView index() {
        ModelAndView model = new ModelAndView("index");
        return model;
    }
    
    @RequestMapping(value = "/selections", method = RequestMethod.GET)
    public ModelAndView selections() {
        logger.info("Trying to get selections model");
        
        ModelAndView model; 
        
        try {
            SortedSet<SelectionInfo> selectionsInfo = appService.getSelectionsInfo();
            model = new ModelAndView("selections");
            model.addObject("selectionsInfo", selectionsInfo);
            
            logger.info("Getting selections model: done");
        }catch(ActionException ex) {
            logger.error("Failed to get selections model: " + ex.getReason().toString());
            
            model = new ModelAndView("error");
            model.addObject("reason", ex.getReason());
        }
        
        return model;
    }
    
    @RequestMapping(value = "/tests", method = RequestMethod.GET)
    public ModelAndView tests() {
        logger.info("Trying to get tests model");
        
        ModelAndView model; 
        
        try {
            List<TestType> testTypes = appService.getTestTypes();
            model = new ModelAndView("tests");
            model.addObject("testTypes", testTypes);
            
            logger.info("Getting tests model: done");
        }catch(ActionException ex) {
            logger.error("Failed to get tests model: " + ex.getReason().toString());
            
            model = new ModelAndView("error");
            model.addObject("reason", ex.getReason());
        }
        
        return model;
    }
    
    @RequestMapping(value = "/testtypes", method = RequestMethod.GET)
    public @ResponseBody String getTestTypes() throws IOException {
        logger.info("Trying to get test types");
        
        ActionResult result; 
        
        try {
            List<TestType> types = appService.getTestTypes();
            result = new SucceededActionResult<>(types);
            
            logger.info("Getting test types: done");
        }catch(ActionException ex) {
            logger.error("Failed to get test types: " + ex.getReason().toString());
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    @RequestMapping(value = "/addtesttype", method = RequestMethod.GET)
    public @ResponseBody String addTestType(
            @RequestParam(value = RequestParams.TEST_TYPE, defaultValue = "") String type,
            @RequestParam(value = RequestParams.TEST_TYPE_NAME, defaultValue = "") String name,
            @RequestParam(value = RequestParams.TEST_TYPE_FOR_SORTED_PARAM, defaultValue = "") boolean forSorted
                ) throws IOException {
        logger.info(
                    new StringBuilder()
                            .append("Trying to add test type \"")
                            .append(type).append("\"")
        );
        
        ActionResult result; 
        
        try {
            appService.addTestType(type, name, forSorted);
            result = new SucceededActionResult<>();
            
            logger.info(
                    new StringBuilder()
                            .append("Adding test type \"")
                            .append(type).append("\": done")
            );
        }catch(ActionException ex) {
            logger.error(
                    new StringBuilder("Failed to add test type \"")
                            .append(type).append("\": ")
                            .append(ex.getReason().toString())
                    );
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    @RequestMapping(value = "/removetesttype", method = RequestMethod.GET)
    public @ResponseBody String removeTestType(
            @RequestParam(value = RequestParams.TEST_TYPE, defaultValue = "") String type
                ) throws IOException {
        logger.info(
                    new StringBuilder()
                            .append("Trying to remove test type \"")
                            .append(type).append("\"")
        );
        
        ActionResult result; 
        
        try {
            appService.removeTestType(type);
            result = new SucceededActionResult<>();
            
            logger.info(
                    new StringBuilder()
                            .append("Removing test type \"")
                            .append(type).append("\": done")
            );
        }catch(ActionException ex) {
            logger.error(
                    new StringBuilder("Failed to remove test type \"")
                            .append(type).append("\": ")
                            .append(ex.getReason().toString())
                    );
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    
    @RequestMapping(value = "/itercounts", method = RequestMethod.GET)
    public @ResponseBody String getIterCounts(
            @RequestParam(value = RequestParams.TEST_TYPE, defaultValue = "") String type
                ) throws IOException {
        logger.info(
                new StringBuilder("Trying to get iterations counts for test type \"")
                            .append(type).append("\"")
        );
        
        ActionResult result; 
        
        try {
            IterationsCountsSet iterCountsSet = appService.getIterationsCounts(type);
            
            result = new SucceededActionResult<>(iterCountsSet);
            
            logger.info(
                    new StringBuilder()
                            .append("Getting iterations counts for test type \"")
                            .append(type).append("\": done")
            );
        }catch(ActionException ex) {
            logger.error(
                    new StringBuilder("Failed to get iterations counts for test type \"")
                            .append(type).append("\": ").append(ex.getReason().toString())
                    );
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    @RequestMapping(value = "/additercount", method = RequestMethod.GET)
    public @ResponseBody String addIterCount(
            @RequestParam(value = RequestParams.TEST_TYPE, defaultValue = "") String type,
            @RequestParam(value = RequestParams.ITERATIONS_COUNT, defaultValue = "0") int count
                ) throws IOException {
        logger.info(
                    new StringBuilder()
                            .append("Trying to add iterations count(")
                            .append(count).append(") for test type \"")
                            .append(type).append("\"")
        );
        
        ActionResult result; 
        
        try {
            appService.addIterationsCount(type, count);
            result = new SucceededActionResult<>();
            
            logger.info(
                    new StringBuilder()
                            .append("Adding iterations count(")
                            .append(count).append(") for test type \"")
                            .append(type).append("\": done")
            );
        }catch(ActionException ex) {
            logger.error(
                    new StringBuilder("Failed to add iterations count(").append(count)
                            .append(") for test type \"").append(type).append("\": ")
                            .append(ex.getReason().toString())
                    );
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    @RequestMapping(value = "/removeitercount", method = RequestMethod.GET)
    public @ResponseBody String removeIterCount(
            @RequestParam(value = RequestParams.TEST_TYPE, defaultValue = "") String type,
            @RequestParam(value = RequestParams.ITERATIONS_COUNT, defaultValue = "0") int count
                ) throws IOException {
        logger.info(
                    new StringBuilder()
                            .append("Trying to remove iterations count(\"")
                            .append(count).append(") for test type \"")
                            .append(type).append("\"")
        );
        
        ActionResult result; 
        
        try {
            appService.removeIterationsCount(type, count);
            result = new SucceededActionResult<>();
            
            logger.info(
                    new StringBuilder()
                            .append("Removing iterations count(\"")
                            .append(count).append(") for test type \"")
                            .append(type).append("\": done")
            );
        }catch(ActionException ex) {
            logger.error(
                    new StringBuilder("Failed to remove iterations count(\"").append(count)
                            .append(") for test type \"").append(type).append("\": ")
                            .append(ex.getReason().toString())
                    );
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    
    @RequestMapping(value = "/signlevels", method = RequestMethod.GET)
    public @ResponseBody String getSignificanceLevels() throws IOException {
        logger.info("Trying to get significance levels");
        
        ActionResult result; 
        
        try {
            List<SignificanceLevel> types = appService.getSignificanceLevels();
            Set<Integer> signLevelsSet = new TreeSet<>();
            
            types.stream().forEach((signLevel) -> {
                signLevelsSet.add(signLevel.getLevel());
            });
            
            result = new SucceededActionResult<>(signLevelsSet);
            
            logger.info("Getting significance levels: done");
        }catch(ActionException ex) {
            logger.error("Failed to get significance levels: " + ex.getReason().toString());
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    @RequestMapping(value = "/addsignlevel", method = RequestMethod.GET)
    public @ResponseBody String addSignificanceLevel(
            @RequestParam(value = RequestParams.SIGNIFICANCE_LEVEL, defaultValue = "0") int signLevel
                ) throws IOException {
        logger.info(
                    new StringBuilder()
                            .append("Trying to add significance level \"")
                            .append(((double)signLevel / (SignificanceLevel.getMaxLevel() * 2))).append("\"")
        );
        
        ActionResult result; 
        
        try {
            appService.addSignificanceLevel(signLevel);
            result = new SucceededActionResult<>();
            
            logger.info(
                    new StringBuilder()
                            .append("Adding significance level \"")
                            .append(((double)signLevel / (SignificanceLevel.getMaxLevel() * 2)))
                            .append("\": done")
            );
        }catch(ActionException ex) {
            logger.error(
                    new StringBuilder("Failed to add significance level \"")
                            .append(((double)signLevel / (SignificanceLevel.getMaxLevel() * 2)))
                            .append("\": ")
                            .append(ex.getReason().toString())
                    );
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    @RequestMapping(value = "/removesignlevel", method = RequestMethod.GET)
    public @ResponseBody String removeSignificanceLevel(
            @RequestParam(value = RequestParams.SIGNIFICANCE_LEVEL, defaultValue = "0") int signLevel
                ) throws IOException {
        logger.info(
                    new StringBuilder()
                            .append("Trying to remove significance level \"")
                            .append(((double)signLevel / (SignificanceLevel.getMaxLevel() * 2)))
                            .append("\"")
        );
        
        ActionResult result; 
        
        try {
            appService.removeSignificanceLevel(signLevel);
            result = new SucceededActionResult<>();
            
            logger.info(
                    new StringBuilder()
                            .append("Removing significance level \"")
                            .append(((double)signLevel / (SignificanceLevel.getMaxLevel() * 2)))
                            .append("\": done")
            );
        }catch(ActionException ex) {
            logger.error(
                    new StringBuilder("Failed to remove significance level \"")
                            .append(((double)signLevel / (SignificanceLevel.getMaxLevel() * 2)))
                            .append("\": ")
                            .append(ex.getReason().toString())
                    );
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    
    @RequestMapping(value = "/modulatequantile", method = RequestMethod.GET)
    public @ResponseBody String getOrModulateQuantile(
            @RequestParam(value = RequestParams.TEST_TYPE, defaultValue = "") String testType,
            @RequestParam(value = RequestParams.ITERATIONS_COUNT, defaultValue = "0") int iterCount,
            @RequestParam(value = RequestParams.SIGNIFICANCE_LEVEL, defaultValue = "0") int signLevel,
            @RequestParam(value = RequestParams.SELECTION_SIZE, defaultValue = "0") int selSize,
            @RequestParam(value = "if_not_exists", defaultValue = "false") boolean ifNotExists
                ) throws IOException {
        logger.info(
                    new StringBuilder()
                            .append("Trying to ").append((ifNotExists)? "get or " : "")
                            .append("modulate quantile(")
                            .append(testType).append("-").append(iterCount)
                            .append("-").append(signLevel)
                            .append("-").append(selSize).append(")")
        );
        
        ActionResult result; 
        
        try {
            Quantile quantile = (ifNotExists) ?
                    appService.getOrModulateQuantile(testType, iterCount, signLevel, selSize) :
                    appService.modulateQuantile(testType, iterCount, signLevel, selSize);
            
            QuantilePair pair = new QuantilePair(selSize, quantile.getFirstValue(), quantile.getSecondValue());
            result = new SucceededActionResult<>(pair);
            
            logger.info(
                    new StringBuilder()
                            .append("Modulatting quantile(")
                            .append(testType).append("-").append(iterCount)
                            .append("-").append(signLevel)
                            .append("-").append(selSize).append("): done")
            );
        }catch(ActionException ex) {
            logger.error(
                    new StringBuilder("Failed to ").append((ifNotExists)? "get or " : "")
                            .append("modulate quantile(")
                            .append(testType).append("-").append(iterCount)
                            .append("-").append(signLevel)
                            .append("-").append(selSize)
                            .append("): ")
                            .append(ex.getReason().toString())
                    );
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    @RequestMapping(value = "/modulatequantiles", method = RequestMethod.GET)
    public @ResponseBody String getOrModulateQuantiles(
            @RequestParam(value = RequestParams.TEST_TYPES, defaultValue = "") String[] testTypes,
            @RequestParam(value = RequestParams.ITERATIONS_COUNT, defaultValue = "0") int iterCount,
            @RequestParam(value = RequestParams.SELECTION_SIZE, defaultValue = "0") int selSize,
            @RequestParam(value = "if_not_exists", defaultValue = "false") boolean ifNotExists
                ) throws IOException {
        logger.info(
                    new StringBuilder()
                            .append("Trying to ").append((ifNotExists)? "get or " : "")
                            .append("modulate quantiles(")
                            .append(testTypes.toString()).append("-").append(iterCount)
                            .append("-").append(selSize).append(")")
        );
        
        ActionResult result; 
        
        try {
            Map<String, Map<Integer, QuantilePair> > quantiles = 
                    appService.getOrModulateMultipleQuantiles(testTypes, iterCount, selSize, ifNotExists);
            
            result = new SucceededActionResult<>(quantiles);
            
            logger.info(
                    new StringBuilder()
                            .append("Modulatting quantiles(")
                            .append(testTypes).append("-").append(iterCount)
                            .append("-").append(selSize).append("): done")
            );
        }catch(ActionException ex) {
            logger.error(
                    new StringBuilder("Failed to ").append((ifNotExists)? "get or " : "")
                            .append("modulate quantiles(")
                            .append(testTypes).append("-").append(iterCount)
                            .append("-").append(selSize)
                            .append("): ")
                            .append(ex.getReason().toString())
                    );
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    @RequestMapping(value = "/getquantiles", method = RequestMethod.GET)
    public @ResponseBody String getQuantiles(
            @RequestParam(value = RequestParams.TEST_TYPE, defaultValue = "") String testType,
            @RequestParam(value = RequestParams.ITERATIONS_COUNT, defaultValue = "0") int iterCount,
            @RequestParam(value = RequestParams.MIN_SELECTION_SIZE, defaultValue = "0") int minSelSize,
            @RequestParam(value = RequestParams.QUANTILES_COUNT, defaultValue = "0") int quantilesCount
                ) throws IOException {
        logger.info(
                    new StringBuilder()
                            .append("Trying get quantiles(")
                            .append(testType).append("-").append(iterCount)
                            .append("-").append(minSelSize)
                            .append("-").append(quantilesCount).append(")")
        );
        
        ActionResult result; 
        
        try {
            QuantilesMap map = appService.getQuantiles(testType, iterCount, minSelSize, quantilesCount);
            
            result = new SucceededActionResult<>(map);
            
            logger.info(
                    new StringBuilder()
                            .append("Getting quantiles(")
                            .append(testType).append("-").append(iterCount)
                            .append("-").append(minSelSize)
                            .append("-").append(quantilesCount).append("): done")
            );
        }catch(ActionException ex) {
            logger.error(
                    new StringBuilder("Failed to get quantiles(")
                            .append(testType).append("-").append(iterCount)
                            .append("-").append(minSelSize)
                            .append("-").append(quantilesCount).append("): ")
                            .append(ex.getReason().toString())
                    );
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    @RequestMapping(value = "/modulatepower", method = RequestMethod.GET)
    public @ResponseBody String modulatePower(
            @RequestParam(value = RequestParams.TEST_TYPE, defaultValue = "") String testType,
            @RequestParam(value = RequestParams.ITERATIONS_COUNT, defaultValue = "0") int iterCount,
            @RequestParam(value = RequestParams.SIGNIFICANCE_LEVEL, defaultValue = "0") int signLevel,
            @RequestParam(value = RequestParams.SELECTION_SIZE, defaultValue = "0") int selSize,
            @RequestParam(value = RequestParams.ALTERNATIVE, defaultValue = "") String alternative,
            @RequestParam(value = "if_not_exists", defaultValue = "false") boolean ifNotExists,
            HttpServletRequest request
                ) throws IOException {
        logger.info(
                    new StringBuilder()
                            .append("Trying to modulate power (")
                            .append(testType).append("-").append(iterCount)
                            .append("-").append(signLevel)
                            .append("-").append(selSize)
                            .append("-").append(alternative).append(")")
        );
        
        ActionResult result; 
        
        try {
            Double power = appService.getOrModulatePower(testType, iterCount, signLevel, selSize, alternative, request, ifNotExists);
            result = new SucceededActionResult<>(power);
            
            logger.info(
                    new StringBuilder()
                            .append("Modulating of power (")
                            .append(testType).append("-").append(iterCount)
                            .append("-").append(signLevel)
                            .append("-").append(selSize)
                            .append("-").append(alternative).append("): done")
            );
        }catch(ActionException ex) {
            logger.error("Failed to modulate power: " + ex.getReason().toString());
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    
    
    @RequestMapping(value = "/modulateweibullpowers", method = RequestMethod.GET)
    public @ResponseBody String modulateWeibullPowers(
            @RequestParam(value = RequestParams.TEST_TYPE, defaultValue = "") String testType,
            @RequestParam(value = RequestParams.ITERATIONS_COUNT, defaultValue = "0") int iterCount,
            @RequestParam(value = RequestParams.SELECTION_SIZE, defaultValue = "0") int selSize,
            @RequestParam(value = "if_not_exists", defaultValue = "false") boolean ifNotExists
                ) throws IOException {
        logger.info(
                    new StringBuilder()
                            .append("Trying to modulate weibull powers (")
                            .append(testType).append("-").append(iterCount)
                            .append("-").append(selSize).append(")")
        );
        
        ActionResult result; 
        
        try {
            Map<Integer, ChartArray > powers = appService.getOrModulateWeibullPower(testType, iterCount, selSize, ifNotExists);
            result = new SucceededActionResult<>(powers);
            
            logger.info(
                    new StringBuilder()
                            .append("Modulating of weibull powers (")
                            .append(testType).append("-").append(iterCount)
                            .append("-").append(selSize).append("): done")
            );
        }catch(ActionException ex) {
            logger.error("Failed to modulate weibull powers: " + ex.getReason().toString());
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    @RequestMapping(value = "/modulatemweibullpowers", method = RequestMethod.GET)
    public @ResponseBody String modulateWeibullPowers(
            @RequestParam(value = RequestParams.TEST_TYPES, defaultValue = "") String[] testTypes,
            @RequestParam(value = RequestParams.ITERATIONS_COUNT, defaultValue = "0") int iterCount,
            @RequestParam(value = RequestParams.SELECTION_SIZE, defaultValue = "0") int selSize,
            @RequestParam(value = "if_not_exists", defaultValue = "false") boolean ifNotExists
                ) throws IOException {
        logger.info(
                    new StringBuilder()
                            .append("Trying to modulate multiple weibull powers (")
                            .append(testTypes).append("-").append(iterCount)
                            .append("-").append(selSize).append(")")
        );
        
        ActionResult result; 
        
        try {
            Map<String, Map<Integer, ChartArray> > powers = appService.getOrModulateWeibullPower(testTypes, iterCount, selSize, ifNotExists);
            result = new SucceededActionResult<>(powers);
            
            logger.info(
                    new StringBuilder()
                            .append("Modulating of multiple weibull powers (")
                            .append(testTypes).append("-").append(iterCount)
                            .append("-").append(selSize).append("): done")
            );
        }catch(ActionException ex) {
            logger.error("Failed to modulate multiple weibull powers: " + ex.getReason().toString());
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    @RequestMapping(value = "/modulatemtap", method = RequestMethod.GET)
    public @ResponseBody String modulateMultipleTrueAcceptancePowers(
            @RequestParam(value = RequestParams.TEST_TYPES, defaultValue = "") String[] testTypes,
            @RequestParam(value = RequestParams.ITERATIONS_COUNT, defaultValue = "0") int iterCount,
            @RequestParam(value = RequestParams.SELECTION_SIZE, defaultValue = "0") int selSize,
            @RequestParam(value = "if_not_exists", defaultValue = "false") boolean ifNotExists
                ) throws IOException {
        logger.info(
                    new StringBuilder()
                            .append("Trying to modulate multiple true acceptance powers (")
                            .append(testTypes).append("-").append(iterCount)
                            .append("-").append(selSize).append(")")
        );
        
        ActionResult result; 
        
        try {
            Map<String, Map<Integer, Double> > powers = appService.getOrModulateTrueAcceptancePower(testTypes, iterCount, selSize, ifNotExists);
            result = new SucceededActionResult<>(powers);
            
            logger.info(
                    new StringBuilder()
                            .append("Modulating of multiple true acceptance powers (")
                            .append(testTypes).append("-").append(iterCount)
                            .append("-").append(selSize).append("): done")
            );
        }catch(ActionException ex) {
            logger.error("Failed to modulate multiple true acceptance powers: " + ex.getReason().toString());
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    @RequestMapping(value = "/addselection", method = RequestMethod.GET)
    public @ResponseBody String addValues(
            @RequestParam(value = RequestParams.SELECTION_NAME, defaultValue = "") String name,
            @RequestParam(value = RequestParams.SELECTION_VALUES, defaultValue = "[]") String values
                ) throws IOException {
        logger.info(
                    new StringBuilder()
                            .append("Trying to add selection \"")
                            .append(name)
                            .append("\"")
        );
        
        ActionResult result; 
        
        try {
            SelectionInfo selectionInfo = appService.addSelection(name, values);
            result = new SucceededActionResult<>(selectionInfo);
            
            logger.info(
                    new StringBuilder()
                            .append("Adding of selection \"")
                            .append(name)
                            .append("\": done")
            );
        }catch(ActionException ex) {
            logger.error("Failed to add selection: " + ex.getReason().toString());
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    
    
    @RequestMapping(value = "/modulateselection", method = RequestMethod.GET)
    public @ResponseBody String addValues(
            @RequestParam(value = RequestParams.SELECTION_NAME, defaultValue = "") String name,
            @RequestParam(value = RequestParams.SELECTION_SIZE, defaultValue = "") int size,
            @RequestParam(value = RequestParams.SELECTION_TYPE, defaultValue = "") String selType,
            @RequestParam(value = "sorted", defaultValue = "false") boolean sorted,
            HttpServletRequest request
                ) throws IOException {
        logger.info(
                    new StringBuilder()
                            .append("Trying to modulate selection (")
                            .append(name).append(", ").append(size).append(", ") 
                            .append(selType).append(")")
        );
        
        ActionResult result; 
        
        try {
            SelectionInfo selectionInfo = appService.modulateSelection(name, size, selType, sorted, request);
            result = new SucceededActionResult<>(selectionInfo);
            
            logger.info(
                    new StringBuilder()
                            .append("Modulating of selection (")
                            .append(name).append(", ").append(size).append(", ") 
                            .append(selType).append("): done")
            );
        }catch(ActionException ex) {
            logger.error("Failed to modulate selection: " + ex.getReason().toString());
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    @RequestMapping(value = "/appendtoselection", method = RequestMethod.GET)
    public @ResponseBody String appendValues(
            @RequestParam(value = RequestParams.SELECTION_NAME, defaultValue = "") String name,
            @RequestParam(value = RequestParams.SELECTION_VALUES, defaultValue = "") String values
                ) throws IOException {
        logger.info(
                    new StringBuilder()
                            .append("Trying to append to selection \"")
                            .append(name)
                            .append("\"")
        );
        
        ActionResult result; 
        
        try {
            SelectionInfo selectionInfo = appService.appendToSelection(name, values);
            result = new SucceededActionResult<>(selectionInfo);
            
            logger.info(
                    new StringBuilder()
                            .append("Appending to selection \"")
                            .append(name)
                            .append("\": done")
            );
        }catch(ActionException ex) {
            logger.error("Failed to append to selection: " + ex.getReason().toString());
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    @RequestMapping(value = "/selection", method = RequestMethod.GET)
    public @ResponseBody String getSelection(
            @RequestParam(value = RequestParams.SELECTION_NAME, defaultValue = "") String name
                ) throws IOException{
        logger.info(
                    new StringBuilder()
                            .append("Trying to get selection \"")
                            .append(name)
                            .append("\"")
        );
        
        ActionResult result; 
        
        try {
            SelectionData selection = appService.getSelection(name);
            result = new SucceededActionResult<>(selection);
            
            logger.info(
                    new StringBuilder()
                            .append("Getting of selection \"")
                            .append(name)
                            .append("\": done")
            );
        }catch(ActionException ex) {
            logger.error("Failed to get selection: " + ex.getReason().toString());
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    @RequestMapping(value = "/removeselection", method = RequestMethod.GET)
    public @ResponseBody String removeSelection(
            @RequestParam(value = RequestParams.SELECTION_NAME, defaultValue = "") String name
                ) throws IOException{
        logger.info(
                    new StringBuilder()
                            .append("Trying to remove selection \"")
                            .append(name)
                            .append("\"")
        );
        
        ActionResult result; 
        
        try {
            appService.removeSelection(name);
            result = new SucceededActionResult();
            
            logger.info(
                    new StringBuilder()
                            .append("Removing of selection \"")
                            .append(name)
                            .append("\": done")
            );
        }catch(ActionException ex) {
            logger.error("Failed to remove selection: " + ex.getReason().toString());
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    @RequestMapping(value = "/sortselection", method = RequestMethod.GET)
    public @ResponseBody String sortSelection(
            @RequestParam(value = RequestParams.SELECTION_NAME, defaultValue = "") String name
                ) throws IOException{
        logger.info(
                    new StringBuilder()
                            .append("Trying to sort selection \"")
                            .append(name)
                            .append("\"")
        );
        
        ActionResult result; 
        
        try {
            SelectionInfo selectionInfo = appService.sortSelection(name);
            result = new SucceededActionResult(selectionInfo);
            
            logger.info(
                    new StringBuilder()
                            .append("Sorting of selection \"")
                            .append(name)
                            .append("\": done")
            );
        }catch(ActionException ex) {
            logger.error("Failed to sort selection: " + ex.getReason().toString());
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    
    @RequestMapping(value = "/checkselection", method = RequestMethod.GET)
    public @ResponseBody String checkSelectionByTests(
            @RequestParam(value = RequestParams.SELECTION_NAME, defaultValue = "") String name,
            @RequestParam(value = RequestParams.TEST_TYPES, defaultValue = "") String[] types,
            @RequestParam(value = RequestParams.SIGNIFICANCE_LEVEL, defaultValue = "0") int signLevel
                ) throws IOException{
        
        SortedSet<String> typesSet = new TreeSet<>(Arrays.asList(types));
            
        logger.info(
                    new StringBuilder()
                            .append("Trying to check selection \"")
                            .append(name).append("\"")
                            .append(" by tests ").append((typesSet.isEmpty()) ? "" : typesSet)
        );
        
        ActionResult result; 
        
        try {
            if (typesSet.isEmpty()) {
                List<TestType> testTypes = appService.getTestTypes();
                testTypes.stream().forEach((type) -> {
                    typesSet.add(type.getType());
                });
            }
            
            TestResultsMap resultsMap = appService.checkSelectionByTests(name, typesSet, signLevel);
            result = new SucceededActionResult(resultsMap);
            
            logger.info(
                    new StringBuilder()
                            .append("Checking selection \"")
                            .append(name).append("\"")
                            .append(" by tests (").append(typesSet)
                            .append("): done")
            );
        }catch(ActionException ex) {
            logger.error(
                    new StringBuilder().append("Failed to check selection \"")
                            .append(name).append("\"")
                            .append(" by tests (").append(typesSet)
                            .append("): ").append(ex.getReason().toString())
            );
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
    
    @RequestMapping(value = "/createresultsarrayagainstweibull", method = RequestMethod.GET)
    public @ResponseBody String createResultsArrayAgainstWeibull(
            @RequestParam(value = RequestParams.ITERATIONS_COUNT, defaultValue = "0") int iterCount,
            @RequestParam(value = RequestParams.SIGNIFICANCE_LEVEL, defaultValue = "0") int signLevel,
            @RequestParam(value = RequestParams.SELECTION_SIZE, defaultValue = "0") int selSize,
            @RequestParam(value = "results", defaultValue = "") String resultsJson
                ) throws IOException {
        logger.info(
                    new StringBuilder()
                            .append("Trying to create powers array for (")
                            .append(iterCount).append("-").append(signLevel)
                            .append("-").append(selSize).append(")")
        );
        
        ActionResult result; 
        
        try {
            WeibullAlternativeResult powers = appService.createResultsArrayAgainstWeibull(iterCount, signLevel, selSize, resultsJson);
            result = new SucceededActionResult<>(powers);
            
            logger.info(
                    new StringBuilder()
                            .append("Creating powers array for (")
                            .append(iterCount).append("-").append(signLevel)
                            .append("-").append(selSize).append("): done")
            );
        }catch(ActionException ex) {
            logger.error("Failed to create powers array: " + ex.getReason().toString());
            result = new FailedActionResult(ex.getReason());
        }
        
        return gson.toJson(result);
    }
}
