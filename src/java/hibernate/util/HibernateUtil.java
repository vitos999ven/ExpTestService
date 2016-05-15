package hibernate.util;

import hibernate.logic.IterationsCount;
import hibernate.logic.Quantile;
import hibernate.logic.Selection;
import hibernate.logic.SignificanceLevel;
import hibernate.logic.TestType;
import hibernate.logic.Power;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;


public class HibernateUtil {
    
    private static SessionFactory sessionFactory = null;
    private static StandardServiceRegistry serviceRegistry = null; 
    
    static{
        try{
            Configuration conf = new Configuration()
                    .addAnnotatedClass(TestType.class)
                    .addAnnotatedClass(IterationsCount.class)
                    .addAnnotatedClass(SignificanceLevel.class)
                    .addAnnotatedClass(Quantile.class)
                    .addAnnotatedClass(Selection.class)
                    .addAnnotatedClass(Power.class)
                    .configure("hibernate.cfg.xml");
            
            serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(conf.getProperties()).build();
            
            sessionFactory = conf.buildSessionFactory(serviceRegistry);
            
        }catch(HibernateException ex){
            System.out.println("Initial SessionFactory failed. " + ex.getMessage());
            StackTraceElement[] stack = ex.getStackTrace();
            for (StackTraceElement stack1 : stack) {
                System.out.println(stack1);
            }
        }
    }
    
    public static SessionFactory getSessionFactory(){
        return sessionFactory;
    }
}
