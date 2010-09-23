% Read in the SURF descriptors which have been calculated for all leaf images

healthydir = [DATADIR '/cassava/namulonge/surf/healthy';
mosaicdir = [DATADIR '/cassava/namulonge/surf/mosaic';

healthydescriptorfiles = dir([healthydir '/*.surf']);
nhealthydescriptorsets = length(healthyimagefiles);
mosaicdescriptorfiles= dir([cmddir '/*.surf']);
nmosaicdescriptorsets = length(cmdimagefiles);

healthy_descriptors = cell(nhealthydescriptorsets,1);
mosaic_descriptors = cell(nmosaicdescriptorsets,1);

for i=1:nhealthydescriptorsets
    K = dlmread([healthydir '/' healthydescriptorfiles(i).name]);
    des = [];
    if size(K,1)>0
        des = K(:,7:end);
    end
    healthy_descriptors{i} = des;
end

for i=1:nmosaicdescriptorsets
    K = dlmread([mosaicdir '/' mosaicdescriptorfiles(i).name]);
    des = [];
    if size(K,1)>0
        des = K(:,7:end);
    end
    mosaic_descriptors{i} = des;
end
