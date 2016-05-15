package hibernate.DAO.Impl;

import hibernate.util.HibernateUtil;
import java.util.List;
import java.util.Map;
import javax.persistence.Entity;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.exception.ConstraintViolationException;


public final class DAOFunctions {
    
    private static final int databaseTimeout = 5;
    
    private DAOFunctions() {}
    
    public static <T> boolean add(
            T element) {
        Session session = null;
        
        try{
            session = HibernateUtil.getSessionFactory().openSession();
            return add(element, session, true);
        }finally{
            if((session != null) && (session.isOpen())) {
                session.close();
            }
        }
    }
    
    public static <T> boolean add(
            T element, 
            Session session, 
            boolean useTransaction) {
        if (element == null || session == null) {
            return false;
        }
        
        try{
            transact(element, session, useTransaction, Operations.ADD);
        }catch(ConstraintViolationException ex) {
            return false;
        }
        return true;
    }
    
    public static <T> boolean addAll(
            List<T> elements) {
        Session session = null;
        
        try{
            session = HibernateUtil.getSessionFactory().openSession();
            return addAll(elements, session, true);
        }finally{
            if((session != null) && (session.isOpen())) {
                session.close();
            }
        }
    }
    
    public static <T> boolean addAll(
            List<T> elements, 
            Session session, 
            boolean useTransaction) {
        if (elements == null || session == null) {
            return false;
        }
        
        return multipleTransact(elements, session, useTransaction, Operations.ADD);
    }
    
    public static <T> boolean update(
            T element) {
        Session session = null;
        
        try{
            session = HibernateUtil.getSessionFactory().openSession();
            return update(element, session, true);
        }finally{
            if((session != null) && (session.isOpen())) {
                session.close();
            }
        }
    }
    
    public static <T> boolean update(
            T element, 
            Session session, 
            boolean useTransaction) {
        if (element == null || session == null) {
            return false;
        }
        
        return transact(element, session, useTransaction, Operations.UPDATE);
    }
    
    public static <T> boolean updateAll(
            List<T> elements) {
        Session session = null;
        
        try{
            session = HibernateUtil.getSessionFactory().openSession();
            return updateAll(elements, session, true);
        }finally{
            if((session != null) && (session.isOpen())) {
                session.close();
            }
        }
    }
    
    public static <T> boolean updateAll(
            List<T> elements, 
            Session session, 
            boolean useTransaction) {
        if (elements == null || session == null) {
            return false;
        }
        
        return multipleTransact(elements, session, useTransaction, Operations.UPDATE);
    }
    
    public static <T> boolean remove(
            T element,  
            Preparer<T> preparer) {
        Session session = null;
        
        try{
            session = HibernateUtil.getSessionFactory().openSession();
            return remove(element, session, true, preparer);
        }finally{
            if((session != null) && (session.isOpen())) {
                session.close();
            }
        }
    }
     
    public static <T> boolean remove(
            T element, 
            Session session, 
            boolean useTransaction,
            Preparer<T> preparer) {
        if (element == null || session == null) {
            return false;
        }
        
        return transact(element, session, useTransaction, Operations.REMOVE, preparer);
    }
    
    public static <T> boolean removeAll(
            List<T> elements, 
            Preparer<T> preparer) {
        Session session = null;
        
        try{
            session = HibernateUtil.getSessionFactory().openSession();
            return removeAll(elements, session, true, preparer);
        }finally{
            if((session != null) && (session.isOpen())) {
                session.close();
            }
        }
    }
    
    public static <T> boolean removeAll(
            List<T> elements, 
            Session session, 
            boolean useTransaction,
            Preparer<T> preparer) {
        if (elements == null || session == null) {
            return false;
        }
        
        return multipleTransact(elements, session, useTransaction, Operations.REMOVE, preparer);
    }
    
    public static <T> T get(
            Class<T> elementClass,
            List<Criterion> criterions) {
        Session session = null;
        T element = null;
        
        try{
            session = HibernateUtil.getSessionFactory().openSession();
            element = DAOFunctions.get(
                    elementClass, 
                    session, 
                    criterions);
        }finally{
            if((session != null) && (session.isOpen())) {
                session.close();
            }
        }
        
        return element;
    }
    
    public static <T> T get(
            Class<T> elementClass,
            Session session,
            List<Criterion> criterions) {
        List<T> list = criteria(elementClass, session, criterions);
        
        if (list == null || list.isEmpty()) {
            return null;
        }
        
        return list.get(0);
    }
    
    public static <T> List<T> criteria(
            Class<T> elementClass,
            List<Criterion> criterions) {
        Session session = null;
        List<T> elements = null;
        
        try{
            session = HibernateUtil.getSessionFactory().openSession();
            elements = DAOFunctions.criteria(
                    elementClass, 
                    session, 
                    criterions);
        }finally{
            if((session != null) && (session.isOpen())) {
                session.close();
            }
        }
        
        return elements;
    }
    
    public static <T> List<T> criteria(
            Class<T> elementClass,
            Session session,
            List<Criterion> criterions) {
        if (elementClass == null || session == null) {
            return null;
        }
        
        Criteria criteria = session.createCriteria(elementClass);
        
        if (criterions != null) {
            criterions.stream().forEach((criterion) -> {
                criteria.add(criterion);
            });
        }
        
        return criteria.list();
    }
    
    private static <T> boolean transact(
            T element, 
            Session session, 
            boolean useTransaction, 
            Operations operation) {
        return transact(element, session, useTransaction, operation, null);
    }
    
    private static <T> boolean transact(
            T element, 
            Session session, 
            boolean useTransaction, 
            Operations operation,
            Preparer<T> preparer) {
        Transaction transaction = null;
        
        try{
            if (useTransaction) {
                transaction = session.beginTransaction();
                transaction.setTimeout(databaseTimeout);
            }
            
            if (preparer != null) {
                preparer.prepare(element, session);
            }
            
            doOperation(element, session, operation);
            
            if (useTransaction && transaction != null) {
                transaction.commit();
            }
        }catch(HibernateException e){
            if (useTransaction && transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
        
        return true;
    } 
    
    private static <T> boolean multipleTransact(
            List<T> elements, 
            Session session, 
            boolean useTransaction, 
            Operations operation){
        return multipleTransact(elements, session ,useTransaction, operation, null);
    }
    
    private static <T> boolean multipleTransact(
            List<T> elements, 
            Session session, 
            boolean useTransaction, 
            Operations operation,
            Preparer<T> preparer) {
        Transaction transaction = null;
        
        try{
            if (useTransaction) {
                transaction = session.beginTransaction();
                transaction.setTimeout(databaseTimeout);
            }
            
            elements.stream().forEach((element) -> {
                if (preparer != null) {
                    preparer.prepare(element, session);
                }
                
                doOperation(element, session, operation);
            });
            
            if (useTransaction && transaction != null) {
                transaction.commit();
            }
        }catch(HibernateException e){
            if (useTransaction && transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
        
        return true;
    } 
    
    private static <T> void doOperation(
            T element, 
            Session session,  
            Operations operation) {
        switch(operation) {
            case ADD:
                session.save(element);
                break;
            case UPDATE:
                session.update(element);
                break;
            case REMOVE:
                session.delete(element);
                break;
        }
    }
    
    public static <T> T queryElement(
            Class<T> resultClass,
            String queryString,
            Map<String, Object> params,
            boolean useTransaction) {
        List<T> resultList = queryList(resultClass, queryString, params, useTransaction);
        
        if (resultList == null || resultList.isEmpty()) {
            return null;
        }
        
        return resultList.get(0);
    }
    
    public static <T> List<T> queryList(
            Class<T> resultClass,
            String queryString,
            Map<String, Object> params,
            boolean useTransaction) {
        if (resultClass == null || queryString == null) {
            return null;
        }
        
        Session session = null;
        Transaction transaction = null;
        List<T> result = null;
        
        try{
            session = HibernateUtil.getSessionFactory().openSession();
            if (useTransaction) {
                transaction = session.beginTransaction();
                transaction.setTimeout(databaseTimeout);
            }
            
            Query query = query(
                    resultClass,
                    session,
                    queryString,
                    params);
            
            if (query != null) {
                result = (List<T>) query.list();
            }
            
             if (useTransaction && transaction != null) {
                transaction.commit();
            }
        }catch(HibernateException e){
            if (useTransaction && transaction != null) {
                transaction.rollback();
            }
            throw e;
        }finally{
            if((session != null) && (session.isOpen())) {
                session.close();
            }
        }
        
        return result;
    }
    
    private static <T> Query query(
            Class<T> resultClass,
            Session session,
            String queryString,
            Map<String, Object> params) {
        if (session == null){
            return null;
        }
        
        Query query;
        
        if (resultClass.isAnnotationPresent(Entity.class)) {
            query = session.createSQLQuery(queryString).addEntity(resultClass);
        }else{
            query = session.createSQLQuery(queryString);
        }
        
        if (params != null) {
            params.entrySet().stream().forEach((entry) -> {
                query.setParameter(entry.getKey(), entry.getValue());
            });
        }
        
        return query;
    }
    
    private static enum Operations {
        ADD,
        UPDATE,
        REMOVE
    }
    
    public static interface Preparer<T> {
        
        public void prepare(T element, Session session);
        
    }
}
