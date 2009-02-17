package org.hibernate.tck.config;

import java.util.StringTokenizer;

public class Strings
{
   static String[] split(String strings, String delims)
   {
      if (strings == null)
      {
         return new String[0];
      }
      else
      {
         StringTokenizer tokens = new StringTokenizer(strings, delims);
         String[] result = new String[tokens.countTokens()];
         int i = 0;
         while (tokens.hasMoreTokens())
         {
            result[i++] = tokens.nextToken();
         }
         return result;
      }
   }
   
   public static boolean isEmpty(String string)
   {
      return string == null || string.length() == 0;
   }
}
