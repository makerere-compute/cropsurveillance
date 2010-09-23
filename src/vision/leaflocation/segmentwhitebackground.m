% For images of leaves taken on a lightbox with a white background,
% this code segments the foreground and background based on hue.

healthydir = [DATADIR '/cassava/namulonge/healthy'];
mosaicdir = [DATADIR '/cassava/namulonge/mosaic'];

healthyimagefiles = dir([healthydir '/*.jpg']);
nhealthyimages = length(healthyimagefiles);
mosaicimagefiles = dir([mosaicdir '/*.jpg']);
nmosaicimages = length(mosaicimagefiles);

LEAF_INTENSITY_THRESHOLD = .6;
RESIZE_FACTOR = .25;
HUE_RANGE_1 = [.15 .25; .25 .35; .35 .45];
nfilters = size(HUE_RANGE_1,1);
se = strel('disk',5);
 
for i=11:nhealthyimages
    im = imread([healthydir '/' healthyimagefiles(i).name]);
    im = imresize(im,RESIZE_FACTOR);  
    filteredim = cell(nfilters,1);
    imhsv = rgb2hsv(im);
    imintensity = imhsv(:,:,3);
    subplot(1,nfilters+1,1);
    imagesc(im);
    for j=1:nfilters
        pixels = imintensity < LEAF_INTENSITY_THRESHOLD;
        pixels = pixels .* (imhsv(:,:,1) > HUE_RANGE_1(j,1));
        pixels = pixels .* (imhsv(:,:,1) <= HUE_RANGE_1(j,2));
        pixelsop = imopen(pixels,se);
        subplot(1,nfilters+1,j+1);
        filteredim{j} = pixelsop;
        imagesc(imintensity .* pixelsop);
        colormap gray
    end
    pause
end

for i=11:nmosaicimages
    im = imread([mosaicdir '/' mosaicimagefiles(i).name]);
    im = imresize(im,RESIZE_FACTOR);  
    filteredim = cell(nfilters,1);
    imhsv = rgb2hsv(im);
    imintensity = imhsv(:,:,3);
    subplot(1,nfilters+1,1);
    imagesc(im);
    for j=1:nfilters
        pixels = imintensity < LEAF_INTENSITY_THRESHOLD;
        pixels = pixels .* (imhsv(:,:,1) > HUE_RANGE_1(j,1));
        pixels = pixels .* (imhsv(:,:,1) <= HUE_RANGE_1(j,2));
        subplot(1,nfilters+1,j+1);
        filteredim{j} = pixels;
        imagesc(imintensity .* pixels);
        colormap gray
    end
    pause
end

