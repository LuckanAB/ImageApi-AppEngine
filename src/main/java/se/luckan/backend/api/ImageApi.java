package se.luckan.backend.api;

import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import se.luckan.backend.helpers.ByteUtils;
import se.luckan.backend.helpers.StorageAuth;
import se.luckan.backend.helpers.StorageAuth.SignedUrlResponse;

/** We are using Signed URL to upload to Google Cloud Storage.
 * https://cloud.google.com/storage/docs/access-control#Signed-URLs
 *
 * The Signed URL (JSON) approach has no callback in itself. We may however add a listener to the Google Cloud Storage
 * bucket using gsutil. So to do post processing we must have a gsutil notification listener running.
 *
 * To start a listener, run the following command in an authorized gcloud session:
 *
 * <pre>
 * gsutil notification watchbucket -i <watcher-name> -t <GS_NOTIFICATION_SECRET> https://<your-project>.appspot.com/images/notification gs://<your-project>.appspot.com
 * </pre>
 *
 * Save the resource-id that this returns so you can stop the listener later if needed.
 *
 * Stop listener: <pre>
 * gsutil notification stopchannel <watcher-name> <resource-id>
 * </pre> */
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@Path("/images")
public class ImageApi {
	/** Class mapped after the JSON structure that a gsutil notification sends. */
	public static class NotificationIn {
		public String id;
		public String name;
	}
	public static final String GS_NOTIFICATION_SECRET = "insert-any-secret-constant";
	protected static final Logger log = Logger.getLogger(ImageApi.class.getName());

	/** Called from gsutil notifications (after signedUrl upload) */
	@POST @Path("/notification") //
	public void notification(final NotificationIn in, @Context final HttpServletRequest req) {
		final String state = req.getHeader("X-Goog-Resource-State");
		if(state.equals("exists")) {
			final String token = req.getHeader("X-Goog-Channel-Token");
			if(!GS_NOTIFICATION_SECRET.equals(token)) {
				log.warning("invalid GS authorization token! token=" + token);
				return;
			}
			final String objectName = in.name;
			/* TODO: do post processing here ... */
		}
	}

	/** Get a signed URL for uploading of an object. Does not work for web forms (Use XMLHttpRequest instead). */
	@GET @Path("/signedUrl") //
	public SignedUrlResponse signedUrl(@QueryParam("ext") final String ext,
	                                   @Context final HttpServletRequest req,
	                                   @Context final HttpServletResponse res) {
		final String extNullSafe = ext != null && !ext.isEmpty() ? ext : "jpg";
		final String contentType = ext != null && !ext.isEmpty() ? "image/" + ext : "";
		/* TODO: authenticate user here ... */
		final String userDir = "guest";
		final String fileName = ByteUtils.longToBase64(System.currentTimeMillis());
		final String objectName = userDir + "/" + fileName + "." + extNullSafe;
		return StorageAuth.createSignedUrlResponse(objectName, contentType);
	}
}
