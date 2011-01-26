/**
 * @author mistaguy
 * This script saves the tile to google cloud
 */
package org.fcitmuk.mlgroup;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

@SuppressWarnings("serial")
public class TileSaveServlet extends HttpServlet {
	 TileSaveServlet()
	 {
		 
	 }
	 public String gaeblobkey="none";
	public String getGaeblobkey() {
		return gaeblobkey;
	}
	public void setGaeblobkey(String gaeblobkey) {
		this.gaeblobkey = gaeblobkey;
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		PrintWriter out = resp.getWriter();
	
		try {
		
			DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
			Entity tile = new Entity("TileInfo");	
			tile.setProperty("lon_ul",req.getParameter("lon_ul"));
			tile.setProperty("lat_ul",req.getParameter("lat_ul"));
			tile.setProperty("lon_lr",req.getParameter("lon_lr"));
			tile.setProperty("lat_lr",req.getParameter("lat_lr"));
			tile.setProperty("blobkey",this.gaeblobkey);
		
			
			ds.put(tile);
			out.println("Tiles Saved!");
		
		} catch (NumberFormatException nfe) {
			// User entered a value that wasn't an integer. Ignore for now.
			out.println("Tiles Not Saved!");
		}
		resp.sendRedirect("/");
	
	
	}
}
