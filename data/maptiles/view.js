/**
*This Script is will be used for displaying the maps
**/

function load()
{
    var pointCenter = new GLatLng(0.4955556,32.5899586);
    var map = new GMap2(document.getElementById("map"));
    map.addControl(new GSmallMapControl());
    map.addControl(new GMapTypeControl());
    map.setCenter(pointCenter, 13);
    
    var counter=0;

    for(counter=0;counter<tileArray.length;counter++)
    {
        var lon_ul=tileArray[counter][0][1];
        var lat_ul=tileArray[counter][1][1];

        var lon_lr=tileArray[counter][2][1];
        var lat_lr=tileArray[counter][3][1];

        var image=tileArray[counter][4][1];

        var pointSW = new GLatLng(lat_lr,lon_ul);
        var pointNE = new GLatLng(lat_ul,lon_lr);

        map.addOverlay(new GGroundOverlay(image, new GLatLngBounds(pointSW, pointNE)));

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
