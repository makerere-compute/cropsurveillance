import numpy
import infpy.gp as gp 
import pylab

# Gaussian process hyperpriors (length scales - the higher the number, the smoother the map)
theta = [.5, .5]

# observed data: x is lon/lat coordinates, y is level of disease
X = numpy.array([[32.1, .6], [32.2, .5], [32.8,.3], [32.7,.9], [32.6,.9], [32.9,.1]])
Y = numpy.array([2.5, 2., 0., 0., 1., 1.5])

# heat map extent, resolution, and geographical locations of pixels
longitude_limit = [32., 33.]
latitude_limit = [.5, 1.5]
mapwidth, mapheight = 30, 30
pixel_longitude = numpy.arange(min(longitude_limit),max(longitude_limit),numpy.diff(longitude_limit)/mapwidth)
pixel_latitude = numpy.arange(min(latitude_limit),max(latitude_limit),numpy.diff(latitude_limit)/mapheight)

# calculate the heat map
kernel = gp.SquaredExponentialKernel(theta,dimensions=2)
G = gp.GaussianProcess(X, Y, kernel)
M = numpy.zeros([mapheight, mapwidth])

for x in range(0,mapwidth):
    for y in range(0,mapheight):
        lon = pixel_longitude[x]
        lat = pixel_latitude[y]
        x_star = numpy.array([[lon, lat]])
        mu,S,ll = G.predict(x_star)
        M[y][x] = mu

# plot predictions
pylab.matshow(M)
pylab.show()
