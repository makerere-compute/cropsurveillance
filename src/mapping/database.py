#this script is for saving data to the mysql database
import MySQLdb
#create a connection to the datase
conn = MySQLdb.connect (host = "localhost",
                           user = "root",
                           passwd = "root",
                           db = "cropsurveillance")
cursor = conn.cursor ()
cursor.execute ("SELECT VERSION()")
row = cursor.fetchone ()
#print server version
print "server version:", row[0]

#this function save data to the database
def savetile(zoomlevel,tile_lon_ul,tile_lat_ul,tile_lon_lr,tile_lat_lr,tile_blob):
    sql = "INSERT INTO imagetiles (zoom,tile_lon_ul,tile_lat_ul,tile_lon_lr,tile_lat_lr,tile_blob) VALUES (%s,%s,%s,%s,%s,%s)"
    cursor.execute(sql, (zoomlevel,tile_lon_ul,tile_lat_ul,tile_lon_lr,tile_lat_lr,tile_blob))
    print "inserted"
def closeCursor():
    cursor.close()
#close the connection to the database
def closeConnection():
    conn.close()
