# ableton-statistic-generator
simple java app to generate statistics of your Ableton projects <br>
works with als files created with Ableton version >= 8 


## pre-build:
download:
[https://github.com/my5t3ry/ableton-statistic-generator/raw/master/dist/als-stats.jar](https://github.com/my5t3ry/ableton-statistic-generator/raw/master/dist/als-stats.jar)

## usage:
```
java -jar als-stats.jar '/absolut/path/directory' or 
java -jar als-stats.jar '/absolut/path/file.als' or
java -jar als-stats.jar '/absolut/path/directory;/absolut/path2/directory' or 
java -jar als-stats.jar '/absolut/path/directory;/absolut/path/file.als' or 
```


## build:
```
mvn clean compile assembly:single 
```


