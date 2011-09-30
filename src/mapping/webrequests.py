#this script is for handling saving and retrieving data from the database
#date created 16/Nov/2010
#mistaguy

import urllib2
import numpy
import mapsettings
import pickle

def fetchdata():
    p = mapsettings.getparams()
    geodata={}
    #development URL
    #url = "http://127.0.0.1:8888/formSubmissionsV?odkId=new_form1"
    #online URL
    url = "http://cropmonitoring.appspot.com/formSubmissionsV?odkId=new_form2"
    #make url request and read content
    response = urllib2.urlopen(url)
    result = response.read()
    #The content got is CVS lets parse the CVS into rows               
    rows=result.split('\n');               
    #go through each row to extract the column  
     
    # lonlat is a list of coordinates [longitude. latitude]
    lonlat = numpy.zeros(shape=(len(rows)-1,2))
    #D are the corresponding disease rates (e.g. 0-5)
    D_cmd = numpy.zeros(len(rows)-1,dtype=int)
    D_cbs = numpy.zeros(len(rows)-1,dtype=int)
    D_cgm = numpy.zeros(len(rows)-1,dtype=int)
    
    for i in range(1,len(rows)-1):                     
        #split the rows into columns 
        columns=rows[i].split(",");
        
        #got through each column/cell
        
        #date
        day=str(columns[0])
        month=str((columns[1]).split(' ')[1])
        date=str((columns[1]).split(' ')[2])                    
        year=str((columns[2]).split(' ')[1])
        
        #timestamp
        time=str((columns[2]).split(' ')[2])
        timeOfDay=str((columns[2]).split(' ')[3])
        timezone=str((columns[2]).split(' ')[4])
        
        #image
        imageurl=columns[3];
        #gps                   
        latitude=columns[9];
        longitude=columns[10];
        altitude=columns[11];
        accurracy=columns[12]
        '''
        rate=0
       
        #remove under score from rate
        if(columns[9].split('_')[1])>0:
                rate=(columns[9].split('_')[1])   
        '''

        #disease
        D_cmd[i-1]=int(columns[6])
        D_cgm[i-1]=int(columns[7])
        D_cbs[i-1]=int(columns[8])
        lonlat[i-1]=numpy.array([longitude,latitude])
        
    #print D[0]             
    #self.response.out.write(rate);      
    geodata['lonlat']=numpy.array(lonlat)
    geodata['D_cmd']=numpy.array(D_cmd)
    geodata['D_cgm']=numpy.array(D_cgm)
    geodata['D_cbs']=numpy.array(D_cbs)

    # cache the data
    pkl_file = open(p['geodata_cache_filename'],'wb')
    pickle.dump(geodata,pkl_file)
    pkl_file.close()

    return geodata

#this function will save data to appengine
def savedata():
    return 1
