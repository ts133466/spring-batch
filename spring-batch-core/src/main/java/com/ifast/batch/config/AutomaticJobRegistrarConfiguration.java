package com.ifast.batch.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.support.AutomaticJobRegistrar;
import org.springframework.batch.core.configuration.support.GenericApplicationContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

@Configuration
@ConditionalOnMissingBean({ AutomaticJobRegistrarConfigurationSupport.class })
public class AutomaticJobRegistrarConfiguration extends AutomaticJobRegistrarConfigurationSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(AutomaticJobRegistrarConfiguration.class);

	@Autowired
	private Environment env;

	/**
	 * @see de.codecentric.batch.configuration.AutomaticJobRegistrarConfigurationSupport#addApplicationContextFactories(org.springframework.batch.core.configuration.support.AutomaticJobRegistrar)
	 */
	@Override
	protected void addApplicationContextFactories(AutomaticJobRegistrar automaticJobRegistrar) throws Exception {
		registerJobsFromXml(automaticJobRegistrar);
		registerJobsFromJavaConfig(automaticJobRegistrar);
	}

	protected void registerJobsFromXml(AutomaticJobRegistrar automaticJobRegistrar) throws IOException {
		// Add all XML-Configurations to the AutomaticJobRegistrar
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		Resource[] xmlConfigurations = resourcePatternResolver.getResources("classpath*:"
				+ env.getProperty("batch.config.path.xml", "/META-INF/spring/batch/jobs") + "/*.xml");
		for (Resource resource : xmlConfigurations) {
			LOGGER.info("Register jobs from {}", resource);
			automaticJobRegistrar.addApplicationContextFactory(new GenericApplicationContextFactory(resource));
		}
	}

	protected void registerJobsFromJavaConfig(AutomaticJobRegistrar automaticJobRegistrar) throws ClassNotFoundException,
	IOException {
		List<Class<?>> classes = findMyTypes("com.ifast.batch.job");
		for (Class<?> clazz : classes) {
			LOGGER.info("Register jobs from {}", clazz);
			automaticJobRegistrar.addApplicationContextFactory(new GenericApplicationContextFactory(clazz));
		}
	}

	private List<Class<?>> findMyTypes(String basePackage) throws IOException, ClassNotFoundException {
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

		List<Class<?>> candidates = new ArrayList<Class<?>>();
		String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + resolveBasePackage(basePackage)
		+ "/" + "**/*.class";
		Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
		for (Resource resource : resources) {
			if (resource.isReadable()) {
				MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
				if (isCandidate(metadataReader)) {
					candidates.add(Class.forName(metadataReader.getClassMetadata().getClassName()));
				}
			}
		}
		return candidates;
	}

	private String resolveBasePackage(String basePackage) {
		return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
	}

	private boolean isCandidate(MetadataReader metadataReader) {
		try {
			Class<?> c = Class.forName(metadataReader.getClassMetadata().getClassName());
			if (c.getAnnotation(Configuration.class) != null) {
				return true;
			}
		} catch (Throwable e) {
		}
		return false;
	}

}
