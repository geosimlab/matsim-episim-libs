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
package org.matsim.episim.model;

import com.google.inject.Inject;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.EpisimPerson;
import org.matsim.episim.EpisimPerson.DiseaseStatus;
import org.matsim.episim.EpisimReporting;
import org.matsim.episim.EpisimUtils;

import java.util.SplittableRandom;

/**
 * Default progression model with deterministic (but random) state transitions at fixed days.
 */
public final class DefaultProgressionModel implements ProgressionModel {

	private final double DAY = 24. * 3600;
	private final SplittableRandom rnd;
	private final EpisimConfigGroup episimConfig;

	@Inject
	public DefaultProgressionModel(SplittableRandom rnd, EpisimConfigGroup episimConfig) {
		this.rnd = rnd;
		this.episimConfig = episimConfig;
	}

	@Override
	public void updateState(EpisimPerson person, int day) {
		// Called at the beginning of iteration
		double now = EpisimUtils.getCorrectedTime(0, day);
		switch (person.getDiseaseStatus()) {
			case susceptible:

				// A healthy quarantined person is dismissed from quarantine after some time
				if (person.getQuarantineStatus() != EpisimPerson.QuarantineStatus.no && person.daysSinceQuarantine(day) > 14) {
					person.setQuarantineStatus(EpisimPerson.QuarantineStatus.no, day);
				}

				break;
			case infectedButNotContagious:
				if (person.daysSince(DiseaseStatus.infectedButNotContagious, day) >= 4) {
					person.setDiseaseStatus(now, DiseaseStatus.contagious);
				}
				break;
			case contagious:

				if (episimConfig.getPutTraceablePersonsInQuarantine() == EpisimConfigGroup.PutTracablePersonsInQuarantine.yes) {
					// 10% chance of getting randomly tested and detected each day
					if (rnd.nextDouble() < 0.1) {
						person.setQuarantineStatus(EpisimPerson.QuarantineStatus.atHome, day);

						for (EpisimPerson pw : person.getTraceableContactPersons(now - person.daysSince(DiseaseStatus.contagious, day) * DAY)) {
							quarantinePerson(pw, day);
						}
					}
				}

				if (person.daysSince(DiseaseStatus.infectedButNotContagious, day) == 6) {
					final double nextDouble = rnd.nextDouble();
					if (nextDouble < 0.8) {
						// 80% show symptoms and go into quarantine
						// Diamond Princess study: (only) 18% show no symptoms.
						person.setDiseaseStatus(now, DiseaseStatus.showingSymptoms);
						person.setQuarantineStatus(EpisimPerson.QuarantineStatus.atHome, day);

						if (episimConfig.getPutTraceablePersonsInQuarantine() == EpisimConfigGroup.PutTracablePersonsInQuarantine.yes) {
							for (EpisimPerson pw : person.getTraceableContactPersons(now - person.daysSince(DiseaseStatus.contagious, day) * DAY)) {
								quarantinePerson(pw, day);
							}
						}

					}

				} else if (person.daysSince(DiseaseStatus.infectedButNotContagious, day) >= 16) {
					person.setDiseaseStatus(now, EpisimPerson.DiseaseStatus.recovered);
				}
				break;
			case showingSymptoms:
				if (person.daysSince(DiseaseStatus.infectedButNotContagious, day) == 10) {
					double proba = getAgeDependantProbaOfTransitioningToSeriouslySick(person, now);
					if (rnd.nextDouble() < proba) {
						person.setDiseaseStatus(now, DiseaseStatus.seriouslySick);
					}

				} else if (person.daysSince(DiseaseStatus.infectedButNotContagious, day) >= 16) {
					person.setDiseaseStatus(now, DiseaseStatus.recovered);
				}
				break;
			case seriouslySick:
				if (person.daysSince(DiseaseStatus.infectedButNotContagious, day) == 11) {
					double proba = getAgeDependantProbaOfTransitioningToCritical(person, now);
					if (rnd.nextDouble() < proba) {
						person.setDiseaseStatus(now, DiseaseStatus.critical);
					}
				} else if (person.daysSince(DiseaseStatus.infectedButNotContagious, day) >= 23) {
					person.setDiseaseStatus(now, DiseaseStatus.recovered);
				}
				break;
			case critical:
				if (person.daysSince(DiseaseStatus.infectedButNotContagious, day) == 20) {
					// (transition back to seriouslySick.  Note that this needs to be earlier than sSick->recovered, otherwise
					// they stay in sSick.  Problem is that we need differentiation between intensive care beds and normal
					// hospital beds.)
					person.setDiseaseStatus(now, DiseaseStatus.seriouslySick);
				}
				break;
			case recovered:
				// one day after recovering person is released from quarantine
				if (person.getQuarantineStatus() != EpisimPerson.QuarantineStatus.no)
					person.setQuarantineStatus(EpisimPerson.QuarantineStatus.no, day);

				break;
			default:
				throw new IllegalStateException("Unexpected value: " + person.getDiseaseStatus());
		}

		// clear tracing older than 7 days
		person.clearTraceableContractPersons(now - 7 * DAY);
	}

	private void quarantinePerson(EpisimPerson p, int day) {

		if (p.getQuarantineStatus() == EpisimPerson.QuarantineStatus.no && p.getDiseaseStatus() != DiseaseStatus.recovered) {
			p.setQuarantineStatus(EpisimPerson.QuarantineStatus.atHome, day);
		}
	}


	private double getAgeDependantProbaOfTransitioningToSeriouslySick(EpisimPerson person, double now) {

		double proba = -1;

		if (person.getAttributes().getAsMap().containsKey("age")) {
			int age = (int) person.getAttributes().getAttribute("age");

			if (age < 0 || age > 120) {
				throw new RuntimeException("Age of person=" + person.getPersonId().toString() + " is not plausible. Age is=" + age);
			}

			if (age < 10) {
				proba = 0.06 / 100;
			} else if (age < 20) {
				proba = 0.19 / 100;
			} else if (age < 30) {
				proba = 0.77 / 100;
			} else if (age < 40) {
				proba = 2.06 / 100;
			} else if (age < 50) {
				proba = 3.16 / 100;
			} else if (age < 60) {
				proba = 6.57 / 100;
			} else if (age < 70) {
				proba = 10.69 / 100;
			} else if (age < 80) {
				proba = 15.65 / 100;
			} else {
				proba = 17.58 / 100;
			}

		} else {
//			log.warn("Person=" + person.getPersonId().toString() + " has no age. Transition to seriusly sick is not age dependent.");
			proba = 0.05625;
		}

		return proba;
	}

	private double getAgeDependantProbaOfTransitioningToCritical(EpisimPerson person, double now) {

		double proba = -1;

		if (person.getAttributes().getAsMap().containsKey("age")) {
			int age = (int) person.getAttributes().getAttribute("age");

			if (age < 0 || age > 120) {
				throw new RuntimeException("Age of person=" + person.getPersonId().toString() + " is not plausible. Age is=" + age);
			}

			if (age < 40) {
				proba = 5. / 100;
			} else if (age < 50) {
				proba = 6.3 / 100;
			} else if (age < 60) {
				proba = 12.2 / 100;
			} else if (age < 70) {
				proba = 27.4 / 100;
			} else if (age < 80) {
				proba = 43.2 / 100;
			} else {
				proba = 70.9 / 100;
			}

		} else {
//			log.warn("Person=" + person.getPersonId().toString() + " has no age. Transition to critical is not age dependent.");
			proba = 0.25;
		}

		return proba;
	}

	@Override
	public boolean canProgress(EpisimReporting.InfectionReport report) {
		return report.nTotalInfected > 0 || report.nInQuarantine > 0;
	}
}
