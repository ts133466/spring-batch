package com.ifast.batch.dao;

import java.io.Serializable;
import java.util.List;

import com.ifast.batch.criteria.SearchCriteria;

public interface GenericDao<T, ID extends Serializable> {

    Serializable add(final T entity);

    void update(final T entity);

    void delete(final T entity);

    void delete(final ID id);

    T get(final ID id);

    List<T> findAll();
    
    List<T> findBySearchCriteria(SearchCriteria sc);
    
	List<T> findBySearchCriteriaWithProjection(SearchCriteria sc);

    T getOne();

    public void evict(final T entity);

    void addOrUpdate(final T entity);
}
