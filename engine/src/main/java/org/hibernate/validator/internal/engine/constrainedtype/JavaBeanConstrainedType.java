/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constrainedtype;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.engine.HibernateConstrainedType;
import org.hibernate.validator.internal.util.classhierarchy.ClassHierarchyHelper;
import org.hibernate.validator.metadata.BeanMetaDataClassNormalizer;

/**
 * An implementation of {@link HibernateConstrainedType} for regular JavaBeans.
 * Wrapps a {@link Class} object to adapt it to the needs of HV usages.
 *
 * @author Marko Bekhta
 */
public class JavaBeanConstrainedType<T> implements HibernateConstrainedType<T> {

	private final Class<T> clazz;

	public JavaBeanConstrainedType(Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	public Class<T> getActuallClass() {
		return clazz;
	}

	@Override
	public List<HibernateConstrainedType<? super T>> getHierarchy() {
		List<Class<? super T>> hierarchy = ClassHierarchyHelper.getHierarchy( clazz );
		List<HibernateConstrainedType<? super T>> result = new ArrayList<>( hierarchy.size() );
		for ( Class<? super T> clazzz : hierarchy ) {
			result.add( new JavaBeanConstrainedType<>( clazzz ) );
		}
		return result;
	}

	@Override
	public boolean isInterface() {
		return clazz.isInterface();
	}

	public HibernateConstrainedType<T> normalize(BeanMetaDataClassNormalizer normalizer) {
		return new NormalizedJavaBeanConstrainedType<>( normalizer, clazz );
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || !( o instanceof JavaBeanConstrainedType ) ) {
			return false;
		}

		JavaBeanConstrainedType<?> that = (JavaBeanConstrainedType<?>) o;

		if ( !clazz.equals( that.clazz ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return clazz.hashCode();
	}

	@Override
	public String toString() {
		return "JavaBeanConstrainedType{" +
				"clazz=" + clazz +
				'}';
	}
}
