package com.dataiku.dctc.command;

import java.util.ArrayList;
import java.util.List;

import com.dataiku.dctc.clo.Option;
import com.dataiku.dctc.command.abs.Command;
import com.dataiku.dip.utils.IndentedWriter;

public class Version extends Command {
    public String tagline() {
        return "Show DCTC version.";
    }
    public void longDescription(IndentedWriter printer) {
        printer.print("Show the version of DCTC");
    }

    @Override
    public void perform(String[] args) {
        System.out.println("dctc " + com.dataiku.dctc.configuration.Version.pretty());
    }
    @Override
    protected String proto() {
        return "";
    }
    @Override
    protected List<Option> setOptions() {
        return new ArrayList<Option>();
    }
    @Override
    public String cmdname() {
        return "version";
    }
}
