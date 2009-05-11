package org.hibernate.validation.engine.resolver;

import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 * @author Emmanuel Bernard
 */
public class TraversableResolverTest {
	@Test
	public void testCorrectPathsAreRequested() {
		Suit suit = new Suit();
		suit.setTrousers( new Trousers() );
		suit.setJacket( new Jacket() );
		suit.setSize( 3333 );
		suit.getTrousers().setLength( 32321 );
		suit.getJacket().setWidth( 432432 );

		SnifferTraversableResolver resolver = new SnifferTraversableResolver( suit );

		// TODO - Investigate why this cast is needed with Java 5. In Java 6 there is no problem.
		Configuration<?> config = (Configuration<?>) Validation.byDefaultProvider().configure().traversableResolver( resolver );

		ValidatorFactory factory = config.buildValidatorFactory();
		Validator v = factory.getValidator();

		//Raises an IllegalStateException if something goes wrong
		v.validate( suit, Default.class, Cloth.class );

		assertEquals( 5, resolver.getReachPaths().size() );
		assertEquals( 2, resolver.getCascadePaths().size() );
	}
}