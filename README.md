<!--
*** Thanks for checking out the Best-README-Template. If you have a suggestion
*** that would make this better, please fork the repo and create a pull request
*** or simply open an issue with the tag "enhancement".
*** Thanks again! Now go create something AMAZING! :D
***
***
***
*** To avoid retyping too much info. Do a search and replace for the following:
*** github_username, repo_name, twitter_handle, email, project_title, project_description
-->


# ms3-coding-challenge
This repo exists to process a CSV file and insert valid records into a SQLite Database. We are using Gradle as our build tool, the SQLite JDBC driver to connect to our SQLite Database, and OpenCSV to assist in processing our CSV files. Also, we are using JUnit 5 for our currently non-existent unit tests.

<!-- Areas of Improvement -->
## "Areas of Improvement" Given by Engineers that "reviewed" this challenge

The notes offered by engineers that reviewed this challenge were the following:
* Not an object oriented solution
* No attempt at error handling

Given that my solution was object-oriented and that there was extensive error handling, this feedback is pretty much useless. 

<!-- GETTING STARTED -->
## Getting Started

To get a local copy up and running follow these simple steps.

## Installation
### Cloning the Code

Clone this code into your local using the following command

`git clone https://github.com/mseh1128/ms3-coding-challenge.git`

### Editing configuration files

Edit `config.properties` in `src\main\java\resources` to change the Database URL, I/O CSV file path, and the Log file path.

By default, the input CSV file is located at `src\main\java\resources`, and the log and output CSV are stored at the root directory.

`DB.Url` is hosted locally by default, so you will need to edit it to make this app work.

### Building the Application 

The application can be built (assemble outputs and run checks) using the following command 

**Windows** : `gradlew.bat build`

**Linux/MacOS**: `./gradlew build`

### Running the Application

The application can be run (assembles app and executes binary) using the following command 

**Windows** : `gradlew.bat run`

**Linux/MacOS**: `./gradlew run`

### Cleaning the Application

The application can be cleaned (deletes contents of build directory) using the following command

**Windows** : `gradlew.bat clean`

**Linux/MacOS**: `./gradlew clean`

## Tests

After the Application is built using the Build command, the test results report can be found in below file.
*build/reports/tests/test/index.html*

There are currently no tests.

## Approach/Design Choices/Assumptions

My approach is fairly simple. I made a DataProcessor class to encapsulate my logic, and have a public facing `processRecords()` method which utilizes a series of helper methods to process our CSV records. At present, this class isn't very reusable, however adding multiple contructors to allow for custom configurations would easily rectify this issue without adding too much complexity.

## Areas of improvement
If I had more time, I would make the following changes:
* Add Unit Tests (Focusing on valid configs, DB Testing, and proper Exception handling)
* Have a Better Workflow (ie Commit more often, use feature branches, add CI/CI pipelines)
* Add non-arbitrary primary indices to the DB schema (b/c any cols can be "null" this is impossible)
* Add more abstraction to DataProcessor (add a class to represent DB entries, move constant Strings to a SQLConstants/ConfigConstants type files)
* Add multiple constructors to support custom configuration (Specifically, w/ regards to Properties/Logging - Need a separate "Logging" config file)
* Improve file structure (Ex: Add folders for logs and output CSV, in appropriate locations)
* Add support for environment variables in config for convenience
* Do some profiling/benchmark to find performance Bottlenecks
* Add better concurrency measures (such as connection pooling) 

