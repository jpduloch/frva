package controller.util.liveviewparser;

import java.time.LocalDateTime;
import model.FrvaModel;

public class CommandT extends AbstractCommand {
  private final LocalDateTime dateTime;
  StringBuilder stringBuilder = new StringBuilder();

  public CommandT(LiveDataParser liveDataParser, FrvaModel model, LocalDateTime dateTime) {
    super(liveDataParser, model);
    this.dateTime = dateTime;
  }

  @Override
  public void sendCommand() {
    liveDataParser.executeCommand(Commands.T.toString());
  }

  @Override
  public void receive(char read) {
    stringBuilder.append((char) read);

    if (stringBuilder.toString().contains("Enter year(0 - 99)")) {
      String yeartouse;
      if (dateTime == null) {
        yeartouse = String.valueOf(LocalDateTime.now().getYear());
      } else {
        yeartouse = String.valueOf(dateTime.getYear());
      }
      liveDataParser.executeCommand(yeartouse);
      stringBuilder = new StringBuilder();
    }

    if (stringBuilder.toString().contains("Enter month(1 - 12)")) {
      String stringToSend;
      if (dateTime == null) {
        stringToSend = String.valueOf(LocalDateTime.now().getMonthValue());
      } else {
        stringToSend = String.valueOf(dateTime.getMonthValue());
      }
      liveDataParser.executeCommand(stringToSend);
      stringBuilder = new StringBuilder();
    }

    if (stringBuilder.toString().contains("Enter day(1 - 31)")) {
      String stringToSend;
      if (dateTime == null) {
        stringToSend = String.valueOf(LocalDateTime.now().getDayOfMonth());
      } else {
        stringToSend = String.valueOf(dateTime.getDayOfMonth());
      }
      liveDataParser.executeCommand(stringToSend);
      stringBuilder = new StringBuilder();
    }

    if (stringBuilder.toString().contains("Enter hour(0 - 23)")) {
      String stringToSend;
      if (dateTime == null) {
        stringToSend = String.valueOf(LocalDateTime.now().getHour());
      } else {
        stringToSend = String.valueOf(dateTime.getHour());
      }
      liveDataParser.executeCommand(stringToSend);
      stringBuilder = new StringBuilder();
    }

    if (stringBuilder.toString().contains("Enter minute(0 - 59)")) {
      String stringToSend;
      if (dateTime == null) {
        stringToSend = String.valueOf(LocalDateTime.now().getMinute());
      } else {
        stringToSend = String.valueOf(dateTime.getMinute());
      }
      liveDataParser.executeCommand(stringToSend);
      stringBuilder = new StringBuilder();
    }

    if (stringBuilder.toString().contains("Enter second(0 - 59)")) {
      String stringToSend;
      if (dateTime == null) {
        stringToSend = String.valueOf(LocalDateTime.now().getSecond());
      } else {
        stringToSend = String.valueOf(dateTime.getSecond());
      }
      liveDataParser.executeCommand(stringToSend);
      stringBuilder = new StringBuilder();
    }

    if (stringBuilder.toString().contains("Send Y to set time now")) {
      liveDataParser.executeCommand("Y");
      liveDataParser.runNextCommand();
    }
  }
}
