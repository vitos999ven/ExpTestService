package controllers;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import entities.ChartArray;
import entities.WeibullAlternativeResult;
import entities.exceptions.ActionException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Type;
import java.util.SortedMap;
import java.util.TreeMap;



public final class GsonHolder {
    
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(WeibullAlternativeResult.class, new WeibullAlternativeResult.Serializer())
            .registerTypeAdapter(ChartArray.class, new ChartArray.Serializer())
            .setPrettyPrinting()
            .create();
    
    private static final Type selectionListType = new TypeToken<ArrayList<BigDecimal> >() {}.getType();
    private static final Type testResultsMapType = new TypeToken<TreeMap<String, Boolean> >() {}.getType();
    
    
    public static List<BigDecimal> parseSelectionValues(String values) throws ActionException {
        return gson.fromJson(values, selectionListType);
    }
    
    public static SortedMap<String, Boolean>  parseTestResults(String results) throws ActionException {
        return gson.fromJson(results, testResultsMapType);
    }
    
    public static <K extends Number, V extends Number> JsonArray serializeMapAsArray(Map<K, V> map) {
        JsonArray resultsArray = new JsonArray();
        if (map != null) {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                JsonArray entryArray = new JsonArray();
                entryArray.add(new JsonPrimitive(entry.getKey()));
                entryArray.add(new JsonPrimitive(entry.getValue()));
                resultsArray.add(entryArray);
            }
        }
        
        return resultsArray;
    }
    
    public static Gson getGson() {
        return gson;
    }
}
