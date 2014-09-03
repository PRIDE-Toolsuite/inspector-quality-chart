inspector-quality-chart
===============

# About Inspector Quality Chart

The purpose of PRIDE Inspector Quality Chart library is to provide a tool for creating charts to assess the quality of your MS experiments.

# License

inspector-quality-chart is a PRIDE API licensed under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt).

# How to cite it:

Wang, R., Fabregat, A., Ríos, D., Ovelleiro, D., Foster, J. M., Côté, R. G., ... & Vizcaíno, J. A. (2012). PRIDE Inspector: a tool to visualize and validate MS proteomics data. Nature biotechnology, 30(2), 135-137. [PDF File](http://www.nature.com/nbt/journal/v30/n2/pdf/nbt.2112.pdf), [Pubmed Record](http://www.ncbi.nlm.nih.gov/pubmed/22318026)

# Main Features
* Peak Intensity Distribution Chart: A histogram of ion intensity vs frequency for all MS2 spectra in a single PRIDE experiment.
* Precursor Ion Charge Distribution Chart: Displays a bar chart of precursor ion charge for a single PRIDE experiment.
* MS2 m/z Distribution Chart: Displays a frequency distribution of product ion m/z for different precursor ion charges.
* Distribution of Precursor Ion Masses Chart: Displays a frequency distribution of product ion m/z for different precursor ion charges.
* Number of Peptides Identified per Protein Chart: Displays a bar chart with the number of peptides identified per protein for a single PRIDE experiment.
* Number of Peaks per Spectrum Chart: Displays a histogram of number of peaks per MS/MS spectrum in a single PRIDE experiment.
* Delta m/z Chart: Displays a relative frequency distribution of theoretical precursor ion mass - experimental precursor ion mass.
* Number of Missed Tryptic Cleavages Chart: Display the number of missed tryptic cleavages.

**Note**: the library is still evolving, we are committed to expand this library and add more useful classes.

# Getting Inspector Quality Chart

The zip file in the releases section contains the PRIDE Inspector Quality Chart jar file and all other required libraries.

Maven Dependency

PRIDE Inspector Quality Chart library can be used in Maven projects, you can include the following snippets in your Maven pom file.
 
 ```maven
 <dependency>
   <groupId>uk.ac.ebi.pride.toolsuite</groupId>
   <artifactId>inspector-quality-chart</artifactId>
   <version>1.0.0-SNAPSHOT</version>
 </dependency> 
 ```
 ```maven
 <!-- EBI repo -->
 <repository>
     <id>nexus-ebi-repo</id>
     <url>http://www.ebi.ac.uk/intact/maven/nexus/content/repositories/ebi-repo</url>
 </repository>
 
 <!-- EBI SNAPSHOT repo -->
 <snapshotRepository>
    <id>nexus-ebi-repo-snapshots</id>
    <url>http://www.ebi.ac.uk/intact/maven/nexus/content/repositories/ebi-repo-snapshots</url>
 </snapshotRepository>
```
Note: you need to change the version number to the latest version.

For developers, the latest source code is available from our SVN repository.

# Getting Help

If you have questions or need additional help, please contact the PRIDE Helpdesk at the EBI: pride-support at ebi.ac.uk (replace at with @).

Please send us your feedback, including error reports, improvement suggestions, new feature requests and any other things you might want to suggest to the PRIDE team.

# This library has been used in:

* Wang, R., Fabregat, A., Ríos, D., Ovelleiro, D., Foster, J. M., Côté, R. G., ... & Vizcaíno, J. A. (2012). PRIDE Inspector: a tool to visualize and validate MS proteomics data. Nature biotechnology, 30(2), 135-137. [PDF File](http://www.nature.com/nbt/journal/v30/n2/pdf/nbt.2112.pdf), [Pubmed Record](http://www.ncbi.nlm.nih.gov/pubmed/22318026)
* Vizcaíno, J. A., Côté, R. G., Csordas, A., Dianes, J. A., Fabregat, A., Foster, J. M., ... & Hermjakob, H. (2013). The PRoteomics IDEntifications (PRIDE) database and associated tools: status in 2013. Nucleic acids research, 41(D1), D1063-D1069. [PRIDE-Archive](http://www.ebi.ac.uk/pride/archive/)

How to use inspector-quality-chart
===============

# Using Inspector Quality Chart 

Here we will show you how to use the PRIDE Quality Chart library to create all the charts associated to a PRIDE Experiment Accession Number (stored in the public PRIDE-database), how to store the intermediate data and how to reuse it instead of calculated it again.

Creating all the charts associated to a PRIDE Experiment Accession Number

Note. If you've download the code from the SVN, make sure that you've configured correctly the database access. If you're using the project as a library, then the connection to the database is already configured.

You can find the class PrideChartSummaryData for calculating the experiment summary data in uk.ac.ebi.pride.toolsuite.chart.controller package. It requires one input parameter:

accessionNumber is the PRIDE Experiment Accession Number in String.
The following lines of code shows you how:

```java
//The PRIDE Experiment Accession Number we are interested in
String accessionNumber = "9759";

//The future list of PrideChart
List<PrideChart> listOfCharts;
try {
    //Using PrideChartSummaryData only with the accesion number STRING, it will use
    //the default configuration in your `settings.xml` file to access to de database
    ExperimentSummaryData summaryData = new PrideChartSummaryData(accessionNumber);
    listOfCharts = PrideChartFactory.getAllCharts(spectralSummaryData);
} catch (SpectralDataPerExperimentException e) {
    listOfCharts = new ArrayList<PrideChart>(); //An empty list
    //Treat the exception
}
//If everything was fine, here listOfCharts contains the available charts
```

The PrideChartFactory object can be found in the uk.ac.ebi.pride.toolsuite.chart.graphics.implementation package.


# Store the intermediate data

In order to visualize the data in the future without having to wait for the summarization process done by PrideChartSummaryData object, the PRIDE Quality Chart library allows you to store an intermediate data in JSon format.

```java
//Supose the `listOfCharts` object previously loaded
for(PrideChart prideChart : listOfCharts){
    //jsonData will contain the neccessary data to create the chart
    String jsonData = prideChart.getChartJsonData();
    //type will contain the identifier associated to the PrideChart Object
    int type = PrideChartFactory.getPrideChartIdentifier(prideChart);
    //Store the string associated to the type in a file in order to use it in the future
}
```

The PrideChartFactory object can be found in the uk.ac.ebi.pride.toolsuite.chart.graphics.implementation package.


# Reuse the intermediate data

If you have the json data stored with the associated type (PrideChart? Object identifier), in order to have your chart again, you only need the next code:

```java
//Supose the data is in a variable `jsonData` (as String) and the type in `type` (as Integer)
PrideChart prideChart = PrideChartFactory.getChart(type, jsonData);
```
The PrideChartFactory object can be found in the uk.ac.ebi.pride.chart.graphics.implementation package.
