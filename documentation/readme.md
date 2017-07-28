# TreasureApp

## Components

### Data Management

#### Data Model

###Entities
- Portfolio
- Stocks
- Transactions
- Holdings
- Stock Prices
- Financial Statements

-To get the value of any portfolio as of a certain date, join Holdings to Stock Prices using the stock ID and date

##### `Record`



#### Data Sources

- Stock Prices (csv)
    - data point type: `PriceRecord`
- Financial Statements (html)
    - data point type: `FinancialStatementRecord`

akka `Actor` and `http` used to download and scrape raw data into a `Seq[A]` where `A` is a `case class` representing a single data point.

### Data Analysis

### Portfolio Back-Testing
