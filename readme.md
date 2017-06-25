# TreasureApp

## Components

### Data Management

#### Data Sources

- Stock Prices (csv)
- Financial Statements (html)

akka `Actor` and `http` used to download and scrape raw data into a `Seq[A]` where `A` is a `case class` representing a single data point.


### Data Analysis

### Portfolio Back-Testing