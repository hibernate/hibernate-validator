/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.tracking;

import java.lang.reflect.Executable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.metadata.PredefinedScopeBeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ContainerCascadingMetaData;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.util.CollectionHelper;

public class PredefinedScopeProcessedBeansTrackingStrategy implements ProcessedBeansTrackingStrategy {

	private final Map<Class<?>, Boolean> trackingEnabledForBeans;

	private final Map<Executable, Boolean> trackingEnabledForReturnValues;

	private final Map<Executable, Boolean> trackingEnabledForParameters;

	public PredefinedScopeProcessedBeansTrackingStrategy(PredefinedScopeBeanMetaDataManager beanMetaDataManager) {
		// TODO: build the maps from the information inside the beanMetaDataManager
		// There is a good chance we will need a structure with the whole hierarchy of constraint classes.
		// That's something we could add to PredefinedScopeBeanMetaDataManager, as we are already doing similar things
		// there (see the ClassHierarchyHelper.getHierarchy call).
		// In the predefined scope case, we will have the whole hierarchy of constrained classes passed to
		// PredefinedScopeBeanMetaDataManager.

		this.trackingEnabledForBeans = CollectionHelper.toImmutableMap(
				new TrackingEnabledStrategyBuilder( beanMetaDataManager ).build()
		);
		this.trackingEnabledForReturnValues = CollectionHelper.toImmutableMap( new HashMap<>() );
		this.trackingEnabledForParameters = CollectionHelper.toImmutableMap( new HashMap<>() );
	}

	private static class TrackingEnabledStrategyBuilder {
		private final PredefinedScopeBeanMetaDataManager beanMetaDataManager;
		private final Map<Class<?>, Boolean> classToBeanTrackingEnabled;

		TrackingEnabledStrategyBuilder(PredefinedScopeBeanMetaDataManager beanMetaDataManager) {
			this.beanMetaDataManager = beanMetaDataManager;
			this.classToBeanTrackingEnabled = new HashMap<>( beanMetaDataManager.getBeanMetaData().size() );
		}

		public Map<Class<?>, Boolean> build() {
			final Set<Class<?>> beanClassesInPath = new HashSet<>();
			for ( BeanMetaData<?> beanMetadata : beanMetaDataManager.getBeanMetaData() ) {
				determineTrackingRequired( beanMetadata.getBeanClass(), beanClassesInPath );
				if ( !beanClassesInPath.isEmpty() ) {
					throw new IllegalStateException( "beanClassesInPath not empty" );
				}
			}
			return classToBeanTrackingEnabled;
		}

		// Do a depth-first search for cycles along paths of cascaded bean classes.
		// The algorithm stops due to one of the following:
		// 1) The bean class was previously put in classToBeanTrackingEnabled
		//    (i.e., the bean class was already determined to either have a cycle,
		//    or not have a cycle).
		// 2) A cycle is found. In this case, all bean classes in the particular path,
		//    starting from beanClass up to first bean class that causes a cycle, will
		//    be registered in classToBeanTrackingEnabled with a value of true.
		//    Once a cycle is found, no further bean classes are examined. Those bean
		//    classes that were examined in the process that are found to not have a
		//    cycle are registered in classToBeanTrackingEnabled with a value of false.
		// 3) No cycle is found. In this case, all bean classes in the tree will be
		//    registered in classToBeanTrackingEnabled with a value of false.
		//
		// Examples: An arrow, ->, indicates a cascading constraint from a bean class.
		//
		// 1) A -> B
		//    |    ^
		//    |    |
		//     ----
		//    A, B have no cycles. A has 2 paths to B, but there are no cycles, because there is no path from B to A.
		//
		// 2) A <-
		//    |   |
		//     ---
		//    A has a cycle to itself.
		//
		// 3) A -> B -> C -> D
		//         ^    |
		//         |    |
		//          -----
		//    A, B, C have cycles; D does not have a cycle.
		//
		private boolean determineTrackingRequired(Class<?> beanClass, Set<Class<?>> beanClassesInPath) {

			final Boolean isBeanTrackingEnabled = classToBeanTrackingEnabled.get( beanClass );
			if ( isBeanTrackingEnabled != null ) {
				// It was already determined for beanClass.
				return isBeanTrackingEnabled;
			}

			// Add beanClass to the path.
			// We don't care about the order of the bean classes in
			// beanClassesInPath. We only care about detecting a duplicate,
			// which indicates a cycle. If no cycle is found in beanClass,
			// it will be removed below.
			if ( !beanClassesInPath.add( beanClass ) ) {
				// The bean was already present in the path being examined.
				// That means that there is cycle involving beanClass.
				// Enable tracking for all elements in beanClassesInPath
				for ( Class<?> dependency : beanClassesInPath ) {
					register( dependency, true );
				}
				beanClassesInPath.clear();
				return true;
			}

			// Now check the cascaded bean classes.
			for ( Class<?> directCascadedBeanClass : getDirectCascadedBeanClasses( beanClass ) ) {
				// Check to see if tracking has already been determined for directCascadedBeanClass
				Boolean isSubBeanTrackingEnabled = classToBeanTrackingEnabled.get( directCascadedBeanClass );
				if ( isSubBeanTrackingEnabled != null ) {
					if ( isSubBeanTrackingEnabled ) {
						// We already know that directCascadedBeanClass has a cycle.
						// That means that all elements in beanClassesInPath
						// will have a cycle.
						for ( Class<?> dependency : beanClassesInPath ) {
							register( dependency, true );
						}
						// No point in checking any others in this loop.
						beanClassesInPath.clear();
						return true;
					}
					else {
						// We already know that directCascadedBeanClass is not involved in
						// any cycles, so move on to the next iteration.
						continue;
					}
				}
				if ( determineTrackingRequired( directCascadedBeanClass, beanClassesInPath ) ) {
					// A cycle was found. No point in checking any others in this loop.
					// beanClassesInPath should have already been cleared.
					assert beanClassesInPath.isEmpty();
					return true;
				}
				// directCascadedBeanClass does not have a cycle.
				// directCascadedBeanClass would have already been removed by the
				// call to #determineTrackingRequired above
			}
			beanClassesInPath.remove( beanClass );
			return register( beanClass, false );
		}

		// TODO: is there a more concise way to do this?
		private <T> Set<Class<?>> getDirectCascadedBeanClasses(Class<T> beanClass ) {
			final BeanMetaData<T> beanMetaData = beanMetaDataManager.getBeanMetaData( beanClass );
			if ( beanMetaData.hasCascadables() ) {
				final Set<Class<?>> directCascadedBeanClasses = new HashSet<>();
				for ( Cascadable cascadable : beanMetaData.getCascadables() ) {
					final CascadingMetaData cascadingMetaData = cascadable.getCascadingMetaData();
					if ( cascadingMetaData.isContainer() ) {
						final ContainerCascadingMetaData containerCascadingMetaData = (ContainerCascadingMetaData) cascadingMetaData;
						if ( containerCascadingMetaData.getEnclosingType() instanceof ParameterizedType ) {
							ParameterizedType parameterizedType = (ParameterizedType) containerCascadingMetaData.getEnclosingType();
							for ( Type typeArgument :  parameterizedType.getActualTypeArguments() ) {
								if ( typeArgument instanceof Class ) {
									directCascadedBeanClasses.add( (Class<?>) typeArgument );
								}
								else {
									throw new UnsupportedOperationException( "Only ParameterizedType values of type Class are supported" );
								}
							}
						}
						else {
							throw new UnsupportedOperationException( "Non-parameterized containers are not supported yet." );
						}
					}
					else {
						// TODO: For now, assume non-container Cascadables are always beans. Truee???
						directCascadedBeanClasses.add( (Class<?>) cascadable.getCascadableType() );
					}
				}
				return directCascadedBeanClasses;
			}
			else {
				return Collections.emptySet();
			}
		}

		private boolean register(Class<?> beanClass, boolean isBeanTrackingEnabled) {
			if ( classToBeanTrackingEnabled.put( beanClass, isBeanTrackingEnabled ) != null ) {
				throw new IllegalStateException( beanClass.getName() + " registered more than once." );
			}
			return isBeanTrackingEnabled;
		}
	}

	@Override
	public boolean isEnabledForBean(Class<?> rootBeanClass, boolean hasCascadables) {
		if ( !hasCascadables ) {
			return false;
		}

		return trackingEnabledForBeans.get( rootBeanClass );
	}

	@Override
	public boolean isEnabledForReturnValue(Executable executable, boolean hasCascadables) {
		if ( !hasCascadables ) {
			return false;
		}

		return trackingEnabledForReturnValues.getOrDefault( executable, true );
	}

	@Override
	public boolean isEnabledForParameters(Executable executable, boolean hasCascadables) {
		if ( !hasCascadables ) {
			return false;
		}

		return trackingEnabledForParameters.getOrDefault( executable, true );
	}

	@Override
	public void clear() {
		trackingEnabledForBeans.clear();
		trackingEnabledForReturnValues.clear();
		trackingEnabledForParameters.clear();
	}
}
