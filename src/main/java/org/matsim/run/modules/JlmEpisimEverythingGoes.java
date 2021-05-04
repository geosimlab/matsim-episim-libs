/*-
 * #%L
 * MATSim Episim
 * %%
 * Copyright (C) 2020 matsim-org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.matsim.run.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.model.AgeDependentProgressionModel;
import org.matsim.episim.model.ContactModel;
import org.matsim.episim.model.HouseholdContactModel;
import org.matsim.episim.model.InitialInfectionHandler;
import org.matsim.episim.model.ProgressionModel;
import org.matsim.episim.model.RandomInitialInfections;
import org.matsim.episim.policy.FixedPolicy;
import org.matsim.episim.policy.Restriction;
import org.matsim.episim.policy.FixedPolicy.ConfigBuilder;
import org.matsim.episim.utils.ReadRestrictions;

/**
 * Scenario based on the publicly available OpenBerlin scenario
 * (https://github.com/matsim-scenarios/matsim-berlin).
 */
public class JlmEpisimEverythingGoes extends AbstractModule {
	private static final Logger log = Logger.getLogger(JlmEpisimEverythingGoes.class);


	final public static String JLM_RESTRICTIONS = "C:/GeoSimLab/episim_jlm/Input_data/raw/restrictions.csv";
	final public static String JLM_RESTRICTIONS_GROUPS = "C:/GeoSimLab/episim_jlm/Input_data/raw/restrictions_groups.csv";
	
	final public static String OUTPUT_FOLDER = "C:/GeoSimLab/episim_jlm/output";
	final public static String RUN_ID = "/" + 48;
	/**
	 * Activity names of the default params from
	 * {@link #addDefaultParams(EpisimConfigGroup)}.
	 */
//	does this work?
	public static final String[] DEFAULT_ACTIVITIES = { "pt", "work", "leisure", "kindergarden", "elementary",
			"junior_high", "high_school", "university", "other", "fjlm", "tjlm", "home_secular", "home_ultra-orthodox",
			"home_arab", "home_tjlm", "home_fjlm", "religion_jewish", "religion_arab" };

	/**
	 * Adds default parameters that should be valid for most scenarios.
	 */
	public static void addDefaultParams(EpisimConfigGroup episimConfig) {

		int spaces = 20;
		episimConfig.getOrAddContainerParams("pt", "tr").setContactIntensity(10.0).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("work").setContactIntensity(1.47).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("leisure").setContactIntensity(9.24).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("kindergarden").setContactIntensity(11.0).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("elementary").setContactIntensity(11.0).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("junior_high").setContactIntensity(11.0).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("high_school").setContactIntensity(11.).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("university").setContactIntensity(5.5).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("other").setContactIntensity(0.88).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("tjlm").setContactIntensity(1.47).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("fjlm").setContactIntensity(1.47).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("home_secular").setContactIntensity(1.0).setSpacesPerFacility(1);
		episimConfig.getOrAddContainerParams("home_ultra-orthodox").setContactIntensity(1.0).setSpacesPerFacility(1);
		episimConfig.getOrAddContainerParams("home_arab").setContactIntensity(1.0).setSpacesPerFacility(1);
		episimConfig.getOrAddContainerParams("home_tjlm").setContactIntensity(1.0).setSpacesPerFacility(1);
		episimConfig.getOrAddContainerParams("home_fjlm").setContactIntensity(1.0).setSpacesPerFacility(1);
		episimConfig.getOrAddContainerParams("religion_jewish").setContactIntensity(11.0).setSpacesPerFacility(1);
		episimConfig.getOrAddContainerParams("religion_arab").setContactIntensity(11.0).setSpacesPerFacility(1);
		episimConfig.getOrAddContainerParams("home").setContactIntensity(1.0).setSpacesPerFacility(1);
		episimConfig.getOrAddContainerParams("quarantine_home").setContactIntensity(1.0).setSpacesPerFacility(1);
	}

	@Override
	protected void configure() {
		bind(ContactModel.class).to(HouseholdContactModel.class).in(Singleton.class);
		bind(ProgressionModel.class).to(AgeDependentProgressionModel.class).in(Singleton.class);
		bind(InitialInfectionHandler.class).to(RandomInitialInfections.class).in(Singleton.class);
	}

	@SuppressWarnings("null")
	@Provides
	@Singleton
	public Config config() {

		Config config = ConfigUtils.createConfig(new EpisimConfigGroup());

		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);
		config.global().setCoordinateSystem("EPSG:2039");
		config.controler().setOutputDirectory(OUTPUT_FOLDER + RUN_ID + "/");
		config.facilities().setInputFile("C:/GeoSimLab/episim_jlm/Input_data/matsim_files/facilities1.0.xml.gz");
		config.network().setInputFile("C:/GeoSimLab/episim_jlm/Input_data/matsim_files/11.output_network.xml.gz");
		config.plans().setInputFile("C:/GeoSimLab/episim_jlm/Input_data/matsim_files/population1.0_district.xml.gz");
		String url = "C:/GeoSimLab/episim_jlm/Input_data/matsim_files/11.output_events-1.0.xml.gz";
		LocalDate startDate = LocalDate.of(2020, 3, 1);
		LocalDate date = startDate;
		episimConfig.setInputEventsFile(url);
		episimConfig.setStartDate(startDate);
		episimConfig.setInitialInfections(100);
		episimConfig.setFacilitiesHandling(EpisimConfigGroup.FacilitiesHandling.snz);
		episimConfig.setSampleSize(1);
		episimConfig.setCalibrationParameter(0.000001);
		episimConfig.setInitialInfectionDistrict("yes");
//		setting initial infections per day
		Map<LocalDate, Integer> infectionsPerDay = new TreeMap<LocalDate, Integer>();
		for (int i = 1;i <= 10;i++) {
			infectionsPerDay.put(date, 10);
			date = startDate.plusDays(i);
		}
		
		episimConfig.setInfections_pers_per_day(infectionsPerDay);

		addDefaultParams(episimConfig);
//		Can't control multiple values, therefore reduce them to 3. a - lowest, c - highest 
		double group_a_open_rate = 0.5;
		double group_b_open_rate = 1;
		double group_c_open_rate = 1;
//		ConfigBuilder jlmRestrictionsPolicy = JlmRestrictions(JLM_RESTRICTIONS);
		ConfigBuilder jlmRestrictionsGroupsPolicy = JlmRestrictionsGroups(JLM_RESTRICTIONS_GROUPS, group_a_open_rate, group_b_open_rate, group_c_open_rate);
//		episimConfig.setPolicy(FixedPolicy.class, jlmRestrictionsPolicy.build());
		episimConfig.setPolicy(FixedPolicy.class, jlmRestrictionsGroupsPolicy.build());
		return config;
	}

	public ConfigBuilder JlmRestrictions(String inputResterctions) {
		ConfigBuilder policy = FixedPolicy.config();
		CSVReader csvReader = null;
		try {
			csvReader = new CSVReaderBuilder(new FileReader(inputResterctions)).withSkipLines(1).build();
			String[] row;
			while ((row = csvReader.readNext()) != null) {
				LocalDate date = LocalDate.of(Integer.parseInt(row[2]), Integer.parseInt(row[3]),
						Integer.parseInt(row[4]));
				Restriction fraction = Restriction.of(Double.parseDouble(row[1]));
				String activity = row[0];
				policy = policy.restrict(date, fraction, activity);
				System.out.println(row[0]);					
			}
			csvReader.close();
		} catch (IOException e) {
			log.error("ERROR: Cannot read restrictions file: " + inputResterctions);
		} catch (NumberFormatException e) {
			log.error("ERROR: Check format of restrictions file: " + inputResterctions);
		} catch (CsvValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return policy;
	}
	public ConfigBuilder JlmRestrictionsGroups(String inputResterctions,
			double group_a_open_rate,double group_b_open_rate,double group_c_open_rate ) {
		ConfigBuilder policy = FixedPolicy.config();
		CSVReader csvReader = null;
		try {
			csvReader = new CSVReaderBuilder(new FileReader(inputResterctions)).withSkipLines(1).build();
			String[] row;
			while ((row = csvReader.readNext()) != null) {
				LocalDate date = LocalDate.of(Integer.parseInt(row[2]), Integer.parseInt(row[3]),
						Integer.parseInt(row[4]));
				double open_rate = 0;
				switch(row[5]) {
				case "a":
					open_rate =group_a_open_rate; 
					break;
				case "b":
					open_rate =group_b_open_rate;
					break;
				case "c":
					open_rate =group_c_open_rate;
					break;
				}
				Restriction fraction = Restriction.of(open_rate);
				String activity = row[0];
				policy = policy.restrict(date, fraction, activity);
				System.out.println(row[0]);					
			}
			csvReader.close();
		} catch (IOException e) {
			log.error("ERROR: Cannot read restrictions file: " + inputResterctions);
		} catch (NumberFormatException e) {
			log.error("ERROR: Check format of restrictions file: " + inputResterctions);
		} catch (CsvValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return policy;
	}
}
