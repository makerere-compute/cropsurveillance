import mapsettings
import numpy
from PIL import Image
import datetime
import webrequests
import database
import pickle

def getpointdata():
    """
    Return a list of samples which have been made. 
    Samples should have longitude, latitude, disease class, disease rate and timestamp. 
    """
    p = mapsettings.getparams()

    if p['use_cached_geodata']:
        pkl_file = open(p['geodata_cache_filename'])
        geodata = pickle.load(pkl_file)
        pkl_file.close()
    else:
        geodata=webrequests.fetchdata()

    # X is a list of coordinates [longitude. latitude]
    X=geodata['lonlat']

    # D are the corresponding disease rates (e.g. 0-5)
    D=geodata['D']

    '''
    X = numpy.array([[ 32.63669052,   0.51917757],
       [ 32.63670452,   0.51908315],
       [ 32.63676197,   0.51908591],
       [ 32.63673679,   0.51907999],
       [ 32.636732  ,   0.519077  ],
       [ 32.63674421,   0.51906578],
       [ 32.63675059,   0.519048  ],
       [ 32.63674697,   0.51905069],
       [ 32.63672599,   0.51906909],
       [ 32.63653416,   0.51928972]]) 
    D = numpy.array([4, 5, 5, 3, 3, 2, 1, 1, 1, 1])
    '''

    return X,D

def intensity_to_rgb(x,upperlim):
    '''
    Given an intensity x in the range 0 to upperlim, convert this to an RGB value.
    '''
    x = max(0,x)
    x = min(upperlim,x)
    r = 255*(x/upperlim)
    g = 0
    b = 255*(1. - x/upperlim)
    return r,g,b

def savetiles(G):
    """
    Save a list of disease densities. 

    Input: GP, a Gaussian process or other density modelling object which can be queried
    for particular longitudes and latitudes.
    """

    xml_string = []
    now = datetime.datetime.now().isoformat()
    xml_string.append('<tilelist timegenerated="%s">\n' % (now))

    p = mapsettings.getparams()

    # how many tiles cover the map, and at what resolution
    ntiles = p['ntiles']
    tilepixels = p['tilepixels']

    # coordinates of upper left and lower right corners of map
    map_lon_ul = min(p['longitude_limit'])
    map_lat_ul = max(p['latitude_limit'])
    map_lon_lr = max(p['longitude_limit'])
    map_lat_lr = min(p['latitude_limit'])

    # extent of each tile, and each pixel
    tile_longitude = numpy.diff(p['longitude_limit'])/ntiles[1]
    tile_latitude = numpy.diff(p['latitude_limit'])/ntiles[0]
    pixel_longitude = tile_longitude/(tilepixels[1]-1) # sub 1 so tiles overlap by 1px
    pixel_latitude = tile_longitude/(tilepixels[0]-1)

    # transparency control
    alphascaling = p['alphascaling']

    for y in range(ntiles[0]):
        for x in range(ntiles[1]):
            # coordinates of upper left and lower right corners of this tile
            tile_lon_ul = map_lon_ul + x*tile_longitude
            tile_lat_ul = map_lat_lr + (y+1)*tile_longitude
            tile_lon_lr = map_lon_ul + (x+1)*tile_longitude
            tile_lat_lr = map_lat_lr + y*tile_longitude

            # create the matrix of pixel values and transparencies
            tileimg = numpy.zeros((tilepixels[0],tilepixels[1],4),numpy.uint8)
            for ypix in range(tilepixels[0]):
                for xpix in range(tilepixels[1]):
                    lon = tile_lon_ul + xpix*pixel_longitude
                    lat = tile_lat_lr + ypix*pixel_latitude
                    x_star = numpy.array([[float(lon), float(lat)]])
                    mu,S,ll = G.predict(x_star)
                    r,g,b = intensity_to_rgb(mu,p['maxseverity'])
                    S = max(0,float(S))
                    alpha = p['alphascaling'] * 1/(numpy.log(1+S) + 10**-10)
                    alpha = min(alpha,255)
                    tileimg[ypix,xpix,0] = r
                    tileimg[ypix,xpix,1] = g
                    tileimg[ypix,xpix,2] = b
                    tileimg[ypix,xpix,3] = alpha

            # save as an image
            pilImage = Image.fromarray(tileimg)
            if p['save_tile_image_files']:
                filename = '%s/tile_%d_%d.png' % (p['tile_directory'],x,y)
                pilImage.save(filename)

            #save the tiles tp database          
            if p['save_tile_images_in_db']:
                database.savetile(0, tile_lon_ul[0], tile_lat_ul[0], tile_lon_lr[0],
                                  tile_lat_lr[0], pilImage);
            
            # create XML element with tile details
            xml_element = '<tile lon_ul="%f" lat_ul="%f" lon_lr="%f" lat_lr="%f" filename="%s" />\n' % (tile_lon_ul, tile_lat_ul, tile_lon_lr, tile_lat_lr, filename)
            xml_string.append(xml_element)

    #we have finished saving the tiles so lets close the connection to the database
    if p['save_tile_images_in_db']:
        database.closeConnection()

    xml_string.append('</tilelist>\n')

    xml_file = file('%s/%s' % (p['tile_directory'], p['tile_metadata_filename']),'w')
    for s in xml_string:
        xml_file.write(s)
    xml_file.close()
