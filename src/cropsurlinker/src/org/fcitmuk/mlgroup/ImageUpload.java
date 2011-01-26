/****
 * @author mistaguy
 * This servlet handles multipart quirks of data upload.
 * it instantiates GAE blob service that uploads an image from a url
 * coutersy of timwhunt <timwhunt@gmail.com>
 * */

package org.fcitmuk.mlgroup;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fcitmuk.mlgroup.constants.GaeConstants;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

@SuppressWarnings("serial")
public class ImageUpload extends HttpServlet {
	//development server
	//String server = "http://127.0.0.1:8888";
	//GAE server
GaeConstants GC= new GaeConstants();
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		try {
			PrintWriter outp = res.getWriter();

			// read image data

			URL url = new URL(req.getParameter("imageurl"));
			URLConnection uc = url.openConnection();
			uc.setReadTimeout(15000);
			String contentType = uc.getContentType();
			String fileName = url.getFile();

			int contentLength = uc.getContentLength();
			if (contentType.startsWith("text/") || contentLength == -1) {
				throw new IOException("This is not a binary file.");
			}
			InputStream raw = uc.getInputStream();
			InputStream in = new BufferedInputStream(raw);
			byte[] data = new byte[contentLength];
			int bytesRead = 0;
			int offset = 0;
			while (offset < contentLength) {
				bytesRead = in.read(data, offset, data.length - offset);
				if (bytesRead == -1)
					break;
				offset += bytesRead;
			}
			in.close();

			// create POST to upload to blogstore
			BlobstoreService blobstoreService = BlobstoreServiceFactory
					.getBlobstoreService();
			String uploadURL = blobstoreService.createUploadUrl("/uploadblob");

			// add host if in dev mode
			if (uploadURL.indexOf("http") == -1)
				uploadURL = GC.getServer() + uploadURL;

			url = new URL(uploadURL);
			// create a boundary string
			String boundary = MultiPartFormOutputStream.createBoundary();
			URLConnection urlConn = MultiPartFormOutputStream
					.createConnection(url);
			urlConn.setReadTimeout(15000);
			urlConn.setRequestProperty("Accept", "*/*");
			urlConn.setRequestProperty("Content-Type",
					MultiPartFormOutputStream.getContentType(boundary));
			// set some other request headers...
			urlConn.setRequestProperty("Connection", "Keep-Alive");
			urlConn.setRequestProperty("Cache-Control", "no-cache");
			// no need to connect because getOutputStream() does it
			MultiPartFormOutputStream out = new MultiPartFormOutputStream(
					urlConn.getOutputStream(), boundary);
			// write a text field element
			out.writeField("myText", "text field text");
			// write bytes directly
			out.writeFile("myFile", contentType, fileName, data);
			out.close();

			// read response from server
			BufferedReader responseIn = new BufferedReader(
					new InputStreamReader(urlConn.getInputStream()));
			StringBuilder redirectResponse = new StringBuilder();
			String line = "";
			while ((line = responseIn.readLine()) != null) {
				redirectResponse.append(line);
			}
			in.close();

			// extract the blobstore key from the response
			String blobKeyStr = redirectResponse.toString();

			outp.println(blobKeyStr);

			// save tile info to gae
			
			TileSaveServlet tileServlet = new TileSaveServlet();
			tileServlet.setGaeblobkey(blobKeyStr);
			tileServlet.doGet(req, res);

			if (blobKeyStr == null || blobKeyStr.equals("FAILED"))
				throw new IOException("Failed to upload image blob");

		} catch (Exception ex) {

			throw new ServletException(ex);
		}
	}

}
