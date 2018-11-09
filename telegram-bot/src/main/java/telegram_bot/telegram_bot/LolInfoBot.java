package telegram_bot.telegram_bot;

import java.io.File;
import java.io.FileReader;

import org.json.simple.parser.JSONParser;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;


import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;

public class LolInfoBot extends TelegramLongPollingBot{

	File path = new File(LolInfoBot.class.getResource("../../staticdata/champion.json").getFile());
	JSONParser parser = new JSONParser();
	
	public void onUpdateReceived(Update update) {
		ApiConfig config = new ApiConfig().setKey("RGAPI-3ae52187-b85e-4788-b0b4-11b60206ee9c");
		RiotApi api = new RiotApi(config);

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
		
		Object obj = parser.parse(new FileReader(LolInfoBot.class.getResource("../../staticdata/champion.json")));
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
