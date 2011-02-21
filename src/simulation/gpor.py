'''
Generation of disease heatmaps using Gaussian process ordinal regression.

Requires the GPOR code:
http://www.gatsby.ucl.ac.uk/~chuwei/code/gpor.tar
to be unpacked to this directory:
cropsurveillance/3rdparty/gpor/

If compiling gpor with command in readme gives buffer overflow at execution
time, disabling compiler optimisation is a workaround:
gcc -o gpor *.c -lm -O0 -Wall
'''

import pylab
import subprocess
import numpy
import csv
import os
import glob

def write_gpor_array(N,filename):
    '''
    Create a space delimited file with one sample per line.
    '''
    writer = csv.writer(open(filename, 'wb'), delimiter=' ',
                        quotechar='|', quoting=csv.QUOTE_MINIMAL)
    for i in range(N.shape[0]):
        writer.writerow(N[i][:])

    return None

def read_gpor_array(filename):
    '''
    Read a space delimited array from file.
    '''
    return None



def gpor_2D_grid(X, D, x1_limit, x2_limit, grid_width, grid_height, Xtest=[]):
    '''
    Given observations, calculate mean and variance of posterior GP with 2D inputs.
    Return evaluations for the cross-product of values in the list test_x1 and
    test_x2, that is on the grid defined by these two lists.
    X are the training inputs (2D)
    D are the training labels (ordinal categories)

    Return (M,S) the mean and variance matrices corresponding to this grid.
    '''
    randkey = '%d_' % (int(numpy.random.rand()*(10**8)))
    gpor_executable_location = '../../3rdparty/gpor/gpor'
    pixel_longitude = numpy.arange(min(x1_limit),max(x1_limit),numpy.diff(x1_limit)/grid_width)
    pixel_latitude = numpy.arange(min(x2_limit),max(x2_limit),numpy.diff(x2_limit)/grid_height)

    # Output formatted data file - train data
    trainfile = randkey + 'cassava_gpor_train.0'
    trainmat = numpy.hstack((X,D.transpose()))
    order = D.argsort()
    trainmat = trainmat[order[0]][:]
    write_gpor_array(trainmat,trainfile)

    # Output formatted data file - test data
    testfile = randkey + 'cassava_gpor_test.0'
    if len(Xtest)==0:
        testmat = numpy.zeros([grid_height*grid_width,2])
        for x in range(0,grid_width):
            for y in range(0,grid_height):
                lon = pixel_longitude[x]
                lat = pixel_latitude[y]
                testmat[x+grid_width*y][0] = lon
                testmat[x+grid_width*y][1] = lat
    else:
        testmat = Xtest

    write_gpor_array(testmat,testfile)

    # Call the GP ordinal regression    
    #p = subprocess.call([gpor_executable_location, '-G', 'cassava_gpor_train.0'])

    # this version suppresses the on-screen output from gpor
    p = subprocess.call([gpor_executable_location, '-G', '-K', '0.03', '-S', '10', randkey + 'cassava_gpor_train.0'], stdout=open("/dev/null", "w"))

    # Read in the predictions from gpor
    reader_prob = csv.reader(open(randkey + "cassava_gpor_test.0.prob", "rb"), delimiter=' ')
    reader_conf = csv.reader(open(randkey + "cassava_gpor_test.0.conf", "rb"), delimiter=' ')

    num_categories = 2
    P = numpy.zeros([grid_height, grid_width, num_categories])
    M = numpy.zeros([grid_height, grid_width])
    S = numpy.zeros([grid_height, grid_width])

    if len(Xtest)==0:
        for x in range(0,grid_width):
            for y in range(0,grid_height):
                row = reader_prob.next()
    
                # distribution over categories
                # there may not be all ordinal categories
                prob = numpy.array([float(val) for val in row[0:-1]])
                if len(prob)<num_categories:
                    prob = numpy.append(prob,numpy.zeros(num_categories-len(prob)))
                # mode
                pred = prob.argmax() + 1
                # mean
                mean = numpy.dot(prob,numpy.arange(1,num_categories+1))
    
                row_conf = reader_conf.next()
                conf = float(row_conf[0])
    
                M[y,x] = mean
                S[y,x] = conf
                P[y,x,:] = prob
    else:
        for i in range(Xtest.shape[0]):
            row = reader_prob.next()
            y = Xtest[i,0]
            x = Xtest[i,1]

            prob = numpy.array([float(val) for val in row[0:-1]])
            if len(prob)<num_categories:
                prob = numpy.append(prob,numpy.zeros(num_categories-len(prob)))
            # mode
            pred = prob.argmax() + 1
            # mean
            mean = numpy.dot(prob,numpy.arange(1,num_categories+1))

            row_conf = reader_conf.next()
            conf = float(row_conf[0])

            M[y,x] = mean
            S[y,x] = conf
            P[y,x,:] = prob

    # tidy up the filespace
    for tempfile in glob.glob(randkey+'*'):
        os.remove(tempfile)

    return P, M, S


if __name__=='__main__':
    ''' Show an example ordinal regression, plotting the mean predicted value on a grid. '''

    # Some example data points
    X = numpy.array([[ 20., 30.],
       [17., 92.], [18., 20.]]) 
    D = numpy.array([[1, 2, 1]])
    
    # Calculate the predicted values across a grid 
    longitude_limit = (0.,99.)
    latitude_limit = (0.,99.)
    mapwidth, mapheight = 100, 100
    P, M, S = gpor_2D_grid(X, D, longitude_limit, latitude_limit, mapwidth, mapheight)

    # Plot predictions and inputs
    pylab.matshow(M)
    
    for i in range(D.shape[1]):
        x = (X[i][1] - longitude_limit[0])/(numpy.diff(longitude_limit)/mapwidth)
        y = (X[i][0] - latitude_limit[0])/(numpy.diff(latitude_limit)/mapheight)
        bbox_props = dict(boxstyle="square", fc="w", ec="w", alpha=0.5)
        s = '%d' % (int(D[0][i]))
        pylab.text(x,y,s,bbox=bbox_props)

    pylab.title('Mean predicted category')
