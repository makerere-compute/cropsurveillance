from gpor import *
import numpy as np
import numpy.random
from simulatedisease import *
import os.path
import copy
import logging

def euc_dist(a,b):
    ''' Euclidean distance between two vectors. '''
    delta = a-b
    dist = np.sqrt(np.dot(delta,delta))
    return dist

def kl_div(a,b):
    ''' KL divergence between two vectors '''
    return sum(np.multiply(a,np.log(a/b)))

def loss(P,Preal,weighted=True):
    '''
    Calculate the loss of GP estimate given by mean M and variance S, compared
    with actual underlying disease distribution given by X.
    initially take e.g. earth mover's distance between GP mean and reality.
    '''
    dist = 0
    tiny = 1e-4
    for y in range(P.shape[0]):
        for x in range(P.shape[1]):
            dist += euc_dist(Preal[y,x,:],P[y,x,:])
            if weighted:
                dist *= Preal[y,x,1]
            #dist += kl_div(Preal[y,x,:]+tiny,P[y,x,:]+tiny)
    return dist

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
    samples = samples + 1 # multinomial fn is 0-indexed
    return np.array([samples])

def all_points_on_routes(routes):
    ''' Return an Nx2 vector with all the points on any route '''
    X = numpy.array(numpy.round(routes[0][0,:]))
    for r in range(len(routes)):
        route = routes[r]
        for i in range(len(route)-1):
            # divide this segment into points to sample
            segment_vector = route[i+1,:] - route[i,:]
            segment_distance = euc_dist(route[i+1,:], route[i,:])
            npoints = int(segment_distance)
            delta = segment_vector/npoints
            for p in range(npoints):
                current_point = route[i,:] + p*delta
                X = numpy.vstack((X,numpy.round(current_point)))

    return X


def most_informative_stop(route,P, M,S,opts,weighted):
    ''' What is the point along the route (adjacent line segments) which is most informative
    or utility-maximising to stop at. Could take the highest variance along the path. Does this
    properly account for uncertainty at all points on the map?
    Return: position (2d vector), dist_from_origin.
    '''
    '''
    This is one way to do it if the field is not uniform. Need a better way
    to choose an arbitrary location if all places are equal though.
    '''
    best_point_so_far = []
    best_score_so_far = [-1e10]
    dist_of_best_point_so_far = []
    tolerance = 1e-5
    value = 0
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
            if weighted:
                v = v * (P[int(current_point[0]),int(current_point[1]),1] + 0.1)**1
            if v > best_score_so_far[0]-tolerance:
                # if indistinguishable from the other points
                if best_score_so_far[0]-v>tolerance:
                    best_score_so_far.append(v)
                    best_point_so_far.append(current_point)
                    dist_of_best_point_so_far.append(dist_to_current_segment + p)
                # otherwise a new best point
                else:
                    best_score_so_far = [v]
                    best_point_so_far = [current_point]
                    dist_of_best_point_so_far = [dist_to_current_segment + p]


        dist_to_current_segment += segment_distance
        
    # random tie breaker if many best points
    if len(best_score_so_far)>1:
        i =int(numpy.floor(numpy.random.rand()*len(best_score_so_far)))
    else:
        i = 0

    return best_point_so_far[i], dist_of_best_point_so_far[i]

def sample_set_of_stop_points(route,P,M,S,k,X,D,opts,weighted):
    ''' Find a set of k points along route, which are maximally informative
    according to sampled data. 
    Return: array of distances from origin
    '''
    stop_distances = []
    stop_positions = []
    routes = {}
    routes[0] = route
    for stop_num in range(k):
        #pylab.matshow(S)
        
        # find the next place to stop according to current estimate
        x, stop_distance = most_informative_stop(route,P,M,S,opts,weighted)
        stop_distances.append(stop_distance)
        stop_positions.append(x)

        # sample data from model at this point
        Dnew = take_model_samples(P,M,S,x,opts['nsamples_per_stop'])

        # recalculate the GP posterior
        D = np.hstack((D,Dnew))
        X = np.vstack((X,np.tile(x,(opts['nsamples_per_stop'],1))))
        P, M, S = gpor_2D_grid(X, D, opts['longitude_limit'], opts['latitude_limit'], opts['mapwidth'], opts['mapheight'],all_points_on_routes(routes))

    return np.array(stop_distances), np.array(stop_positions)

def find_next_stop_point(route,P,M,S,k,X,D,opts,weighted):
    ''' 
    Find the next point by sampling a number of sets of points, then taking the mean
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
        new_sample_distances, new_sample_positions = sample_set_of_stop_points(route,P,M,S,k,X,D,opts,weighted)
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
    
def do_optimised_survey(routes,Preal,P,M,S,X,D,opts,weighted=True):
    # if specified, carry out a first tour, stopping at regular intervals
    if opts['stops_on_first_tour']>0:
        opts2 = copy.copy(opts)
        opts2['stops_per_group'] = opts2['stops_on_first_tour']
        P,M,S,X,D,survey_locations_by_group = do_regular_survey(copy.deepcopy(routes),Preal,P,M,S,X,D,opts2)
    else: 
        for g in range(opts['num_groups']):
            survey_locations_by_group = {}
            survey_locations_by_group[g] = []

    # carry out the optimised tour, adapting the next location at each step
    for k in range(opts['stops_per_group'] - opts['stops_on_first_tour'],0,-1):
        print('%d stops to go' % (k))
        for g in range(opts['num_groups']):
            print('group %d. %d observations' % (g,X.shape[0]))
            
            # calculate the next point on this route
            next_point, remaining_route = find_next_stop_point(routes[g],P,M,S,k,X,D,opts,weighted)
            routes[g] = remaining_route
            survey_locations_by_group[g].append(next_point)
            
            # take samples at this location
            Dnew = take_real_samples(Preal,next_point,opts['nsamples_per_stop'])
            D = np.hstack((D,Dnew))
            X = np.vstack((X,np.tile(next_point,(opts['nsamples_per_stop'],1))))  
            
            # update the model
            P, M, S = gpor_2D_grid(X, D, opts['longitude_limit'], opts['latitude_limit'], opts['mapwidth'], opts['mapheight'],all_points_on_routes(routes))

    # last model update across entire grid
    P, M, S = gpor_2D_grid(X, D, opts['longitude_limit'], opts['latitude_limit'], opts['mapwidth'], opts['mapheight'])
    return P,M,S,X,D,survey_locations_by_group
    
def position_along_route(route,dist):
    ''' Where route is a list of vertices joined by line segments, start from
    the beginning and travel dist units. Return the coordinates of the destination. '''
    remaining_dist = dist

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
            
    return current_point
    
def do_regular_survey(routes,Preal,P,M,S,X,D,opts):
    # for each route find the total distance
    route_distances = np.zeros(opts['num_groups'])
    route_intervals = np.zeros(opts['num_groups'])
    for g in range(opts['num_groups']):
        for v in range(len(routes[g])-1):
            route_distances[g] += euc_dist(routes[g][v,:],routes[g][v+1,:])
        route_intervals[g] = route_distances[g]/(opts['stops_per_group']+1)
        
    # find the positions evenly distributed along these routes
    survey_locations_by_group = {}  
    for g in range(opts['num_groups']):
        survey_locations_by_group[g] = [] 
    for g in range(opts['num_groups']):
        for stop in range(opts['stops_per_group']):
            dist = route_intervals[g]*(stop+1)
            next_stop = position_along_route(routes[g],dist)
            survey_locations_by_group[g].append(next_stop)
            
    # draw samples at each of these positions
    for g in range(opts['num_groups']):
        for stop in range(opts['stops_per_group']):
            Dnew = take_real_samples(Preal,survey_locations_by_group[g][stop],opts['nsamples_per_stop'])
            D = np.hstack((D,Dnew))
            X = np.vstack((X,np.tile(survey_locations_by_group[g][stop],(opts['nsamples_per_stop'],1))))  

    # update the model
    P, M, S = gpor_2D_grid(X, D, opts['longitude_limit'], opts['latitude_limit'], opts['mapwidth'], opts['mapheight'])

    return P,M,S,X,D,survey_locations_by_group

def compare_survey_types(routes,X,D,Preal,opts):
    ''' Save scores for the two types of surveys. Need routes '''
    
    # initial GP estimate given measurements at origin
    P, M, S = gpor_2D_grid(X, D, opts['longitude_limit'], opts['latitude_limit'], opts['mapwidth'], opts['mapheight'])

    # visualise selection of next point
    Popt,Mopt,Sopt,Xopt,Dopt,survey_locations_by_group_opt = do_optimised_survey(copy.deepcopy(routes),Preal,P,M,S,X,D,opts)
    Preg,Mreg,Sreg,Xreg,Dreg,survey_locations_by_group_reg = do_regular_survey(copy.deepcopy(routes),Preal,P,M,S,X,D,opts)

    # scores for both of the surveys
    score_reg = utility(Preg,Preal)
    score_opt = utility(Popt,Preal)  
    
    return score_reg, score_opt
      
def compare_multiple_surveys():
    
    logging.basicConfig(filename='survey_simulation.log',level=logging.INFO)
    logger = logging.getLogger(os.uname()[1])
    
    origin = np.array([50,50])
    opts = {}
    opts['longitude_limit'] = (0.,99.)
    opts['latitude_limit'] = (0.,99.)
    opts['mapwidth'] = 100
    opts['mapheight'] = 100
    opts['nsamples_per_stop'] = 5
    opts['num_groups'] = 3
    opts['total_survey_vertices'] = 30
    opts['initial_samples'] = 5
    opts['nsets_of_points'] = 2
    opts['num_disease_categories'] = 2

    # simulate a new survey
    Preal,tmp = simulate_disease([opts['mapheight'],opts['mapwidth']])
    routes = simulate_routes([opts['mapheight'],opts['mapwidth']], opts['num_groups'], opts['total_survey_vertices'])

    # sample data at the origin
    D = take_real_samples(Preal,origin,opts['initial_samples'])
    
    # if any categories are missing from the sample, add them here
    missing_categories = np.array([np.setdiff1d(np.arange(1,opts['num_disease_categories']+1),D[0,:])])
    D = np.hstack((D,missing_categories))
    X = np.tile(origin,(opts['initial_samples']+len(missing_categories[0]),1))

    # initial GP estimate given measurements at origin
    P, M, S = gpor_2D_grid(X, D, opts['longitude_limit'], opts['latitude_limit'], opts['mapwidth'], opts['mapheight'])

    for stops in [2]: #range(1,11):    
        opts['stops_per_group'] = stops
        score_reg, score_opt = compare_survey_types(routes,X,D,Preal,opts)
        logger.info('%.2f, %.2f, %d' % (score_reg, score_opt, stops))

def plot_routes_and_stops(routes,survey_locations_by_group_reg,survey_locations_by_group_opt,opts):
    ''' Show the routes with markers indicating survey sites. Compare regular and optimised survey
    side by side.  '''

    # plot the results - regular survey
    path_colours = ['r','g','b']
    path_linestyles = ['-','--','-.']
    pylab.figure()
    for g in range(opts['num_groups']):
        pylab.plot(routes[g][:,1],routes[g][:,0],c=path_colours[g],ls=path_linestyles[g],linewidth=2)
        for k in range(opts['stops_per_group']):
            pylab.plot(survey_locations_by_group_reg[g][k][1],survey_locations_by_group_reg[g][k][0],
                ls='None',marker='o',color=path_colours[g],markersize=10,markeredgewidth=2)
    pylab.title('Regular')

    # plot the results - optimised survey
    pylab.figure()
    for g in range(opts['num_groups']):
        pylab.plot(routes[g][:,1],routes[g][:,0],c=path_colours[g],ls=path_linestyles[g],linewidth=2)
        for k in range(opts['stops_per_group']):
            pylab.plot(survey_locations_by_group_opt[g][k][1],survey_locations_by_group_opt[g][k][0],
                ls='None',marker='o',color=path_colours[g],markersize=10,markeredgewidth=2)
    pylab.title('Optimised')

def compare_placement():
    numpy.random.seed(8)
    logging.basicConfig(filename='survey_simulation.log',level=logging.INFO)
    logger = logging.getLogger(os.uname()[1])
    
    origin = np.array([50,50])
    opts = {}
    opts['longitude_limit'] = (0.,99.)
    opts['latitude_limit'] = (0.,99.)
    opts['mapwidth'] = 100
    opts['mapheight'] = 100
    opts['nsamples_per_stop'] = 5
    opts['num_groups'] = 3
    opts['total_survey_vertices'] = 30
    opts['initial_samples'] = 5
    opts['nsets_of_points'] = 1
    opts['stops_per_group'] = 6

    # simulate a new survey
    Preal,tmp = simulate_disease([opts['mapheight'],opts['mapwidth']])
    routes = simulate_routes([opts['mapheight'],opts['mapwidth']], opts['num_groups'], opts['total_survey_vertices'])

    # sample data at the origin
    D = take_real_samples(Preal,origin,opts['initial_samples'])
    
    # if any categories are missing from the sample, add them here
    missing_categories = np.array([np.setdiff1d(np.arange(1,opts['num_disease_categories']+1),D[0,:])])
    D = np.hstack((D,missing_categories))
    X = np.tile(origin,(opts['initial_samples']+len(missing_categories[0]),1))

    # initial GP estimate given measurements at origin
    P, M, S = gpor_2D_grid(X, D, opts['longitude_limit'], opts['latitude_limit'], opts['mapwidth'], opts['mapheight'])

    # do the different types of surveys
    Popt,Mopt,Sopt,Xopt,Dopt,survey_locations_by_group_opt = do_optimised_survey(copy.deepcopy(routes),Preal,P,M,S,X,D,opts)
    Preg,Mreg,Sreg,Xreg,Dreg,survey_locations_by_group_reg = do_regular_survey(copy.deepcopy(routes),Preal,P,M,S,X,D,opts)

    plot_routes_and_stops(routes,survey_locations_by_group_reg,survey_locations_by_group_opt, opts)

if __name__=='__main__':

    # For one specific case, look at the positioning of survey sites
    compare_placement()

    # Compare utility of a number of surveys
    #compare_multiple_surveys()

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
    '''
    pylab.gca().set_xlim((0,100))
    pylab.gca().set_ylim((0,100))
    pylab.gray()
    '''
