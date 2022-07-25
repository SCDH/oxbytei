xquery version "3.1";

declare namespace obt = "http://scdh.wwu.de/oxbytei";
declare namespace map = "http://www.w3.org/2005/xpath-functions/map";
declare default element namespace "http://www.tei-c.org/ns/1.0";

(: for debugging only :)
declare namespace output = "w3.org/2010/xslt-xquery-serialization";
declare option output:method "text";
declare option saxon:output "omit-xml-declaration=yes";

(: path to the witness catalogue, e.g. '${pdu}/witnesses.xml' :)
declare variable $witness-catalog as xs:string external  := "../../WitnessCatalogue.xml";

(: entry function for oXbytei :)
declare function obt:generate-entries ()
as map(xs:string, xs:string)*
{ for $witness in doc($witness-catalog)/TEI/text//witness[@xml:id]
    let $id := string($witness/@xml:id)
    let $result as map(xs:string, xs:string) := map{'key': concat('#', $id), 'label': concat('#', $id)}
    return $result
};

(: not used by oXbytei :)
for $entry in obt:generate-entries()
    return string-join((map:get($entry, 'key'), map:get($entry, 'label')), ',') || '&#xa;'