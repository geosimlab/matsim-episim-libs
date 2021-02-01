package org.matsim.episim.policy;

import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import it.unimi.dsi.fastutil.objects.Object2DoubleAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Person;
import org.matsim.episim.EpisimReporting;
import org.matsim.episim.ReplayHandler;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Policy that takes given administrative restrictions and automatically adjust remaining activities based on mobility data.
 */
@SuppressWarnings("unchecked")
public final class AdjustedPolicy extends ShutdownPolicy {

	/**
	 * Handler with all events.
	 */
	private final ReplayHandler handler;

	/**
	 * Out-of-home durations for all days.
	 */
	private final SortedMap<LocalDate, Double> outOfHome = new Object2DoubleAVLTreeMap<>();

	/**
	 * Mapping for base days.
	 */
	private final Map<DayOfWeek, LocalDate> baseDay = new EnumMap<>(DayOfWeek.class);

	/**
	 * Base duration of activities in the simulation.
	 */
	private final Map<DayOfWeek, Object2DoubleMap<String>> simDurations = new EnumMap<>(DayOfWeek.class);

	/**
	 * Activities to administrative periods. (dates as edges)
	 */
	private final Map<String, SortedSet<LocalDate>> periods = new HashMap<>();

	/**
	 * Config builder for fixed policy.
	 */
	public static ConfigBuilder config() {
		return new ConfigBuilder();
	}

	@Inject
	protected AdjustedPolicy(@Named("policy") Config config, ReplayHandler handler) {
		super(config);
		this.handler = handler;
	}

	@Override
	public void init(LocalDate start, ImmutableMap<String, Restriction> restrictions) {

		for (Map.Entry<String, ConfigValue> e : config.getConfig("outOfHome").root().entrySet()) {
			LocalDate date = LocalDate.parse(e.getKey());
			outOfHome.put(date, (Double) e.getValue().unwrapped());
		}

		for (Map.Entry<String, ConfigValue> e : config.getConfig("baseDays").root().entrySet()) {
			baseDay.put(DayOfWeek.valueOf(e.getKey()), LocalDate.parse((CharSequence) e.getValue().unwrapped()));
		}

		for (Map.Entry<String, ConfigValue> e : config.getConfig("periods").root().entrySet()) {

			SortedSet<LocalDate> dates = new TreeSet<>();

			((List<String>) e.getValue().unwrapped()).forEach(d -> dates.add(LocalDate.parse(d)));

			periods.put(e.getKey(), dates);
		}


		// init base durations
		for (Map.Entry<DayOfWeek, List<Event>> e : handler.getEvents().entrySet()) {

			Object2DoubleMap<String> durations = new Object2DoubleOpenHashMap<>();
			Map<Id<Person>, ActivityStartEvent> enterTimes = new HashMap<>();

			for (Event event : e.getValue()) {
				if (event instanceof ActivityStartEvent) {
					enterTimes.put(((ActivityStartEvent) event).getPersonId(), (ActivityStartEvent) event);
				} else if (event instanceof ActivityEndEvent) {
					durations.mergeDouble(
							((ActivityEndEvent) event).getActType(),
							event.getTime() - enterTimes.getOrDefault(((ActivityEndEvent) event).getPersonId(),
									new ActivityStartEvent(0, null, null, null, null)).getTime(),
							Double::sum);

					enterTimes.remove(((ActivityEndEvent) event).getPersonId());
				}
			}

			// add unclosed activities
			enterTimes.forEach((k, v) -> durations.mergeDouble(v.getActType(), Math.max(0, 24 * 3600 - v.getTime()), Double::sum));
			simDurations.put(e.getKey(), durations);
		}

		FixedPolicy.initRestrictions(start, restrictions, config.getConfig("administrative"));
	}

	@Override
	public void updateRestrictions(EpisimReporting.InfectionReport report, ImmutableMap<String, Restriction> restrictions) {

		Config admin = config.getConfig("administrative");

		LocalDate today = LocalDate.parse(report.date);

		double baseDuration = simDurations.get(today.getDayOfWeek())
				.object2DoubleEntrySet().stream()
				.filter(e -> !e.getKey().contains("home"))
				.mapToDouble(Object2DoubleMap.Entry::getDoubleValue).sum();

		double outOfHomeDuration = baseDuration;

		// store administrative activities for the day
		Set<String> administrative = new HashSet<>();

		for (Map.Entry<String, Restriction> e : restrictions.entrySet()) {

			SortedSet<LocalDate> periods = this.periods.get(e.getKey());

			// check if in admin period, today must lie between two dates
			if (periods == null || periods.headSet(today).size() % 2 == 0)
				continue;

			Restriction r = FixedPolicy.readForDay(report, admin, e.getKey());
			if (r != null)
				e.getValue().update(r);

			double frac = e.getValue().getRemainingFraction();

			administrative.add(e.getKey());

			outOfHomeDuration -= (1 - frac) * simDurations.get(today.getDayOfWeek()).getDouble(e.getKey());

		}

		double frac = 1 - outOfHome.get(today) / outOfHome.get(baseDay.get(today.getDayOfWeek()));

		frac = frac / (1 - outOfHomeDuration / baseDuration);

		for (Map.Entry<String, Restriction> e : restrictions.entrySet()) {

			// skip administrative
			if (administrative.contains(e.getKey()) || e.getKey().contains("home")) continue;

			e.getValue().setRemainingFraction(1 - frac);


		}


	}

	/**
	 * Builder for {@link AdjustedPolicy} config.
	 */
	public static final class ConfigBuilder extends ShutdownPolicy.ConfigBuilder<Map<String, ?>> {

		/**
		 * Specify the base days, i.e. default level of durations.
		 *
		 * @param days map week day to date.
		 */
		public ConfigBuilder baseDays(Map<DayOfWeek, LocalDate> days) {

			Map<String, String> data = new HashMap<>();
			days.forEach((k, v) -> data.put(k.toString(), v.toString()));
			params.put("baseDays", data);

			return this;
		}

		/**
		 * Set activity durations for one day for all activities.
		 * Previously set durations for this day are overwritten.
		 *
		 * @param durations must contain durations for all activities for this day.
		 */
		public ConfigBuilder outOfHomeDurations(Map<LocalDate, Double> durations) {

			Map<String, Double> data = new HashMap<>();
			durations.forEach((k, v) -> data.put(k.toString(), v));
			params.put("outOfHome", data);

			return this;
		}

		/**
		 * Set administrative restrictions. These overwrite restriction from the mobility input.
		 */
		public ConfigBuilder administrativePolicy(FixedPolicy.ConfigBuilder policy) {
			params.put("administrative", policy.params);
			return this;
		}

		/**
		 * Configure periods during which an activity will be restricted according to {@link #administrativePolicy(FixedPolicy.ConfigBuilder)}.
		 *
		 * @param periods arguments of (multiple) from and to date
		 */
		public ConfigBuilder administrativePeriod(String activity, LocalDate... periods) {

			Map<String, List<String>> map = (Map<String, List<String>>) params.computeIfAbsent("periods", k -> new HashMap<>());
			map.put(activity, Arrays.stream(periods).map(LocalDate::toString).collect(Collectors.toList()));

			return this;
		}

	}

}