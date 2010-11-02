def getparams():
    '''
    Set the extent and resolution of the points where density is to be calculated.
    '''
    settings = {}

    # the edges of the map
    settings['longitude_limit'] = [32., 33.]
    settings['latitude_limit'] = [.5, 1.5]

    # how many points to calculate density for in each axis
    settings['mapwidth'] = 60
    settings['mapheight'] = 60

    # how to scale inverse variance to an alpha value
    settings['alphascaling'] = 10

    # Gaussian process hyperpriors (length scales - the higher the number,
    # the smoother the map)
    settings['theta'] = [.5, .5]

    # filename for calculated density points
    settings['filename'] = 'heatmappoints.csv'

    return settings
