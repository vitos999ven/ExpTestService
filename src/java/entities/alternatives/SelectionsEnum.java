package entities.alternatives;

import controllers.GsonHolder;


public enum SelectionsEnum {
    
    EXP("exp", ExpParams.class),
    WEIBULL("weibull", WeibullParams.class);

    private final String type;
    private final Class<? extends SelectionParams> selectionClass;

    SelectionsEnum(String type, Class<? extends SelectionParams> selectionClass) {
        this.type = type;
        this.selectionClass = selectionClass;
    }
    
    @Override
    public String toString() {
        return type;
    }
    
    public SelectionParams parseSelectionParams(String json) {
        if (selectionClass == null) return null;
        return GsonHolder.getGson().fromJson(json, selectionClass);
    }
    
}
