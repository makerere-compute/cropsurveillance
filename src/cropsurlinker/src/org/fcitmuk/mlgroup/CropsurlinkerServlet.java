/****
 * @author mistaguy
 * This Servlet is used for Uploading the heatmap tiles by a user.
 * Only mlgroup users can do so
 */

package org.fcitmuk.mlgroup;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fcitmuk.mlgroup.htmutils.htmlbase;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class CropsurlinkerServlet extends HttpServlet {

	htmlbase htmlbase = new htmlbase();

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		PrintWriter out = resp.getWriter();
		String thisURL = req.getRequestURI();

		if (req.getUserPrincipal() != null) {

			String tileForm = "";

			tileForm = "<form action=\"/imageupload\" method=\"get\" enctype=\"multipart/form-data\"  >"
					+ "<label for=\"lon_ul\">"
					+ "lon_ul"
					+ "</label>"
					+ "<input name=\"lon_ul\" id=\"lon_ul\" type=\"text\" size=\"4\" "
					+ "value=\"\" />"
					+

					"<label for=\"lat_ul\">"
					+ "lat_ul"
					+ "</label>"
					+ "<input name=\"lat_ul\" id=\"lat_ul\" type=\"text\" size=\"4\" "
					+ "value=\"\" />"
					+

					"<label for=\"lon_lr\">"
					+ "lon_lr"
					+ "</label>"
					+ "<input name=\"lon_lr\" id=\"lon_lr\" type=\"text\" size=\"4\" "
					+ "value=\"\" />"
					+

					"<label for=\"lat_lr\">"
					+ "lat_lr"
					+ "</label>"
					+ "<input name=\"lat_lr\" id=\"lat_lr\" type=\"text\" size=\"4\" "
					+ "value=\"\" />"
					+
					
					
					"Image<input type=\"file\" name=\"myFile\"><br>"
					+ "<input type=\"submit\" name=\"Submit\" value=\"Upload Files\">"
					+

					"</form>";
            String userDetails="<p><h3>Hi,"+userService.getCurrentUser()+"  <a href=\""+userService.createLogoutURL("\\")+"\">sign Out</a></h3></p>";
			resp.setContentType("text/html");

			htmlbase.setContent(userDetails+tileForm);
			htmlbase.printHtml(out);

		} else {
			resp.setContentType("text/html");
			String content =  
					"<p>Please <a href=\""
					+ userService.createLoginURL(thisURL)
					+ "\">sign in</a>.</p>";
				
				
			htmlbase.setContent(content);

			htmlbase.printHtml(out);

		}
	}
}
