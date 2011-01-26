/******************************************
 *Author:mistaguy
 *email:abiccel@yahoo.com
 *The base script is supposed to make ajax request and provide handy routines for handling data
 *******************************************/


/* creates an XMLHttpRequest/Ajax instance */
function createXmlHttpRequestObject()
{
    // will store the reference to the XMLHttpRequest object
    var xmlHttp;
    // this should work for all browsers except IE6 and older
    try
    {
        // try to create XMLHttpRequest object
        xmlHttp = new XMLHttpRequest();
    }
    catch(e)
    {
        // assume IE6 or older
        var XmlHttpVersions = new Array("MSXML2.XMLHTTP.6.0",
            "MSXML2.XMLHTTP.5.0",
            "MSXML2.XMLHTTP.4.0",
            "MSXML2.XMLHTTP.3.0",
            "MSXML2.XMLHTTP",
            "Microsoft.XMLHTTP");
        // try every prog id until one works
        for (var i=0; i<XmlHttpVersions.length && !xmlHttp; i++)
        {
            try
            {
                // try to create XMLHttpRequest object
                xmlHttp = new ActiveXObject(XmlHttpVersions[i]);
            }
            catch (e) {}
        }
    }
    // return the created object or display an error message
    if (!xmlHttp)
        alert("Error creating the XMLHttpRequest object.");
    else
        return xmlHttp;
}
/***
 * This function should load the xml
 */
function loadXml(xhttp,tileFileName)
{
    if(xhttp==null)
    {
        var xmlhttp= createXmlHttpRequestObject();
        xmlhttp.open("GET",tileFileName,false);
        xmlhttp.send(null);
        xmlDoc=xmlhttp.responseXML;
        return xmlDoc;
    }
    else
    {
        xhttp.open("GET",tileFileName,false);
        xhttp.send();
        xmlDoc=xhttp.responseXML;
        return xmlDoc;
    }
}
/***
 *This function should return an array of attributes from a node
 *@TODO it should return an associative array
 */
function xmlParser2Array(xmlDoc, containerTag)
{
    var output= new Array();
    var rawData = xmlDoc.getElementsByTagName(containerTag)[0];
    var tiles=rawData.children;
    var attributes;
    var i=0;
    var j=0;
    /*********
     * This loop goes through a childs/tiles node adding its attributes to an array
     */
    for(i=0;i<tiles.length;i++)
    {      
        attributes= new Array();

        for(j=0;j<tiles[i].attributes.length;j++)
        {
            attributes.push(new Array(tiles[i].attributes[j].name,tiles[i].attributes[j].nodeValue));
        }
        output.push(attributes);
    }
    return output;
}

