/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cdi.scope;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

/**
 * @author Hardy Ferentschik
 */
public class ModuleScopeContextHolder implements Serializable {

	private static ModuleScopeContextHolder INSTANCE;

	private Map<Class, ModuleScopeInstance> beans;//we will have only one instance of a type so the key is a class

	private ModuleScopeContextHolder() {
		beans = Collections.synchronizedMap( new HashMap<Class, ModuleScopeInstance>() );
	}

	public static synchronized ModuleScopeContextHolder getInstance() {
		if ( INSTANCE == null ) {
			INSTANCE = new ModuleScopeContextHolder();
		}
		return INSTANCE;
	}

	public Map<Class, ModuleScopeInstance> getBeans() {
		return beans;
	}

	public ModuleScopeInstance getBean(Class type) {
		return getBeans().get( type );
	}

	public void putBean(ModuleScopeInstance customInstance) {
		getBeans().put( customInstance.bean.getBeanClass(), customInstance );
	}

	void destroyBean(ModuleScopeInstance moduleScopeInstance) {
		getBeans().remove( moduleScopeInstance.bean.getBeanClass() );
		moduleScopeInstance.bean.destroy( moduleScopeInstance.instance, moduleScopeInstance.ctx );
	}

	/**
	 * wrap necessary properties so we can destroy the bean later:
	 */
	public static class ModuleScopeInstance<T> {

		Bean<T> bean;
		CreationalContext<T> ctx;
		T instance;
	}
}
