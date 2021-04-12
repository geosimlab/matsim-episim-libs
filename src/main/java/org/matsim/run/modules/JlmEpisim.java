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

import java.time.LocalDate;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.policy.FixedPolicy;

/**
 * Scenario based on the publicly available OpenBerlin scenario (https://github.com/matsim-scenarios/matsim-berlin).
 */
public class JlmEpisim extends AbstractModule {
	final public static String OUTPUT_FOLDER = "E:/geosimlab/MATSim-JLM/Episim-JLM_Output";
	final public static String RUN_ID = "/" + 1;

	/**
	 * Activity names of the default params from {@link #addDefaultParams(EpisimConfigGroup)}.
	 */
	public static final String[] DEFAULT_ACTIVITIES = {
			"pt", "work", "leisure","school", "other", "home"
	};

	/**	
	 * Adds default parameters that should be valid for most scenarios.
	 */
	public static void addDefaultParams(EpisimConfigGroup config) {
		// pt
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("pt", "tr"));
		// regular out-of-home acts:
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("work"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("leisure", "leis"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("other"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("school"));
		// freight act:
		// home act:
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("home"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("quarantine_home"));
	}
	
	/**
	 * set contat intensties for each activity
	 */
	private static void setContactIntensities(EpisimConfigGroup episimConfig) {
		episimConfig.getOrAddContainerParams("pt")
		.setContactIntensity(10.0);
//		episimConfig.getOrAddContainerParams("tr")`
//		.setContactIntensity(10.0);
		episimConfig.getOrAddContainerParams("leisure")
		.setContactIntensity(5.0);
		episimConfig.getOrAddContainerParams("school")
		.setContactIntensity(4.0);
		episimConfig.getOrAddContainerParams("other")
		.setContactIntensity(2.0);
		episimConfig.getOrAddContainerParams("home")
		.setContactIntensity(3.0);
		episimConfig.getOrAddContainerParams("quarantine_home")
		.setContactIntensity(1.0);
	}

	@Provides
	@Singleton
	public Config config() {

		Config config = ConfigUtils.createConfig(new EpisimConfigGroup());
		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);
		config.global().setCoordinateSystem("EPSG:2039");
		config.controler().setOutputDirectory(OUTPUT_FOLDER + RUN_ID + "/");
//		config.plans().setInputFile
		config.network().setInputFile("E:/geosimlab/MATSim-JLM/MATSim-JLM_Output/14/14.output_network.xml.gz");

		// String episimEvents_1pct = "../public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-1pct-schools/output-berlin-v5.4-1pct-schools/berlin-v5.4-1pct-schools.output_events_for_episim.xml.gz";
		// String episimEvents_1pct = "../public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-1pct/output-berlin-v5.4-1pct/berlin-v5.4-1pct.output_events_for_episim.xml.gz";
		// String episimEvents_10pct = "../public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct-schools/output-berlin-v5.4-10pct-schools/berlin-v5.4-10pct-schools.output_events_for_episim.xml.gz";

//		String url = "https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-1pct/output-berlin-v5.4-1pct/berlin-v5.4-1pct.output_events_for_episim.xml.gz";
		
//		String url = "E:/geosimlab/git/matsim-episim/output/scenario jlm/14.output_events-0.1.xml.gz";
		String url = "E:\\geosimlab\\MATSim-JLM\\Episim-JLM_input\\tr_14.output_events-0.1.xml.gz";

		episimConfig.setInputEventsFile(url);
		episimConfig.setStartDate(LocalDate.of(2020, 8, 17));
		episimConfig.setFacilitiesHandling(EpisimConfigGroup.FacilitiesHandling.snz);
		episimConfig.setSampleSize(0.3*0.1);
		episimConfig.setCalibrationParameter(2);
		//  episimConfig.setOutputEventsFolder("events");
		long closingIteration = 14;

		addDefaultParams(episimConfig);
		
		setContactIntensities(episimConfig);

		episimConfig.setPolicy(FixedPolicy.class, FixedPolicy.config()
				.shutdown(closingIteration, "leisure", "school")
				.restrict(closingIteration, 0.2, "work")
				.restrict(closingIteration, 0.3, "other")
				.restrict(closingIteration, 0.5, "pt")
				.open(closingIteration + 60, DEFAULT_ACTIVITIES)
				.build()
		);

		return config;
	}

}
