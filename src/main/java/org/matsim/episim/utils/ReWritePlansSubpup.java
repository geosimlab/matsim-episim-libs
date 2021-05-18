package org.matsim.episim.utils;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.io.PopulationWriter;

public class ReWritePlansSubpup {
	private static final Logger log = Logger.getLogger(ReadPlans.class);
	private static String filenameInPopulation = "C:/GeoSimLab/episim_jlm/Input_data/matsim_files/population1.0.xml.gz";
	private static String filenameOutPopulation = "C:/GeoSimLab/episim_jlm/Input_data/matsim_files/population1.0_district_subpop.xml.gz";
	public static void main(String[] args) {
		
		Population pop = PopulationUtils.readPopulation(filenameInPopulation);
		for(Person person : pop.getPersons().values()) {
			
			String sector = person.getAttributes().getAttribute("subpopulation").toString();
			person.getAttributes().putAttribute("district", sector);

		}
		new PopulationWriter(pop).write(filenameOutPopulation);
	}
}
