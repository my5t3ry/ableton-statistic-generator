package de.my5t3ry;

import de.my5t3ry.als_parser.AbletonFileParser;
import de.my5t3ry.als_parser.domain.AbletonProject.AbletonProject;
import de.my5t3ry.als_parser.domain.AbletonProject.DeprecatedAbletonProject;
import de.my5t3ry.als_parser.domain.AbletonProject.device.Device;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * created by: sascha.bast
 * since: 08.09.17
 */
public class App {

    private static final AbletonFileParser fileParser = new AbletonFileParser();
    private static final List<AbletonProject> projects = new ArrayList<>();

    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
        } else {
            final String pathArg = args[0];
            if (pathArg.contains(";")) {
                buildStats(pathArg.split(";"));
            } else {
                buildStats(new String[]{args[0]});
            }
        }
    }

    private static void buildStats(final String[] filesPaths) {
        final List<File> files = buildFiles(filesPaths);
        collectAbletonProjects(files);
        printStats();
    }

    private static List<File> buildFiles(final String[] filesPaths) {
        final List<File> result = new ArrayList<>();
        Arrays.asList(filesPaths).forEach(filePath -> result.add(new File(filePath)));
        return result;
    }

    private static void collectAbletonProjects(final List<File> files) {
        printBusyMsg();
        files.forEach(file -> {
            if (file.isDirectory()) {
                projects.addAll(fileParser.parseDirectory(file));
            } else {
                projects.add(fileParser.parse(file));
            }
        });
    }

    private static void printBusyMsg() {
        System.out.println("calculating ableton project statistics ... \n");
    }

    private static void printStats() {
        printProcessedCount(projects);
        printAverageTrackCount(projects);
        printOldestTrackDate(projects);
        printLatestTrackDate(projects);
        printTotalDeviceCount(projects);
        printDeviceStats(projects.stream()
                .flatMap(curProject -> curProject.getInternalDevices().stream())
                .collect(Collectors.toList()), "\n\nInternal Effects:\n");
        printDeviceStats(projects.stream()
                .flatMap(curProject -> curProject.getExternalDevices().stream())
                .collect(Collectors.toList()), "\n\nExternal Effects:\n");
        printDeprecatedCount(projects);
    }

    private static void printOldestTrackDate(final List<AbletonProject> projects) {
        projects.stream().filter(abletonProject -> abletonProject.getCreationFileTime() != null).collect(Collectors.toList()).sort(Comparator.comparing(AbletonProject::getCreationFileTime));
        System.out.println("Oldest project: '".concat(projects.get(0).getCreationFileTimeAsString()));
    }

    private static void printLatestTrackDate(final List<AbletonProject> projects) {
        projects.stream().filter(abletonProject -> abletonProject.getCreationFileTime() != null).collect(Collectors.toList()).sort(Comparator.comparing(AbletonProject::getCreationFileTime).reversed());
        System.out.println("Latest project: '".concat(projects.get(0).getCreationFileTimeAsString()));
    }

    private static void printDeprecatedCount(final List<AbletonProject> abletonProjects) {
        System.out.println("\n\nIgnored projects (deprecated version < Ableton 8): '".concat(String.valueOf(getIgnoredProjectFileCount(abletonProjects)).concat("'")));
    }

    private static void printProcessedCount(final List<AbletonProject> abletonProjects) {
        System.out.println("Processed projects: '".concat(String.valueOf(getProcessedProjectFilesCount(abletonProjects)).concat("'")));
    }

    private static int getProcessedProjectFilesCount(final List<AbletonProject> abletonProjects) {
        return abletonProjects.stream()
                .filter(p -> p instanceof DeprecatedAbletonProject == false)
                .collect(Collectors.toList()).size();
    }

    private static int getIgnoredProjectFileCount(final List<AbletonProject> abletonProjects) {
        return abletonProjects.stream()
                .filter(p -> p instanceof DeprecatedAbletonProject)
                .collect(Collectors.toList()).size();
    }

    private static void printTotalDeviceCount(final List<AbletonProject> abletonProjects) {
        System.out.println("Total devices used: '".concat(String.valueOf(abletonProjects.stream().collect(Collectors.summingInt(p -> p.getTotalDeviceCount()))).concat("'")));
    }

    private static void printAverageTrackCount(final List<AbletonProject> abletonProjects) {
        int totalTrackCount;
        totalTrackCount = abletonProjects.stream().collect(Collectors.summingInt(p -> p.getTotalTracks()));
        System.out.println("Average tracks per project: '" + new BigDecimal(totalTrackCount).divide(new BigDecimal(getProcessedProjectFilesCount(abletonProjects)), 0, RoundingMode.HALF_DOWN) + "'");
    }

    private static void printDeviceStats(final List<Device> devices, final String caption) {
        final HashMap<Device, Integer> result = new HashMap<>();
        System.out.println(caption);
        devices.forEach(device -> {
            if (result.containsKey(device)) {
                result.put(device, result.get(device) + device.getCount());
            } else {
                result.put(device, device.getCount());
            }
        });
        result.entrySet().stream().sorted(Collections.reverseOrder(Comparator.comparing(Map.Entry::getValue)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (device, count) -> device, LinkedHashMap::new)).forEach((device, count) -> System.out.print(device.getName().concat(": ").concat(String.valueOf(count)).concat("; ")));
    }

    private static void printUsage() {
        System.out.println("invalid argument count. \n" +
                " Usage:\n" +
                " java -jar als-stats.jar '/absolut/path/directory' or \n" +
                " java -jar als-stats.jar '/absolut/path/directory;/absolut/path2/directory' or \n" +
                " java -jar als-stats.jar '/absolut/path/directory;/absolut/path/file.als' or \n" +
                " java -jar als-stats.jar '/absolut/path/file.als'");
    }
}
