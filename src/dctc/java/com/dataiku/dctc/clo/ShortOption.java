package com.dataiku.dctc.clo;

import com.dataiku.dctc.clo.Printer;

public class ShortOption implements Option {
    public String read(String optName) {
        if (opt.indexOf(optName.substring(0, 1)) != -1) {
            return optName.substring(0, 1);
        }
        return "";
    }
    public String read(String optName, String argName) {
        return "";
    }
    public String getArgument(String optLine) {
        if (optLine.isEmpty()) {
            return null;
        }
        else {
            if (optLine.startsWith("=")) {
                return optLine.substring(1);
            }
            else {
                return optLine;
            }
        }
    }
    public void print(Printer printer) {
        for (int i = 0; i < opt.length(); ++i) {
            printer.add(opt.charAt(i));
        }
    }
    public boolean print() {
        return true;
    }

    // Getters - Setters
    public String getOpt() {
        return opt;
    }
    public void setOpt(char opt) {
        this.opt = "" + opt;
    }
    public void setOpt(String opt) {
        this.opt = opt;
    }
    public ShortOption withOpt(String opt) {
        setOpt(opt);
        return this;
    }
    public ShortOption withOpt(char opt) {
        setOpt(opt);
        return this;
    }
    // Attributes
    private String opt = "";
}
