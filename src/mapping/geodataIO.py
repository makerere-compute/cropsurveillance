import mapsettings
import csv
import numpy

def getpointdata():
    """
    Return a list of samples which have been made. 
    Samples should have longitude, latitude, disease class, disease rate and timestamp. 
    """
    # TODO: currently hard coded test data, should pick this up from the DB.
    # TODO: add time of sample.
    # TODO: add disease class of sample.

    # X is a list of coordinates [longitude. latitude]
    X = numpy.array([[32.1, .6], [32.2, .5], [32.8,.3], [32.7,.9],
                    [32.6,.9], [32.9,.1]])

    # D are the corresponding disease rates (e.g. 0-5)
    D = numpy.array([2, 3, 0, 2, 1, 1])

    return X,D

def savetiles(GP):
    """
    Save a list of disease densities. 

    Input: GP, a Gaussian process or other density modelling object which can be queried
    for particular longitudes and latitudes.
    """
    p = mapsettings
    pixel_longitude = p['pixel_longitude']
    pixel_latitude = p['pixel_latitude']

    for x in range(0,mapwidth):
        for y in range(0,mapheight):
            lon = pixel_longitude[x]
            lat = pixel_latitude[y]
            x_star = numpy.array([[lon, lat]])
            mu,S,ll = G.predict(x_star)
            M[y][x] = mu
            alpha[y][x] = 1/(S + 10**-10)

    outfile = open(filename, 'wb')

    for x in range(0,mu.shape[0]):
        for y in range(0,mu.shape[1]):
            line = '%.6f, %.6f, %.6f, %.6f\n' % (pixel_longitude[x], 
                pixel_latitude[y], mu[y][x], alpha[y][x])
            outfile.write(line)

    outfile.close()

