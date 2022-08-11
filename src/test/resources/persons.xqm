xquery version "3.1";

module namespace persons="http://scdh.wwu.de/oxbytei";

declare namespace map = "http://www.w3.org/2005/xpath-functions/map";
declare default element namespace "http://www.tei-c.org/ns/1.0";

(: path to the personography, e.g. '${pdu}/persons.xml' :)
declare variable $persons:personography as xs:string external  := "../persons.xml";

(: entry function for oXbytei :)
declare function persons:generate-entries ()
as map(xs:string, xs:string)*
{ for $person in doc($persons:personography)/TEI/text//person[@xml:id]
    let $id := string($person/@xml:id)
    let $result as map(xs:string, xs:string) := map{'key': concat('#', $id), 'label': concat($person/persName/forename, ' ', $person/persName/surname)}
    return $result
};
