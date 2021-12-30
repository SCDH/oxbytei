# Configuration #

The information given in TEI's [`<prefixDef>`]() is sufficient for
interpreting costom URI schemes. However, it's not sufficient for
generating editor functions.

- There is no information in which editing context to use which URI
  scheme(s), e.g. to use the URI scheme `psn:...` in the editing
  context of `persName/@ref`.

- And there is certainly no information about the structure of the
  source, the URI scheme refers to.

oXbytei does not hardcode this information into the framework, but
provides it via a configuration file. **The configuration makes
oXbytei very flexible; and in the end you do not have to evaluate any
information in the TEI header but can put everything into the
configuration file.**

oXbytei comes with a [configuration file with reasonable
defaults](../frameworks/oxbytei/config/default.xml). But it can be
replaced by the user.

## Plugins and editing contexts ##

The configuration binds plugins to editing contexts and passes
arguments for this editing context to the plugin. Then, it's the
plugin's job to provide suggestions for content completion.

Let's look at an example: The default configuration for
`persName/@ref` looks like this:


```{xml}
<teilspConfiguration xmlns="http://wwu.de/scdh/teilsp/config/">
    <plugins>
		
        <plugin>
            <class>de.wwu.scdh.teilsp.extensions.LabelledEntriesFromXMLByPrefixDef</class>
            <type>de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider</type>
            <configurations>
                <configuration>
                    <conditions>
                        <condition domain="context">self::*:persName and //*:teiHeader//*:prefixDef[matches(@ident, '^(psn|prs|pers|person)')]</condition>
                        <condition domain="priority">10</condition>
                        <condition domain="nodeType">attributeValue</condition>
                        <condition domain="nodeName">ref</condition>
                    </conditions>
                    <arguments>
                        <argument name="href">tokenize(//t:teiHeader//t:prefixDef[matches(@ident, '(psn|prs|pers)')]/@replacementPattern, '#')[1]</argument>
                        <argument name="prefix">concat(//t:teiHeader//t:prefixDef[matches(@ident, '(psn|prs|pers)')]/@ident, ':')</argument>
                        <argument name="selection">//t:text/descendant::t:person[@xml:id]</argument>
                        <argument name="key">@xml:id</argument>
                        <argument name="label">normalize-space(concat(t:persName, ', *', t:birth, unparsed-text(test.txt)))</argument>
                        <argument name="namespaces">t:http://www.tei-c.org/ns/1.0 xml:http://www.w3.org/XML/1998/namespace</argument>
                    </arguments>
                </configuration>

				<!-- ... other configurations for this plugin -->
				
            </configurations>
        </plugin>

		
    </plugins>
</teilspConfiguration>
```


Configurations are defined for plugins. Here, a configuration for the
plugin
`de.wwu.scdh.teilsp.extensions.LabelledEntriesFromXMLByPrefixDef` is
defined. This plugin provides pairs of keys and labels from (local)
XML files, the URL of which is be read from the currently edited
document. There is one configuration for this plugin in this example,
but there can be arbitrary many.

Each configuration consists of **conditions** and **arguments**.
Conditions are evaluated by the plugin loader. They have a
*domain*--i.e. a name from a controlled set of names--, and a
*value*. Arguments are evaluated by the plugin. Arguments are pairs of
*names* and *values*. The names differ for each plugin type, as each
plugin type may be setup up with different sets of parameters. In
contrast, the domains of each plugin are equal, because they all are
evaluated by the same piece of code, namely the plugin loader.

### Conditions ###

The most import condition domains are `context`, `nodeType`, and
`nodeName`.

The plugin is configured for the context `self::*:persName ...`,
i.e. if the currently edited context node is `<persName>`. In this
context, the plugin provides completion suggestions. But only, if the
other conditions are met, too: If the node type, that the plugin
provides suggestions for, is an `attributeValue` and if the name of
the attribute is `ref`. But `self::*:persName` is not the only context
condition; there must be a `<prefixDef>` element in the file, too, and
its `@ident` attribute must be ...

Note on `context`: The XPath given here will be put into an XPath
[predicate](https://www.saxonica.com/html/documentation10/expressions/filter.html)
and the resulting expression is tested for existence.

E.g. `self::*:persName` in an editing context somewhere in a paragraph
in a TEI document will be inserted into an expression for identifying
this context, e.g. `/TEI[1]/text[1]/div[23]/p[42]`. The resulting
expression that is evaluated by the plugin loader will be
`/TEI[1]/text[1]/div[23]/p[42][self::*:persName]`, which will return
the empty set. So the context condition is *not* met. But in the
context `/TEI[1]/text[1]/div[23]/p[42]/persName[1]` the resulting
expression
`/TEI[1]/text[1]/div[23]/p[42]/persName[1][self::*:persName]` returns
a node, so the context condition is met.

### Arguments ###

The `de.wwu.scdh.teilsp.extensions.LabelledEntriesFromXMLByPrefixDef`
plugin takes some XPath expressions as argument for getting the URL of
the referenced XML document, and for getting keys and labelled from
this document.

The `href` argument is an XPath for getting the URL of the referenced
XML document. This XPath is evaluated on the currently edited
document. Note, that you can use this plugin not only for getting the
URL from a `<prefixDef>`, but also from other fragments of the
currently edited file. You can also provide a constant string, to link
a document, the URL of which does not appear the the currently edited
document. E.g. `<argument
name="href">'https://my.domain.org/normdata.xml'</argument>`.

Same with `prefix` argument: It's evaluated on the currently edited
document and sets a fixed prefix for the keys provided by the
plugin. In this case, the prefix is composed of the `@ident` of a
`<prefixDef>` element and a colon. You can also put a constant XPath
expression there, e.g. `<argument name="prefix">'#'</argument>`.

The arguments `selection`, `key`, and `label` are XPath that are
evaluated on the referenced document.

The argument `namespaces` is common to many plugins and binds the
namespace prefixes in the other argument values to namespace
names. The form is `prefix:namespace-name[ prefix:namespace-name]*`,
i.e. space-separated pairs of a colon-separated prefix and namespace
name.

### Multiple plugins for the same editing context ###

It's possible to define several plugin setups (plugin+arguments) for
the same editing context. If this is the case, the suggestions of all
plugins are concatenated.

In future, the `priority` condition may be evaluated for sorting or
shadowing, but right now it's not evaluated at all.

### Ambiguous plugin configurations ###

If the plugin is configured with arguments, that lead to ambiguous
results, it's the plugin job to handle things. It's a matter of the
plugin's specification.

E.g., let's assume that there are a prefix definitions for `psn:...`
and also `pers:...` in your TEI header. With the default
configuration, the plugin will find both prefix definitions. In this
case, it throws an exception instead of returning suggestions. If your
personography is really split into multiple files, then you should
provide distinct plugin configurations, one for each file/prefix, i.e.
multiple plugin setups for the same editing context (see above).

Also note, that there is a bug in the user dialogue: If two
suggestions have the same label, it's not guaranteed that the keys are
correct. But you can help out, by adding the prefix to the label.


## Custom configuration file ##

The default configuration file is packetized into the framework. You
can overwrite it using an XML catalog.

Let's assame that you have an XML catalog file in
`resources/catalog.xml` in your oXygen project and a customized
configuration file in `resources/oxbytei-config.xml`. Then you should
put the following lines in the catalog file:

```{xml}
<!-- redirect to oXbytei config for project -->
<uriSuffix uriSuffix="oxbytei/config/default.xml" uri="oxbytei-config.xml"/>
<systemSuffix systemIdSuffix="oxbytei/config/default.xml" uri="oxbytei-config.xml"/>
```

Note, that you do *not* have to restart oXygen to get configuration
changes reflected. 
