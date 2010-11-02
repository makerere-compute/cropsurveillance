import numpy
import infpy.gp as gp 
import pylab
import mapsettings
import geodataIO

def updateheatmap():
    """
    Read all the recorded samples of plant disease, calculate the interpolated 
    density across a grid and save the results.
    """
    p = mapsettings.getparams()
    mapheight = p['mapheight']
    mapwidth = p['mapwidth']
    
    # observed data: X is lon/lat coordinates, D is level of disease
    [X,D] = geodataIO.getpointdata()
    # geographical locations of pixels
    pixel_longitude = numpy.arange(min(p['longitude_limit']),
                        max(p['longitude_limit']),
                            numpy.diff(p['longitude_limit'])/p['mapwidth'])
    pixel_latitude = numpy.arange(min(p['latitude_limit']),
                        max(p['latitude_limit']),
                            numpy.diff(p['latitude_limit'])/p['mapheight'])
    
    # calculate the heat map
    kernel = gp.SquaredExponentialKernel(p['theta'],dimensions=2)
    G = gp.GaussianProcess(X, D, kernel)
    M = numpy.zeros([mapheight, mapwidth])
    alpha = numpy.zeros([mapheight, mapwidth])
    
    for x in range(0,mapwidth):
        for y in range(0,mapheight):
            lon = pixel_longitude[x]
            lat = pixel_latitude[y]
            x_star = numpy.array([[lon, lat]])
            mu,S,ll = G.predict(x_star)
            M[y][x] = mu
            alpha[y][x] = 1/(S + 10**-10)

    # for the moment, linear scaling for alpha
    
    
    if not p['filename']==None:
        geodataIO.writedensities(M, alpha, pixel_longitude, 
                                 pixel_latitude, p['filename'])

    return M,alpha

if __name__=='__main__':
    M,alpha = updateheatmap()
    pylab.matshow(M)
    pylab.show()
