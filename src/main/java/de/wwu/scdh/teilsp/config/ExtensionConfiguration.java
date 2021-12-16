package de.wwu.scdh.teilsp.config;

import java.util.List;

public class ExtensionConfiguration {

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
