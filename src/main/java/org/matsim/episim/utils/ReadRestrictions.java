package org.matsim.episim.utils;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;

import org.matsim.episim.policy.FixedPolicy;
import org.matsim.episim.policy.FixedPolicy.ConfigBuilder;
import org.matsim.episim.policy.Restriction;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.typesafe.config.Config;

import org.apache.log4j.Logger;

public class ReadRestrictions {
	private static final Logger log = Logger.getLogger(ReadRestrictions.class);
	private static String fileName = "C:/GeoSimLab/episim_jlm/Input_data/raw/restrictions.csv";
	public static Config restrictions() {
		ConfigBuilder policy = FixedPolicy.config();
		CSVReader csvReader = null;
		try {
			csvReader = new CSVReaderBuilder(new FileReader(fileName)).withSkipLines(1).build();
			String[] row;
			while ((row = csvReader.readNext()) != null) {
				LocalDate date = LocalDate.of(Integer.parseInt(row[2]), Integer.parseInt(row[3]), Integer.parseInt(row[4]));
				Restriction fraction = Restriction.of(Integer.parseInt(row[1]));
				String activity = row[0];
				policy.restrict(date, fraction, activity);
				System.out.println(row[0]);

			}
			csvReader.close();
		} catch (IOException e) {
			log.error("ERROR: Cannot read restrictions file: " + fileName);
		} catch (NumberFormatException e) {
			log.error("ERROR: Check format of restrictions file: " + fileName);
		} catch (CsvValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return policy.build();
	}
	public static void main(String[] args) {
		restrictions();
	}
}
