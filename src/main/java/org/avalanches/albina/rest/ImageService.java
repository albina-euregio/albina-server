package org.avalanches.albina.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import io.swagger.annotations.Api;

@Path("/images")
@Api(value = "/images")
public class ImageService {

	private static Logger logger = LoggerFactory.getLogger(ImageService.class);

	@Context
	UriInfo uri;

	@POST
	@Path("/fileupload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(@DefaultValue("true") @FormDataParam("enabled") boolean enabled,
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) {
		String uploadedFileLocation = "D://images/" + fileDetail.getFileName();
		File objFile = new File(uploadedFileLocation);
		if (objFile.exists()) {
			objFile.delete();
		}
		saveToFile(uploadedInputStream, uploadedFileLocation);
		logger.debug("File uploaded via Jersey based RESTFul Webservice to: " + uploadedFileLocation);
		return Response.status(201).build();
	}

	private void saveToFile(InputStream uploadedInputStream, String uploadedFileLocation) {
		try {
			OutputStream out = null;
			int read = 0;
			byte[] bytes = new byte[1024];
			out = new FileOutputStream(new File(uploadedFileLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
