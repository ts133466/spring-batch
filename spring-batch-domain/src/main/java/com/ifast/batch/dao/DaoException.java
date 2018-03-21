package com.ifast.batch.dao;

public class DaoException extends RuntimeException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1996171730911183549L;

	public DaoException(Throwable e) {
		super(e);
	}
	
	public DaoException(String error, Throwable e) {
		super(error, e);
	}
	
}
