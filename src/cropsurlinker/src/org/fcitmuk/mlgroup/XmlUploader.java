/***
 * @author mistaguy
 * This is the xml uploader servlet. It reads the geo information required about the tiles
 * and then calls appengine to save all the contents
 ***/
package org.fcitmuk.mlgroup;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class XmlUploader extends HttpServlet {
	public void doGet(HttpServletRequest req,
			HttpServletResponse resp)
			throws IOException {
		  UserService userService = UserServiceFactory.getUserService();
		    PrintWriter out = resp.getWriter();
		    String thisURL = req.getRequestURI();

		String tileForm="";
		

		      if (req.getUserPrincipal() != null) {

		
		tileForm = 
		"<form action=\"/imageupload\" method=\"get\" enctype=\"multipart/form-data\"  >" +
		"<label for=\"lon_ul\">" +
		"lon_ul" +
		"</label>" +
		"<input name=\"lon_ul\" id=\"lon_ul\" type=\"text\" size=\"4\" " +
		"value=\"\" />" +
		
		"<label for=\"lat_ul\">" +
		"lat_ul" +
		"</label>" +
		"<input name=\"lat_ul\" id=\"lat_ul\" type=\"text\" size=\"4\" " +
		"value=\"\" />" +
		
		"<label for=\"lon_lr\">" +
		"lon_lr" +
		"</label>" +
		"<input name=\"lon_lr\" id=\"lon_lr\" type=\"text\" size=\"4\" " +
		"value=\"\" />" +
		
		"<label for=\"lat_lr\">" +
		"lat_lr" +
		"</label>" +
		"<input name=\"lat_lr\" id=\"lat_lr\" type=\"text\" size=\"4\" " +
		"value=\"\" />" +	
	
		"Image<input type=\"file\" name=\"file1\"><br>" +
		"<input type=\"submit\" name=\"Submit\" value=\"Upload Files\">"+		
		
	
		"</form>";
		 
			resp.setContentType("text/html");
		
			out.println(tileForm);
		      }
		      else
		      {
		    	  resp.getWriter().println("<p>Please <a href=\"" +
                          userService.createLoginURL(thisURL) +
                          "\">sign in</a>.</p>");
  
		      }
			
	}
}
