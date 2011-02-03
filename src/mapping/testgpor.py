'''
Sample script for the generation of disease heatmaps using
Gaussian process ordinal regression.

Requires the GPOR code:
http://www.gatsby.ucl.ac.uk/~chuwei/code/gpor.tar
to be unpacked to this directory:
cropsurveillance/3rdparty/gpor/
'''


import pylab
import subprocess
import numpy
import csv
import os

def write_gpor_array(N,filename):
    '''
    Create a space delimited file with one sample per line.
    '''
    writer = csv.writer(open(filename, 'wb'), delimiter=' ',
                        quotechar='|', quoting=csv.QUOTE_MINIMAL)
    N[:,0:2] = N[:,0:2]*10000 % 10
    for i in range(N.shape[0]):
        writer.writerow(N[i][:])

    return None

def read_gpor_array(filename):
    '''
    Read a space delimited array from file.
    '''
    return None

if __name__=='__main__':
    # Generate some data points
    X = numpy.array([[ 32.63669052,   0.51917757],
       [ 32.63670452,   0.51908315],
       [ 32.63676197,   0.51908591],
       [ 32.63673679,   0.51907999],
       [ 32.636732  ,   0.519077  ],
       [ 32.63674421,   0.51906578],
       [ 32.63675059,   0.519048  ],
       [ 32.63674697,   0.51905069],
       [ 32.63672599,   0.51906909],
       [ 32.63663416,   0.51921972], 
       [ 32.63653416,   0.51928972],
       [ 32.63653416,   0.51910972], 
       [ 32.63653416,   0.51915972]]) 
    D = numpy.array([[3, 2, 2, 2, 2, 2, 1, 1, 1, 4, 5, 3, 3]])
    
    # Read the results

    # Plot results
    longitude_limit = [32.6365, 32.6368]
    latitude_limit = [.519, .5193]
    mapwidth, mapheight = 100, 100

    pixel_longitude = numpy.arange(min(longitude_limit),max(longitude_limit),numpy.diff(longitude_limit)/mapwidth)
    pixel_latitude = numpy.arange(min(latitude_limit),max(latitude_limit),numpy.diff(latitude_limit)/mapheight)

    # Output formatted data file - train data
    trainfile = 'cassava_gpor_train.0'
    trainmat = numpy.hstack((X,D.transpose()))
    order = D.argsort()
    trainmat = trainmat[order[0]][:]
    write_gpor_array(trainmat,trainfile)

    # Output formatted data file - test data
    testfile = 'cassava_gpor_test.0'
    testmat = numpy.zeros([mapheight*mapwidth,2])
    for x in range(0,mapwidth):
        for y in range(0,mapheight):
            lon = pixel_longitude[x]
            lat = pixel_latitude[y]
            testmat[x+mapwidth*y][0] = lon
            testmat[x+mapwidth*y][1] = lat
    write_gpor_array(testmat,testfile)

    # Call the GP ordinal regression
    p = subprocess.call(['../../3rdparty/gpor/gpor/gpor', '-G', 'cassava_gpor_train.0'])

    # Read in the predictions from gpor
    reader_prob = csv.reader(open("cassava_gpor_test.0.prob", "rb"), delimiter=' ')
    reader_conf = csv.reader(open("cassava_gpor_test.0.conf", "rb"), delimiter=' ')

    M = numpy.zeros([mapheight, mapwidth])
    S = numpy.zeros([mapheight, mapwidth])

    for y in range(0,mapheight):
        for x in range(0,mapwidth):
            row = reader_prob.next()

            # distribution over categories
            prob = numpy.array([float(val) for val in row[0:5]])
            # mode
            pred = prob.argmax() + 1
            # mean
            mean = numpy.dot(prob,numpy.array([1,2,3,4,5]).transpose())

            row_conf = reader_conf.next()
            conf = float(row_conf[0])

            M[y][x] = mean
            S[y][x] = conf



    # Plot predictions and inputs
    
    ax1 = pylab.subplot(121)
    pylab.matshow(M,fignum=0)
    
    for i in range(D.shape[1]):
        x = (X[i][0] - longitude_limit[0])/(numpy.diff(longitude_limit)/mapwidth)
        y = (X[i][1] - latitude_limit[0])/(numpy.diff(latitude_limit)/mapheight)
        bbox_props = dict(boxstyle="square", fc="w", ec="w", alpha=0.5)
        s = '%d' % (int(D[0][i]))
        pylab.text(x,y,s,bbox=bbox_props)

    pylab.title('Mean predicted category')
    
    ax1.xaxis.set_major_locator(pylab.NullLocator())
    ax1.yaxis.set_major_locator(pylab.NullLocator())

    '''
    ax2 = pylab.subplot(122)
    
    pylab.matshow(S,fignum=0)
    pylab.colorbar()
    pylab.title('Variance of prediction')

    for i in range(D.shape[1]):
        x = (X[i][0] - longitude_limit[0])/(numpy.diff(longitude_limit)/mapwidth)
        y = (X[i][1] - latitude_limit[0])/(numpy.diff(latitude_limit)/mapheight)
        pylab.scatter(x,y,edgecolor=[[1,1,1]],marker='+')


    ax2.xaxis.set_major_locator(pylab.NullLocator())
    ax2.yaxis.set_major_locator(pylab.NullLocator())
    '''
