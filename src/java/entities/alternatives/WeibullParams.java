package entities.alternatives;

import controllers.GsonHolder;
import java.math.BigDecimal;
import java.util.Objects;


public class WeibullParams implements SelectionParams {

    private BigDecimal param;
    
    public WeibullParams(BigDecimal weibulParam) {
        this.param = weibulParam;
    }
    
    @Override
    public SelectionsEnum getSelectionType() {
        return SelectionsEnum.WEIBULL;
    }

    @Override
    public String toJson() {
        return GsonHolder.getGson().toJson(this);
    }

    public BigDecimal getWeibullParam() {
        return param;
    }
    
    public void setWeibullParam(BigDecimal weibullParam) {
        this.param = weibullParam;
    }
    
    @Override
    public boolean equals(Object obj){
        if (obj == null || !(obj instanceof WeibullParams)) return false;
        
        WeibullParams other = (WeibullParams) obj;
        
        return (Objects.equals(param, other.param));
    }

    @Override
    public int compareTo(SelectionParams other) {
        if (other == null) return 1;
        
        int compareResult = SelectionsEnum.WEIBULL.compareTo(other.getSelectionType());
        if (compareResult != 0) return compareResult;
        
        WeibullParams o = (WeibullParams) other;
        
        return (param == null) ? -1 : param.compareTo(o.param);
    }

}
