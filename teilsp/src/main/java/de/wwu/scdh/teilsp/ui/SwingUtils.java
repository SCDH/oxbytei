/*
 * @(#)SwingUtils.java
 *
 *
 */
package de.wwu.scdh.teilsp.ui;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

/**
 * A collection of utility methods for Swing.
 *
 * @author Darryl Burke
 * @source https://tips4java.wordpress.com/2008/11/13/swing-utils/
 *
 * Code that is not required was stripped by clueck, 2022.
 */
public final class SwingUtils {

   /**
    * Convenience method for searching below <code>container</code> in the
    * component hierarchy and return nested components that are instances of
    * class <code>clazz</code> it finds. Returns an empty list if no such
    * components exist in the container.
    * <P>
    * Invoking this method with a class parameter of JComponent.class
    * will return all nested components.
    * <P>
    * This method invokes getDescendantsOfType(clazz, container, true)
    * 
    * @param clazz the class of components whose instances are to be found.
    * @param container the container at which to begin the search
    * @return the List of components
    */
   public static <T extends JComponent> List<T> getDescendantsOfType(
         Class<T> clazz, Container container) {
      return getDescendantsOfType(clazz, container, true);
   }

   /**
    * Convenience method for searching below <code>container</code> in the
    * component hierarchy and return nested components that are instances of
    * class <code>clazz</code> it finds. Returns an empty list if no such
    * components exist in the container.
    * <P>
    * Invoking this method with a class parameter of JComponent.class
    * will return all nested components.
    * 
    * @param clazz the class of components whose instances are to be found.
    * @param container the container at which to begin the search
    * @param nested true to list components nested within another listed
    * component, false otherwise
    * @return the List of components
    */
   public static <T extends JComponent> List<T> getDescendantsOfType(
         Class<T> clazz,
	 Container container,
	 boolean nested) {
      List<T> tList = new ArrayList<T>();
      for (Component component : container.getComponents()) {
         if (clazz.isAssignableFrom(component.getClass())) {
            tList.add(clazz.cast(component));
         }
         if (nested || !clazz.isAssignableFrom(component.getClass())) {
            tList.addAll(SwingUtils.<T>getDescendantsOfType(clazz,
                  (Container) component, nested));
         }
      }
      return tList;
   }

}
