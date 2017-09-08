package de.my5t3ry;

import de.my5t3ry.als_parser.AbletonFileParser;
import de.my5t3ry.als_parser.domain.AbletonProject.AbletonProject;
import de.my5t3ry.als_parser.domain.AbletonProject.device.Device;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Hello world!
 */
public class App {

    private static final AbletonFileParser fileParser = new AbletonFileParser();

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 1) {
            printUsage();
        } else {
            buildStats(new File(args[0]));
        }

    }

    private static void buildStats(final File file) {
        if (file.isDirectory()) {
            printDirectoryStats(file);
        } else {
            printFileStats(file);
        }
    }

    private static void printFileStats(final File file) {
        printBusyMsg();
        final AbletonProject abletonProject = fileParser.parse(file);
        printDeviceStats(abletonProject.getInternalDevices(), "Internal Effects:");
        printDeviceStats(abletonProject.getExternalDevices(), "External Effects:");
    }

    private static void printBusyMsg() {
        System.out.println("parsing files ... \n");
    }

    private static void printDirectoryStats(final File file) {
        printBusyMsg();
        final List<AbletonProject> abletonProjects = fileParser.parseDirectory(file);
        System.out.println("processed Ableton projects:'".concat(String.valueOf(abletonProjects.size())).concat("'"));
        printAverageTrackCount(abletonProjects);
        printTotalDeviceCount(abletonProjects);
        printDeviceStats(abletonProjects.stream()
                .flatMap(curProject -> curProject.getInternalDevices().stream())
                .collect(Collectors.toList()), "\n\nInternal Effects:\n");
        printDeviceStats(abletonProjects.stream()
                .flatMap(curProject -> curProject.getExternalDevices().stream())
                .collect(Collectors.toList()), "\n\nExternal Effects:\n");
    }

    private static void printTotalDeviceCount(final List<AbletonProject> abletonProjects) {
        System.out.println("Total devices used: '".concat(String.valueOf(abletonProjects.stream().collect(Collectors.summingInt(p -> p.getTotalDeviceCount()))).concat("'")));
    }

    private static void printAverageTrackCount(final List<AbletonProject> abletonProjects) {
        int totalTrackCount;
        totalTrackCount = abletonProjects.stream().collect(Collectors.summingInt(p -> p.getTotalTracks()));
        System.out.println("\nAverage tracks per project: '" + new BigDecimal(totalTrackCount).divide(new BigDecimal(abletonProjects.size()), 4, RoundingMode.HALF_DOWN) + "'");
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
        System.out.println("invalid argument count. \n Usage:\n java -jar als-stats.jar '/absolut/path/directory' or \n java -jar als-stats.jar '/absolut/path/file.als'");
    }
}
