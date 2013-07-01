package com.dataiku.dctc.configuration;

import static com.dataiku.dip.utils.PrettyString.eol;
import static com.dataiku.dip.utils.PrettyString.nl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.dataiku.dctc.exception.UserException;
import com.dataiku.dctc.file.FileManipulation;
import com.dataiku.dip.utils.Params;
import com.dataiku.dip.utils.StreamUtils;

public class Configuration {
    public void appendNewProtocol(String protocol, String user,
                                  Map<String, String> parameters) throws IOException {
        if (parameters.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();

        if (!lastProtocolDefined.equals(protocol)) {
            sb.append("[" + protocol + "]");
        }

        for(Map.Entry<String, String> confEntry: parameters.entrySet()) {
            sb.append(eol());
            sb.append(user + "." + confEntry.getKey() + " = " + confEntry.getValue());
        }

        sb.append(eol());

        appendToConf(sb.toString());
    }
    // public void appendConfTo(String file) throws IOException {
    //     if (conf.size() == 0) {
    //         return;
    //     }
    //     DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    //     Date date = new Date();

    //     StringBuilder sb = new StringBuilder();

    //     sb.append(eol());
    //     sb.append("# Added by dctc wizard (" + dateFormat.format(date) + ").");
    //     for (Map.Entry<String, Map<String, String>> e: conf.entrySet()) {
    //         sb.append(eol());
    //         sb.append("[" + e.getKey() + "]" + eol());
    //         for (Map.Entry<String, String> confEntry : e.getValue().entrySet()) {
    //             sb.append(confEntry.getKey() + " = " + confEntry.getValue() + eol());
    //         }
    //     }

    //     appendToConf(sb.toString());
    // }
    public void appendToConf(String conf) throws IOException {
        BufferedWriter writer = StreamUtils.writeToFile(GlobalConf.confFile(), true);

        if (!conf.startsWith(eol())) {
            writer.write(eol());
        }
        writer.write(conf);
        if (!conf.endsWith(eol())) {
            writer.write(eol());
        }
        writer.close();

    }
    public void parse(File file) throws IOException {
        if (!file.exists()) {
            create(file);
            if (!file.exists()) {
                return;
            }
        }

        BufferedReader stream = StreamUtils.readFile(file);
        String line;
        Map<String, String> protocol = null;
        try {
            while((line = stream.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("#") && !line.isEmpty()) { // Skip empty lines and comments
                    if (line.startsWith("[")) {
                        int closingElt = line.lastIndexOf(']');
                        if (closingElt > 1) {
                            String protocolName = line.substring(1, closingElt);
                            protocol = getNotNull(protocolName, conf);
                        }
                        else {
                            if (closingElt == -1) {
                                stream.close();
                                throw ue("Element beginning by [ must be closed by a ] character.");
                            }
                            else {
                                stream.close();
                                throw ue("Protocol element is empty.");
                            }
                        }
                    }
                    else {
                        if (protocol == null) {
                            stream.close();
                            throw ue("variables must be defined in a protocol.");
                        }
                        String[] split = FileManipulation.split(line, "=", 2);

                        protocol.put(split[0].trim(), split[1].trim());
                    }
                }
            }
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
    }

    public Map<String, Map<String, String>> getSections() {
        return conf;
    }
    public Map<String, String> getOrCreateSection(String section) {
        return getNotNull(section, conf);
    }
    private Map<String, String> getNotNull(String section, Map<String, Map<String, String>> from) {
        Map<String, String> sectionData = from.get(section);
        if (sectionData == null) {
            sectionData = new HashMap<String, String>();
            from.put(section, sectionData);
            nonValidSection.add(section);
        }
        return sectionData;
    }
    public void put(String proto, String accountName, String keyName, String keyValue) {
        getNotNull(proto, conf).put(accountName + "." + keyName , keyValue);
    }
    public Params getSectionAsParams(String section) {
        return new Params(getOrCreateSection(section));
    }
    private void create(File f) {
        try {
            System.err.println("First run, creating configuration file");
            File parent = new File(f.getParent());

            if (!(parent.mkdirs() || parent.getParentFile().exists())) {
                System.err.println("dctc configuration: could not create the path of the configuration file.");
            }

            BufferedWriter w = StreamUtils.writeToFile(f, false);
            w.write(nl("# This file was generated by dctc"
                       , "[alias]"
                       , "  ls=ls -G"
                       , ""
                       , "[global]"
                       , "  display=auto"
                       , ""
                       , "# Write here your own configuration"));
            w.close();
        }
        catch (IOException e) {
            System.err.println("dctc configuration: could not auto-create configuration file: "
                               + f.getAbsoluteFile() + " (" + e.getMessage() + ")");
            return;
        }
    }
    public void valid(String section) {
        nonValidSection.remove(section);
    }
    public Set<String> getNonValidSection() {
        return nonValidSection;
    }

    // Private
    private UserException ue(String msg) throws IOException {
        return new UserException("dctc conf: " + msg);
    }

    // Attributes
    private Set<String> nonValidSection = new HashSet<String>();
    private Map<String, Map<String, String>> conf = new HashMap<String, Map<String, String>>();
    private String lastProtocolDefined = null;
}
