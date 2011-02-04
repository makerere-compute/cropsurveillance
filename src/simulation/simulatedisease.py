import numpy
import scipy
import scipy.signal
import pylab

def gauss_kern(size, sizey=None):
    """ Returns a normalized 2D gauss kernel array for convolutions """
    size = int(size)
    if not sizey:
        sizey = size
    else:
        sizey = int(sizey)
    x, y = scipy.mgrid[-size:size+1, -sizey:sizey+1]
    g = scipy.exp(-(x**2/float(size)+y**2/float(sizey)))
    return g / g.sum()

if __name__=='__main__':
    filter_size = 100
    field_size = [300, 300]
    
    # sample the mean disease level - start with uniform noise
    noise = numpy.random.rand(field_size[0], field_size[1])
    # filter out high frequency components to leave smooth function
    M = scipy.signal.convolve2d(noise, gauss_kern(filter_size), mode='valid')
    # scale the matrix to the right range
    M = 4*(M-0.47)/(0.55-0.47) + 1
    
    # sample the spread amongst disease levels
    noise = numpy.random.rand(field_size[0], field_size[1])
    # filter out high frequency components to leave smooth function
    S = scipy.signal.convolve2d(noise, gauss_kern(filter_size), mode='valid')
    # scale the matrix to the right range
    S = 10*(S-0.47)/(0.55-0.47) + .5  
    
    # calculate the densities across disease levels at each pixel
    for y in range(field_size[0]):
        for x in range(field_size[1]):
            for d in range(1,6):

    pylab.matshow(M)
