import numpy
import scipy
import scipy.signal
import scipy.stats
import pylab
import pickle
import tsp
import os.path

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

def simulate_disease(field_size):
    ''' Return D, underlying distribution of disease at each point
    and M, modal disease rate at each point. '''
    pickle_filename = 'simulationobjects.pkl'
    filter_size = 100
    field_size[0] += 2*filter_size
    field_size[1] += 2*filter_size

    use_stored_matrices = True 
    if use_stored_matrices and os.path.isfile(pickle_filename):
        pkl_file = open('simulationobjects.pkl', 'rb')
        M = pickle.load(pkl_file)
        S = pickle.load(pkl_file)
        D = pickle.load(pkl_file)
        pkl_file.close()
    else:
        # sample the mean disease level - start with uniform noise
        noise = numpy.random.rand(field_size[0], field_size[1])
        # filter out high frequency components to leave smooth function
        M = scipy.signal.convolve2d(noise, gauss_kern(filter_size), mode='valid')
        # scale the matrix to the right range
        M = 4*(M-0.47)/(0.55-0.47) + 1
        
        # sample the spread amongst disease levels
        noise = numpy.random.rand(field_size[0], field_size[1])
        S = scipy.signal.convolve2d(noise, gauss_kern(filter_size), mode='valid')
        S = 3*(S-0.47)/(0.55-0.47) + .5

        # calculate the densities across disease levels at each pixel
        D = numpy.zeros((M.shape[0],M.shape[1],5))
        for y in range(M.shape[0]):
            for x in range(M.shape[1]):
                for d in range(5):
                    D[y,x,d] = scipy.stats.norm.pdf(d+1,loc=M[y,x],scale=S[y,x])
                D[y,x,:] = D[y,x,:]/sum(D[y,x,:])

        # store the results for quick execution later 
        output = open(pickle_filename, 'wb')
        pickle.dump(M,output)
        pickle.dump(S,output)
        pickle.dump(D,output)
        output.close()

    return D,M

def simulate_routes(field_size,num_groups,num_stops):
    ''' Return a dict routes, containing a list of coordinates for a survey tour for each group. '''

    found_valid_route = False
    while not found_valid_route:
        # first choose random points across the fields
        all_stops = numpy.random.rand(num_stops,2)
        all_stops[:,0] = all_stops[:,0]*field_size[0] 
        all_stops[:,1] = all_stops[:,1]*field_size[1] 
        centroids = numpy.random.rand(num_groups,2)
        centroids[:,0] = centroids[:,0]*field_size[0] 
        centroids[:,1] = centroids[:,1]*field_size[1] 
    
        # split the points up into three groups, according to nearest among three points.
        # (split the field up according to a voronoi diagram)
        stops_in_group = {}
        origin = numpy.array([field_size[0]/2, field_size[1]/2])
        for g in range(num_groups):
            stops_in_group[g] = [origin]
    
        for s in range(num_stops):
            dists = numpy.zeros(num_groups)
            for g in range(num_groups):
                delta = all_stops[s,:] - centroids[g,:]
                dists[g] = numpy.dot(delta,delta)
            stops_in_group[dists.argmin()].append(all_stops[s,:])
    
        # order the points in each group by solving a small TSP
        routes = {}
        for g in range(num_groups):
            route_indices = numpy.array(tsp.tsp(stops_in_group[g]))
            routes[g] = numpy.array(stops_in_group[g])
            start_index = numpy.nonzero(route_indices==0)[0][0]
            indices_in_order = route_indices[start_index:len(route_indices)]
            indices_in_order = numpy.append(indices_in_order,route_indices[0:start_index+1])
            routes[g] = routes[g][indices_in_order,:]

        # check that all routes are non-trivial
        found_valid_route = True
        for g in range(num_groups):
            if len(routes[g][:,0])<4:
                found_valid_route = False

    return routes

if __name__=='__main__':

    save_figures = True

    field_size = [100, 100]
    num_groups = 3
    num_stops = 30

    D,M = simulate_disease(field_size)
    routes = simulate_routes(field_size, num_groups, num_stops)

    # plot the sampled trajectories
    f1 = pylab.figure()
    path_colours = ['r','g','b']
    path_linestyles = ['-','--','-.']
    for g in range(num_groups):
        pylab.plot(routes[g][:,1],routes[g][:,0],c=path_colours[g],ls=path_linestyles[g],linewidth=2)
    ax = pylab.gca()
    ax.set_xticks([])
    ax.set_yticks([])

    if save_figures:
        f1.set_figwidth(4)
        f1.set_figheight(4)
        pylab.savefig('sampled_trajectories.png',bbox_inches='tight')

    # plot the sampled mean disease density
    pylab.matshow(M)
    pylab.colorbar()
    ax = pylab.gca()
    ax.set_xticks([])
    ax.set_yticks([])

    if save_figures:
        f2 = pylab.gcf()
        f2.set_figwidth(8)
        f2.set_figheight(4)
        pylab.savefig('sampled_mean_density.png',bbox_inches='tight')

