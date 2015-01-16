package org.atelierdesmedias.presence.networkstat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.xpath.XPathExpressionException;

import android.util.Log;

public final class NeufBoxUtils {
	private static String getToken(InputStream inputStream) {
		// FIXME: Should use XPATH ideally but can't find why it does not work
		// with the emulator
		// XPath xpath = XPathFactory.newInstance().newXPath();
		// Attr attr = (Attr) xpath.evaluate("//auth[1]/@token", inputStream,
		// XPathConstants.NODE);
		// return attr.getValue();

		String content;
		try {
			content = new String(readFully(inputStream), "UTF-8");
		} catch (Exception e) {
			Log.e(NeufBoxUtils.class.getName(),
					"Failed to read HTTP response content", e);

			return null;
		}

		Matcher matcher = Pattern.compile("token=\"([^\"]*)\"")
				.matcher(content);
		matcher.find();

		return matcher.group(1);

	}

	public static String getToken() throws MalformedURLException, IOException,
			XPathExpressionException {
		InputStream inputStream = (InputStream) new URL(
				"http://192.168.1.1/api/1.0/?method=auth.getToken")
				.getContent();

		try {
			return getToken(inputStream);
		} finally {
			inputStream.close();
		}
	}

	public static String checkToken(String token) throws MalformedURLException,
			IOException, GeneralSecurityException, XPathExpressionException {
		// Hash the user/pass
		String username = "admin";
		String password = "idhyggivyirphyuslob7";

		String username_hash = computeHash(username);
		String username_hmac = computeSignature(username_hash, token);

		String password_hash = computeHash(password);
		String password_hmac = computeSignature(password_hash, token);

		String hash = username_hmac + password_hmac;

		// Get the token
		InputStream inputStream = (InputStream) new URL(
				"http://192.168.1.1/api/1.0/?method=auth.checkToken&token="
						+ token + "&hash=" + hash).getContent();

		try {
			return getToken(inputStream);
		} finally {
			inputStream.close();
		}
	}

	public static byte[] getHostsList(String token)
			throws MalformedURLException, IOException {
		InputStream inputStream = (InputStream) new URL(
				"http://neufbox/api/1.0/?method=lan.getHostsList&token="
						+ token).getContent();

		byte[] content;
		try {
			content = readFully(inputStream);
		} finally {
			inputStream.close();
		}

		return content;
	}

	public static byte[] getHostsList() throws MalformedURLException,
			IOException, XPathExpressionException, GeneralSecurityException {
		return getHostsList(checkToken(getToken()));
	}

	private static byte[] readFully(InputStream input) throws IOException {
		byte[] buffer = new byte[8192];
		int bytesRead;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
		return output.toByteArray();
	}

	private static String computeHash(String input)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.reset();

		byte[] byteData = digest.digest(input.getBytes("UTF-8"));
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < byteData.length; i++) {
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16)
					.substring(1));
		}

		return sb.toString();
	}

	String hmac = "";

	private static String computeSignature(String baseString, String keyString)
			throws GeneralSecurityException, UnsupportedEncodingException {
		Mac mac = Mac.getInstance("HmacSHA256");
		SecretKeySpec secret = new SecretKeySpec(keyString.getBytes("UTF-8"),
				"HmacSHA256");
		mac.init(secret);
		byte[] byteData = mac.doFinal(baseString.getBytes("UTF-8"));

		BigInteger hash = new BigInteger(1, byteData);
		String hmac = hash.toString(16);

		if (hmac.length() % 2 != 0) {
			hmac = "0" + hmac;
		}

		return hmac;
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16));
		}
		return data;
	}
}
