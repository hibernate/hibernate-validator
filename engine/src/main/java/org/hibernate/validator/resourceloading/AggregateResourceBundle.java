/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.resourceloading;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * A {@link ResourceBundle} whose content is aggregated from multiple source bundles.
 *
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
class AggregateResourceBundle extends ResourceBundle {

	@Immutable
	private final Map<String, Object> contents;

	AggregateResourceBundle(List<ResourceBundle> bundles) {
		if ( bundles == null || bundles.isEmpty() ) {
			this.contents = Collections.emptyMap();
			return;
		}

		Map<String, Object> contents = new HashMap<>();
		for ( ResourceBundle bundle : bundles ) {
			Enumeration<String> keys = bundle.getKeys();
			while ( keys.hasMoreElements() ) {
				String key = keys.nextElement();
				contents.putIfAbsent( key, bundle.getObject( key ) );
			}
		}
		this.contents = CollectionHelper.toImmutableMap( contents );
	}

	@Override
	protected Object handleGetObject(String key) {
		return contents.get( key );
	}

	@Override
	protected Set<String> handleKeySet() {
		return contents.keySet();
	}

	@Override
	public Enumeration<String> getKeys() {
		if ( parent == null ) {
			return Collections.enumeration( contents.keySet() );
		}

		Set<String> keySet = newHashSet( contents.keySet() );
		keySet.addAll( Collections.list( parent.getKeys() ) );

		return Collections.enumeration( keySet );
	}
}
