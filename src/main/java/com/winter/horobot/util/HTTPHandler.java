/*
	HoroBot - An open-source Discord bot
	Copyright (C) 2017	WiNteR

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.winter.horobot.util;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.winter.horobot.core.Config;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class HTTPHandler {
	
	public enum KONA_RATING {
		SAFE,
		ECCHI,
		NSFW
	}

	public static String stringFromUrl(URL url) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader rd;
		if (url.getProtocol().equals("https")) {
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:10.0.2) Gecko/20100101 Firefox/10.0.2");
			con.connect();
			rd = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
		} else {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:10.0.2) Gecko/20100101 Firefox/10.0.2");
			con.connect();
			rd = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
		}

		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

	public static void postStats(int shard, int shardCount, int serverCount) {
		JSONObject obj = new JSONObject();
		obj.put("server_count", serverCount);
		obj.put("shard_id", shard);
		obj.put("shard_count", shardCount);

		try {
			Unirest.post("https://bots.discord.pw/api/bots/289381714885869568/stats")
					.header("Authorization", Config.DISCORD_BOTS_PW_TOKEN)
					.header("Content-Type", "application/json")
					.body(obj.toString())
					.asJson();
		} catch(UnirestException e) {
			e.printStackTrace();
		}
		try {
			Unirest.post("https://discordbots.org/api/bots/289381714885869568/stats")
					.header("Authorization", Config.DISCORD_BOTS_ORG_TOKEN)
					.header("Content-Type", "application/json")
					.body(obj.toString())
					.asJson();
		} catch(UnirestException e) {
			e.printStackTrace();
		}
	}

	public static String requestCat() throws UnirestException {
		GetRequest request = Unirest.get("http://www.random.cat/meow");
		HttpResponse<JsonNode> response = request.asJson();
		response.getBody().getObject();
		return response.getBody().getObject().getString("file");
	}

	public static String searchYouTube(String search) throws UnirestException, UnsupportedEncodingException {
		String temp = URLEncoder.encode(search, "UTF-8");
		String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&q=" + temp + "&maxResults=10&key=" + Config.YOUTUBE_KEY;
		HttpResponse<JsonNode> response = Unirest.get(url)
				.header("Accept", "text/plain")
				.asJson();
		if(response.getStatus() == 200) {
			return "https://www.youtube.com/watch?v=" + response.getBody().getObject().getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId");
		}
		return null;
	}

	public static EmbedObject requestTranslation(String from, String to, String text, String author, String authorIcon) throws UnsupportedEncodingException, UnirestException {
		String temp = URLEncoder.encode(text, "UTF-8");
		String url = "http://www.transltr.org/api/translate?text=" + temp + "&to=" + to + "&from=" + from;
		HttpResponse<JsonNode> response = Unirest.get(url)
				.header("X-Mashape-Key", Config.MASHAPE_KEY)
				.header("Accept", "text/plain")
				.asJson();

		if(response.getStatus() == 200) {
			String translation = response.getBody().getObject().getString("translationText");
			EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder.withColor(Color.CYAN);
			embedBuilder.withAuthorName("Requested by @" + author);
			embedBuilder.withAuthorIcon(authorIcon);
			embedBuilder.appendField(text, translation, false);
			return embedBuilder.build();
		}
		return null;
	}

	public static EmbedObject requestUrban(String[] term, String guildID, IUser user) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.withColor(Color.CYAN);
		builder.withAuthorName("Requested by @" + user.getName());
		builder.withAuthorIcon(Utility.getAvatar(user));
		try {
			String url = "https://mashape-community-urban-dictionary.p.mashape.com/define?term=";
			for(String tag : term) {
				if(term.length == 1) {
					url += tag;
				} else {
					url += tag + "+";
				}
			}

			HttpResponse<JsonNode> response = Unirest.get(url)
					.header("X-Mashape-Key", Config.MASHAPE_KEY)
					.header("Accept", "text/plain")
					.asJson();

			if(!response.getBody().getObject().getString("result_type").equals("no_results")) {
				String definition = response.getBody().getObject().getJSONArray("list").getJSONObject(0).getString("definition");
				if(definition.length() >= 1024) {
					definition = definition.substring(0, 1021);
					definition += "...";
				}
				builder.appendField("Definition", definition, false);

				try {
					String example = response.getBody().getObject().getJSONArray("list").getJSONObject(0).getString("example");
					if(example.length() >= 1024) {
						example = definition.substring(0, 1021);
						example += "...";
					}
					builder.appendField("Example", example, false);
				} catch (NullPointerException e) {
					e.printStackTrace();
				}

				return builder.build();
			} else {
				builder.appendField("Error", Localisation.getMessage(guildID, "html-no-results"), false);
				return builder.build();
			}
		} catch (UnirestException e) {
			builder.appendField("Error", Localisation.getMessage(guildID, "html-error"), false);
			return builder.build();
		}
	}

	public static String requestR34(String[] tags) throws SAXException, ParserConfigurationException, URISyntaxException, IOException {
		String url = "http://rule34.xxx/index.php?page=dapi&s=post&q=index";
		String search = "";
		for(String tag : tags) {
			search += tag + " ";
		}
		URIBuilder builder = new URIBuilder(new URI(url));
		builder.addParameter("limit", "100");
		builder.addParameter("tags", search);

		HttpGet request = new HttpGet(builder.build());
		HttpClient client = HttpClientBuilder.create().build();
		org.apache.http.HttpResponse response = client.execute(request);

		if(response.getStatusLine().getStatusCode() == 200) {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(response.getEntity().getContent());

			NodeList results = doc.getElementsByTagName("posts");
			Element posts = (Element) results.item(0);
			Random rand = new Random();
			Element post = (Element) posts.getElementsByTagName("post").item(rand.nextInt(posts.getElementsByTagName("post").getLength()));
			try {
				URL urll = new URL("http:" + post.getAttribute("file_url"));
				return urll.toURI().toASCIIString();
			} catch (Exception e) {
				return "https:" + post.getAttribute("file_url");
			}
		}
		return Localisation.getPMMessage("html-error");
	}

	public static String requestGelbooru(String[] tags) throws SAXException, ParserConfigurationException, URISyntaxException, IOException {
		String url = "http://gelbooru.com/index.php?page=dapi&s=post&q=index";
		String search = "";
		for(String tag : tags) {
			search += tag + " ";
		}
		URIBuilder builder = new URIBuilder(new URI(url));
		builder.addParameter("limit", "100");
		builder.addParameter("tags", search);

		HttpGet request = new HttpGet(builder.build());
		HttpClient client = HttpClientBuilder.create().build();
		org.apache.http.HttpResponse response = client.execute(request);

		if(response.getStatusLine().getStatusCode() == 200) {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(response.getEntity().getContent());

			NodeList results = doc.getElementsByTagName("posts");
			Element posts = (Element) results.item(0);
			Random rand = new Random();
			Element post = (Element) posts.getElementsByTagName("post").item(rand.nextInt(posts.getElementsByTagName("post").getLength()));
			try {
				URL urll = new URL("http:" + post.getAttribute("file_url"));
				return "http:" + post.getAttribute("file_url");
			} catch (Exception e) {
				return "https:" + post.getAttribute("file_url");
			}
		}

		return Localisation.getPMMessage("html-error");
	}

	public static EmbedObject requestAnime(String[] tags, String author, String authorIcon, int searchType) throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
		HttpGet request;
		if(searchType == 0) {
			request = new HttpGet("https://myanimelist.net/api/anime/search.xml");
		} else {
			request = new HttpGet("https://myanimelist.net/api/manga/search.xml");
		}
		String auth = Config.MAL_CREDENTIALS;
		byte[] encodedPath = Base64.encodeBase64(auth.getBytes(Charset.forName("ISO-8859-1")));
		String authHeader = "Basic " + new String(encodedPath);
		request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

		String searchOrig = "";
		for(String tag : tags) {
			searchOrig += tag + " ";
		}
		searchOrig = searchOrig.substring(0, searchOrig.length() - 1);

		URIBuilder builder = new URIBuilder(request.getURI());
		builder.addParameter("q", searchOrig);
		request.setURI(builder.build());

		HttpClient client = HttpClientBuilder.create().build();
		org.apache.http.HttpResponse response = client.execute(request);

		int status = response.getStatusLine().getStatusCode();

		if (status == 200) {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(response.getEntity().getContent());

			EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder.withColor(Color.CYAN);
			embedBuilder.withAuthorName("Requested by @" + author);
			embedBuilder.withAuthorIcon(authorIcon);

			NodeList results = doc.getElementsByTagName("entry");
			if(results.getLength() == 0) {
				return null;
			}

			Element thumb = (Element) results.item(0);
			embedBuilder.withThumbnail(thumb.getElementsByTagName("image").item(0).getTextContent());

			int cycle = results.getLength();
			if(results.getLength() > 5) cycle = 5;
			for(int i = 0; i < cycle; i++) {
				Element element = (Element) results.item(i);

				StringBuilder stringBuilder = new StringBuilder();

				if(searchType == 0) {
					String episodes = element.getElementsByTagName("episodes").item(0).getTextContent();
					stringBuilder.append("**Episodes:** " + episodes + "\n");
				}

				String type = element.getElementsByTagName("type").item(0).getTextContent();
				stringBuilder.append("**Type:** " + type + "\n");

				String animeStatus = element.getElementsByTagName("status").item(0).getTextContent();
				stringBuilder.append("**Status:** " + animeStatus + "\n");

				String desc = element.getElementsByTagName("synopsis").item(0).getTextContent();
				stringBuilder.append("**Description:** " + desc + "\n");

				String unescaped = stringBuilder.toString();
				unescaped = StringEscapeUtils.unescapeXml(unescaped);
				unescaped = unescaped.replaceAll("\\<.*?>","");
				unescaped = unescaped.replaceAll("\\[.*?]","");

				unescaped = unescaped.substring(0, Math.min(unescaped.length(), 1000));
				embedBuilder.appendField(StringEscapeUtils.unescapeXml(element.getElementsByTagName("title").item(0).getTextContent()), unescaped, false);
			}
			return embedBuilder.build();
		}
		return null;
	}

	public static String requestDanbooru(String[] tags) throws URISyntaxException, UnirestException {
		String url = "https://danbooru.donmai.us/posts.json?limit=100?tags=";
		for(String tag : tags) {
			if(tags.length == 1) {
				url += tag;
			} else {
				url += tag + "+";
			}
		}

		HttpResponse<JsonNode> response = Unirest.get(url)
				.header("Accept", "text/plain")
				.asJson();

		if(response.getStatus() == 200) {
			if(response.getBody().getArray().length() > 0) {
				if(response.getBody().getArray().length() == 1) {
					System.out.println("https://danbooru.donmai.us" + response.getBody().getArray().getJSONObject(0).getString("file_url"));
					return "https://danbooru.donmai.us" + response.getBody().getArray().getJSONObject(0).getString("file_url");
				} else {
					Random rand = new Random();
					return "https://danbooru.donmai.us" + response.getBody().getArray().getJSONObject(rand.nextInt(response.getBody().getArray().length())).getString("file_url");
				}
			} else {
				return "html-no-results";
			}
		}
		return Localisation.getPMMessage("html-error");
	}

	public static String requestYandere(String[] tags) throws URISyntaxException, UnirestException {
		String url = "https://yande.re/post.json";
		String search = "";
		for(String tag : tags) {
			search += tag + " ";
		}
		URIBuilder builder = new URIBuilder(new URI(url));
		builder.addParameter("limit", "100");
		builder.addParameter("tags", search);

		HttpResponse<JsonNode> response = Unirest.get(builder.build().toASCIIString())
				.header("Accept", "text/plain")
				.asJson();

		if(response.getStatus() == 200) {
			if(response.getBody().getArray().length() > 0) {
				if(response.getBody().getArray().length() == 1) {
					return response.getBody().getArray().getJSONObject(0).getString("file_url");
				} else {
					Random rand = new Random();
					return response.getBody().getArray().getJSONObject(rand.nextInt(response.getBody().getArray().length())).getString("file_url");
				}
			} else {
				return "html-no-results";
			}
		}
		return Localisation.getPMMessage("html-error");
	}

	public static String requestKona(String[] tags, KONA_RATING rating) throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet request = new HttpGet("http://www.konachan.com/post.xml");
		URIBuilder builder = new URIBuilder(request.getURI()).addParameter("limit", "100");

		String msgSearch = "";
		StringBuilder searchOrig = new StringBuilder();
		for (String tag : tags) {
			if (tags.length == 1) {
				searchOrig.append(tag);
			} else {
				searchOrig.append(msgSearch).append(tag).append(" ");
			}
		}

		if (rating == KONA_RATING.SAFE) {
			msgSearch = "order:score rating:safe " + searchOrig;
		} else if (rating == KONA_RATING.ECCHI) {
			msgSearch = "order:score rating:questionable " + searchOrig;
		} else if (rating == KONA_RATING.NSFW) {
			msgSearch = "order:score rating:explicit " + searchOrig;
		}

		builder.addParameter("tags", msgSearch);

		URI uri = builder.build();

		request.setURI(uri);

		org.apache.http.HttpResponse response;

		response = client.execute(request);
		int status = response.getStatusLine().getStatusCode();

		client.close();

		if(status == 200) {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(response.getEntity().getContent());

			NodeList list = doc.getElementsByTagName("post");
			int index;
			if(list.getLength() == 1) {
				index = 0;
			} else if(list.getLength() > 1) {
				Random rand = new Random();
				index = rand.nextInt(list.getLength());
			} else {
				return Localisation.getPMMessage("html-no-results");
			}
			Node node = list.item(index);
			NamedNodeMap map = node.getAttributes();
			Node url = map.getNamedItem("file_url");
			return "http:" + url.getNodeValue();
		}
		return Localisation.getPMMessage("html-error");
	}
}