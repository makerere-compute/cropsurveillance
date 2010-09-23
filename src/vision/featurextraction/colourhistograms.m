% Read images of leaves taken with a near-white background and calculate hue
% histograms for each class.

healthydir = [DATADIR '/cassava/namulonge/healthy';
cmddir = [DATADIR '/cassava/namulonge/mosaic';

RESIZE_FACTOR = .25;
COLOUR_SPACE = 'hsv';
NUM_HIST_BINS = 50;
LEAF_INTENSITY_THRESHOLD = .6;
HUE_UPPER_CUTOFF = .45;
HUE_LOWER_CUTOFF = .15;

healthyimagefiles = dir([healthydir '/*.jpg']);
nhealthyimages = length(healthyimagefiles);
cmdimagefiles = dir([cmddir '/*.jpg']);
ncmdimages = length(cmdimagefiles);

switch COLOUR_SPACE
    case 'hsv'
        hist_edges = HUE_LOWER_CUTOFF:(HUE_UPPER_CUTOFF - HUE_LOWER_CUTOFF)/(NUM_HIST_BINS-1):HUE_UPPER_CUTOFF;
    case 'lab'
        hist_edges = 0:255/(.5*NUM_HIST_BINS-1):255;
end

if COLOUR_SPACE=='lab'
    colourtransform = makecform('srgb2lab');
end

healthy_hist = zeros(nhealthyimages,NUM_HIST_BINS);
cmd_hist = zeros(ncmdimages,NUM_HIST_BINS);

for i=1:nhealthyimages
    im = imread([healthydir '/' healthyimagefiles(i).name]);
    leafcolourhistogram
    healthy_hist(i,:) = leafhist;
    plot(leafhist)
    drawnow;   
    disp([num2str(i) '/' num2str(nhealthyimages)]);
end

for i=1:ncmdimages
    im = imread([cmddir '/' cmdimagefiles(i).name]);
    leafcolourhistogram
    cmd_hist(i,:) = leafhist;
    plot(leafhist)
    drawnow;
    disp([num2str(i) '/' num2str(ncmdimages)]);
end
