# create spark workspace
workspace_path=$HOME/spark-workspace

mkdir -rf $workspace_path
cd $workspace_path

# download spark from website
wget https://d3kbcqa49mib13.cloudfront.net/spark-2.2.0-bin-hadoop2.7.tgz

# open https://spark.apache.org/downloads.html
