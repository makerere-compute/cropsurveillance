import glob
import os
import detectwhitefly
import trainhistograms
from lxml import etree
import copy

def getboundingboxes(imgfile):
    annofile = imgfile[:-3] + 'xml'
    annofileexists = os.path.exists(annofile)
    boundingboxes = []

    if (annofileexists):
        # extract the bounding boxes from xml
        tree = etree.parse(annofile)
        r = tree.xpath('//bndbox')

        if (len(r) != 0):
            for i in range(len(r)):
                xmin = round(float(r[i].xpath('xmin')[0].text))
                xmin = max(xmin,1)
                xmax = round(float(r[i].xpath('xmax')[0].text))
                ymin = round(float(r[i].xpath('ymin')[0].text))
                ymin = max(ymin,1)
                ymax = round(float(r[i].xpath('ymax')[0].text))
                xmin, xmax, ymin, ymax = int(xmin), int(xmax), int(ymin), int(ymax)

                boundingboxes.append((xmin,xmax,ymin,ymax))
                    
    return boundingboxes

            
if __name__=='__main__':
    histograms = trainhistograms.trainhistograms(True)    
    showfiles = False
    test_data_dir = '../../../data/whitefly/test/good/'
    testfiles = glob.glob(test_data_dir + '*.jpg')   
    TP = FP = FN = 0
    if showfiles:
        cv.NamedWindow("result", 1)  
        
    for testfile in testfiles:
        print testfile

        im = cv.LoadImageM(testfile)          
        matchingcoords = detectwhitefly.detect(im,histograms)
        boundingboxes = getboundingboxes(testfile)
        '''
        for match in matchingcoords:
            xcentre = match[0]
            ycentre = match[1]
            cv.Circle( im, (xcentre,ycentre), 10, [0,0,255] )
        
        for bb in boundingboxes:
            cv.Rectangle(im,(bb[0],bb[2]),(bb[1],bb[3]), [255, 255, 255], 1)
        '''
        
        TPim = FPim = FNim = 0
        
        # test the positives
        tmpbb = copy.deepcopy(boundingboxes)  
        for match in matchingcoords:
            xcentre = match[0]
            ycentre = match[1]
            matched = False
            for bb in tmpbb:
                if (xcentre>bb[0] and xcentre<bb[1] and ycentre>bb[2] and ycentre<bb[3]):
                    TPim+=1
                    tmpbb.remove(bb)
                    cv.Circle( im, (xcentre,ycentre), 10, [255,0,0] )
                    matched = True
                    break
            if not matched:
                FPim+=1
                cv.Circle( im, (xcentre,ycentre), 10, [0,0,255] )
                
        for bb in tmpbb:
            FNim+=1
            cv.Rectangle(im,(bb[0],bb[2]),(bb[1],bb[3]), [255, 255, 255], 1)
        
        TP += TPim
        FP += FPim
        FN += FNim
        
        if len(matchingcoords)>0:
            cv.PutText(im, 'TP=%d, FP=%d, FN=%d' % (TPim,FPim,FNim), (11,21), cv.InitFont(cv.CV_FONT_HERSHEY_SIMPLEX,0.5,0.5), (0,0,0))
            cv.PutText(im, 'TP=%d, FP=%d, FN=%d' % (TPim,FPim,FNim), (11,21), cv.InitFont(cv.CV_FONT_HERSHEY_SIMPLEX,0.5,0.5), (255,255,255))

        if showfiles:
            cv.ShowImage("result", im)

            if cv.WaitKey(0) == 27:
                break
            
    if showfiles:
        cv.DestroyWindow("result")    
        
    print('TP=%d, FP=%d, FN=%d' % (TP,FP,FN))
    print('Precision=%.3f, Recall=%.3f' % ((TP*1.0)/(TP+FP),(TP*1.0)/(TP+FN)))
    
    