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
 * Uses {@link org.hibernate.validator.metadata.BeanMetaDataClassNormalizer} before
 * creating a {@link HibernateConstrainedType}.
 *
 * @author Marko Bekhta
 */
public class NormalizedJavaBeanConstrainedType<T> extends JavaBeanConstrainedType<T> {
	private final BeanMetaDataClassNormalizer normalizer;

	public NormalizedJavaBeanConstrainedType(BeanMetaDataClassNormalizer normalizer, Class<T> clazz) {
		super( (Class<T>) normalizer.normalize( clazz ) );
		this.normalizer = normalizer;
	}

	@Override
	public List<HibernateConstrainedType<? super T>> getHierarchy() {
		List<Class<? super T>> hierarchy = ClassHierarchyHelper.getHierarchy( getActuallClass() );
		List<HibernateConstrainedType<? super T>> result = new ArrayList<>( hierarchy.size() );
		//TODO : question - do we actually need to normalize anything here. Wouldn't it be enough to normalize only when
		// the metadata is retrieved ?
		for ( Class<? super T> clazzz : hierarchy ) {
			result.add( (NormalizedJavaBeanConstrainedType<? super T>) new NormalizedJavaBeanConstrainedType<>( normalizer, normalizer.normalize( clazzz ) ) );
		}
		return result;
	}
}
