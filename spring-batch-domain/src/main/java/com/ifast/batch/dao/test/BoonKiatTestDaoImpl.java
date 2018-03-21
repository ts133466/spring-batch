package com.ifast.batch.dao.test;

import org.springframework.stereotype.Repository;

import com.ifast.batch.dao.GenericDaoImpl;
import com.ifast.batch.entity.test.BoonKiatTestBean;

@Repository
public class BoonKiatTestDaoImpl extends GenericDaoImpl<BoonKiatTestBean, Long> implements BoonKiatTestDao {

}