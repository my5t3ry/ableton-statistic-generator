# ableton-statistic-generator
simple java app to generate statistics of your ableton projects Edit
Add topics


## build:
```
mvn clean compile assembly:single 
```
##usage:
```
cd target
java -jar als-stats.jar '/absolut/path/directory' or 
java -jar als-stats.jar '/absolut/path/file.als' or
java -jar als-stats.jar '/absolut/path/directory;/absolut/path2/directory' or 
java -jar als-stats.jar '/absolut/path/directory;/absolut/path/file.als' or 
```


