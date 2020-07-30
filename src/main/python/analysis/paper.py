import os
from datetime import datetime

import pandas as pd
import numpy as np
import seaborn as sns
import matplotlib.pyplot as plt
from matplotlib.ticker import ScalarFormatter
from matplotlib.dates import AutoDateLocator, AutoDateFormatter, ConciseDateFormatter


from utils import read_batch_run, read_case_data, read_run, infection_rate
from plot import comparison_plots

#%%

sns.set_style("whitegrid")
sns.set_context("paper")

dateFormater = ConciseDateFormatter(AutoDateLocator())

palette = sns.color_palette()


#%%

rki, hospital = read_case_data("berlin-cases.csv", "berlin-hospital.csv")

#%% Activity participation

from pandas.tseries.offsets import BDay

isBusinessDay = BDay().onOffset

act = pd.read_csv("C:/home/Development/matsim-org/matsim-episim/output/BerlinSnzData_daily_until20200705.csv",
                  sep="\t", parse_dates=[0])

act_week = act[act.date.map(isBusinessDay)]
act_wend = act[act.date.map(lambda *args: not isBusinessDay(*args))]

fig, ax = plt.subplots(dpi=250, figsize=(7.5, 3.8))


ax = sns.scatterplot(x="date", y="home", label="home", s=40, data=act_week, ax=ax)
sns.scatterplot(x="date", y="notAtHomeExceptLeisureAndEdu", label="notAtHome", s=40, data=act_week, ax=ax)
#sns.scatterplot(x="date", y="notAtHomeExceptLeisureAndEdu", label="notAtHome (Weekend)", s=40, data=act_wend, ax=ax)

ax.xaxis.set_major_formatter(dateFormater)

plt.ylabel("activity participation in %")
plt.legend(loc="best")

plt.xlim(datetime.fromisoformat("2020-03-01"), datetime.fromisoformat("2020-07-01"))

#%% Section 3-1

df31 = read_batch_run("data/section-3-1.zip")

#%%

fig, ax = plt.subplots(dpi=250, figsize=(7.5, 3.8))

rki.plot.scatter(x="date", y=["cases"], label=["RKI Cases"], color=palette[4], ax=ax, logy=True)

sns.lineplot(x="date", y="cases", estimator="mean", ci="q95", ax=ax,
             label=r"Calibrated $\theta$", data=df31)

ax.xaxis.set_major_formatter(dateFormater)
ax.yaxis.set_major_formatter(ScalarFormatter())

plt.ylim(bottom=1)
plt.xlim(datetime.fromisoformat("2020-02-01"), datetime.fromisoformat("2020-06-01"))
plt.legend(loc="upper left")

#%%


df = read_batch_run("data/section3-3-data.zip")
#dfOld = read_batch_run("data/section3-3.zip")

baseCase = df[df.alpha==1.0]

#%%

# NOTE: For ci="q95", the seaborn library was modified locally


fig, ax = plt.subplots(dpi=250, figsize=(7.5, 3.8))

ci = datetime.fromisoformat("2020-03-07")

plt.axvline(ci, color="gray", linewidth=1, linestyle="--", alpha=0.8)
plt.text(ci, 1.2, ' Date of ci change', color="gray")

rki.plot.scatter(x="date", y=["cases"], label=["RKI Cases"], color=palette[4], ax=ax, logy=True)

sns.lineplot(x="date", y="cases", estimator="mean", ci="q95", ax=ax,
             label=r"Unrestricted", data=df31)

# In the data alpha=0 is a special case
sns.lineplot(x="date", y="cases", estimator="mean", ci="q95", ax=ax,
             label=r"$\alpha=1.0$", data=df[(df.alpha==0)])

#sns.lineplot(x="date", y="cases", estimator="mean", ci="q95", ax=ax,
#             label=r"$\alpha=1.7$", data=df[df.alpha==1.7])

sns.lineplot(x="date", y="cases", estimator="mean", ci="q95", ax=ax,
             label=r"$\alpha=1.0$, ci adaptation 7-mar", data=baseCase)

ax.xaxis.set_major_formatter(dateFormater)
ax.yaxis.set_major_formatter(ScalarFormatter())

plt.ylim(bottom=1)
plt.xlim(datetime.fromisoformat("2020-02-01"), datetime.fromisoformat("2020-06-01"))
plt.legend(loc="upper left")


#%%

fig, ax = plt.subplots(dpi=250, figsize=(7.5, 3.8))

hospital.plot.scatter(x="Datum", y=["Stationäre Behandlung"], label=["Hospital treatment"], color=palette[4], logy=True, ax=ax)
hospital.plot.scatter(x="Datum", y=["Intensivmedizin"], label=["Intensive care"], color=palette[5], logy=True, ax=ax)

baseCase["inHospital"] =  baseCase.nSeriouslySick + baseCase.nCritical

sns.lineplot(x="date", y="inHospital", estimator="mean", ci="q95", ax=ax,            
             label=r"In Hospital", data=baseCase)

sns.lineplot(x="date", y="nCritical", estimator="mean", ci="q95", ax=ax,
             label=r"In ICU", data=baseCase)


ax.xaxis.set_major_formatter(dateFormater)
ax.yaxis.set_major_formatter(ScalarFormatter())
plt.ylim(bottom=1, top=10000)
plt.ylabel("Hospitalized persons")
plt.xlim(datetime.fromisoformat("2020-02-01"), datetime.fromisoformat("2020-06-01"))
plt.legend(loc="upper left")

#%%  Graphs for outdoor / indoor runs

outdoor = read_batch_run("data/outdoor.zip")

#%% 

fig, ax = plt.subplots(dpi=250, figsize=(7.5, 3.8))
hue = sns.color_palette(n_colors=2)

rki.plot.scatter(x="date", y=["cases"], label=["RKI Cases"], color=palette[4], ax=ax, logy=True)

sns.lineplot(x="date", y="cases", estimator="mean", ci="q95", ax=ax,
             style="tracingCapacity", hue="furtherMeasuresOnOct1", palette=hue, 
             data=outdoor)


ax.xaxis.set_major_formatter(dateFormater)
ax.yaxis.set_major_formatter(ScalarFormatter())


plt.ylim(bottom=1)
plt.xlim(datetime.fromisoformat("2020-02-01"), datetime.fromisoformat("2021-07-31"))
plt.legend(loc="upper left")
plt.title("Daily new infections aggregated over all random seeds")

current_handles, current_labels = plt.gca().get_legend_handles_labels()
current_labels[5] = "inf"

plt.legend(current_handles, current_labels)
