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
        sortProjects();
        printStats();
    }

    private static void sortProjects() {
        projects.stream().filter(abletonProject -> abletonProject.getCreationFileTime() != null).collect(Collectors.toList())
                .sort(Comparator.comparing(AbletonProject::getCreationFileTime));
    }

    private static List<File> buildFiles(final String[] filesPaths) {
        return Arrays.asList(filesPaths).stream().map(path -> new File(path)).collect(Collectors.toList());
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
        System.out.println("calculating Ableton project statistics ... \n");
    }

    private static void printStats() {
        printTotalCount(projects);
        printProcessedCount(projects);
        printDeprecatedCount(projects);
        printOldestTrackDate(projects);
        printLatestTrackDate(projects);
        printAverageTrackCount(projects);
        printTotalMidiTrackCount(projects);
        printTotalAudioTrackCount(projects);
        printTotalGroupsTrackCount(projects);
        printTotalDeviceCount(projects);
        printDeviceStats(projects.stream()
                .flatMap(curProject -> curProject.getInternalDevices().stream())
                .collect(Collectors.toList()), "\n\nInternal Effects:\n");
        printDeviceStats(projects.stream()
                .flatMap(curProject -> curProject.getExternalDevices().stream())
                .collect(Collectors.toList()), "\n\nExternal Effects:\n");
    }

    private static void printTotalMidiTrackCount(final List<AbletonProject> projects) {
        System.out.println("Total midi tracks: '".concat(String.valueOf((Integer) projects.stream().mapToInt(p -> p.getMidiTracksCount()).sum()).concat("'")));
    }

    private static void printTotalGroupsTrackCount(final List<AbletonProject> projects) {
        System.out.println("Total group tracks: '".concat(String.valueOf((Integer) projects.stream().mapToInt(p -> p.getGroupTracksCount()).sum()).concat("'")));
    }

    private static void printTotalAudioTrackCount(final List<AbletonProject> projects) {
        System.out.println("Total audio tracks: '".concat(String.valueOf((Integer) projects.stream().mapToInt(p -> p.getAudioTracksCount()).sum()).concat("'")));
    }

    private static void printTotalCount(final List<AbletonProject> projects) {
        System.out.println("Total projects: '".concat(String.valueOf(projects.size()).concat("'")));
    }

    private static void printLatestTrackDate(final List<AbletonProject> projects) {
        final List<AbletonProject> projectsWithCreationDate = projects.stream().filter(abletonProject -> abletonProject.getCreationFileTime() != null).collect(Collectors.toList());
        System.out.println("Latest project: '".concat(projectsWithCreationDate.get(projectsWithCreationDate.size() - 1).getCreationFileTimeAsString()));
    }

    private static void printOldestTrackDate(final List<AbletonProject> projects) {
        System.out.println("Oldest project: '".concat(projects.stream().filter(abletonProject -> abletonProject.getCreationFileTime() != null).collect(Collectors.toList()).get(0).getCreationFileTimeAsString()));
    }

    private static void printDeprecatedCount(final List<AbletonProject> abletonProjects) {
        System.out.println("Ignored projects (deprecated version < Ableton 8.0): '".concat(String.valueOf(getIgnoredProjectFileCount(abletonProjects)).concat("'")));
    }

    private static void printProcessedCount(final List<AbletonProject> abletonProjects) {
        System.out.println("Processed projects: '".concat(String.valueOf(getProcessedProjectFilesCount(abletonProjects)).concat("'")));
    }

    private static int getProcessedProjectFilesCount(final List<AbletonProject> abletonProjects) {
        return abletonProjects.stream()
                .filter(p -> !(p instanceof DeprecatedAbletonProject))
                .collect(Collectors.toList()).size();
    }

    private static int getIgnoredProjectFileCount(final List<AbletonProject> abletonProjects) {
        return abletonProjects.stream()
                .filter(p -> p instanceof DeprecatedAbletonProject)
                .collect(Collectors.toList()).size();
    }

    private static void printTotalDeviceCount(final List<AbletonProject> abletonProjects) {
        System.out.println("Total devices used: '".concat(String.valueOf((Integer) abletonProjects.stream().mapToInt(p -> p.getTotalDeviceCount()).sum()).concat("'")));
    }

    private static void printAverageTrackCount(final List<AbletonProject> abletonProjects) {
        int totalTrackCount;
        totalTrackCount = abletonProjects.stream().mapToInt(p -> p.getTotalTracks()).sum();
        System.out.println("Average tracks per project: '" + new BigDecimal(totalTrackCount).divide(new BigDecimal(getProcessedProjectFilesCount(abletonProjects)), 0, RoundingMode.UP) + "'");
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
