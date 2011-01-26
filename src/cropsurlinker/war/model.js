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
  
    tileArray= xmlParser2Array(loadXml(null, "http://localhost:8888/serveapps"),"tilelist");

}

