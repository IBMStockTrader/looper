/*
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
*/

import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;

public class Cleanup {
	private String basicAuth = null;
	private String jwtAuth = null;
//	private static long HOUR_IN_MILLIS = 3600000;
//	private static byte[] KEY = "stocktrader".getBytes();

	public static void main(String[] args) {
		if (args.length == 3) try {

			System.out.print("User ID: ");
			String id = System.console().readLine();

			System.out.print("Password: ");
			String pwd = new String(System.console().readPassword());
			System.out.println();

	        String credentials = id + ":" + pwd;
    	    String auth = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

			Cleanup cleanup = new Cleanup(auth);
			cleanup.getJWT(args[0]);
			cleanup.deleteBrokersWithPrefix(args[1], args[2]);
		} catch (Throwable t) {
			t.printStackTrace();
		} else {
			System.out.println("Usage:   cleanup <looper-url> <broker-url> <prefix>");
			System.out.println("Cleanup will delete all portfolios starting with <prefix>.");
			System.out.println("Example: cleanup http://looper-service:9080/looper http://broker-service:9080/broker gatling-");
			System.out.println("Result:  Cleanup iterates over all portfolios and deletes any starting with gatling-");
		}
	}

	public Cleanup(String auth) {
		basicAuth = auth;
	}

	public void getJWT(String url) throws InterruptedException, IOException {
/*
		JwtBuilder jwtBuilder = Jwts.builder();
		jwtBuilder.id("admin");
		jwtBuilder.audience().add("stock-trader");
		jwtBuilder.issuer("http://stock-trader.ibm.com");
		jwtBuilder.expiration(new Date(System.currentTimeMillis()+HOUR_IN_MILLIS));
//		jwtBuilder.signWith(Jwts.SIG.RS256.keyPair().build(), Jwts.SIG.RS256);
//		jwtBuilder.signWith(Jwts.SIG.HS256.key().build());
//		jwtBuilder.signWith(Keys.secretKeyFor(SignatureAlgorithm.RS256));
		jwtBuilder.signWith(Keys.keyPairFor(SignatureAlgorithm.RS256).getPrivate());
auth = "Bearer "+jwtBuilder.compact();
*/
		jwtAuth = invokeREST("GET", url+"/jwt");
		System.out.println("auth: "+jwtAuth);
	}

	public void deleteBrokersWithPrefix(String url, String prefix) throws InterruptedException, IOException {
		String response = invokeREST("GET", url);
		System.out.println(response);

		int deleted = 0;
		JSONArray brokers = new JSONArray(response);
		Iterator iter = brokers.iterator();
		while (iter.hasNext()) {
			JSONObject broker = (JSONObject) iter.next();
			String owner = (String) broker.get("owner");
			if ((owner!=null) && owner.startsWith(prefix)) {
				invokeREST("DELETE", url+"/"+owner);
				deleted++;
				System.out.println("Deleted portfolio: "+owner);
			} else {
				System.out.println("Ignored portfolio: "+owner);
			}
		}
		System.out.println("Number of portfolios deleted: "+deleted);
	}

    private String invokeREST(String verb, String uri) throws IOException {
        URL url = new URL(uri);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod(verb);
        conn.setRequestProperty("Content-Type", "text/plain");
        conn.setRequestProperty("Authorization", jwtAuth!=null ? jwtAuth : basicAuth);
        conn.setDoOutput(true);
        InputStream stream = conn.getInputStream();

        String response = stringFromStream(stream);

        stream.close();

        return response; //I use JsonStructure here so I can return a JsonObject or a JsonArray
    }

    private String stringFromStream(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        return out.toString();
    }
}
