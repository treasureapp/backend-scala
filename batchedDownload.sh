
# https://www.google.com/finance/historical?output=csv&q=AA&startdate=Nov+1%2C+1991
function downloadPriceData {
    symbol=$1
    destDir=$2
    start_date="Nov+1%2C+1991"
    out_file="$(printf "%s/%s.txt" $destDir $symbol)"
    url="$(printf "https://www.google.com/finance/historical?output=csv&q=%s&startdate=%s" $symbol $start_date)"
    
    printf "downloadingPriceData: %s\n" $symbol
    
    status="$(curl -s -o /dev/null -I -w "%{http_code}" $url)"

    if [ "$status" = "200" ]; then 
      curl -s $url -o $out_file
      printf "\tsaving: %s\n" "$out_file"      
    else
      printf "\tstatus: %s\n" "$status"
    fi
}



dataDir=$HOME/TreasureData/PriceData
mkdir -p $dataDir

while read p; do
  downloadPriceData $p $dataDir
done <stock_list.txt