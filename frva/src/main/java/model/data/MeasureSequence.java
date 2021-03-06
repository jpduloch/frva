/*
 *     This file is part of FRVA
 *     Copyright (C) 2018 Andreas Hüni
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package model.data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * The MeasurementSequence holds the effective raw data recorded.
 * Reflectance and Radiance are calculated on this object.
 * The MeasurementSeuence implements LazyLoading to handle the data from the stored files,
 * to reduce the memory needed.
 *   Metadata explained:
 *    0 Counter
 *    1 Date? YYMMDD
 *    2 hhmmss (internal clock)
 *    3 Mode (auto/manual/app)
 *    4 Integration time microseconds IT WR
 *    5 Integration time microsceconds IT VEG
 *    6 Time for one measurement miliseconds
 */
public class MeasureSequence {

  private final String sequenceUuid;
  private String[] metadata;
  private DataFile dataFile;
  private ReflectionIndices reflectionIndices;
  private BooleanProperty deleted;


  /**
   * Returns IT Veg from metadata in [ms].
   *
   * @return long value in [ms]
   */
  public long getIntegrationTimeVeg() {
    return Long.parseLong(metadata[7]) / 1000;
  }

  /**
   * Returns IT Wr from metadata in [ms].
   *
   * @return long value in [ms]
   */
  public long getIntegrationTimeWr() {
    return Long.parseLong(metadata[5]) / 1000;
  }


  public enum SequenceKeyName {
    VEG,
    WR,
    WR2,
    DC_VEG,
    DC_WR,
    RADIANCE_VEG,
    RADIANCE_WR,
    REFLECTANCE
  }

  public MeasureSequence() {
    sequenceUuid = UUID.randomUUID().toString();
  }

  /**
   * Constructor, same as above.
   *
   * @param metadata String containing the metadata.
   * @param dataFile contains the path to the datafiles.
   */
  public MeasureSequence(String[] metadata, DataFile dataFile) {
    this();
    this.metadata = metadata;
    this.dataFile = dataFile;
    this.deleted = new SimpleBooleanProperty(false);
  }

  /**
   * Reads in the measurement-data from the file system.
   *
   * @return the read in measurements in a map.
   */

  public Map<SequenceKeyName, double[]> getData() {
    Map<SequenceKeyName, double[]> measurements = FileInOut.readInMeasurement(this);

    return measurements.isEmpty() ? null : measurements;
  }


  /**
   * Creates csv-format from a measurementSequenz.
   *
   * @return a string containing the data.
   */
  public String getCsv() {
    StringBuilder sb = new StringBuilder();
    Arrays.stream(metadata).forEach(a -> sb.append(a + ";"));

    Map<SequenceKeyName, double[]> measurements = getData();

    sb.append("\n" + "WR" + ";");
    Arrays.stream(measurements.get(SequenceKeyName.WR)).forEach(a -> sb.append((int) a + ";"));
    sb.append(";");

    sb.append("\n" + "VEG" + ";");
    Arrays.stream(measurements.get(SequenceKeyName.VEG)).forEach(a -> sb.append((int) a + ";"));
    sb.append(";");

    sb.append("\n" + "WR2" + ";");
    Arrays.stream(measurements.get(SequenceKeyName.WR2)).forEach(a -> sb.append((int) a + ";"));
    sb.append(";");

    sb.append("\n" + "DC_WR" + ";");
    Arrays.stream(measurements.get(SequenceKeyName.DC_WR)).forEach(a -> sb.append((int) a + ";"));
    sb.append(";");

    sb.append("\n" + "DC_VEG" + ";");
    Arrays.stream(measurements.get(SequenceKeyName.DC_VEG)).forEach(a -> sb.append((int) a + ";"));
    //sb.deleteCharAt(sb.length() - 1);
    sb.append(";");

    sb.append("\n");
    return sb.toString();
  }

  public String getId() {
    return metadata[0];
  }

  /**
   * Getter for the Time of the MeasurementSequence.
   *
   * @return the Time as String of Type HH:MM.
   */
  public String getTime() {
    String timestamp = metadata[2];

    if (timestamp.length() == 5) {
      return "0" + timestamp.substring(0, 1)
          + ":" + timestamp.substring(1, 3) + ":" + timestamp.substring(3, 5);
    }
    if (timestamp.length() == 6) {
      return timestamp.substring(0, 2)
          + ":" + timestamp.substring(2, 4) + ":" + timestamp.substring(4, 6);
    }
    throw new IllegalArgumentException();
  }

  /**
   * Getter for the Hour (Timestamp) o the MeasurementSequence.
   *
   * @return the Hour as int.
   */
  public int getHour() {
    String timestamp = metadata[2];

    if (timestamp.length() == 5) {
      return Integer.parseInt(timestamp.substring(0, 1));
    }
    if (timestamp.length() == 6) {
      return Integer.parseInt(timestamp.substring(0, 2));
    }
    throw new IllegalArgumentException();
  }

  /**
   * Getter for the Serial of the containing SD-Card.
   *
   * @return Serial as String.
   */

  public String getSerial() {
    return metadata[14];
  }

  /**
   * Getter for the Date of the Sequence.
   *
   * @return Date as String of Type YY-MM-DD.
   */
  public String getDate() {
    return metadata[1].substring(0, 2) + "-" + metadata[1].substring(2, 4) + "-"
        + metadata[1].substring(4, 6);
  }

  /**
   * Calculates the Radiance of this MeasurementSequence.
   *
   * @return A Map with the Keys VEG and WR.
   */
  public Map<SequenceKeyName, double[]> getRadiance() {
    /*
    Radiance L
      Data:
        L(VEG) = ((DN(VEG) - DC(VEG)) * FLAMEradioVEG_2017-08-03) / IntegrationTimeVEG
        L(WR) = ((DN(WR) - DC(WR)) * FLAMEradioWR_2017-08-03) / IntegrationTimeWR
      X-Axis: Wavelength[Nanometers]/Bands[dn]
      Y-Axis: W/( m²sr nm) which can also be written as W m-2 sr-1 nm-1
     */

    double[] waveCalibration = getWlF1Calibration();
    double[] vegCalibration = getDwCoefF1Calibration();
    double[] wrCalibration = getUpCoefF1Calibration();

    Map<SequenceKeyName, double[]> measurements = getData();

    double[] vegs = measurements.get(SequenceKeyName.VEG);
    double[] dcVegs = measurements.get(SequenceKeyName.DC_VEG);

    double[] wrs = measurements.get(SequenceKeyName.WR);
    double[] dcWrs = measurements.get(SequenceKeyName.DC_WR);

    double[] vegRadiance = new double[waveCalibration.length];
    double[] wrRadiance = new double[waveCalibration.length];

    for (int i = 0; i < waveCalibration.length; i++) {
      wrRadiance[i] = (
          (wrs[i] - dcWrs[i]) / wrCalibration[i]) / (Double.parseDouble(metadata[5]));
      vegRadiance[i] = (
          (vegs[i] - dcVegs[i]) / vegCalibration[i]) / (Double.parseDouble(metadata[7]));
    }

    Map<SequenceKeyName, double[]> radianceMap = new HashMap<>();
    radianceMap.put(SequenceKeyName.RADIANCE_VEG, vegRadiance);
    radianceMap.put(SequenceKeyName.RADIANCE_WR, wrRadiance);

    return radianceMap;
  }


  /**
   * Calculates the Reflectance of this MeasurementSequence.
   *
   * @return A DoubleArray.
   */
  public Map<SequenceKeyName, double[]> getReflectance() {
    /*
    Reflectance R
      Data:   R(VEG) = L(VEG) / L(WR)
      X-Axis: Wavelength[Nanometers]/Bands[dn]
      Y-Axis: ReflectanceFactor (none)
     */
    Map<SequenceKeyName, double[]> radianceMap = this.getRadiance();

    double[] vegRadiance = radianceMap.get(SequenceKeyName.RADIANCE_VEG);
    double[] wrRadiance = radianceMap.get(SequenceKeyName.RADIANCE_WR);

    double[] reflection = new double[vegRadiance.length];

    for (int i = 0; i < reflection.length; i++) {
      reflection[i] = vegRadiance[i] / wrRadiance[i];
    }

    Map<SequenceKeyName, double[]> reflectionMap = new HashMap<>();
    reflectionMap.put(SequenceKeyName.REFLECTANCE, reflection);

    reflectionIndices = new ReflectionIndices(reflection, getWlF1Calibration());

    return reflectionMap;

  }

  /**
   * Calculates the indices.
   * Based on the reflectance factors R:
   * TCARI : 3 × ((R700 – R760) – 0.2 × (R700 – R550) × (R700/R670))
   * PRI: (R531 -R570 )/(R531 +R570 )
   * NDVI: (R920 - R696) / (R920 + R696)
   *
   * @return ReflectionIndices.
   */
  public ReflectionIndices getIndices() {
    if (reflectionIndices == null) {
      Map<SequenceKeyName, double[]> reflectance = getReflectance();

      reflectionIndices = new ReflectionIndices(reflectance.get(SequenceKeyName.REFLECTANCE),
          getWlF1Calibration());

    }
    return reflectionIndices;
  }


  public double[] getWlF1Calibration() {
    return dataFile.getSdCard().getCalibrationFile().getWlF1();
  }

  public double[] getDwCoefF1Calibration() {
    return dataFile.getSdCard().getCalibrationFile().getDwCoefF1();
  }

  public double[] getUpCoefF1Calibration() {
    return dataFile.getSdCard().getCalibrationFile().getUpCoefF1();
  }

  public String getSequenceUuid() {
    return sequenceUuid;
  }


  public DataFile getDataFile() {
    return dataFile;
  }

  /**
   * Returns Year created as a String.
   *
   * @return year created.
   */
  public String getYear() {
    return "20" + this.getDate().substring(0, 2);
  }

  /**
   * Getter for Month of measurement.
   *
   * @return month as String.
   */
  public String getMonth() {
    LocalDate localDate = LocalDate.parse(getDate(), DateTimeFormatter.ofPattern("yy-MM-dd"));

    return localDate.format(DateTimeFormatter.ofPattern("MMM")).toUpperCase();
  }


  /**
   * Getter for the metadata.
   *
   * @return metadata as String.
   */
  public String getMetadataAsString() {
    StringBuilder sb = new StringBuilder();
    for (String s : metadata) {
      sb.append(s);
      sb.append(";");
    }
    return sb.toString();
  }

  public SdCard getContainingSdCard() {
    return this.dataFile.getSdCard();
  }

  public void setDeleted(boolean deleted) {
    this.deleted.set(deleted);
  }

  public boolean isDeleted() {
    return deleted.get();
  }

  public BooleanProperty deletedProperty() {
    return deleted;
  }

  public void setMetadata(String[] metadata) {
    this.metadata = metadata;
  }

  public String[] getMetadata() {
    return metadata;
  }

  @Override
  public int hashCode() {
    return (((Integer.parseInt(metadata[0])) + Integer.parseInt(metadata[1]) * 31)
        + Integer.parseInt(metadata[2]) * 31);
  }
}