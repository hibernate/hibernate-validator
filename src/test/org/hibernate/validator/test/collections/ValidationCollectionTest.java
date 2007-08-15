//$Id: $
package org.hibernate.validator.test.collections;

import junit.framework.TestCase;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;

/**
 * @author Emmanuel Bernard
 */
public class ValidationCollectionTest extends TestCase {
	public void testCollection() throws Exception {
		Tv tv = new Tv();
		tv.name = "France 2";
		Presenter presNok = new Presenter();
		presNok.name = null;
		Presenter presOk = new Presenter();
		presOk.name = "Thierry Ardisson";
		tv.presenters.add( presOk );
		tv.presenters.add( presNok );
		ClassValidator validator = new ClassValidator( Tv.class );
		InvalidValue[] values = validator.getInvalidValues( tv );
		assertEquals( 1, values.length );
		assertEquals( "presenters[1].name", values[0].getPropertyPath() );
	}

	public void testMap() throws Exception {
		Tv tv = new Tv();
		tv.name = "France 2";
		Show showOk = new Show();
		showOk.name = "Tout le monde en parle";
		Show showNok = new Show();
		showNok.name = null;
		tv.shows.put( "Midnight", showOk );
		tv.shows.put( "Primetime", showNok );
		ClassValidator validator = new ClassValidator( Tv.class );
		InvalidValue[] values = validator.getInvalidValues( tv );
		assertEquals( 1, values.length );
		assertEquals( "shows['Primetime'].name", values[0].getPropertyPath() );
	}

	public void testArray() throws Exception {
		Tv tv = new Tv();
		tv.name = "France 2";
		Movie movieOk = new Movie();
		movieOk.name = "Kill Bill";
		Movie movieNok = new Movie();
		movieNok.name = null;
		tv.movies = new Movie[]{
				movieOk,
				null,
				movieNok
		};
		ClassValidator validator = new ClassValidator( Tv.class );
		InvalidValue[] values = validator.getInvalidValues( tv );
		assertEquals( 1, values.length );
		assertEquals( "movies[2].name", values[0].getPropertyPath() );
	}
}
