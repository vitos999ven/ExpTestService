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


public class ChartArray {

    public Map<BigDecimal, Double> array;
    
    public ChartArray(Map<BigDecimal, Double> array) {
        this.array = array;
    }
    
    public static class Serializer implements JsonSerializer<ChartArray> {

        @Override
        public JsonElement serialize(ChartArray array, Type type, JsonSerializationContext jsc) {
            return GsonHolder.serializeMapAsArray(array.array);
        }
        
    }
    
}
