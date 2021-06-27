package org.matsim.episim.utils;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

public class ReWritePlansSubpupUltraSecular3 {
	private static final Logger log = Logger.getLogger(ReadPlans.class);
	private static String filenameInPopulation = "C:/GeoSimLab/episim_jlm/Input_data/matsim_files/population1.0.xml.gz";
//	private static String filenameInRecoverd = "C:/GeoSimLab/episim_jlm/Input_data/matsim_files/population1.0.xml.gz";
	private static String filenameOutPopulation = "C:/GeoSimLab/episim_jlm/Input_data/matsim_files/population1.0_district_subpop_ultra_secular_equal_recovered.xml.gz";
	public static void main(String[] args) {
		
		Population pop = PopulationUtils.readPopulation(filenameInPopulation);
		int totsick = 100;
		int n_ultra = (int) Math.round(totsick * 0.6);
		int n_secular = (int) Math.round(totsick * 0.4);
		List<? extends Person> list_ultra = pop.getPersons().values().stream().filter(p -> p.getAttributes().getAttribute("subpopulation").equals("internal_Ultra-Orthodox")).collect(Collectors.toList());
		Collections.shuffle(list_ultra);
		List<? extends Person> list_secular = pop.getPersons().values().stream().filter(p -> p.getAttributes().getAttribute("subpopulation").equals("internal_Secular")).collect(Collectors.toList());
		Collections.shuffle(list_secular );
		for(int i = 0; i < n_ultra; i++) {
			Id<Person> id = list_ultra.get(i).getId();
			pop.getPersons().get(id).getAttributes().putAttribute("district", "yes");
		}
		for(int i = 0; i < n_secular; i++) {
			Id<Person> id = list_secular.get(i).getId();
			pop.getPersons().get(id).getAttributes().putAttribute("district", "yes");
		}
//		read csv with ids
//		CSVReader csvReader = null;
//		try {
//			csvReader = new CSVReaderBuilder(new FileReader(inputResterctions)).withSkipLines(1).build();
//			String[] row;
//			while ((row = csvReader.readNext()) != null) {
//				LocalDate date = LocalDate.of(Integer.parseInt(row[2]), Integer.parseInt(row[3]),
//						Integer.parseInt(row[4]));
//				Restriction fraction = Restriction.of(Double.parseDouble(row[1]));
//				String activity = row[0];
//				policy = policy.restrict(date, fraction, activity);
//				System.out.println(row[0]);					
//			}
//			csvReader.close();
//		} catch (IOException e) {
//			log.error("ERROR: Cannot read restrictions file: " + inputResterctions);
//		} catch (NumberFormatException e) {
//			log.error("ERROR: Check format of restrictions file: " + inputResterctions);
//		} catch (CsvValidationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return policy;
		int rec_ultra = 643;//15.6 
		int rec_secular = 260; //15.6 
		for(int i = n_ultra + rec_ultra; i < n_ultra; i++) {
			Id<Person> id = list_ultra.get(i).getId();
			pop.getPersons().get(id).getAttributes().putAttribute("recovered", "yes");
		}
		for(int i = n_secular; i < n_secular + rec_secular ; i++) {
			Id<Person> id = list_secular.get(i).getId();
			pop.getPersons().get(id).getAttributes().putAttribute("recovered", "yes");
		}
		
		new PopulationWriter(pop).write(filenameOutPopulation);
	}
}
