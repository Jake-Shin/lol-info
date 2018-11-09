package telegram_bot.telegram_bot;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;


import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;

public class LolInfoBot extends TelegramLongPollingBot{

	JSONParser parser = new JSONParser();
	
	public void onUpdateReceived(Update update) {
		ApiConfig config = new ApiConfig().setKey("RGAPI-3ae52187-b85e-4788-b0b4-11b60206ee9c");
		RiotApi api = new RiotApi(config);
		
		//test
		try {
			Object obj = parser.parse(new FileReader("../staticdata/data/ko_KR/champion.json"));
			JSONObject jsonObject = (JSONObject) obj;
			
			Map<String, Object> map = new HashMap<String, Object>();
			JSONObject jsonObj = (JSONObject) jsonObject.get("data");
			
			Iterator<String> keysItr = jsonObj.keySet().iterator();
			while (keysItr.hasNext()) {
				String key = keysItr.next();
				Object value = jsonObj.get(key);
				
				System.out.println(key + " : " + value);
				
				if (value instanceof JSONArray) {
					System.out.println("json array");
				} else if (value instanceof JSONObject) {
					System.out.println("json object");
				}
				
				map.put(key, value);
			}
		} catch (IOException | ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("------------------------------------------------------");
		}
		//test

		Summoner summoner = null;
		try {
			summoner = api.getSummonerByName(Platform.KR, "핀구");
		} catch (RiotApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Name: " + summoner.getName());
		System.out.println("Summoner ID: " + summoner.getId());
		System.out.println("Account ID: " + summoner.getAccountId());
		System.out.println("Summoner Level: " + summoner.getSummonerLevel());
		System.out.println("Profile Icon ID: " + summoner.getProfileIconId());
		
	}

	public String getBotUsername() {
		// TODO Auto-generated method stub
		return "itlol_bot";
	}

	@Override
	public String getBotToken() {
		// TODO Auto-generated method stub
		return "687333359:AAHen9ZFe5bAQLbahX98QpuIvitXSDqL-iQ";
	}

}
