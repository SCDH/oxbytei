package de.wwu.scdh.teilsp.config;

import java.util.List;

public class ExtensionConfiguration {

    public static final String ELEMENT_NODE = "element";
    public static final String ATTRIBUTE_NODE = "attribute";
    public static final String TEXT_NODE = "text";
    public static final String ATTRIBUTE_VALUE = "attribute"; // FIXME

    private String className;
    private String type;
    private List<ArgumentsConditionsPair> specification;
    
    public ExtensionConfiguration(String cls,
				  String typ,
				  List<ArgumentsConditionsPair> spec) {
	this.className = cls;
	this.type = typ;
	this.specification = spec;
    }

    public String getClassName() {
	return className;
    }
    public String getType() {
	return type;
    }
    public List<ArgumentsConditionsPair> getSpecification() {
	return specification;
    }
}
