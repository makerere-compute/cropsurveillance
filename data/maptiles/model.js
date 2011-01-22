/***
 *@Author mistaguy
 *This class contains the inputs of the geopricing component
 *
 **/


/***
 * The Map model 
 */
var tileArray;

function geomap(){
    xmlDoc=loadXml(null, "tiles.xml");
    tileArray= xmlParser2Array(xmlDoc,"tilelist");
}

