package org.matsim.episim.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PersonUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.utils.objectattributes.attributable.Attributes;

import com.opencsv.CSVWriter;

public class ReadPlans {
	private static final Logger log = Logger.getLogger(ReadPlans.class);
	public static void main(String[] args) {
		Population pop = PopulationUtils.readPopulation("C:/GeoSimLab/episim_jlm/Input_data/matsim_files/population1.0.xml.gz");
		Map<Id<Person>, ? extends Person> ids = pop.getPersons();
		File f = new File("C:/GeoSimLab/episim_jlm/Input_data/analysis/11.pop.csv");
		String filename = "";
		if (f.getParent() != null)
			filename = f.getParent() + "/";
		filename = filename  + f.getName();
		CSVWriter csvWriter = null;
		try {
			csvWriter = new CSVWriter(new FileWriter(filename));
			csvWriter.writeNext(new String[]{"id", "age", "caravail", "license",
					"sex","homeFacilityRefId","subpopulation","hhid"}, false);
			for(Id<Person> personId:ids.keySet()) {
				Person person = ids.get(personId);
				Integer age = PersonUtils.getAge(person);
				String caravail = PersonUtils.getCarAvail(person);
				String license = PersonUtils.getLicense(person);
				String sex = PersonUtils.getSex(person);
				String subpopulation = person.getAttributes().getAttribute("subpopulation").toString();
				String homeFacilityRefId;
				String hhid;
				if (person.getAttributes().getAttribute("homeFacilityRefId") == null) {
					homeFacilityRefId = "";
				}else {
					homeFacilityRefId = person.getAttributes().getAttribute("homeFacilityRefId").toString();	
				}
				if(person.getAttributes().getAttribute("hhid") == null) {
					hhid = "";
				}else {
					hhid = person.getAttributes().getAttribute("hhid").toString();	
				}
				System.out.println(person.getId().toString() + "," + age + "," + caravail + "," + license + "," +
						sex + "," + homeFacilityRefId + "," + subpopulation + "," + hhid);
				String[] csvRow = {person.getId().toString(),age.toString(),caravail,license,sex,homeFacilityRefId,subpopulation,hhid};
				csvWriter.writeNext(csvRow, false);				

			}
						csvWriter.close();
		} catch (IOException e) {
			log.error("ERROR: Cannot write nodes file: " + filename);
		} catch (NumberFormatException e) {
			log.error("ERROR: Check format of nodes file: " + filename);
		}
	}

}
