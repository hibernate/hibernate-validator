/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.tracking;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ContainerCascadingMetaData;
import org.hibernate.validator.internal.metadata.aggregated.PotentiallyContainerCascadingMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ReturnValueMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ValidatableParametersMetaData;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.facets.Validatable;
import org.hibernate.validator.internal.util.CollectionHelper;

public class PredefinedScopeProcessedBeansTrackingStrategy implements ProcessedBeansTrackingStrategy {

	private final Map<Class<?>, Boolean> trackingEnabledForBeans;

	public PredefinedScopeProcessedBeansTrackingStrategy(Map<Class<?>, BeanMetaData<?>> rawBeanMetaDataMap) {
		this.trackingEnabledForBeans = CollectionHelper.toImmutableMap(
				new TrackingEnabledStrategyBuilder( rawBeanMetaDataMap ).build()
		);
	}

	private static class TrackingEnabledStrategyBuilder {
		private final Map<Class<?>, BeanMetaData<?>> rawBeanMetaDataMap;
		private final Map<Class<?>, Boolean> classToBeanTrackingEnabled;
		// Map values are a set of subtypes for the key class, including "self" i.e. the "key":
		private final Map<Class<?>, Set<Class<?>>> subtypesMap;

		TrackingEnabledStrategyBuilder(Map<Class<?>, BeanMetaData<?>> rawBeanMetaDataMap) {
			this.rawBeanMetaDataMap = rawBeanMetaDataMap;
			this.classToBeanTrackingEnabled = CollectionHelper.newHashMap( rawBeanMetaDataMap.size() );
			this.subtypesMap = CollectionHelper.newHashMap( rawBeanMetaDataMap.size() );
			for ( Class<?> beanClass : rawBeanMetaDataMap.keySet() ) {
				for ( Class<?> otherBeanClass : rawBeanMetaDataMap.keySet() ) {
					if ( beanClass.isAssignableFrom( otherBeanClass ) ) {
						subtypesMap.computeIfAbsent( beanClass, k -> new HashSet<>() )
								.add( otherBeanClass );
					}
				}
			}
		}

		public Map<Class<?>, Boolean> build() {
			final Set<Class<?>> beanClassesInPath = new HashSet<>();
			for ( BeanMetaData<?> beanMetadata : rawBeanMetaDataMap.values() ) {
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
		//
		// We also need to account for the case when the subtype is used at runtime that may change the cycles:
		//  4) A -> B -> C -> D
		//     And C1 extends C where C1 -> A
		//     Hence, at runtime we "may" get:
		//     A -> B -> C1 -> D
		//     ^          |
		//     |          |
		//     -----------
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
		private <T> Set<Class<?>> getDirectCascadedBeanClasses(Class<T> beanClass) {
			final Set<Class<?>> directCascadedBeanClasses = new HashSet<>();
			// At runtime, if we are not looking at the root bean the actual value of a cascadable
			//  can be either the same `beanClass` or one of its subtypes... since subtypes can potentially add
			//  more constraints we want to iterate through the subclasses (for which there is some metadata defined)
			//  and include the info from them too.
			Set<Class<?>> classes = subtypesMap.get( beanClass );
			if ( classes == null ) {
				// It may be that some bean property without any constraints is marked for cascading validation,
				//  In that case the metadata entry will be missing from the map:
				return Set.of();
			}
			for ( Class<?> otherBeanClass : classes ) {
				final BeanMetaData<?> beanMetaData = rawBeanMetaDataMap.get( otherBeanClass );

				if ( beanMetaData == null || !beanMetaData.hasCascadables() ) {
					continue;
				}

				for ( Cascadable cascadable : beanMetaData.getCascadables() ) {
					processSingleCascadable( cascadable, directCascadedBeanClasses );
				}
			}
			return directCascadedBeanClasses;
		}

		private boolean register(Class<?> beanClass, boolean isBeanTrackingEnabled) {
			if ( classToBeanTrackingEnabled.put( beanClass, isBeanTrackingEnabled ) != null ) {
				throw new IllegalStateException( beanClass.getName() + " registered more than once." );
			}
			return isBeanTrackingEnabled;
		}
	}

	private static void processSingleCascadable(Cascadable cascadable, Set<Class<?>> directCascadedBeanClasses) {
		CascadingMetaData cascadingMetaData = cascadable.getCascadingMetaData();
		if ( cascadingMetaData.isContainer() ) {
			final ContainerCascadingMetaData containerCascadingMetaData = cascadingMetaData.as( ContainerCascadingMetaData.class );
			processContainerCascadingMetaData( containerCascadingMetaData, directCascadedBeanClasses );
		}
		else if ( cascadingMetaData instanceof PotentiallyContainerCascadingMetaData ) {
			// If it's a potentially container cascading one, we are "in trouble" as thing can be "almost anything".
			//  Let's be much more cautious and just assume that it can be "anything":
			directCascadedBeanClasses.add( Object.class );
		}
		else {
			// TODO: For now, assume non-container Cascadables are always beans. True???
			directCascadedBeanClasses.add( typeToClassToProcess( cascadable.getCascadableType() ) );
		}
	}

	private static void processContainerCascadingMetaData(ContainerCascadingMetaData metaData, Set<Class<?>> directCascadedBeanClasses) {
		if ( metaData.isCascading() ) {
			if ( metaData.getDeclaredTypeParameterIndex() != null ) {
				if ( metaData.getEnclosingType() instanceof ParameterizedType parameterizedType ) {
					Type typeArgument = parameterizedType.getActualTypeArguments()[metaData.getDeclaredTypeParameterIndex()];
					if ( typeArgument instanceof Class<?> typeArgumentClass ) {
						directCascadedBeanClasses.add( typeArgumentClass );
					}
					else if ( typeArgument instanceof TypeVariable<?> typeVariable ) {
						for ( Type bound : typeVariable.getBounds() ) {
							directCascadedBeanClasses.add( typeToClassToProcess( bound ) );
						}
					}
					else if ( typeArgument instanceof WildcardType wildcard ) {
						for ( Type bound : wildcard.getUpperBounds() ) {
							directCascadedBeanClasses.add( typeToClassToProcess( bound ) );
						}
						if ( wildcard.getLowerBounds().length != 0 ) {
							// if it's a lower bound ? super smth ... it doesn't matter anymore since it can contain anything so go with object ?
							directCascadedBeanClasses.add( Object.class );
						}
					}
					else {
						// In any unexpected case treat things as if they require tracking just to be on the safe side:
						directCascadedBeanClasses.add( Object.class );
					}
				}
			}
			else {
				// If we do not have the type arguments then we can go though the value extractors,
				//  as they are required to define the `@ExtractedValue(type = ???)` ...
				//  this way we should get the type we want:
				for ( ValueExtractorDescriptor valueExtractorCandidate : metaData.getValueExtractorCandidates() ) {
					valueExtractorCandidate.getExtractedType().ifPresent( directCascadedBeanClasses::add );
				}
			}
		}

		for ( ContainerCascadingMetaData sub : metaData.getContainerElementTypesCascadingMetaData() ) {
			processContainerCascadingMetaData( sub, directCascadedBeanClasses );
		}
	}

	private static Class<?> typeToClassToProcess(Type type) {
		if ( type instanceof Class<?> cascadableClass ) {
			return cascadableClass;
		}
		else if ( type instanceof ParameterizedType parameterizedType ) {
			return typeToClassToProcess( parameterizedType.getRawType() );
		}
		else {
			return Object.class;
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
	public boolean isEnabledForReturnValue(ReturnValueMetaData returnValueMetaData) {
		return isEnabledForExecutableValidatable( returnValueMetaData );
	}

	@Override
	public boolean isEnabledForParameters(ValidatableParametersMetaData parametersMetaData) {
		return isEnabledForExecutableValidatable( parametersMetaData );
	}

	private boolean isEnabledForExecutableValidatable(Validatable validatable) {
		if ( !validatable.hasCascadables() ) {
			return false;
		}

		Set<Class<?>> directCascadedBeanClasses = new HashSet<>();
		for ( Cascadable cascadable : validatable.getCascadables() ) {
			processSingleCascadable( cascadable, directCascadedBeanClasses );
		}
		for ( Class<?> directCascadedBeanClass : directCascadedBeanClasses ) {
			if ( Boolean.TRUE.equals( trackingEnabledForBeans.get( directCascadedBeanClass ) ) ) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void clear() {
		trackingEnabledForBeans.clear();
	}
}
