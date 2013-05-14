package com.dataiku.dctc.file;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import com.dataiku.dctc.file.FileBuilder.Protocol;
import com.dataiku.dip.hadoop.HadoopUtils;
import com.dataiku.dip.utils.ErrorContext;
import com.dataiku.dip.utils.Params;

public class HttpFileBuilder extends ProtocolFileBuilder {

    @Override
    public Protocol getProtocol() {
        return Protocol.HTTP;
    }

    @Override
    public void validateAccountParams(String accountSettings, Params p) {
        checkAllowedOnly(accountSettings, p, new String[]{});
    }

    @Override
    public HttpFile buildFile(String accountSettings, String rawPath) {
        return new HttpFile(rawPath);
    }
    @Override
    public final String fileSeparator() {
        return "/";
    }
}
