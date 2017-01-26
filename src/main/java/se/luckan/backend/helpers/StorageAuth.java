package se.luckan.backend.helpers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.commons.codec.binary.Base64;
import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityService.SigningResult;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;

/** It seems Google Cloud Storage protocols are similar to Amazon S3 protocols (a good thing for us developers). So we
 * can either sign with a Signed URL (https://cloud.google.com/storage/docs/access-control#Signed-URLs), or a Signed
 * Policy Document (https://cloud.google.com/storage/docs/xml-api/post-object#policydocument). We use Signed URLs. */
public class StorageAuth {
	public static class SignedUrlResponse {
		public String method;
		public String objectName;
		public String uploadUrl;

		public SignedUrlResponse(final String method, final String url) {
			this.method = method;
			uploadUrl = url;
			objectName = Env.bucket_name + "/" + objectName;
		}
	}
	private static final int EXPIRATION = 24 * 60 * 60 * 1000;
	private static final String SUBDOMAIN_URL = "https://%s.storage.googleapis.com/%s";

	public static SignedUrlResponse createSignedUrlResponse(final String objectName, final String contentType) {
		return new SignedUrlResponse("PUT", StorageAuth.getSignedUrl("PUT", objectName, contentType));
	}

	public static String getSignedUrl(final String method, final String objectName, final String contentType) {
		final AppIdentityService appIdentity = AppIdentityServiceFactory.getAppIdentityService();
		final String accessId = appIdentity.getServiceAccountName();
		final long expiration = System.currentTimeMillis() + EXPIRATION;
		final String unsigned = stringPolicy(method, objectName, contentType, expiration);
		final String signature = sign(appIdentity, unsigned);
		final String encodedSignature;
		try {
			encodedSignature = URLEncoder.encode(signature, "UTF-8");
		} catch(final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return new StringBuilder(objectUrl(objectName)).append("?GoogleAccessId=").append(accessId).append("&Expires=")
		                                               .append(expiration).append("&Signature=").append(encodedSignature)
		                                               .toString();
	}

	public static String objectUrl(final String objectName) {
		return String.format(SUBDOMAIN_URL, Env.bucket_name, objectName);
	}

	public static String sign(final AppIdentityService appIdentity, final String stringToSign) {
		try {
			final SigningResult signingResult = appIdentity.signForApp(stringToSign.getBytes());
			return new String(Base64.encodeBase64(signingResult.getSignature(), false), "UTF-8");
		} catch(final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private static String stringPolicy(final String verb,
	                                   final String filename,
	                                   final String contentType,
	                                   final long expiration) {
		final String contentMD5 = "";
		final String canonicalizedExtensionHeaders = "";
		final String canonicalizedResource = "/" + Env.bucket_name + "/" + filename;
		return verb + "\n" + contentMD5 + "\n" + contentType + "\n" + expiration + //
		       "\n" + canonicalizedExtensionHeaders + canonicalizedResource;
	}
}
