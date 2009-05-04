package org.hibernate.validation.engine.resolver;

import java.lang.annotation.ElementType;
import java.util.Set;
import java.util.HashSet;
import javax.validation.TraversableResolver;

/**
 * @author Emmanuel Bernard
 */
public class SnifferTraversableResolver implements TraversableResolver {
	Set<String> paths = new HashSet<String>();
	Set<Call> calls = new HashSet<Call>();

	public SnifferTraversableResolver(Suit suit) {
		calls.add( new Call(suit, "size", Suit.class, "", ElementType.FIELD ) );
		calls.add( new Call(suit, "trousers", Suit.class, "", ElementType.FIELD ) );
		calls.add( new Call(suit.getTrousers(), "length", Suit.class, "trousers", ElementType.FIELD ) );
		calls.add( new Call(suit, "jacket", Suit.class, "", ElementType.METHOD ) );
		calls.add( new Call(suit.getJacket(), "width", Suit.class, "jacket", ElementType.METHOD ) );
	}

	public Set<String> getPaths() {
		return paths;
	}

	//TODO add test with correct paths and types to make sure the impl does not mess it up
	public boolean isTraversable(Object traversableObject, String traversableProperty, Class<?> rootBeanType, String pathToTraversableObject, ElementType elementType) {
		String path = "";
		if (! (pathToTraversableObject == null || pathToTraversableObject.length() == 0 ) ) {
			path = pathToTraversableObject + ".";
		}
		paths.add( path + traversableProperty );
		Call call = new Call(traversableObject, traversableProperty, rootBeanType, pathToTraversableObject, elementType);
		if ( ! calls.contains( call ) ) {

			throw new IllegalStateException( "Unexpected " + call.toString() );
		}
		return true;
	}

	private static final class Call {
		private Object traversableObject;
		private String traversableProperty;
		private Class<?> rootBeanType;
		private String pathToTraversableObject;
		private ElementType elementType;

		private Call(Object traversableObject, String traversableProperty, Class<?> rootBeanType, String pathToTraversableObject, ElementType elementType) {
			this.traversableObject = traversableObject;
			this.traversableProperty = traversableProperty;
			this.rootBeanType = rootBeanType;
			this.pathToTraversableObject = pathToTraversableObject;
			this.elementType = elementType;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			Call call = ( Call ) o;

			if ( elementType != call.elementType ) {
				return false;
			}
			if ( !pathToTraversableObject.equals( call.pathToTraversableObject ) ) {
				return false;
			}
			if ( !rootBeanType.equals( call.rootBeanType ) ) {
				return false;
			}
			if ( traversableObject != null ? !(traversableObject == call.traversableObject) : call.traversableObject != null ) {
				return false;
			}
			if ( !traversableProperty.equals( call.traversableProperty ) ) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result = traversableObject != null ? traversableObject.hashCode() : 0;
			result = 31 * result + traversableProperty.hashCode();
			result = 31 * result + rootBeanType.hashCode();
			result = 31 * result + pathToTraversableObject.hashCode();
			result = 31 * result + elementType.hashCode();
			return result;
		}

		@Override
		public String toString() {
			return "Call{" +
					"traversableObject=" + traversableObject +
					", traversableProperty='" + traversableProperty + '\'' +
					", rootBeanType=" + rootBeanType +
					", pathToTraversableObject='" + pathToTraversableObject + '\'' +
					", elementType=" + elementType +
					'}';
		}
	}
}
