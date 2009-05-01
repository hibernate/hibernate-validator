package org.hibernate.validation.engine.resolver;

import java.lang.annotation.ElementType;
import java.util.HashMap;
import java.util.Map;
import javax.validation.TraversableResolver;

/**
 * Cache results of a delegated traversable resovler to optimize calls
 * It works only for a single validate* call and should not be used if
 * the TraversableResolver is accessed concurrently
 *
 * @author Emmanuel Bernard
 */
public class SingleThreadCachedTraversableResolver implements TraversableResolver {
	private TraversableResolver delegate;
	private Map<LazyHolder, LazyHolder> lazys = new HashMap<LazyHolder, LazyHolder>();
	private Map<TraversableHolder, TraversableHolder> traversables = new HashMap<TraversableHolder, TraversableHolder>();

	public SingleThreadCachedTraversableResolver(TraversableResolver delegate) {
		this.delegate = delegate;
	}

	public boolean isTraversable(Object traversableObject, String traversableProperty, Class<?> rootBeanType, String pathToTraversableObject, ElementType elementType) {
		Boolean delegateResult = null;

		//if traversableObject is null we can't determine lazyness
		if (traversableObject != null) {
			LazyHolder currentLH = new LazyHolder( traversableObject, traversableProperty );
			LazyHolder cachedLH = lazys.get( currentLH );
			if (cachedLH == null) {
				delegateResult = delegate.isTraversable(
						traversableObject,
						traversableProperty,
						rootBeanType,
						pathToTraversableObject,
						elementType );
				currentLH.isTraversable = delegateResult;
				lazys.put( currentLH, currentLH );
				cachedLH = currentLH;
			}
			if ( ! cachedLH.isTraversable ) return false;
		}


		TraversableHolder currentTH = new TraversableHolder( rootBeanType, pathToTraversableObject, elementType );
		TraversableHolder cachedTH = traversables.get(currentTH);
		if ( cachedTH == null ) {
			if (delegateResult == null) {
				delegateResult = delegate.isTraversable(
					traversableObject,
					traversableProperty,
					rootBeanType,
					pathToTraversableObject,
					elementType );
			}
			currentTH.isTraversable = delegateResult;
			traversables.put( currentTH, currentTH );
			cachedTH = currentTH;
		}
		return cachedTH.isTraversable;
	}

	private static class LazyHolder {
		private final Object traversableObject;
		private final String traversableProperty;
		private final int hashCode;
		private boolean isTraversable;

		private LazyHolder(Object traversableObject, String traversableProperty) {
			this.traversableObject = traversableObject;
			this.traversableProperty = traversableProperty == null ? "" : traversableProperty;
			hashCode = this.traversableObject.hashCode() + this.traversableProperty.hashCode();
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if ( ! (obj instanceof LazyHolder) ) {
				return false;
			}
			LazyHolder that = (LazyHolder) obj;
			return traversableObject == that.traversableObject
					&& traversableProperty.equals( that.traversableProperty ); 
		}
	}
	
	private static class TraversableHolder {
		private final Class<?> rootBeanType;
		private final String pathToTraversableObject;
		private final ElementType elementType;
		private final int hashCode;
		
		private boolean isTraversable;

		private TraversableHolder(Class<?> rootBeanType, String pathToTraversableObject, ElementType elementType) {
			this.rootBeanType = rootBeanType;
			this.pathToTraversableObject = pathToTraversableObject == null ? "" : pathToTraversableObject;
			this.elementType = elementType;
			hashCode = this.rootBeanType.hashCode() + this.pathToTraversableObject.hashCode() + this.elementType.hashCode();
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if ( ! (obj instanceof TraversableHolder) ) {
				return false;
			}
			TraversableHolder that = (TraversableHolder) obj;
			return rootBeanType == that.rootBeanType
					&& pathToTraversableObject.equals( that.pathToTraversableObject )
					&& elementType == that.elementType; 
		}
	}
}
