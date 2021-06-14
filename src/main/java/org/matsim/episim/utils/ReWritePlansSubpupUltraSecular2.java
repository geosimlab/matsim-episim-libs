package org.matsim.episim.utils;

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

public class ReWritePlansSubpupUltraSecular2 {
	private static final Logger log = Logger.getLogger(ReadPlans.class);
	private static String filenameInPopulation = "C:/GeoSimLab/episim_jlm/Input_data/matsim_files/population1.0.xml.gz";
	private static String filenameOutPopulation = "C:/GeoSimLab/episim_jlm/Input_data/matsim_files/population1.0_district_subpop_ultra_secular_equal.xml.gz";
	public static void main(String[] args) {
		
		Population pop = PopulationUtils.readPopulation(filenameInPopulation);
		int totsick = 100;
		int n_ultra = (int) Math.round(totsick * 0.6);
		int n_secular = (int) Math.round(totsick * 0.4);
//		Random random = new Random();
		
//		totsick = n_ultra + n_secular;
//		int tot_ultra = pop.getPersons().values().stream().filter(p -> p.getAttributes().getAttribute("subpopulation").equals("internal_Ultra-Orthodox")).collect(Collectors.toList()).size();
//		int tot_secular = pop.getPersons().values().stream().filter(p -> p.getAttributes().getAttribute("subpopulation").equals("internal_Ultra-Orthodox")).collect(Collectors.toList()).size();
//		IntStream stops_secular = random.ints(n_secular, 0, tot_ultra);
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
//		System.out.println(list_ultra.get(0).getId().toString());
//		List<Id<Person>> list = new ArrayList<Id<Person>>(pop.getPersons().keySet());
//		Collections.shuffle(list);
//		for(int i = 0;i < n_ultra; i++) {
//			list.get(i);
//		}
//		for(Person person : pop.getPersons().values()) {
//			String sector = person.getAttributes().getAttribute("subpopulation").toString();
//			if(sector.equals("internal_Secular")|| sector.equals("internal_Ultra-Orthodox")) {
//				person.getAttributes().putAttribute("district", "yes");	
//			}else {
//				person.getAttributes().putAttribute("district", "no");
//			}
//			
//
//		}
		new PopulationWriter(pop).write(filenameOutPopulation);
	}
}
