% This script writes histograms in Weka ARFF format

of = fopen('cassava.arff','w');

fprintf(of,'@relation cassava \n\n');


for i=1:NUM_HIST_BINS
    fprintf(of,['@attribute bin' num2str(i) ' real \n']);
end

fprintf(of,['@attribute class {healthy, mosaic} \n\n']);
fprintf(of,'@data \n');

for i=1:size(healthy_hist,1)
    for j=1:NUM_HIST_BINS
        fprintf(of,[num2str(healthy_hist(i,j)) ', ']);
    end
    fprintf(of,'healthy \n');
end

for i=1:size(cmd_hist,1)
    for j=1:NUM_HIST_BINS
        fprintf(of,[num2str(cmd_hist(i,j)) ', ']);
    end
    fprintf(of,'mosaic \n');
end


fclose(of);
