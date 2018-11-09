package telegram_bot.telegram_bot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	ApiContextInitializer.init();
        TelegramBotsApi api = new TelegramBotsApi();
        try {
        	api.registerBot(new LolInfoBot());
        } catch	(TelegramApiException e) {
        	e.printStackTrace();
        }
    }
}
