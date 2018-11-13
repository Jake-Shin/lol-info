package telegram_bot.telegram_bot;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiAsync;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.league.constant.LeagueQueue;
import net.rithms.riot.api.endpoints.league.dto.LeaguePosition;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.api.request.AsyncRequest;
import net.rithms.riot.api.request.RequestAdapter;
import net.rithms.riot.constant.Platform;

public class LolInfoBot extends TelegramLongPollingBot{

	JSONParser parser = new JSONParser();
	String resourcePath = "/main/java/props/key.properties";
	Properties props = new Properties();
	
	// Inner class to store information in
	private class ExtendedSummoner {
		public Summoner summoner;
		public LeaguePosition leagueSolo;
		public LeaguePosition leagueFlexSR;
		public LeaguePosition leagueFlexTT;
	}
	
	public void onUpdateReceived(Update update) {
		ApiConfig config = new ApiConfig().setKey(props.getProperty("RiotApiKey"));
		RiotApi api = new RiotApi(config);
		RiotApiAsync apiAsync = api.getAsyncApi();
		
		final ExtendedSummoner eSummoner = new ExtendedSummoner(); // Object where we want to store the data
		
		String sInputText = update.getMessage().getText();
		
		//test
		/*try {
			Object obj = parser.parse(new FileReader(System.getProperty("user.dir") + "/src/main/java/telegram_bot/staticdata/data/ko_KR/champion.json"));
			System.out.println("-==-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-");
			System.out.println(System.getProperty("user.dir"));
			JSONObject jsonObject = (JSONObject) obj;
			
			Map<String, Object> map = new HashMap<String, Object>();
			JSONObject jsonObj = (JSONObject) jsonObject.get("data");
			Iterator<String> keysItr = jsonObj.keySet().iterator();
			while (keysItr.hasNext()) {
				String key = keysItr.next();
				Object value = jsonObj.get(key);
				
				if ("Aatrox".equals(key)) {
					System.out.println(key + " :=> " + value);
				}
				
				if (value instanceof JSONArray) {
					System.out.println("json array");
				} else if (value instanceof JSONObject) {
					System.out.println("json object");
				}
				
				//map.put(key, value);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("------------------------------------------------------");
		}*/
		//test
		
		if (sInputText.contains("/info")) {
			// Asynchronously get summoner information
			Summoner summoner = null;
			String sText = sInputText.replaceFirst("/info", "").trim();
			try {
				summoner = api.getSummonerByName(Platform.KR, sText);
				SendMessage msg = new SendMessage().setParseMode(ParseMode.HTML).disableWebPagePreview();
				msg.setChatId(update.getMessage().getChatId());
				
				AsyncRequest requestSummoner = apiAsync.getSummoner(Platform.KR, summoner.getId());
				requestSummoner.addListeners(new RequestAdapter() {
					@Override
					public void onRequestSucceeded(AsyncRequest request) {
						eSummoner.summoner = request.getDto();
					}
				});
				
				// Asynchronously get league information
				AsyncRequest requestLeague = apiAsync.getLeaguePositionsBySummonerId(Platform.KR, summoner.getId());
				requestLeague.addListeners(new RequestAdapter() {
					@Override
					public void onRequestSucceeded(AsyncRequest request) {
						Set<LeaguePosition> leaguePositions = request.getDto();
						if (leaguePositions == null || leaguePositions.isEmpty()) {
							return;
						}
						for (LeaguePosition leaguePosition : leaguePositions) {
							if (leaguePosition.getQueueType().equals(LeagueQueue.RANKED_SOLO_5x5.name())) {
								eSummoner.leagueSolo = leaguePosition;
							} else if (leaguePosition.getQueueType().equals(LeagueQueue.RANKED_FLEX_SR.name())) {
								eSummoner.leagueFlexSR = leaguePosition;
							} else if (leaguePosition.getQueueType().equals(LeagueQueue.RANKED_FLEX_TT.name())) {
								eSummoner.leagueFlexTT = leaguePosition;
							}
						}
					}
				});
				
				try {
					// Wait for all asynchronous requests to finish
					apiAsync.awaitAll();
				} catch (InterruptedException e) {
					// We can use the Api's logger
					RiotApi.log.log(Level.SEVERE, "Waiting Interrupted", e);
				}
				
				// Print information stored in eSummoner
				System.out.println("Summoner name: " + eSummoner.summoner.getName());
				String sSoloInfoText = "";
				String sFlexSRInfoText = "";
				String sFlexTTInfoText = "";
				
				System.out.print("Solo Rank: ");
				if (eSummoner.leagueSolo == null) {
					System.out.println("unranked");
				} else {
					System.out.println(eSummoner.leagueSolo.getTier() + " " + eSummoner.leagueSolo.getRank());
					double iPOV = (((float)eSummoner.leagueSolo.getWins()) / ((float)eSummoner.leagueSolo.getWins() + (float)eSummoner.leagueSolo.getLosses())) * 100;
					sSoloInfoText = "<b>Solo Rank</b>" + "\n"
									+ "  <b>Tier</b> : " + eSummoner.leagueSolo.getTier() + " " + eSummoner.leagueSolo.getRank() + "\n"
									+ "  <b>Wins</b> : " + eSummoner.leagueSolo.getWins() + "\n"
									+ "  <b>Defeats</b> : " + eSummoner.leagueSolo.getLosses() + " <b>POV</b> : " + String.format("%.2f", iPOV) + "%\n"
									+ "  <b>League points</b> : " + eSummoner.leagueSolo.getLeaguePoints();
				}

				System.out.print("Flex SR Rank: ");
				if (eSummoner.leagueFlexSR == null) {
					System.out.println("unranked");
				} else {
					System.out.println(eSummoner.leagueFlexSR.getTier() + " " + eSummoner.leagueFlexSR.getRank());
					double iPOV = (((float)eSummoner.leagueFlexSR.getWins()) / ((float)eSummoner.leagueFlexSR.getWins() + (float)eSummoner.leagueFlexSR.getLosses())) * 100;
					sFlexSRInfoText = "<b>Flex Rank</b>" + "\n"
							+ "  <b>Tier</b> : " + eSummoner.leagueFlexSR.getTier() + " " + eSummoner.leagueFlexSR.getRank() + "\n"
							+ "  <b>Wins</b> : " + eSummoner.leagueFlexSR.getWins() + "\n"
							+ "  <b>Defeats</b> : " + eSummoner.leagueFlexSR.getLosses() + " <b>POV</b> : " + String.format("%.2f", iPOV) + "%\n"
							+ "  <b>League points</b> : " + eSummoner.leagueFlexSR.getLeaguePoints();
				}

				/*System.out.print("Flex TT Rank: ");
				if (eSummoner.leagueFlexTT == null) {
					System.out.println("unranked");
				} else {
					System.out.println(eSummoner.leagueFlexTT.getTier() + " " + eSummoner.leagueFlexTT.getRank());
					sFlexTTInfoText = "<b>뒤틀린숲</b>" + "\n"
							+ "  <b>Tier</b> : " + eSummoner.leagueFlexTT.getTier() + " " + eSummoner.leagueFlexTT.getRank() + "\n"
							+ "  <b>Wins</b> : " + eSummoner.leagueFlexTT.getWins() + "\n"
							+ "  <b>Defeats</b> : " + eSummoner.leagueFlexTT.getLosses() + "\n"
							+ "  <b>League points</b> : " + eSummoner.leagueFlexTT.getLeaguePoints();
				}*/
				
				msg.setText("<b>Name</b> : <a href='www.op.gg/summoner/userName= "+ summoner.getName() + "'>" + summoner.getName() + "</a>\n"
					     + "<b>Level</b> : <i>" + summoner.getSummonerLevel() + "</i>"
					     + "\n\n"
					     + sSoloInfoText
					     + "\n\n"
					     + sFlexSRInfoText);
				//sendImageUploadingAFile(System.getProperty("user.dir") + "/src/main/java/telegram_bot/staticdata/img/profileicon/" + summoner.getProfileIconId() + ".png", update.getMessage().getChatId().toString());
				sendProFileImage("opgg-static.akamaized.net/images/profile_icons/profileIcon" + summoner.getProfileIconId() + ".jpg", update.getMessage().getChatId().toString());
				execute(msg);
			} catch (RiotApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("1");
			} catch (TelegramApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("2");
			}
		}
	}

	public String getBotUsername() {
		// TODO Auto-generated method stub
		return "itlol_bot";
	}

	@Override
	public String getBotToken() {
		// TODO Auto-generated method stub
		return props.getProperty("TelegramApiKey");
	}
	
	
	public void sendProFileImage(String url, String chatId) {
        // Create send method
        SendPhoto sendPhotoRequest = new SendPhoto();
        // Set destination chat id
        sendPhotoRequest.setChatId(chatId);
        // Set the photo file as a new photo (You can also use InputStream with a method overload)
        sendPhotoRequest.setPhoto(url);
        try {
            // Execute the method
            execute(sendPhotoRequest);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
