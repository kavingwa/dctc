package com.dataiku.dctc.dispatch;

import com.dataiku.dip.datalayer.Column;
import com.dataiku.dip.datalayer.Row;

public class MergeFunction implements SplitFunction {
    public MergeFunction() {
    }
    public String split(Row row, Column column) {
        return "";
    }
}
