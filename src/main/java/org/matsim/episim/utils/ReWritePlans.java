package org.matsim.episim.utils;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.episim.policy.Restriction;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

public class ReWritePlans {
	private static final Logger log = Logger.getLogger(ReadPlans.class);
	private static String fileNameInfected = "C:/GeoSimLab/episim_jlm/Input_data/analysis/initial_infected.csv";
	private static String filenameInPopulation = "C:/GeoSimLab/episim_jlm/Input_data/matsim_files/population1.0.xml.gz";
	private static String filenameOutPopulation = "C:/GeoSimLab/episim_jlm/Input_data/matsim_files/population1.0_district.xml.gz";
	public static void main(String[] args) {
		
		Population pop = PopulationUtils.readPopulation(filenameInPopulation);
		CSVReader csvReader = null;
		List<String> ids = new ArrayList<String>();
		try {
			csvReader = new CSVReaderBuilder(new FileReader(fileNameInfected)).withSkipLines(1).build();
			String[] row;
			
			while ((row = csvReader.readNext()) != null) {
				ids.add(row[3]);

			}
			csvReader.close();
		} catch (IOException e) {
			log.error("ERROR: Cannot read restrictions file: " + fileNameInfected);
		} catch (NumberFormatException e) {
			log.error("ERROR: Check format of restrictions file: " + fileNameInfected);
		} catch (CsvValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(Person person : pop.getPersons().values()) {
			if(ids.contains(person.getId().toString())){
				person.getAttributes().putAttribute("district", "yes");
			}else {
				person.getAttributes().putAttribute("district", "no");
			}
		}
		new PopulationWriter(pop).write(filenameOutPopulation);
	}
}
