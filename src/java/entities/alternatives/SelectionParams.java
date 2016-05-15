package entities.alternatives;


public interface SelectionParams extends Comparable<SelectionParams> {
    public SelectionsEnum getSelectionType();
    public String toJson();
}
