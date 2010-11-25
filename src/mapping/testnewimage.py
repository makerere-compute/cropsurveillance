from PIL import Image
import numpy

w,h=255,255 ## this is the size image we want to create
img = numpy.zeros((h,w,4),numpy.uint8)

for x in range(w):
    img[:,x,0] = x
    
for y in range(h):
    img[y,:,3] = y

pilImage = Image.fromarray(img)
pilImage.save('test.png')


'''
size = [200,300]
im = Image.new('RGB',[size[1],size[0]])

for x in range(size[0]):
    for y in range(size[1]):
        im.point()

im.save('test.png')
'''
