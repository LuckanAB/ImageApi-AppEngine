package se.luckan.backend.helpers;

import java.util.logging.Logger;
import com.google.apphosting.api.ApiProxy;

public class Env {
	public static final String backend_root;
	public static final String bucket_name;
	public static final String hostname;
	static {
		final Logger log = Logger.getLogger(Env.class.getName());
		final ApiProxy.Environment env = ApiProxy.getCurrentEnvironment();
		hostname = (String)env.getAttributes().get("com.google.appengine.runtime.default_version_hostname");
		backend_root = "https://" + hostname;
		bucket_name = hostname; // default bucketname is exactly the same as the hostname
		log.info("Running on " + hostname + ".");
	}
}
