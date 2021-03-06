package com.dataiku.dctc.file;

import static com.dataiku.dip.utils.PrettyString.eol;
import static com.dataiku.dip.utils.PrettyString.quoted;
import static com.dataiku.dip.utils.PrettyString.scat;

import java.util.Set;

import com.dataiku.dctc.command.policy.YellPolicy;
import com.dataiku.dctc.configuration.CredentialProviderBank;
import com.dataiku.dctc.exception.UserException;
import com.dataiku.dctc.file.FileBuilder.Protocol;
import com.dataiku.dip.utils.Params;
import com.google.common.collect.Sets;

public abstract class ProtocolFileBuilder {
    protected boolean checkAllowedOnly(String account
                                       , Params p
                                       , String[] allowed)
        throws IllegalArgumentException {
        StringBuilder sb = new StringBuilder();
        boolean failed = false;
        Set<String> allowedSet = Sets.newHashSet(allowed);
        for (String key : p.getAll().keySet()) {
            if (!allowedSet.contains(key)) {
                sb.append(scat("For protocol"
                               , getProtocol().getCanonicalName()
                               , "and"
                               , (account == null
                                  ? "default account"
                                  : "account " + account)
                               + ","
                               , "parameter"
                               , quoted(key)
                               , "is not recognized."
                               , eol()));
                failed = true;
            }
        }
        System.err.print(sb.toString());
        System.err.flush();

        return failed;
    }
    protected boolean checkMandatory(String account, Params p, String key) {
        if (!p.hasParam(key) || p.getParamOrEmpty(key).isEmpty()) {
            System.err.println(scat("For protocol"
                                    , getProtocol().getCanonicalName()
                                    , "and"
                                    , (account == null
                                       ? "default account"
                                       : "account" + account)
                                    , "parameter"
                                    , key
                                    , "is mandatory"));
            return true;
        }
        return false;
    }

    public CredentialProviderBank getBank() {
        return bank;
    }
    public void setBank(CredentialProviderBank bank) {
        this.bank = bank;
    }
    public ProtocolFileBuilder withBank(CredentialProviderBank bank) {
        this.bank = bank;
        return this;
    }

    protected String translateDefaultPath(Params accountParams
                                          , String protocolData) {
        String defaultPath = accountParams.getParamOrEmpty("default_path");
        if (defaultPath.isEmpty()) {
            return protocolData;
        }
        if (PathManip.isAbsolute(protocolData, fileSeparator())) {
            return protocolData;
        }
        return PathManip.concat(defaultPath, protocolData, fileSeparator());
    }
    protected UserException invalidAccountSettings(String account) {
        return new UserException(scat("For the protocol", getProtocol()
                                      , "and the"
                                      , (account == null
                                         ? "default account"
                                         : account
                                         + "account")
                                      , "one or more parameters are"
                                      + " incorrect"));
    }

    // Abstract
    public abstract Protocol getProtocol();
    public abstract GFile buildFile(String accountData
                                    , String protocolData
                                    , YellPolicy yell);
    public abstract boolean validateAccountParams(String account, Params p);
    public abstract String fileSeparator();

    // Attributes
    private CredentialProviderBank bank;
}
