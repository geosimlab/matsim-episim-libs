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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.IntStream;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.VaccinationConfigGroup;
import org.matsim.episim.EpisimConfigGroup.SnapshotSeed;
import org.matsim.episim.model.AgeDependentProgressionModel;
import org.matsim.episim.model.ContactModel;
import org.matsim.episim.model.GroupInfectionModel;
import org.matsim.episim.model.HouseholdContactModel;
import org.matsim.episim.model.HouseholdSecularContactModel;
import org.matsim.episim.model.HouseholdSecularUltraContactModel;
import org.matsim.episim.model.HouseholdUltraOrthodoxContactModel;
import org.matsim.episim.model.InfectionModel;
import org.matsim.episim.model.InitialInfectionHandler;
import org.matsim.episim.model.ProgressionModel;
import org.matsim.episim.model.RandomInitialInfections;
import org.matsim.episim.model.RandomVaccination;
import org.matsim.episim.model.VaccinationByAge;
import org.matsim.episim.model.VaccinationModel;
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
	final public static String RUN_ID = "/" + 152 + "/" + 1;
	final public static int iterations = 90;
	final public static double ultraOrthodoxInfectionRate = 1;
	final public static double secularInfectionRate = 1;
	/**
	 * Activity names of the default params from
	 * {@link #addDefaultParams(EpisimConfigGroup)}.
	 */

	/**
	 * Adds default parameters that should be valid for most scenarios.
	 */
	public static void addDefaultParams(EpisimConfigGroup episimConfig) {

		int spaces = 20;
		episimConfig.getOrAddContainerParams("pt", "tr").setContactIntensity(10.0).setSpacesPerFacility(spaces);
		//		episimConfig.getOrAddContainerParams("work").setContactIntensity(1.47).setSpacesPerFacility(spaces);
		//		episimConfig.getOrAddContainerParams("leisure").setContactIntensity(9.24).setSpacesPerFacility(spaces);
		//		episimConfig.getOrAddContainerParams("kindergarden").setContactIntensity(11.0).setSpacesPerFacility(spaces);
		//		episimConfig.getOrAddContainerParams("elementary").setContactIntensity(11.0).setSpacesPerFacility(spaces);
		//		episimConfig.getOrAddContainerParams("junior_high").setContactIntensity(11.0).setSpacesPerFacility(spaces);
		//		episimConfig.getOrAddContainerParams("high_school").setContactIntensity(11.).setSpacesPerFacility(spaces);
		//		episimConfig.getOrAddContainerParams("university").setContactIntensity(5.5).setSpacesPerFacility(spaces);
		//		episimConfig.getOrAddContainerParams("other").setContactIntensity(0.88).setSpacesPerFacility(spaces);
		//		episimConfig.getOrAddContainerParams("tjlm").setContactIntensity(1.47).setSpacesPerFacility(spaces);
		//		episimConfig.getOrAddContainerParams("fjlm").setContactIntensity(1.47).setSpacesPerFacility(spaces);
		//		_internal_arab
		episimConfig.getOrAddContainerParams("work_internal_arab").setContactIntensity(1.47).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("leisure_internal_arab").setContactIntensity(9.24).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("kindergarden_internal_arab").setContactIntensity(11.0).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("elementary_internal_arab").setContactIntensity(11.0).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("junior_high_internal_arab").setContactIntensity(11.0).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("high_school_internal_arab").setContactIntensity(11.).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("university_internal_arab").setContactIntensity(5.5).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("other_internal_arab").setContactIntensity(0.88).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("tjlm_internal_arab").setContactIntensity(1.47).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("fjlm_internal_arab").setContactIntensity(1.47).setSpacesPerFacility(spaces);
		//		_internal_Secular
		episimConfig.getOrAddContainerParams("work_internal_Secular").setContactIntensity(1.47).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("leisure_internal_Secular").setContactIntensity(9.24).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("kindergarden_internal_Secular").setContactIntensity(11.0).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("elementary_internal_Secular").setContactIntensity(11.0).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("junior_high_internal_Secular").setContactIntensity(11.0).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("high_school_internal_Secular").setContactIntensity(11.).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("university_internal_Secular").setContactIntensity(5.5).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("other_internal_Secular").setContactIntensity(0.88).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("tjlm_internal_Secular").setContactIntensity(1.47).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("fjlm_internal_Secular").setContactIntensity(1.47).setSpacesPerFacility(spaces);
		//		_internal_Ultra-Orthodox
		episimConfig.getOrAddContainerParams("work_internal_Ultra-Orthodox").setContactIntensity(1.47).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("leisure_internal_Ultra-Orthodox").setContactIntensity(9.24).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("kindergarden_internal_Ultra-Orthodox").setContactIntensity(11.0).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("elementary_internal_Ultra-Orthodox").setContactIntensity(11.0).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("junior_high_internal_Ultra-Orthodox").setContactIntensity(11.0).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("high_school_internal_Ultra-Orthodox").setContactIntensity(11.).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("university_internal_Ultra-Orthodox").setContactIntensity(5.5).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("other_internal_Ultra-Orthodox").setContactIntensity(0.88).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("tjlm_internal_Ultra-Orthodox").setContactIntensity(1.47).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("fjlm_internal_Ultra-Orthodox").setContactIntensity(1.47).setSpacesPerFacility(spaces);
		//		_external
		//		episimConfig.getOrAddContainerParams("work_external").setContactIntensity(1.47).setSpacesPerFacility(spaces);
		//		episimConfig.getOrAddContainerParams("leisure_external").setContactIntensity(9.24).setSpacesPerFacility(spaces);
		//		episimConfig.getOrAddContainerParams("kindergarden_external").setContactIntensity(11.0).setSpacesPerFacility(spaces);
		//		episimConfig.getOrAddContainerParams("elementary_external").setContactIntensity(11.0).setSpacesPerFacility(spaces);
		//		episimConfig.getOrAddContainerParams("junior_high_external").setContactIntensity(11.0).setSpacesPerFacility(spaces);
		//		episimConfig.getOrAddContainerParams("high_school_external").setContactIntensity(11.).setSpacesPerFacility(spaces);
		//		episimConfig.getOrAddContainerParams("university_external").setContactIntensity(5.5).setSpacesPerFacility(spaces);
		//		episimConfig.getOrAddContainerParams("other_external").setContactIntensity(0.88).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("tjlm_external").setContactIntensity(1.47).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("fjlm_external").setContactIntensity(1.47).setSpacesPerFacility(spaces);
		//		
		episimConfig.getOrAddContainerParams("home_secular").setContactIntensity(1.0).setSpacesPerFacility(1);
		episimConfig.getOrAddContainerParams("home_ultra-orthodox").setContactIntensity(1.0).setSpacesPerFacility(1);
		episimConfig.getOrAddContainerParams("home_arab").setContactIntensity(1.0).setSpacesPerFacility(1);
		episimConfig.getOrAddContainerParams("home_tjlm").setContactIntensity(1.0).setSpacesPerFacility(1);
		episimConfig.getOrAddContainerParams("home_fjlm").setContactIntensity(1.0).setSpacesPerFacility(1);
		//		episimConfig.getOrAddContainerParams("religion_jewish").setContactIntensity(11.0).setSpacesPerFacility(1);
		//		episimConfig.getOrAddContainerParams("religion_arab").setContactIntensity(11.0).setSpacesPerFacility(1);
		//		_internal_arab
		episimConfig.getOrAddContainerParams("religion_jewish_internal_arab").setContactIntensity(11.0).setSpacesPerFacility(1);
		episimConfig.getOrAddContainerParams("religion_arab_internal_arab").setContactIntensity(11.0).setSpacesPerFacility(1);
		//		_internal_Secular
		episimConfig.getOrAddContainerParams("religion_jewish_internal_Secular").setContactIntensity(11.0).setSpacesPerFacility(1);
		episimConfig.getOrAddContainerParams("religion_arab_internal_Secular").setContactIntensity(11.0).setSpacesPerFacility(1);
		//		_internal_Ultra-Orthodox
		episimConfig.getOrAddContainerParams("religion_jewish_internal_Ultra-Orthodox").setContactIntensity(11.0).setSpacesPerFacility(1);
		episimConfig.getOrAddContainerParams("religion_arab_internal_Ultra-Orthodox").setContactIntensity(11.0).setSpacesPerFacility(1);

		episimConfig.getOrAddContainerParams("home").setContactIntensity(1.0).setSpacesPerFacility(1);
		episimConfig.getOrAddContainerParams("quarantine_home").setContactIntensity(1.0).setSpacesPerFacility(1);
	}

	@Override
	protected void configure() {
		bind(ContactModel.class).to(HouseholdSecularUltraContactModel.class).in(Singleton.class);
		bind(ProgressionModel.class).to(AgeDependentProgressionModel.class).in(Singleton.class);
		bind(InitialInfectionHandler.class).to(RandomInitialInfections.class).in(Singleton.class);
//		bind(VaccinationModel.class).to(RandomVaccination.class).in(Singleton.class);
		bind(InfectionModel.class).to(GroupInfectionModel.class).in(Singleton.class);
		
	}

	@SuppressWarnings("null")
	@Provides
	@Singleton
	public Config config() {

		Config config = ConfigUtils.createConfig(new EpisimConfigGroup());

		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);
		config.global().setCoordinateSystem("EPSG:2039");
		Random rand = new Random();
		//		config.global().setRandomSeed(-3815788422936807906L);
		config.global().setRandomSeed(rand.nextLong());
		config.controler().setOutputDirectory(OUTPUT_FOLDER + RUN_ID + "/");
		config.facilities().setInputFile("C:/GeoSimLab/episim_jlm/Input_data/matsim_files/facilities1.0.fixed.xml.gz");
		config.network().setInputFile("C:/GeoSimLab/episim_jlm/Input_data/matsim_files/11.output_network.xml.gz");
		config.plans().setInputFile("C:/GeoSimLab/episim_jlm/Input_data/matsim_files/population1.0_district_subpop_ultra_secular_equal.xml.gz");
		String url = "C:/GeoSimLab/episim_jlm/Input_data/matsim_files/test.xml";
		LocalDate startDate = LocalDate.of(2020, 3, 8);
//		LocalDate date = startDate;
		episimConfig.setInputEventsFile(url);
		episimConfig.setStartDate(startDate);
		//poisson first sick - 36*3.333 patients
		int[] diseaseimport = {12,11,14,10,10,10,5,6,10,12,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		Map<LocalDate, Integer> intialInfections = new HashMap<LocalDate,Integer>();
		for(int j = 0; j < iterations;j++) {
			intialInfections.put(startDate.plusDays(j), diseaseimport[j]);
		}
		episimConfig.setInfections_pers_per_day(intialInfections);
		episimConfig.setInitialInfections(IntStream.of(diseaseimport).sum());
		episimConfig.setFacilitiesHandling(EpisimConfigGroup.FacilitiesHandling.snz);
		episimConfig.setSampleSize(1);
		episimConfig.setCalibrationParameter(0.0000015);
		episimConfig.setInitialInfectionDistrict("yes");
//		episimConfig.setSnapshotSeed(SnapshotSeed.reseed);
		episimConfig.setSnapshotInterval(50);
		//		episimConfig.setStartFromSnapshot("C:/GeoSimLab/episim_jlm/output/91/2/episim-snapshot-300-2020-12-20.zip");


		addDefaultParams(episimConfig);
		//		more general restrictions

		String[] group_arabs_a_activities = {"kindergarden_internal_arab", "elementary_internal_arab","junior_high_internal_arab", "high_school_internal_arab", 
				"university_internal_arab","religion_jewish_internal_arab", "religion_arab_internal_arab","leisure_internal_arab"};
		String[] group_arabs_b_activities = {"pt", "work_internal_arab", "other_internal_arab", "fjlm_internal_arab", "tjlm_internal_arab",};
		String[] group_secular_a_activities = {"kindergarden_internal_Secular", "elementary_internal_Secular","junior_high_internal_Secular", "high_school_internal_Secular", 
				"university_internal_Secular","religion_jewish_internal_Secular", "religion_arab_internal_Secular","leisure_internal_Secular"};
		String[] group_secular_b_activities = {"pt", "work_internal_Secular", "other_internal_Secular", "fjlm_internal_Secular", "tjlm_internal_Secular",};
		String[] group_ultra_a_activities = {"kindergarden_internal_Ultra-Orthodox", "elementary_internal_Ultra-Orthodox","junior_high_internal_Ultra-Orthodox", "high_school_internal_Ultra-Orthodox", 
				"university_internal_Ultra-Orthodox","religion_jewish_internal_Ultra-Orthodox", "religion_arab_internal_Ultra-Orthodox","leisure_internal_Ultra-Orthodox"};
		String[] group_ultra_b_activities = {"pt", "work_internal_Ultra-Orthodox", "other_internal_Ultra-Orthodox", "fjlm_internal_Ultra-Orthodox", "tjlm_internal_Ultra-Orthodox",};
		//		first clsure
		LocalDate closingDate = LocalDate.of(2020, 3, 15);
		double group_secular_a_open_rate_closing_date = 0.2;
		double group_ultra_a_open_rate_closing_date = 0.2;
		double group_b_open_rate_closing_date = 1;
//		//		end of first closure
		LocalDate openingDate= LocalDate.of(2020, 5, 5);
		double group_secular_a_open_rate_opening_date = 0.4;
		double group_ultra_a_open_rate_opening_date = 0.4;
		double group_b_open_rate_opening_date = 1;
//		//		out of school
//		LocalDate closingDate2 = LocalDate.of(2020, 6, 21);
//		double group_secular_a_open_rate_closing_date2 = 0.6;
//		double group_ultra_a_open_rate_closing_date2 = 0.7;
//		double group_b_open_rate_closing_date2 = 1;
//		LocalDate closingDate3 = LocalDate.of(2020, 7, 6);
//		double group_secular_a_open_rate_closing_date3 = 0.4;
//		double group_ultra_a_open_rate_closing_date3 = 0.4;
//		double group_b_open_rate_closing_date3 = 1;
//		LocalDate closingDate4 = LocalDate.of(2020, 8, 23);
//		double group_secular_a_open_rate_closing_date4 = 0.6;
//		double group_ultra_a_open_rate_closing_date4 = 0.7;
//		double group_b_open_rate_closing_date4 = 1;
//		LocalDate closingDate5 = LocalDate.of(2020, 9, 28);
//		double group_secular_a_open_rate_closing_date5 = 0.3;
//		double group_ultra_a_open_rate_closing_date5 = 0.3;
//		double group_b_open_rate_closing_date5 = 1;
//		LocalDate closingDate6 = LocalDate.of(2020, 10, 18);
//		double group_secular_a_open_rate_closing_date6 = 0.4;
//		double group_ultra_a_open_rate_closing_date6 = 0.4;
//		double group_b_open_rate_closing_date6 = 1;
//		LocalDate closingDate7 = LocalDate.of(2020, 11, 27);
//		double group_secular_a_open_rate_closing_date7 = 0.6;
//		double group_ultra_a_open_rate_closing_date7 = 0.7;
//		double group_b_open_rate_closing_date7 = 1;
//		LocalDate closingDate8 = LocalDate.of(2020, 12, 27);
//		double group_secular_a_open_rate_closing_date8 = 0.6;
//		double group_ultra_a_open_rate_closing_date8 = 0.7;
//		double group_b_open_rate_closing_date8 = 1;
//		LocalDate closingDate9 = LocalDate.of(2021, 1, 8);
//		double group_a_open_rate_closing_date9 = 0.3;
//		double group_b_open_rate_closing_date9 = 1;
		episimConfig.setPolicy(FixedPolicy.class, FixedPolicy.config()
				.restrict(closingDate , group_secular_a_open_rate_closing_date , group_secular_a_activities)
				.restrict(closingDate , group_b_open_rate_closing_date , group_secular_b_activities)
				.restrict(openingDate , group_secular_a_open_rate_opening_date , group_secular_a_activities)
				.restrict(openingDate , group_b_open_rate_opening_date , group_secular_b_activities)
//				.restrict(closingDate2 , group_secular_a_open_rate_closing_date2  , group_secular_a_activities)
//				.restrict(closingDate2 , group_b_open_rate_closing_date2 , group_secular_b_activities)
//				.restrict(closingDate3 , group_secular_a_open_rate_closing_date3 , group_secular_a_activities)
//				.restrict(closingDate3 , group_b_open_rate_closing_date3 , group_secular_b_activities)
//				.restrict(closingDate4 , group_secular_a_open_rate_closing_date4  , group_secular_a_activities)
//				.restrict(closingDate4 , group_b_open_rate_closing_date4 , group_secular_b_activities)
//				.restrict(closingDate5 , group_secular_a_open_rate_closing_date5 , group_secular_a_activities)
//				.restrict(closingDate5 , group_b_open_rate_closing_date5 , group_secular_b_activities)
//				.restrict(closingDate6 , group_secular_a_open_rate_closing_date6 , group_secular_a_activities)
//				.restrict(closingDate6 , group_b_open_rate_closing_date6 , group_secular_b_activities)
//				.restrict(closingDate7 , group_secular_a_open_rate_closing_date7 , group_secular_a_activities)
//				.restrict(closingDate7 , group_b_open_rate_closing_date7 , group_secular_b_activities)
//				.restrict(closingDate8 , group_secular_a_open_rate_closing_date8 , group_secular_a_activities)
//				.restrict(closingDate8 , group_b_open_rate_closing_date8 , group_secular_b_activities)
//				.restrict(closingDate9 , group_a_open_rate_closing_date9 , group_secular_a_activities)
//				.restrict(closingDate9 , group_b_open_rate_closing_date9 , group_secular_b_activities)
				.restrict(closingDate , group_ultra_a_open_rate_closing_date , group_ultra_a_activities)
				.restrict(closingDate , group_b_open_rate_closing_date , group_ultra_b_activities)
				.restrict(openingDate , group_ultra_a_open_rate_opening_date , group_ultra_a_activities)
				.restrict(openingDate , group_b_open_rate_opening_date , group_ultra_b_activities)
//				.restrict(closingDate2 , group_ultra_a_open_rate_closing_date2 , group_ultra_a_activities)
//				.restrict(closingDate2 , group_b_open_rate_closing_date2 , group_ultra_b_activities)
//				.restrict(closingDate3 , group_ultra_a_open_rate_closing_date3 , group_ultra_a_activities)
//				.restrict(closingDate3 , group_b_open_rate_closing_date3 , group_ultra_b_activities)
//				.restrict(closingDate4 , group_ultra_a_open_rate_closing_date4 , group_ultra_a_activities)
//				.restrict(closingDate4 , group_b_open_rate_closing_date4 , group_ultra_b_activities)
//				.restrict(closingDate5 , group_ultra_a_open_rate_closing_date5 , group_ultra_a_activities)
//				.restrict(closingDate5 , group_b_open_rate_closing_date5 , group_ultra_b_activities)
//				.restrict(closingDate6 , group_ultra_a_open_rate_closing_date6 , group_ultra_a_activities)
//				.restrict(closingDate6 , group_b_open_rate_closing_date6 , group_ultra_b_activities)
//				.restrict(closingDate7 , group_ultra_a_open_rate_closing_date7 , group_ultra_a_activities)
//				.restrict(closingDate7 , group_b_open_rate_closing_date7 , group_ultra_b_activities)
//				.restrict(closingDate8 , group_ultra_a_open_rate_closing_date8 , group_ultra_a_activities)
//				.restrict(closingDate8 , group_b_open_rate_closing_date8 , group_ultra_b_activities)
//				.restrict(closingDate9 , group_a_open_rate_closing_date9 , group_ultra_a_activities)
//				.restrict(closingDate9 , group_b_open_rate_closing_date9 , group_ultra_b_activities)
				.build()
				);
//				VaccinationConfigGroup vaccinationConfigGroup = ConfigUtils.addOrGetModule(config, VaccinationConfigGroup.class);
//				String vaccinationStartDate = "2020-12-21";
//				int dailyVaccinations = 2666;
//				vaccinationConfigGroup.setVaccinationCapacity_pers_per_day(Map.of(
//						episimConfig.getStartDate(), 0,
//						LocalDate.parse(vaccinationStartDate), dailyVaccinations));
//				vaccinationConfigGroup.setEffectiveness(0.99);
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
