package entities.alternatives;

import controllers.GsonHolder;


public class ExpParams implements SelectionParams {

    private static class InstanceHolder {
        public static ExpParams params = new ExpParams();
    }
    
    @Override
    public SelectionsEnum getSelectionType() {
        return SelectionsEnum.EXP;
    }

    @Override
    public String toJson() {
        return "{}";
    }

    @Override
    public int compareTo(SelectionParams other) {
        if (other == null) return 1;
        
        return SelectionsEnum.EXP.compareTo(other.getSelectionType());
    }
    
    public static ExpParams getInstance() {
        return InstanceHolder.params;
    }

}
