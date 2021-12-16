package de.wwu.scdh.teilsp.config;

import java.util.Map;

public class ArgumentsConditionsPair {

    private Map<String, String> conditions;
    private Map<String, String> arguments;

    public ArgumentsConditionsPair(Map<String, String> cond, Map<String, String> args) {
	this.conditions = cond;
	this.arguments = args;
    }

    public Map<String, String> getConditions() {
	return conditions;
    }

    public Map<String, String> getArguments() {
	return arguments;
    }
    
}
