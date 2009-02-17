package org.hibernate.tck.config;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * An enumeration -> iterator adapter
 *  
 * @author Pete Muir
 */
@SuppressWarnings("unchecked")
class EnumerationIterator<T> implements Iterator<T>
{
   // The enumeration
   private Enumeration e;

   /**
    * Constructor
    * 
    * @param e The enumeration
    */
   public EnumerationIterator(Enumeration e)
   {
      this.e = e;
   }

   /**
    * Indicates if there are more items to iterate
    * 
    * @return True if more, false otherwise
    */
   public boolean hasNext()
   {
      return e.hasMoreElements();
   }

   /**
    * Gets the next item
    * 
    * @return The next items
    */
   public T next()
   {
      return (T) e.nextElement();
   }

   /**
    * Removes an item. Not supported
    */
   public void remove()
   {
      throw new UnsupportedOperationException();
   }
   
}