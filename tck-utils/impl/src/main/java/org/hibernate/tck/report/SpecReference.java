package org.hibernate.tck.report;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the metadata for a single instance of @SpecAssertion
 *
 * @author Shane Bryzak
 */
public class SpecReference {
   
    private String section;
    private String assertion;
    private String packageName;
    private String className;
    private String methodName;
    private List<String> groups;
    
    public SpecReference()
    {
        this.groups = new ArrayList<String>();
    }

    public void setSection(String section) {
        this.section = section;
    }

    public void setAssertion(String assertion) {
        this.assertion = assertion;
    }
    
    public void setPackageName(String packageName) {
       this.packageName = packageName;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getSection() {
        return section;
    }

    public String getAssertion() {
        return assertion;
    }
    
    public String getPackageName() {
       return packageName;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }
    
    public List<String> getGroups()
   {
      return groups;
   }
    
    @Override
    public String toString()
    {
       return "SpecReference[section=" + section + ";assertion=" + assertion + 
          ";class=" + packageName + "." + className + ";method=" + methodName + "]";
    }
}
