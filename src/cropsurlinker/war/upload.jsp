<%@page import="java.io.PrintWriter"%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<%
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
%>

<html>
<head>
	<link rel="stylesheet" href="css/main.css" type="text/css" />
	<title>CROPSURLINKER</title>
</head>
    <body>
    <div id="content">
		<div id="logo">
			<h1>CROPSURLINKER</h1>
		</div>
		<ul id="menu">
			<li><a href="#">Home</a></li>
			<li><a href="/cropsurlinker">File Upload</a></li>
			<li><a href="#">Upload Cron Jobs</a></li>
			<li><a href="/tilelist">Data Available</a></li>	
		</ul>
		
		<div id="intro">
			<h1>Geo tile <span class="white">Uploader</span>!</h1>
			<p>connecting mlgroup at FCIT to GAE.</p>
			<div id="login">
				<p><a href="upload.jsp">File Upload</a> <a href="#">Upload Cron Jobs</a> <a href="/tilelist">Data Available</a></p>
			</div>
					
		</div>
			<%
			UserService userService = UserServiceFactory.getUserService();
			if (userService.getCurrentUser() != null) {
			out.println("<p><h3>Hi,"+userService.getCurrentUser()+"  <a href=\""+userService.createLogoutURL("/index.html")+"\">sign Out</a></h3></p>");
			
			%>
			 <form action="<%=blobstoreService.createUploadUrl("/upload") %>" method="post" enctype="multipart/form-data">
            
            <table>
            <tr>
            <td colspan="2"></td>
            </tr>
            
            <tr>
            <td>Upper Left Longitude<td>
            <td> <input type="text" name="lon_ul" id="lon_ul"><td>
            </tr>
             <tr>
            <td>Lower Right Longitude<td>
            <td><input type="text" name="lon_lr" id="lon_lr"><td>
            </tr>
             <tr>
            <td>Upper Left Latitude<td>
            <td><input type="text" name="lat_ul" id="lat_ul"><td>
            </tr>
             <tr>
            <td>Lower Right Latitude<td>
            <td><input type="text" name="lat_lr" id="lat_lr"><td>
            </tr>
             <tr>
            <td>Image<td>
            <td> <input type="file" name="myFile"><td>
            </tr>
             <tr>
            <td colspan="2">
            <input type="submit" value="Submit"><td>          
            </tr>            
            </table>  
        </form>	
        <%
			}
			else
			{
				
				String content =  
					
					"<p>Please <a href=\""
					+ userService.createLoginURL("/index.html")
					+ "\">sign in</a>.</p>";
					
					out.println(content);
			}
        %>
		</div>
       
    </body>
</html>
