% Given an image of a leaf on a near-white background, calculate
% a histogram of leaf hue distribution.

im = imresize(im,RESIZE_FACTOR,'method','nearest');
[height,width,c] = size(im);

% Convert to hue-saturation-value
imhsv = rgb2hsv(im);
hue = imhsv(:,:,1);
intensity = imhsv(:,:,3);

% Reshape to image vectors
hue = reshape(hue,height*width,1);
intensity = reshape(intensity,height*width,1);

% Find pixels where the leaf is present, based on intensity
leaf_pixels = intensity < LEAF_INTENSITY_THRESHOLD;
hue_leaf = hue(leaf_pixels);

switch COLOUR_SPACE
    case 'hsv'
        leafhist = histc(hue_leaf,hist_edges);
        
    case 'lab'     
        % Convert to L*a*b* colour space
        imlab = applycform(im,colourtransform);
        
        a = reshape(imlab(:,:,2),width*height,1);
        b = reshape(imlab(:,:,3),width*height,1);
        
        leafhist = [histc(a,hist_edges); histc(b,hist_edges)];
        
    otherwise 
        error('unknown colour space.')
end

leafhist = leafhist / sum(leafhist);
