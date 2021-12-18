/**
 * This needs an implementation.
 *
 */

package de.wwu.scdh.teilsp.services.extensions;


public class ArgumentDescriptor {

    public static final int TYPE_JAVA_OBJECT = 0;

    public static final int TYPE_XPATH_EXPRESSION = 1;

    public static final int TYPE_SCRIPT = 2;

    public static final int TYPE_STRING = 3;

    public static final int TYPE_NAMESPACE_DECL = 4;
    
    public ArgumentDescriptor(String name, int type, String description) {
	
    }

    public ArgumentDescriptor(String name, int type, String description, String defaultValue) {
	
    }

    public ArgumentDescriptor(String name, int type, String description, String[] allowedValues, String defaultValue) {
	
    }
}
