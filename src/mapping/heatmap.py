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
    [X,D] = geodataIO.getpointdata()

    # construct a Gaussian process model given the observed data
    kernel = gp.SquaredExponentialKernel(p['theta'],dimensions=2)
    G = gp.GaussianProcess(X, D, kernel)

    # produce the heatmap tiles
    geodataIO.savetiles(G)

if __name__=='__main__':
    updateheatmap()
