from gpor import *
import numpy as np
import numpy.random
from simulatedisease import *
import os.path
import copy

def utility(M,S,X):
    '''
    Calculate the utility of GP estimate given by mean M and variance S, compared
    with actual underlying disease distribution given by X.
    initially take e.g. KL divergence between GP and reality.
    '''
    return None

def take_real_samples(Preal,x,n):
    ''' Take n samples from the actual disease distribution at position x
    Return: samples (n x 2 matrix)
    '''  
    samples = np.nonzero(numpy.random.multinomial(1,Preal[x[0],x[1],:],(n,1)))[2]
    samples = samples + 1
    return np.array([samples])

def take_model_samples(P, M,S,x,n):
    ''' Take n samples at position x given mean matrix M and variance S 
    Return: samples (n x 2 matrix)
    '''  
    # Sample from GP, or from mean histogram?
    # Should sample values one at a time from the GP...
    # What does the confidence value mean?
    # Should be sampling underlying function and then calculating likelihoods.
    samples =  np.nonzero(numpy.random.multinomial(1,P[x[0],x[1],:],(n,1)))[2] 
    samples = samples + 1
    return np.array([samples])


def euc_dist(a,b):
    ''' Euclidean distance between two vectors. '''
    delta = a-b
    dist = np.sqrt(np.dot(delta,delta))
    return dist

def most_informative_stop(route,P, M,S,opts):
    ''' What is the point along the route (adjacent line segments) which is most informative
    or utility-maximising to stop at. Could take the highest variance along the path. Does this
    properly account for uncertainty at all points on the map?
    Return: position (2d vector), dist_from_origin.
    '''
    '''
    This is one way to do it if the field is not uniform. Need a better way
    to choose an arbitrary location if all places are equal though.
    '''
    best_point_so_far = None
    best_score_so_far = 0
    value = 0
    dist_of_best_point_so_far = 0
    dist_to_current_segment = 0
    for i in range(len(route)-1):
        # divide this segment into points to sample
        segment_vector = route[i+1,:] - route[i,:]
        segment_distance = euc_dist(route[i+1,:], route[i,:])
        npoints = int(segment_distance)
        delta = segment_vector/npoints
        for p in range(npoints):
            current_point = route[i,:] + p*delta
            v = S[int(current_point[0]),int(current_point[1])]
            if v > best_score_so_far:
                best_score_so_far = v
                best_point_so_far = current_point
                dist_of_best_point_so_far = dist_to_current_segment + p
        dist_to_current_segment += segment_distance
        
    return best_point_so_far, dist_of_best_point_so_far

def sample_set_of_stop_points(route,P,M,S,k,X,D,opts):
    ''' Find a set of k points along route, which are maximally informative
    according to sampled data. 
    Return: array of distances from origin
    '''
    stop_distances = []
    stop_positions = []
    for stop_num in range(k):
        #pylab.matshow(S)
        
        # find the next place to stop according to current estimate
        x, stop_distance = most_informative_stop(route,P,M,S,opts)
        stop_distances.append(stop_distance)
        stop_positions.append(x)

        # sample data from model at this point
        Dnew = take_model_samples(P,M,S,x,opts['nsamples_per_stop'])

        # recalculate the GP posterior
        D = np.hstack((D,Dnew))
        X = np.vstack((X,np.tile(x,(opts['nsamples_per_stop'],1))))
        P, M, S = gpor_2D_grid(X, D, opts['longitude_limit'], opts['latitude_limit'], opts['mapwidth'], opts['mapheight'])

    return np.array(stop_distances), np.array(stop_positions)

def find_next_stop_point(route,P,M,S,k,X,D,opts):
    ''' Find the next point by sampling a number of sets of points, then taking the mean
    of the closest point.
    ARGUMENTS
    route: list of vertices on the route, from current position to destination
    M: mean of GP posterior
    S: variance of GP posterior
    k: number of stops still to be made 
    RETURN
    x, 2d position vector
    remaining_route 
    '''
    nsets_of_points = opts['nsets_of_points']
    stop_point_samples = []
    for i in range(nsets_of_points):
        new_sample_distances, new_sample_positions = sample_set_of_stop_points(route,P,M,S,k,X,D,opts)
        stop_point_samples.append(min(new_sample_distances))

    next_point_dist = np.mean(np.array(stop_point_samples))

    # find the coordinates corresponding to this distance along the current path
    remaining_dist = next_point_dist

    current_point = route[0,:]
    i = 0
    while remaining_dist>0:
        delta = route[i,:] - route[i+1,:]
        dist_to_next_point = np.sqrt(np.dot(delta,delta))
        if dist_to_next_point < remaining_dist:
            remaining_dist -= dist_to_next_point
            current_point = route[i+1,:]
            i += 1
        else:
            v = route[i+1,:] - route[i,:]
            v = v/np.sqrt(np.dot(v,v))
            current_point = route[i,:] + remaining_dist*v
            remaining_dist = 0

    remaining_route = np.vstack((current_point,route[i+1:,:]))

    return current_point, remaining_route
    
def do_optimised_survey(routes,P,M,S,X,D,opts):
    survey_locations_by_group = {}  
    for g in range(opts['num_groups']):
        survey_locations_by_group[g] = []
        
    for k in range(opts['stops_per_group'],0,-1):
        print('%d stops to go' % (k))
        for g in range(opts['num_groups']):
            print('group %d. %d observations' % (g,X.shape[0]))
            
            # calculate the next point on this route
            next_point, remaining_route = find_next_stop_point(routes[g],P,M,S,k,X,D,opts)
            routes[g] = remaining_route
            survey_locations_by_group[g].append(next_point)
            
            # take samples at this location
            Dnew = take_real_samples(Preal,next_point,opts['nsamples_per_stop'])
            D = np.hstack((D,Dnew))
            X = np.vstack((X,np.tile(next_point,(opts['nsamples_per_stop'],1))))  
            
            # update the model
            P, M, S = gpor_2D_grid(X, D, opts['longitude_limit'], opts['latitude_limit'], opts['mapwidth'], opts['mapheight'])

    return P,M,S,X,D,survey_locations_by_group

def do_regular_survey():
    # for each route find the total distance
    
    # find the positions evenly distributed along these routes
    
    # draw samples at each of these positions
    
    # update the model
    
    return P,M,S,X,D,survey_locations_by_group

def sample_set_of_stops(routes,P,M,S,k,X,D,opts):
    ''' Get a set of points to stop at, performing . '''
    ngroups = len(routes)

if __name__=='__main__':
    num_samples_per_stop = 10

    # set up the grid
    # Some example data points
    origin = np.array([50,50])

    opts = {}
    opts['longitude_limit'] = (0.,99.)
    opts['latitude_limit'] = (0.,99.)
    opts['mapwidth'] = 100
    opts['mapheight'] = 100
    opts['nsamples_per_stop'] = 4
    opts['num_groups'] = 3
    opts['total_survey_vertices'] = 30
    opts['stops_per_group'] = 3
    opts['initial_samples'] = 5
    opts['nsets_of_points'] = 2

    # simulate a new survey
    Preal,tmp = simulate_disease([opts['mapheight'],opts['mapwidth']])
    routes = simulate_routes([opts['mapheight'],opts['mapwidth']], opts['num_groups'], opts['total_survey_vertices'])

    # sample data at the origin
    D = take_real_samples(Preal,origin,opts['initial_samples'])
    # if any categories are missing from the sample, add them here
    missing_categories = np.array([np.setdiff1d(np.array([1,2,3,4,5]),D[0,:])])
    D = np.hstack((D,missing_categories))
    X = np.tile(origin,(opts['initial_samples']+len(missing_categories[0]),1))
    # add some noise to prevent numerical problems
    X = X + numpy.random.rand(X.shape[0],X.shape[1])

    # initial GP estimate given measurements at origin
    P, M, S = gpor_2D_grid(X, D, opts['longitude_limit'], opts['latitude_limit'], opts['mapwidth'], opts['mapheight'])

    pylab.matshow(S)

    path_colours = ['r','g','b']
    path_linestyles = ['-','--','-.']
    
    # visualise selection of next point
    P,M,S,X,D,survey_locations_by_group = do_optimised_survey(copy.deepcopy(routes),P,M,S,X,D,opts)
    pylab.matshow(S) 
    
    for g in range(opts['num_groups']):
        pylab.plot(routes[g][:,1],routes[g][:,0],c=path_colours[g],ls=path_linestyles[g],linewidth=2)
        for k in range(opts['stops_per_group']):
            pylab.plot(survey_locations_by_group[g][k][1],survey_locations_by_group[g][k][0],ls='None',marker='o',color=path_colours[g],markersize=10,markeredgewidth=2)            
 
    '''
    # visualise the selection of a number of points
    for g in range(opts['num_groups']):
        pylab.plot(routes[g][:,1],routes[g][:,0],c=path_colours[g],ls=path_linestyles[g],linewidth=2)
        stop_distances, stop_positions = sample_set_of_stop_points(routes[g],P,M,S,opts['stops_per_group'],X,D,opts)
        pylab.plot(stop_positions[:,1],stop_positions[:,0],ls='None',marker='o',color=path_colours[g],markersize=10,markeredgewidth=2)
    '''
    
    '''
    # visualise the effect of choosing the most informative point 
    for g in range(opts['num_groups']-2):
        pylab.plot(routes[g][:,1],routes[g][:,0],c=path_colours[g],ls=path_linestyles[g],linewidth=2)
        x, remaining_dist = most_informative_stop(routes[g],P,M,S,opts)
        pylab.plot(x[1],x[0],'w+',markeredgewidth=5)
    '''
    
    pylab.gca().set_xlim((0,100))
    pylab.gca().set_ylim((0,100))
    pylab.gray()
