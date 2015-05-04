/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cdi.scope;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Bean;

import org.hibernate.validator.cdi.ModuleScoped;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

public class ModuleScopeContext implements Context, Serializable {

	private static final Log log = LoggerFactory.make();

	private ModuleScopeContextHolder customScopeContextHolder;

	public ModuleScopeContext() {
		log.info( "Init" );
		this.customScopeContextHolder = ModuleScopeContextHolder.getInstance();
	}

	@Override
	public <T> T get(final Contextual<T> contextual) {
		Bean bean = (Bean) contextual;
		if ( customScopeContextHolder.getBeans().containsKey( bean.getBeanClass() ) ) {
			return (T) customScopeContextHolder.getBean( bean.getBeanClass() ).instance;
		}
		else {
			return null;
		}
	}

	@Override
	public <T> T get(final Contextual<T> contextual, final CreationalContext<T> creationalContext) {
		Bean bean = (Bean) contextual;
		if ( customScopeContextHolder.getBeans().containsKey( bean.getBeanClass() ) ) {
			return (T) customScopeContextHolder.getBean( bean.getBeanClass() ).instance;
		}
		else {
			T t = (T) bean.create( creationalContext );
			ModuleScopeContextHolder.ModuleScopeInstance customInstance = new ModuleScopeContextHolder.ModuleScopeInstance();
			customInstance.bean = bean;
			customInstance.ctx = creationalContext;
			customInstance.instance = t;
			customScopeContextHolder.putBean( customInstance );
			return t;
		}
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return ModuleScoped.class;
	}

	public boolean isActive() {
		return true;
	}

	public void destroy(@Observes KillEvent killEvent) {
		if ( customScopeContextHolder.getBeans().containsKey( killEvent.getBeanType() ) ) {
			customScopeContextHolder.destroyBean( customScopeContextHolder.getBean( killEvent.getBeanType() ) );
		}
	}
}
