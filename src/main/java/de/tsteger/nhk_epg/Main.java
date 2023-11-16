package de.tsteger.nhk_epg;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.json.JSONArray;
import org.json.JSONObject;

import de.tsteger.nhk_epg.generated.Channel;
import de.tsteger.nhk_epg.generated.Desc;
import de.tsteger.nhk_epg.generated.Programme;
import de.tsteger.nhk_epg.generated.SubTitle;
import de.tsteger.nhk_epg.generated.Title;
import de.tsteger.nhk_epg.generated.Tv;

public class Main {	
	
	private final static DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("uuuuMMddHHmmss Z").withZone(ZoneOffset.UTC);

	public static void main(String[] args) {
		final Config config = args.length > 0 ?  Config.load(args[0]) : Config.getInstance();
		int exitCode = -1;
		
		if(config != null) {
			final long currentTime = System.currentTimeMillis();
			final long endTime = currentTime + ( 24L * 3600 * 1000 * config.getDays());
			
			try {
				final HttpRequest request = HttpRequest.newBuilder(new URI(String.format(config.getApiUrl(), currentTime, endTime)))
						.GET()
						.build();
				
				final HttpClient client = HttpClient.newHttpClient();
				
				System.out.println("Requesting: " + request.uri());
				final HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
				if(response.statusCode() == HttpURLConnection.HTTP_OK) {
					final JSONObject jsonEpg = new JSONObject(response.body());
					
					final Tv xmlTv = new Tv();
					final Channel xmlChannel = new Channel();
					xmlChannel.setId("NHK World");
					xmlTv.getChannel().add(xmlChannel);
					
					final JSONArray programList = jsonEpg.getJSONObject("channel").getJSONArray("item");
					
					for(int i = 0; i < programList.length(); i++) {
						final JSONObject item = programList.getJSONObject(i);

						final Instant itemStartTime = Instant.ofEpochMilli(Long.parseLong((String) item.get("pubDate")));
						final Instant itemEndTime = Instant.ofEpochMilli(Long.parseLong((String) item.get("endDate")));

						final Programme newEntry = new Programme();

						newEntry.setChannel(xmlChannel.getId());

						newEntry.setStart(DATE_TIME_FORMAT.format(itemStartTime));
						newEntry.setStop(DATE_TIME_FORMAT.format(itemEndTime));

						final Title title = new Title();
						title.setvalue(item.getString("title"));
						newEntry.getTitle().add(title);

						final Desc description = new Desc();
						description.setvalue(item.getString("description"));
						newEntry.getDesc().add(description);

						final SubTitle subtitle = new SubTitle();
						subtitle.setvalue(item.getString("subtitle"));
						newEntry.getSubTitle().add(subtitle);

						xmlTv.getProgramme().add(newEntry);
					}
					
					final JAXBContext context = JAXBContext.newInstance( Tv.class );
					final Marshaller m = context.createMarshaller();
					m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
					m.marshal( xmlTv, new FileOutputStream(config.getOutputFile()));
					
					System.out.println("Successfully converted " + programList.length() + " entries.");
					
					exitCode = 0;
				}
				else {
					System.err.println(response);
				}
			} catch (URISyntaxException | IOException | InterruptedException | JAXBException e) {
				e.printStackTrace();
			}
		}

		System.exit(exitCode);
	}

}
