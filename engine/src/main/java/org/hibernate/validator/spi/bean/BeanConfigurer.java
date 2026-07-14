/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.bean;

import org.hibernate.validator.Incubating;

/**
 * An object responsible for defining beans that can then be resolved by Hibernate Validator.
 * <p>
 * Bean configurers can be discovered via the Java {@link java.util.ServiceLoader} mechanism:
 * create a file named {@code org.hibernate.validator.spi.bean.BeanConfigurer}
 * in the {@code META-INF/services} directory of your JAR,
 * and set the content of this file to the fully-qualified name of your {@link BeanConfigurer} implementation.
 * <p>
 * They can also be registered programmatically via
 * {@code BaseHibernateValidatorConfiguration.addBeanConfigurer(BeanConfigurer)}.
 *
 * @see <a href="https://github.com/hibernate/hibernate-search/blob/main/engine/src/main/java/org/hibernate/search/engine/environment/bean/spi/BeanConfigurer.java">
 *      Original concept from Hibernate Search</a>
 * @since 9.2.0
 */
@Incubating
public interface BeanConfigurer {

	/**
	 * Configure beans as necessary using the given {@code context}.
	 * @param context A context exposing methods to configure beans.
	 */
	void configure(BeanConfigurationContext context);
}
