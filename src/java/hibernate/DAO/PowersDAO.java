package hibernate.DAO;

import entities.alternatives.SelectionParams;
import entities.alternatives.SelectionsEnum;
import hibernate.logic.Power;
import hibernate.logic.Quantile;
import hibernate.logic.TestType;
import java.util.List;
import org.hibernate.Session;


public interface PowersDAO {
    public boolean addPower(Power power);
    public boolean updatePower(Power power);
    public Power getPower(Long id);
    public Power getPower(Quantile quantile, SelectionParams params);
    public boolean removePower(Long id);
    public boolean removePower(Quantile quantile, SelectionParams params);
    public List<Power> getPowers(Quantile quantile);
    public List<Power> getPowers(Quantile quantile, SelectionsEnum alternative);
    public List<Power> getAllPowers();
    public void removePowers(Quantile quantile);
    public void removePowers(Quantile quantile, Session session);
    public void removeAllPowers();
}
