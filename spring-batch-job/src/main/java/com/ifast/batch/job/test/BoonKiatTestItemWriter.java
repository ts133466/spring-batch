package com.ifast.batch.job.test;

import java.util.List;

import javax.inject.Inject;

import org.springframework.batch.item.ItemWriter;

import com.ifast.batch.dao.test.BoonKiatTestDao;
import com.ifast.batch.entity.test.BoonKiatTestBean;

public class BoonKiatTestItemWriter implements ItemWriter<BoonKiatTestBean> {

	@Inject
	BoonKiatTestDao boonKiatTestDao;
	
	@Override
	public void write(List<? extends BoonKiatTestBean> items) throws Exception {
		for(BoonKiatTestBean boonKiatTestBean : items) {
			boonKiatTestDao.add(boonKiatTestBean);
		}
	}

}
