package org.matsim.run.batch;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.episim.BatchRun;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.TracingConfigGroup;
import org.matsim.episim.model.*;
import org.matsim.episim.policy.FixedPolicy;
import org.matsim.episim.policy.FixedPolicy.ConfigBuilder;
import org.matsim.episim.policy.Restriction;

import org.matsim.run.modules.JlmEpisimEverythingGoes;
import org.matsim.run.modules.SnzBerlinProductionScenario;
import org.matsim.run.modules.SnzBerlinWeekScenario2020;

import javax.annotation.Nullable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

/**
 * Batch class for curfew runs
 */
public class JlmParallel implements BatchRun<JlmParallel.Params> {
	@Override
	public Module getBindings(int id, @Nullable Params params) {
		/// TODO hardcoded now and needs to be adjusted before runs
		/// XXX
		return new Binding(Params.OLD);
	}
	
//	public AbstractModule getBindings(int id, @Nullable Params params) {
//
//		return new JlmEpisimEverythingGoes();
//	}

	@Override
	public Metadata getMetadata() {
		return Metadata.of("Jerusalem", "hilonim");
	}


	@Override
	public Config prepareConfig(int id, Params params) {

		JlmEpisimEverythingGoes module = new JlmEpisimEverythingGoes();
		Config config = module.config();
		config.global().setRandomSeed(params.seed);

		return config;
	}

	public static final class Params {

		public static final String OLD = null;
		@GenerateSeeds(4)
		public long seed;


	}
	private static final class Binding extends AbstractModule {

		private final AbstractModule delegate;

		public Binding(String contactModel) {
			delegate = new JlmEpisimEverythingGoes();
		}

		@Override
		protected void configure() {
			bind(ContactModel.class).to(HouseholdSecularContactModel.class);
			bind(ProgressionModel.class).to(AgeDependentProgressionModel.class);
			bind(InitialInfectionHandler.class).to(RandomInitialInfections.class);
		}

		@Provides
		@Singleton
		public Config config() {

			Config config;

			if (delegate instanceof JlmEpisimEverythingGoes)
				config = ((JlmEpisimEverythingGoes) delegate).config();
			else
				config = ((SnzBerlinProductionScenario) delegate).config();

//			EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);
//			episimConfig.clearInputEventsFiles();
//			episimConfig.addInputEventsFile(SnzBerlinProductionScenario.INPUT.resolve("be_2020-week_snz_episim_events_wt_25pt_split.xml.gz").toString())
//			.addDays(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

			return config;
		}

//		@Provides
//		@Singleton
////		public Scenario scenario(Config config) {
////			if (delegate instanceof SnzBerlinWeekScenario2020)
////				return ((SnzBerlinWeekScenario2020) delegate).scenario(config);
////			else
////				return ((SnzBerlinProductionScenario) delegate).scenario(config);
////		}

	}


}
