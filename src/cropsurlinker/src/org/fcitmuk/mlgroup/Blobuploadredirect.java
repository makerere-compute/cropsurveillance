/****
 * @author mistaguy
 * this servlet redirects the appropriate page with the key
 * 
 */
package org.fcitmuk.mlgroup;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class Blobuploadredirect extends HttpServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String blobkey = request.getParameter("blobkey");
		if (blobkey == null)
			blobkey = "FAILED";

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		// out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		// "Transitional//EN\">\n" +
		// "<HTML>\n" +
		// "<HEAD><TITLE>Hello WWW</TITLE></HEAD>\n" +
		// "<BODY>\n");

		// out.println("<H1>blogkey = '" + blobkey + "'</H1></BODY></HTML>");
		out.println(blobkey);
	}
}