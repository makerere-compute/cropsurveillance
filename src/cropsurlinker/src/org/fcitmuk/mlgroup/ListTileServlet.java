/***
 * @author mistaguy
 * This servlet displays the tiles uploaded to GAE
 */
package org.fcitmuk.mlgroup;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fcitmuk.mlgroup.htmutils.htmlbase;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;


@SuppressWarnings("serial")
public class ListTileServlet  extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		htmlbase htmlbase = new htmlbase();
		UserService userService = UserServiceFactory.getUserService();
		PrintWriter out = resp.getWriter();
		String thisURL = req.getRequestURI();

		if (req.getUserPrincipal() != null) {
		//instantiate Google datastore
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
	
        //instantiate GQL
		
		Query q = new Query("TileInfo");
		resp.setContentType("text/html");
		PreparedQuery pq = ds.prepare(q);
		String CSV="";
		CSV=CSV+("<table width=\"100%\" border=\"1\">" +
				"<tr>" +
				"<td>lon_ul</td>" +
				"<td>lat_ul</td>" +
				"<td>lon_lr</td>" +
				"<td>lat_lr</td>" +
				"<td>blobkey</td>" +
				"<tr>");
		//display result as CSV
		
		for (Entity result : pq.asIterable()) {
			if(result.getProperty("lon_ul")!=null)
			{
			CSV=CSV+"<tr>";
			CSV=CSV+"<td>";
			CSV=CSV+(result.getProperty("lon_ul"));
			CSV=CSV+"</td>";
			CSV=CSV+"<td>";
			CSV=CSV+( result.getProperty("lat_ul"));
			CSV=CSV+"</td>";
			CSV=CSV+"<td>";
			CSV=CSV+( result.getProperty("lon_lr"));
			CSV=CSV+"</td>";
			CSV=CSV+"<td>";
			CSV=CSV+( result.getProperty("lat_lr"));
			CSV=CSV+"</td>";
			CSV=CSV+"<td>";			
			CSV=CSV+("<a href=\"serveimage?blobkey="+result.getProperty("blobkey")+"\">"+ result.getProperty("blobkey")+"<a>");
			CSV=CSV+"</td>";
			CSV=CSV+"<tr>";
			}	  
		}
		CSV=CSV+"</table>";
		  String userDetails="<p><h3>Hi,"+userService.getCurrentUser()+"  <a href=\""+userService.createLogoutURL("\\")+"\">sign Out</a></h3></p>";
			htmlbase.setContent(userDetails+CSV);
			htmlbase.printHtml(out);
			
		}
		else
		{
			String content =  
				"<p>Please <a href=\""
				+ userService.createLoginURL(thisURL)
				+ "\">sign in</a>.</p>";
			
			
		htmlbase.setContent(content);
		htmlbase.printHtml(out);
		}
	}
}
