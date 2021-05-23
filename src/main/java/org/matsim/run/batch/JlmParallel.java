package org.matsim.run.batch;

import com.google.inject.AbstractModule;
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
import org.matsim.run.modules.SnzBerlinWeekScenario2020;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.Map;

/**
 * Batch class for curfew runs
 */
public class JlmParallel implements BatchRun<JlmParallel.Params> {


	@Override
	public AbstractModule getBindings(int id, @Nullable Params params) {

		return new JlmEpisimEverythingGoes();
	}

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

		@GenerateSeeds(4)
		public long seed;


	}


}
