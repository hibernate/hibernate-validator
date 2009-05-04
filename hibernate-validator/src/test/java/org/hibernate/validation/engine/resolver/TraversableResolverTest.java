package org.hibernate.validation.engine.resolver;

import java.lang.annotation.ElementType;
import java.util.HashSet;
import java.util.Set;
import javax.validation.TraversableResolver;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

import org.hibernate.validation.engine.HibernateValidatorConfiguration;

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

		SnifferTraversableResolver resolver = new SnifferTraversableResolver(suit);
		ValidatorFactory factory = Validation.byDefaultProvider()
				.configure().traversableResolver( resolver )
				.buildValidatorFactory();
		Validator v = factory.getValidator();

		//Raises an IllegalStateException if something goes wrong
		v.validate( suit, Default.class, Cloth.class );

		assertEquals( 5, resolver.getPaths().size() );
	}
}