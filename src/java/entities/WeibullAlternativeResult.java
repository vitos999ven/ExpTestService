package entities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import controllers.GsonHolder;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

public class WeibullAlternativeResult {

    public Map<BigDecimal, Double> result = new TreeMap<>();
    public String comment = "";
    public double total = 0.0;
    
    public static class Serializer implements JsonSerializer<WeibullAlternativeResult> {

        @Override
        public JsonElement serialize(WeibullAlternativeResult result, Type type, JsonSerializationContext jsc) {
            JsonObject resultObj = new JsonObject();
            
            JsonArray resultsArray = GsonHolder.serializeMapAsArray(result.result);
            resultObj.add("result", resultsArray);
            if (!result.comment.isEmpty()) {
                resultObj.add("comment", new JsonPrimitive(result.comment));
            }
            resultObj.add("total", new JsonPrimitive(result.total));
            
            return resultObj;
        }
        
    }
    
}
