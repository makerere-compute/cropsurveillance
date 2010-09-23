% Read in the SIFT descriptors which have been calculated for all leaf images

healthydir = [DATADIR '/cassava/namulonge/sift/healthy'];
mosaicdir = [DATADIR '/cassava/namulonge/sift/mosaic'];

healthydescriptorfiles = dir([healthydir '/*.sift']);
nhealthydescriptorsets = length(healthydescriptorfiles);
mosaicdescriptorfiles= dir([mosaicdir '/*.sift']);
nmosaicdescriptorsets = length(mosaicdescriptorfiles);

healthy_descriptors = cell(nhealthydescriptorsets,1);
mosaic_descriptors = cell(nmosaicdescriptorsets,1);

for i=1:nhealthydescriptorsets
    K = dlmread([healthydir '/' healthydescriptorfiles(i).name]);
    ndescriptors = size(K,1)/8;
    des = zeros(ndescriptors,128);
    if ndescriptors>0
        for d=1:ndescriptors
            startrow = (d-1)*8 + 2;
            sgldes = [K(startrow,:) K(startrow+1,:) K(startrow+2,:) K(startrow+3,:) K(startrow+4,:) K(startrow+5,:) K(startrow+6,1:8)];
            des(d,:) = sgldes;
        end
    end
    healthy_descriptors{i} = des;
end

for i=1:nmosaicdescriptorsets
    K = dlmread([mosaicdir '/' mosaicdescriptorfiles(i).name]);
    ndescriptors = size(K,1)/8;
    des = zeros(ndescriptors,128);
    if ndescriptors>0
        for d=1:ndescriptors
            startrow = (d-1)*8 + 2;
            sgldes = [K(startrow,:) K(startrow+1,:) K(startrow+2,:) K(startrow+3,:) K(startrow+4,:) K(startrow+5,:) K(startrow+6,1:8)];
            des(d,:) = sgldes;
        end
    end
    mosaic_descriptors{i} = des;
end

