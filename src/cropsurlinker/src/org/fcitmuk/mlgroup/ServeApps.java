package org.fcitmuk.mlgroup;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fcitmuk.mlgroup.constants.GaeConstants;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

@SuppressWarnings("serial")
public class ServeApps extends HttpServlet {
 GaeConstants GC= new GaeConstants();
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		PrintWriter out = resp.getWriter();

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
	
        //instantiate GQL
		resp.setContentType("text/xml");
		Query q = new Query("TileInfo");
		//resp.setContentType("text/html");
		PreparedQuery pq = ds.prepare(q);
		
		//display result as CSV
		String CSV="<tilelist>\n";
		for (Entity result : pq.asIterable()) {
			if(result.getProperty("lon_ul")!=null)
			{
		
		    CSV=CSV+"<tile";		  
			CSV=CSV+(" lon_ul=\""+result.getProperty("lon_ul")+"\"");
			CSV=CSV+"\t";		
			CSV=CSV+(" lat_ul=\""+ result.getProperty("lat_ul")+"\"");
			CSV=CSV+"\t";
			CSV=CSV+("lon_lr=\""+ result.getProperty("lon_lr")+"\"");
			CSV=CSV+"\t";
			CSV=CSV+("lat_lr=\""+ result.getProperty("lat_lr")+"\"");
			CSV=CSV+"\t";		
			CSV=CSV+("filename=\""+GC.getServer()+"?blobkey="+result.getProperty("blobkey")+"\"");
			CSV=CSV+" />\n";
			}	  
		}
		CSV=CSV+"</tilelist>";
		out.println(CSV);
	}
}
