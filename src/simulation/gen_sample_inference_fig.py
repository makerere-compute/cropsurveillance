from simulatedisease import *
import numpy.random
import matplotlib.pylab
from optimise_stops import *

if __name__=='__main__':
    save_figures = True

    numpy.random.seed(2)
    field_size = [100, 100]
    num_groups = 2
    num_stops = 20

    origin = np.array([50,50])
    opts = {}
    opts['longitude_limit'] = (0.,99.)
    opts['latitude_limit'] = (0.,99.)
    opts['mapwidth'] = 100
    opts['mapheight'] = 100
    opts['nsamples_per_stop'] = 10 
    opts['num_groups'] = 3
    opts['total_survey_vertices'] = 30
    opts['initial_samples'] = 5
    opts['nsets_of_points'] = 1
    opts['stops_per_group'] = 10 
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

    '''
    # do the different types of surveys
    Preg,Mreg,Sreg,Xreg,Dreg,survey_locations_by_group_reg = do_regular_survey(copy.deepcopy(routes),Preal,P,M,S,X,D,opts)

    # plot the sampled mean disease density (true incidence)
    pylab.matshow(Preal[:,:,1])
    pylab.hot()
    ax = pylab.gca()
    ax.set_xticks([])
    ax.set_yticks([])
    pylab.title(r'$I(x)$')
    if save_figures:
        f2 = pylab.gcf()
        f2.set_figwidth(3.5)
        f2.set_figheight(3.5)
        pylab.savefig('sampled_mean_density.pdf', bbox_inches='tight')


    # plot the sampled trajectories, and inference
    f1 = pylab.figure()
    pylab.matshow(Preg[:,:,1],fignum=0)
    path_colours = ['b','b','b']
    path_linestyles = ['-','-','-']
    for g in range(opts['num_groups']):
        pylab.plot(routes[g][:,1],routes[g][:,0],c=path_colours[g],ls=path_linestyles[g],linewidth=2)
        for k in range(opts['stops_per_group']):
            pylab.plot(survey_locations_by_group_reg[g][k][1],survey_locations_by_group_reg[g][k][0],
                ls='None',marker='o',color=path_colours[g],markersize=10,markeredgewidth=2)

    pylab.hot()
    ax = pylab.gca()
    ax.set_xlim(0,100)
    ax.set_ylim(100,0)
    ax.set_xticks([])
    ax.set_yticks([])
    pylab.title(r'$P(y=d_2|x,\mathcal{D})$' )

    if save_figures:
        f1.set_figwidth(3.5)
        f1.set_figheight(3.5)
        pylab.savefig('sampled_trajectories.pdf', bbox_inches='tight')
    '''
    numpy.random.seed(8) # 7 - crosses over
    routes = simulate_routes([opts['mapheight'],opts['mapwidth']], opts['num_groups'], opts['total_survey_vertices'])
    # do the different types of surveys
    opts['longitude_limit'] = (0.,99.)
    opts['latitude_limit'] = (0.,99.)
    opts['mapwidth'] = 100
    opts['mapheight'] = 100
    opts['nsamples_per_stop'] = 1 
    opts['num_groups'] = 1
    opts['total_survey_vertices'] = 30
    opts['initial_samples'] = 5
    opts['nsets_of_points'] = 1
    opts['stops_per_group'] = 8 
    opts['num_disease_categories'] = 2

    Popt,Mopt,Sopt,Xopt,Dopt,survey_locations_by_group_opt = do_optimised_survey(copy.deepcopy(routes),Preal,P,M,S,X,D,opts)
    Preg,Mreg,Sreg,Xreg,Dreg,survey_locations_by_group_reg = do_regular_survey(copy.deepcopy(routes),Preal,P,M,S,X,D,opts)
    plot_routes_and_stops(routes,survey_locations_by_group_reg,survey_locations_by_group_opt,opts)
    pylab.ion()
    pylab.show()
