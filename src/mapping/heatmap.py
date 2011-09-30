import infpy.gp as gp 
import mapsettings
import geodataIO

def updateheatmap():
    """
    Read all the recorded samples of plant disease, calculate the interpolated 
    density across a grid and save the results.
    """
    p = mapsettings.getparams()
    
    # observed data: X is lon/lat coordinates, D is level of disease
    [X,D_cmd,D_cgm,D_cbs] = geodataIO.getpointdata()

    # construct a Gaussian process model given the observed data
    kernel = gp.SquaredExponentialKernel(p['theta'],dimensions=2) + gp.noise_kernel(p['sigma'])
    G = gp.GaussianProcess(X, D_cmd, kernel)
    # produce the heatmap tiles
    geodataIO.savetiles(G,'cmd_incidence')

    # construct a Gaussian process model given the observed data
    kernel = gp.SquaredExponentialKernel(p['theta'],dimensions=2) + gp.noise_kernel(p['sigma'])
    G = gp.GaussianProcess(X, D_cgm, kernel)
    # produce the heatmap tiles
    geodataIO.savetiles(G,'cgm_incidence')
    

    # construct a Gaussian process model given the observed data
    kernel = gp.SquaredExponentialKernel(p['theta'],dimensions=2) + gp.noise_kernel(p['sigma'])
    G = gp.GaussianProcess(X, D_cbs, kernel)
    # produce the heatmap tiles
    geodataIO.savetiles(G,'cbs_incidence')

if __name__=='__main__':
    updateheatmap()
