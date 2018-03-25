package com.ifast.batch.config;

import javax.annotation.PostConstruct;

import org.springframework.batch.core.configuration.support.AutomaticJobRegistrar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

public abstract class AutomaticJobRegistrarConfigurationSupport {

	@Autowired
	private AutomaticJobRegistrar automaticJobRegistrar;

	@PostConstruct
	public void initialize() throws Exception {
		// Default order for the AutomaticJobRegistrar is Ordered.LOWEST_PRECEDENCE. Since we want to register
		// listeners after the jobs are registered through the AutomaticJobRegistrar, we need to decrement its
		// order value by one. The creation of the AutomaticJobRegistrar bean is hidden deep in the automatic
		// batch configuration, so we unfortunately have to do it here.
		automaticJobRegistrar.setOrder(Ordered.LOWEST_PRECEDENCE-1);
		addApplicationContextFactories(automaticJobRegistrar);
	}

	/**
	 * Add ApplicationContextFactories to the given job registrar.
	 * 
	 * @param automaticJobRegistrar
	 *            Bean
	 * @throws Exception
	 *             Some error.
	 */
	protected abstract void addApplicationContextFactories(AutomaticJobRegistrar automaticJobRegistrar)
			throws Exception;

}
