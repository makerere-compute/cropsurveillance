package org.fcitmuk.mlgroup.htmutils;

import java.io.PrintWriter;

public class htmlbase {
	String header;
	String content;

	public htmlbase() {
		this.header = "<head>"
				+ "<meta name=\"author\" content=\"mistaguy (abiccel@yahoo.com)\" />"
				+ "<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\" />"
				+ "<link rel=\"stylesheet\" href=\"css/main.css\" type=\"text/css\" />"
				+ "<title>CROPSURLINKER</title>" + "</head>";
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	/***
	 * This function is suppposed to print an html page
	 * 
	 * @param out
	 *            is the prinwriter
	 * @return false or true for success
	 */
	public boolean printHtml(PrintWriter out) {

		boolean success = false;
		try {
			out.println("<html>");
			out.println(this.getHeader());
			out.println("<body>");
			out.println("<div id=\"content\">"
					+ "<div id=\"logo\">"
					+ "<h1>CROPSURLINKER</h1>"
					+ "</div>"
					+ "<div id=\"intro\">"
					+ "	<h1>Geo tile <span class=\"white\">Uploader</span>!</h1>"
					+ "	<p>connecting mlgroup at FCIT to GAE.</p>"
					+ "	<div id=\"login\">"
					+ "		<p><a href=\"upload.jsp\">File Upload</a> <a href=\"#\">Upload Cron Jon</a> <a href=\"/tilelist\">Data Available</a></p>"
					+ "	</div>" + "	</div>	");
			out.println(this.getContent());
			out.println("</div>");
			out.println("</body>");
			out.println("</html>");
			success = true;
		} catch (Exception e) {

		}
		return success;
	}

}
