def getparams():
    '''
    Set the extent and resolution of the points where density is to be calculated.
    '''
    settings = {}

    # the edges of the map
    settings['longitude_limit'] = [32., 33.]
    settings['latitude_limit'] = [0., 1.]

    # how many tiles to generate (this is effectively the zoom level)
    settings['ntiles'] = [3, 3]

    # how many pixels per tile
    settings['tilepixels'] = [50,50]

    # how to scale inverse variance to an alpha value
    settings['alphascaling'] = 8 

    # Gaussian process hyperpriors (length scales - the higher the number,
    # the smoother the map)
    settings['theta'] = [.5, .5]

    # filename for calculated density points
    settings['filename'] = 'heatmappoints.csv'

    # maximum severity possible
    settings['maxseverity'] = 5
    return settings
