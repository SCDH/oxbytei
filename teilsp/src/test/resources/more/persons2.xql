xquery version "3.1";

import module namespace obt="persons" at "../persons.xqm";

declare namespace map = "http://www.w3.org/2005/xpath-functions/map";
declare default element namespace "http://www.tei-c.org/ns/1.0";

(: for debugging only :)
declare namespace output = "w3.org/2010/xslt-xquery-serialization";
declare option output:method "text";
declare option saxon:output "omit-xml-declaration=yes";

(: not used by oXbytei :)
for $entry in obt:generate-entries()
    return string-join((map:get($entry, 'key'), map:get($entry, 'label')), ',') || '&#xa;'