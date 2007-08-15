//$Id: $
package org.hibernate.validator.test.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;

/**
 * @author Emmanuel Bernard
 */
public class Tv {
	@NotNull
	public String name;

	@Valid
	public List<Presenter> presenters = new ArrayList<Presenter>();

	@Valid
	public Map<String, Show> shows = new HashMap<String, Show>();

	public
	@Valid
	Movie[] movies;
}
