package model.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DataFile {

  private final File originalFile;
  private final SdCard sdCard;
  private final String containingFolderName;
  private List<MeasureSequence> measureSequences = new LinkedList<>();

  /**
   * Constructor.
   *
   * @param filename Name of the file
   * @param sdCard   The SDCARD the datafile belongs to
   */
  public DataFile(SdCard sdCard, String containingFolderName, File filename) {
    this.originalFile = filename;
    this.sdCard = sdCard;
    this.containingFolderName=containingFolderName;

    List<String> fileContent = new ArrayList<>();
    String line = "";
    try (BufferedReader br = new BufferedReader(new FileReader(filename));) {
      while ((line = br.readLine()) != null) {
        if (!"".equals(line)) {
          if (Character.isDigit(line.charAt(0)) && fileContent.size() > 0) {
            measureSequences.add(new MeasureSequence(fileContent, sdCard));
            fileContent.clear();
          }
          fileContent.add(line);
        }
      }
      measureSequences.add(new MeasureSequence(fileContent, sdCard));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public List<MeasureSequence> getMeasureSequences() {
    return measureSequences;
  }

  public String getOriginalFileName() {
    return originalFile.getName();
  }

  public String getFolderName(){
    return this.containingFolderName;
  }
}
