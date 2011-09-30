def getparams():
    '''
    Set the extent and resolution of the points where density is to be calculated.
    '''
    settings = {}

    # the edges of the map - currently all in Namulonge
    settings['longitude_limit'] = [32.631, 32.642]
    settings['latitude_limit'] = [0.514, 0.524]

    # how many tiles to generate (this is effectively the zoom level)
    settings['ntiles'] = [1, 1]

    # how many pixels per tile
    settings['tilepixels'] = [50,50]

    # how to scale inverse variance to an alpha value
    settings['alphascaling'] = 10 

    # Gaussian process hyperpriors (theta is the length scales - the higher
    # the number, the smoother the map. Sigma is observation noise.)
    settings['theta'] = [.003, .003]
    settings['sigma'] = [.3]

    # filename for calculated density points
    settings['filename'] = 'heatmappoints.csv'

    # maximum severity possible
    settings['maxseverity'] = 5

    # whether to used cached geodata
    settings['use_cached_geodata'] = True
    settings['geodata_cache_filename'] = '../../data/geo/geodata.pkl'

    # whether to save tile image files
    settings['save_tile_image_files'] = True

    # whether to save tile images as blobs in a database
    settings['save_tile_images_in_db'] = False

    # directory to write map tile images to
    settings['tile_directory'] = '../../data/maptiles'

    # filename for storing tile details (lat/lon extents)
    settings['tile_metadata_filename'] = 'tiles'

    return settings
