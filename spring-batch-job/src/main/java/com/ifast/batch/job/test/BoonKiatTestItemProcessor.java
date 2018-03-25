package com.ifast.batch.job.test;

import java.util.Date;

import org.springframework.batch.item.ItemProcessor;

import com.ifast.batch.entity.test.BoonKiatTestBean;

public class BoonKiatTestItemProcessor implements ItemProcessor<BoonKiatTestBean, BoonKiatTestBean> {

	@Override
	public BoonKiatTestBean process(BoonKiatTestBean item) throws Exception {
		item.setCreateDate(new Date());
		System.out.println(item.getBidPrice());
		return item;
	}

}
