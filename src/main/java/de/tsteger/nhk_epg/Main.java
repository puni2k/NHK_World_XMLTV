package de.tsteger.nhk_epg;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDate;
import java.time.ZonedDateTime;
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
	
	private final static DateTimeFormatter REQUEST_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
	private final static DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("uuuuMMddHHmmss Z");

	public static void main(String[] args) {
		final Config config = args.length > 0 ?  Config.load(args[0]) : Config.getInstance();
		int exitCode = -1;
		
		if(config != null) {
			final LocalDate startDate = LocalDate.now();
			
			final Tv xmlTv = new Tv();
			final Channel xmlChannel = new Channel();
			xmlChannel.setId("NHK World");
			xmlTv.getChannel().add(xmlChannel);
			
			int importCounter = 0;
			
			for(int i = 0; i < config.getDays(); i++) {
				final LocalDate currentDate = startDate.plusDays(i);
				
				try {
					final HttpRequest request = HttpRequest.newBuilder(new URI(String.format(config.getApiUrl(), currentDate.format(REQUEST_DATE_FORMAT))))
							.GET()
							.build();
					
					final HttpClient client = HttpClient.newHttpClient();
					
					System.out.println("Requesting: " + request.uri());
					
					final HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
					
					if(response.statusCode() == HttpURLConnection.HTTP_OK) {
						final JSONObject jsonEpg = new JSONObject(response.body());
						
						final JSONArray programList = jsonEpg.getJSONArray("data");
						
						for(int j = 0; j < programList.length(); j++) {
							final JSONObject item = programList.getJSONObject(j);
							
							final ZonedDateTime startTime = ZonedDateTime.parse(item.getString("startTime"), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
							final ZonedDateTime endTime = ZonedDateTime.parse(item.getString("endTime"), DateTimeFormatter.ISO_OFFSET_DATE_TIME);

							final Programme newEntry = new Programme();

							newEntry.setChannel(xmlChannel.getId());

							newEntry.setStart(startTime.format(DATE_TIME_FORMAT));
							newEntry.setStop(endTime.format(DATE_TIME_FORMAT));

							final Title title = new Title();
							title.setvalue(item.getString("title"));
							newEntry.getTitle().add(title);

							final Desc description = new Desc();
							description.setvalue(item.getString("description"));
							newEntry.getDesc().add(description);

							final SubTitle subtitle = new SubTitle();
							subtitle.setvalue(item.getString("episodeTitle"));
							newEntry.getSubTitle().add(subtitle);

							xmlTv.getProgramme().add(newEntry);
							importCounter++;
						}
						
					}
					else {
						System.err.println(response);
					}
				} catch (URISyntaxException | IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if(importCounter > 0) {
				try {
					final JAXBContext context = JAXBContext.newInstance( Tv.class );
					final Marshaller m = context.createMarshaller();
					m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
					m.marshal( xmlTv, new FileOutputStream(config.getOutputFile()));
					
					System.out.println("Successfully converted " + importCounter + " entries.");
					exitCode = 0;
					
				} catch (JAXBException | FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		System.exit(exitCode);
	}

}
