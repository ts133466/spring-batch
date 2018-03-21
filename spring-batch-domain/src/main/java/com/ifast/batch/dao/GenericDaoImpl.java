package com.ifast.batch.dao;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.method.P;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ifast.batch.criteria.CriteriaSet;
import com.ifast.batch.criteria.Operator;
import com.ifast.batch.criteria.SearchCriteria;
import com.ifast.batch.util.ClassUtil;

@Transactional(propagation = Propagation.MANDATORY)
public abstract class GenericDaoImpl<T, ID extends Serializable> extends GenericXmlDaoImpl implements GenericDao<T, ID> {

	private final Logger logger = LoggerFactory.getLogger(GenericDaoImpl.class);

	private static final int MAX_IN_CRI_PARAM = 1000;

	@Inject
	private SessionFactory sessionFactory;

	protected final Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	@Override
	protected Class<?> getSqlXmlClass() {
		return null;
	}

	private final Class<P> bean;

	@SuppressWarnings("unchecked")
	protected GenericDaoImpl() {
		bean = (Class<P>) ClassUtil.getTypeArguments(GenericDaoImpl.class, this.getClass()).get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> findAll() {
		final Session s = this.getSession();
		final Criteria c = s.createCriteria(bean);
		return c.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> findBySearchCriteria(final SearchCriteria sc) {
		if(sc.getProjections() != null && !sc.getProjections().isEmpty()) {
			return findBySearchCriteriaWithProjection(sc);
		}
		Criteria criteria = getCriteria(sc);

//		System.out.println(criteria.toString());

		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> findBySearchCriteriaWithProjection(final SearchCriteria sc)  {

		List<T> results = new ArrayList<>();
		for(Object resuls : getCriteria(sc).list()) {
			String cols[] = new String[sc.getProjections().size()];
			Class<T> type = (Class<T>) ClassUtil.getTypeArguments(GenericDaoImpl.class, this.getClass()).get(0);
			try {
				results.add(initializeBean(type.newInstance(), sc.getProjections().toArray(cols), resuls));
			} catch (Exception e) {
				logger.error("error in findBySearchCriteriaWithProjection GenericDaoImpl", e);
			}
		}

		return results;
	}

	private T initializeBean(final T t, final String[] cols, final Object result) {
		int i = 0;
		for(String col : cols) {
			try {
				Method[] methods = t.getClass().getMethods();
				for(Method method : methods) {
					if(method.getName().equals("set".concat(col.substring(0, 1).toUpperCase() + col.substring(1)))) {
						if (result instanceof Object[]) {
							method.invoke(t, ((Object[]) result)[i]);
						} else {
							method.invoke(t , result);
						}
						i++;
					}
				}
			} catch (Exception e) {
				logger.error("error in initializeBean GenericDaoImpl", e);
			}
		}
		return t;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get(final ID id) {
		if (id == null) {
			return null;
		}
		return (T) getSession().get(bean, id);
	}

	@Override
	public Serializable add(final T entity) {
		return getSession().save(entity);
	}

	@Override
	public void delete(final T entity) {
		getSession().delete(entity);
		getSession().flush();
	}

	@Override
	public void delete(final ID id) {
		final Session s = this.getSession();
		s.delete(s.load(bean, id));
		s.flush();
	}

	@Override
	public void update(final T entity) {
		getSession().merge(entity);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getOne() {
		final Session session = getSession();
		final Criteria c = session.createCriteria(bean);
		c.setMaxResults(1);
		return (T) c.uniqueResult();
	}

	protected int getTotalRows(final Criteria criteria) {
		criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));
		return ((Long) criteria.uniqueResult()).intValue();
	}

	protected int getTotalRows() {
		final Session s = this.getSession();
		return getTotalRows(s.createCriteria(bean));
	}

	@Override
	public void evict(final T entity) {
		getSession().evict(entity);
	}

	@Override
	public void addOrUpdate(final T entity) {
	    getSession().saveOrUpdate(entity);
	}

	private <E> Criterion getInCriteria(final String propertyName, final Collection<E> c, final boolean notIn) {
		if (c == null) {
			return Restrictions.sqlRestriction("(1=1)");
		}

		if (c.size() <= MAX_IN_CRI_PARAM) {
			return notIn ? Restrictions.not(Restrictions.in(propertyName, c)) : Restrictions.in(propertyName, c);
		} else {
			Set<Criterion> criSet = new HashSet<Criterion>();
			Set<E> tempParamSet = new HashSet<E>();
			int i = 0;

			for (Iterator<E> iter = c.iterator(); iter.hasNext(); i++) {
				tempParamSet.add(iter.next());

				if ((i + 1) % MAX_IN_CRI_PARAM == 0 || !iter.hasNext()) {
					criSet.add(notIn ? Restrictions.not(Restrictions.in(propertyName, tempParamSet)) : Restrictions.in(propertyName, tempParamSet));
					tempParamSet = new HashSet<E>();
				}
			}

			return Restrictions.or(criSet.toArray(new Criterion[criSet.size()]));
		}
	}

	protected <E> Criterion getInCriteria(final String propertyName, final Collection<E> c) {
		return getInCriteria(propertyName, c, false);
	}

	protected <E> Criterion getNotInCriteria(final String propertyName, final Collection<E> c) {
		return getInCriteria(propertyName, c, true);
	}

	protected <E> Criterion getInCriteria(final String propertyName, final E[] arr) {
		return getInCriteria(propertyName, Arrays.asList(arr));
	}

	protected <E> Criterion getNotInCriteria(final String propertyName, final E[] arr) {
		return getNotInCriteria(propertyName, Arrays.asList(arr));
	}

	protected Criteria getCriteria(final SearchCriteria sc) {
		final Session s = this.getSession();
		final Criteria c = s.createCriteria(bean);

		List<CriteriaSet> aliasCriteriaSets = sc.getAliasCriterias();

		for(CriteriaSet aliasCriteriaSet : aliasCriteriaSets) {
		    createAliasCriteria(aliasCriteriaSet, c);
		}

		List<CriteriaSet> andCriteriaSets = sc.getAndCriterias();

		Conjunction conjunction = null;
		for (CriteriaSet set : andCriteriaSets) {
			if (conjunction == null) {
				conjunction = Restrictions.conjunction();
			}
			createCriteria(conjunction, set);
		}
		List<CriteriaSet> orCriteriaSets = sc.getOrCriterias();
		if (!orCriteriaSets.isEmpty()) {
			if (conjunction == null && orCriteriaSets.size() == 1) {
				c.add(getCriterion(orCriteriaSets.get(0).getOperator(), orCriteriaSets.get(0).getCol(),
						orCriteriaSets.get(0).getVal()));
			} else {
				Disjunction discon = Restrictions.disjunction();
				for (CriteriaSet set : orCriteriaSets) {
					createCriteria(discon, set);
				}
				c.add(Restrictions.or(conjunction, discon));
			}
		} else {
			if (conjunction == null) {
				conjunction = Restrictions.conjunction();
			}
			c.add(conjunction);
		}

		if (sc.getOrderByMap() != null) {
			for (String orderBy : sc.getOrderByMap().keySet()) {
				if (sc.getOrderByMap().get(orderBy)) {
					c.addOrder(Order.desc(orderBy));
				} else {
					c.addOrder(Order.asc(orderBy));
				}
			}
		}

		if (!sc.getDistincts().isEmpty()) {
			ProjectionList projectionList = Projections.projectionList();
			for (String distinction : sc.getDistincts()) {
				projectionList.add(Projections.property(distinction), distinction);
			}
			c.setProjection(projectionList);
		}

		if (!sc.getProjections().isEmpty()) {
			ProjectionList projectionList = Projections.projectionList();
			for (String projection : sc.getProjections()) {
				projectionList.add(Projections.property(projection), projection);
			}
			c.setProjection(projectionList);
		}

		if (sc.getMaxResults() > 0) {
			c.setMaxResults(sc.getMaxResults());
		}

		if (sc.getFirstResult() > 0) {
			c.setFirstResult(sc.getFirstResult());
		}

		return c;
	}

    private void createAliasCriteria(final CriteriaSet set, final Criteria c) {
        if (StringUtils.isNotBlank(set.getAlias())) {
            c.createAlias(set.getAlias(), set.getAlias());
        }
    }

	private void createCriteria(final Junction c, final CriteriaSet set) {
		if (set.getOperator() != null) {
			c.add(getCriterion(set.getOperator(), set.getCol(), set.getVal()));
		} else {
			if (set.getSc() != null) {
				createJunction(c, set);
			}
		}
	}

	private Criterion getCriterion(final Operator operator, final String col, final Object val) {
		switch (operator) {
		case EQ:
			return Restrictions.eq(col, val);
		case NE:
			return Restrictions.ne(col, val);
		case GT:
			return Restrictions.gt(col, val);
		case GE:
			return Restrictions.ge(col, val);
		case LE:
			return Restrictions.le(col, val);
		case LT:
			return Restrictions.lt(col, val);
		case IN:
			return Restrictions.in(col, (Object[]) val);
		case NOTIN:
			return Restrictions.not(Restrictions.in(col, (Object[]) val));
		case LIKE:
			return Restrictions.like(col, (String) val, MatchMode.ANYWHERE);
		case NOTNULL:
			return Restrictions.isNotNull(col);
		case NULL:
			return Restrictions.isNull(col);
		default:
			return null;
		}
	}

	private void createJunction(final Junction junction, final CriteriaSet set) {
		Conjunction conjunction = null;

		for (CriteriaSet s : set.getSc().getAndCriterias()) {
			if (conjunction == null) {
				conjunction = Restrictions.conjunction();
			}
			createCriteria(conjunction, s);
		}
		List<CriteriaSet> orCriteriaSets = set.getSc().getOrCriterias();
		if (!orCriteriaSets.isEmpty()) {
			if (conjunction == null && orCriteriaSets.size() == 1) {
				junction.add(getCriterion(orCriteriaSets.get(0).getOperator(), orCriteriaSets.get(0).getCol(),
						orCriteriaSets.get(0).getVal()));
			} else {
				Disjunction discon = Restrictions.disjunction();
				for (CriteriaSet s : orCriteriaSets) {
					createCriteria(discon, s);
				}
				junction.add(Restrictions.or(conjunction, discon));
			}
		} else {
			junction.add(conjunction);
		}
	}

}
