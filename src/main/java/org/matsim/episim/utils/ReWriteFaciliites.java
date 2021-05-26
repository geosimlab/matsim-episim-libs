package org.matsim.episim.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.poi.ss.formula.functions.T;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsReaderXMLv1;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.algorithms.EventWriterXML;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacilitiesFactory;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;
import org.matsim.facilities.FacilitiesUtils;
import org.matsim.facilities.FacilitiesWriter;
import org.matsim.facilities.MatsimFacilitiesReader;
import org.matsim.scenarioCreation.FilterHandler;

public class ReWriteFaciliites {
	private static final Logger log = Logger.getLogger(ReWriteFaciliites.class);
	private static String filenameInFacilites = "C:/GeoSimLab/episim_jlm/Input_data/matsim_files/facilities1.0.xml.gz";
	
	private static String filenameOutFacilites = "C:/GeoSimLab/episim_jlm/Input_data/matsim_files/facilities1.0.fixed.xml.gz";
	public static void main(String[] args) {
		Config config = ConfigUtils.createConfig();
		config.facilities().setInputFile(filenameInFacilites);
		Scenario scenario = ScenarioUtils.loadScenario(config);
		
		Map<Id<ActivityFacility>, ? extends ActivityFacility> facilities = scenario.getActivityFacilities().getFacilities();
		ActivityFacilitiesFactory factory = scenario.getActivityFacilities().getFactory();
		for(ActivityFacility fac : facilities.values() ) {
			Map<String, ActivityOption> q = fac.getActivityOptions();
			Set<String> res = q.keySet().stream()
					.filter(map -> !map.startsWith("home"))
					.collect(Collectors.toSet());
			Set<String> seculars = res.stream().map(w ->w+"_internal_Secular").collect(Collectors.toSet());
			Set<String> ultras= res.stream().map(w ->w+"_internal_arab").collect(Collectors.toSet());
			Set<String> arabs= res.stream().map(w ->w+"_internal_Secular").collect(Collectors.toSet());
			Set<String> externals= res.stream().map(w ->w+"_external").collect(Collectors.toSet());
			Set<String> mergedSet = new HashSet<String>();
			mergedSet.addAll(seculars);
			mergedSet.addAll(ultras);
			mergedSet.addAll(arabs);
			mergedSet.addAll(externals);
			
			for(String activity:mergedSet) {
				ActivityOption option = factory.createActivityOption(activity);
				fac.addActivityOption(option);
			}
		}
		new FacilitiesWriter(scenario.getActivityFacilities()).write(filenameOutFacilites);
	}
}
