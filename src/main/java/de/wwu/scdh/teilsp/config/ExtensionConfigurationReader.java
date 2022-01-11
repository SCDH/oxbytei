package de.wwu.scdh.teilsp.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.wwu.scdh.teilsp.xml.NamespaceContextImpl;
import de.wwu.scdh.teilsp.exceptions.ConfigurationException;


public class ExtensionConfigurationReader {

    public static final String pluginsXPath = "/c:teilspConfiguration/c:plugins/c:plugin";
    public static final String classNameXPath = "c:class";
    public static final String typeXPath = "c:type";
    public static final String conditionXPath = "descendant::c:conditions/c:condition";
    public static final String argumentXPath = "descendant::c:arguments/c:argument";
    
    public static final NamespaceContext namespace =
	new NamespaceContextImpl("c:http://wwu.de/scdh/teilsp/config/");

    public static List<ExtensionConfiguration> getExtensionsConfiguration(String configURL)
	throws ConfigurationException {
	try {
	    InputStream input = new URL(configURL).openStream();
	    List<ExtensionConfiguration> config = getExtensionsConfiguration(input);
	    input.close();
	    return config;
	} catch (IOException e) {
	    throw new ConfigurationException("Error opening config from '"
					     + configURL
					     + "'. File not found");
	}
    }

    public static List<ExtensionConfiguration> getExtensionsConfiguration(InputStream input)
	throws ConfigurationException {
	List<ExtensionConfiguration> config = new ArrayList<ExtensionConfiguration>();

	XPath xpath = XPathFactory.newInstance().newXPath();
	xpath.setNamespaceContext(namespace);

	ExtensionConfiguration extension;
	try {
	    InputSource inputSource = new InputSource(input);
	    NodeList pluginNodes = (NodeList) xpath.compile(pluginsXPath).evaluate(inputSource, XPathConstants.NODESET);

	    for (int i = 0; i < pluginNodes.getLength(); i++) {
		Element plugin = (Element) pluginNodes.item(i);
		String className = (String) xpath.compile(classNameXPath).evaluate(plugin, XPathConstants.STRING);
		String typeName = (String) xpath.compile(typeXPath).evaluate(plugin, XPathConstants.STRING);
		
		extension = new ExtensionConfiguration(className, typeName,
						       getSpecifications(plugin, xpath));
		config.add(extension);
	    }
	} catch (XPathExpressionException e) {
	    throw new ConfigurationException(e);
	}//  catch (SAXException e) {
	//     throw new ConfigurationException(e);
	// }
	
	return config;
    }

    private static List<ArgumentsConditionsPair> getSpecifications(Element specsNode, XPath xpath)
	throws ConfigurationException {
	List<ArgumentsConditionsPair> spec = new ArrayList<ArgumentsConditionsPair>();
	try {
	    NodeList nodes = (NodeList) xpath.compile("descendant::c:configuration").evaluate(specsNode, XPathConstants.NODESET);
	    for (int i = 0; i < nodes.getLength(); i++) {
		Element specNode = (Element) nodes.item(i);
		ArgumentsConditionsPair pair = 
		    new ArgumentsConditionsPair(getConditions(specNode, xpath),
						getArguments(specNode, xpath));
		spec.add(pair);
	    }
	} catch (XPathExpressionException e) {
	    throw new ConfigurationException(e);
	}
	return spec;
    }

    private static Map<String, String> getConditions(Element conditionsNode, XPath xpath)
	throws ConfigurationException {
	Map<String, String> conditions = new HashMap<String, String>();
	Element node;
	String domain, condition;
	try {
	    NodeList conditionNodes = (NodeList) xpath.compile(conditionXPath).evaluate(conditionsNode, XPathConstants.NODESET);
	    for (int i = 0; i < conditionNodes.getLength(); i++) {
		node = (Element) conditionNodes.item(i);
		domain = (String) xpath.compile("@domain").evaluate(node, XPathConstants.STRING);
		condition = (String) xpath.compile("text()").evaluate(node, XPathConstants.STRING);
		conditions.put(domain, condition);
	    }
	} catch (XPathExpressionException e) {
	    throw new ConfigurationException(e);
	}
	return conditions;
    }

    private static Map<String, String> getArguments(Element argumentsNode, XPath xpath)
	throws ConfigurationException {
	Map<String, String> arguments = new HashMap<String, String>();
	Element node;
	String name, argument;
	try {
	    NodeList argumentNodes = (NodeList) xpath.compile(argumentXPath).evaluate(argumentsNode, XPathConstants.NODESET);
	    for (int i = 0; i < argumentNodes.getLength(); i++) {
		node = (Element) argumentNodes.item(i);
		name = (String) xpath.compile("@name").evaluate(node, XPathConstants.STRING);
		argument = (String) xpath.compile("text()").evaluate(node, XPathConstants.STRING);
		arguments.put(name, argument);
	    }
	} catch (XPathExpressionException e) {
	    throw new ConfigurationException(e);
	}
	return arguments;
    }

    public static String getProperty(String propertyName, String configURL)
	throws ConfigurationException {
	try {
	    InputStream input = new URL(configURL).openStream();
	    String property = getProperty(propertyName, input);
	    input.close();
	    return property;
	} catch (IOException e) {
	    throw new ConfigurationException("Error opening config from '"
					     + configURL
					     + "'. File not found");
	}
    }

    public static String getProperty(String propertyName, InputStream input)
	throws ConfigurationException {
	XPath xpath = XPathFactory.newInstance().newXPath();
	xpath.setNamespaceContext(namespace);
	String propertyXPath = "/c:teilspConfiguration/c:properties/c:property[@name = '" + propertyName + "']";
	String property = null;
	try {
	    InputSource inputSource = new InputSource(input);
	    property = (String) xpath.compile(propertyXPath).evaluate(inputSource, XPathConstants.STRING);
	} catch (XPathExpressionException e) {
	    throw new ConfigurationException(e);
	}//  catch (SAXException e) {
	//     throw new ConfigurationException(e);
	// }
	return property;
    }

}
