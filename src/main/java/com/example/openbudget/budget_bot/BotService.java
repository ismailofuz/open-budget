package com.example.openbudget.budget_bot;

import com.example.openbudget.entity.BotUser;
import com.example.openbudget.entity.Project;
import com.example.openbudget.entity.enums.BotState;
import com.example.openbudget.repository.BotUserRepository;
import com.example.openbudget.repository.ProjetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BotService {
    private final BotUserRepository botUserRepository;
    private final ProjetRepository projectRepository;

    public SendMessage start(Update update) {
        BotUser currentUser = findCurrentUser(update);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(currentUser.getChatId());
        sendMessage.setText("Please enter id of the title");

        return sendMessage;
    }

    public BotUser findCurrentUser(Update update) {
        Long chatId = update.getMessage().getChatId();

        Optional<BotUser> optionalBotUser = botUserRepository.findByChatId(chatId.toString());
        if (optionalBotUser.isPresent()) {
            return optionalBotUser.get();
        } else {
            BotUser botUser = new BotUser();
            botUser.setChatId(chatId.toString());
            botUser.setStatus(BotState.SELECT_PROJECT);

            return botUserRepository.save(botUser);
        }
    }

    private void saveBotUserChanges(BotUser botUser) {
        Optional<BotUser> optionalUser = botUserRepository.findById(botUser.getId());
        if (optionalUser.isPresent()) {
            botUserRepository.save(botUser);
        }
    }

    public SendMessage chooseProject(BotUser currentUser, Update update) {
        Integer id;
        try {
            id = Integer.valueOf(update.getMessage().getText());
        } catch (NumberFormatException e) {
            return new SendMessage(currentUser.getChatId(), "Id must be a number");
        }
        Optional<Project> byTitleId = projectRepository.findByTitleId(id);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(currentUser.getChatId());
        if (!byTitleId.isPresent()) {
            sendMessage.setText("project not found");
            return sendMessage;
        }
        Project project = byTitleId.get();
        currentUser.setStatus(BotState.ASK_QUESTION);
        botUserRepository.save(currentUser);

        String project_info = "\uD83D\uDCCA Project_name : "+project.getTitle();
        sendMessage.setText(project_info);

        // reply
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setInputFieldPlaceholder("Enter something");

        List<KeyboardRow> rowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        KeyboardButton button1 = new KeyboardButton("✅ Yes");
        KeyboardButton button2 = new KeyboardButton("❌ No");

        row.add(button1);
        row.add(button2);

        rowList.add(row);
        replyKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        return sendMessage;
    }

}
