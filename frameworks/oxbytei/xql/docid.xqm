xquery version "3.1";

(: Replace this module with your own for making document identifiers for your project.:)

module namespace obt = "http://scdh.wwu.de/oxbytei";

declare default element namespace "http://www.tei-c.org/ns/1.0";

(: This implementation tries to get the document identifier from an
<idno type="document-identifier">ID</idno>
somewhere in a document's header.:)
declare function obt:getDocId($context as node()) as xs:string*
{
    root($context)//teiHeader//idno[@type eq 'document-identifier']/text()
};
