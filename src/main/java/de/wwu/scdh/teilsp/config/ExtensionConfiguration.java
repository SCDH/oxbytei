package de.wwu.scdh.teilsp.config;

import java.util.Map;

public class ExtensionConfiguration {

    private String className;
    private String type;
    private Map<String, String> condition;
    private Map<String, String> configuration;
    
    public ExtensionConfiguration(String cls,
				  String typ,
				  Map<String, String> cond,
				  Map<String, String> config) {
	this.className = cls;
	this.type = typ;
	this.condition = cond;
	this.configuration = config;
    }

    public String getClassName() {
	return className;
    }
    public String getType() {
	return type;
    }
    public Map<String, String> getCondition() {
	return condition;
    }
    public Map<String, String> getConfiguration() {
	return configuration;
    }
    
}
