/**
*This Script is will be used for displaying the maps
**/

function load()
{
	var map = new GMap2(document.getElementById("map"));
    map.addControl(new GLargeMapControl());
    map.addControl(new GMapTypeControl());
    map.setCenter(new GLatLng(0.4955556,32.5899586),13);

    
    var counter=0;

    for(counter=0;counter<tileArray.length;counter++)
    {
        var lon_ul=tileArray[counter][0][1];
        var lat_ul=tileArray[counter][1][1];

        var lon_lr=tileArray[counter][2][1];
        var lat_lr=tileArray[counter][3][1];

        var image=tileArray[counter][4][1];

        var pointSW = new GLatLng(lat_lr,lon_lr);
        var pointNE = new GLatLng(lat_ul,lon_ul);
        
    	
    	
        var sw = new GLatLng(lat_lr,lon_ul);
        var ne = new GLatLng(lat_ul,lon_lr);
        var bounds=new GLatLngBounds(sw,ne);
        var overlay = EInsert.groundOverlay(image,bounds);
        map.addOverlay(overlay);

       // map.addOverlay(new GGroundOverlay(image, new GLatLngBounds(pointSW, pointNE)));

    }
    GEvent.addListener(map, "mousemove", function(latlng) {
        document.getElementById('div_coordinates').innerHTML =
        'Div Coordinates: ' +
        map.fromLatLngToContainerPixel(latlng).x + ', ' +
        map.fromLatLngToContainerPixel(latlng).y;

 
        document.getElementById('geo_coordinates').innerHTML =
        'Geographical Coordinates: ' + latlng.lat() + ', ' + latlng.lng();

   

    });


             
}
