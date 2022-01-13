package com.example.mobileproject.model;

public enum Languages {
    FIXED     ("Fixed",       "fx"),                // A mini-hack for help on interface, don't put any parser with this value
    ENGLISH     ("English",       "en"),
    PORTUGUES_BR("Português(Brasil)",  "ptBR"),
    PORTUGUES_PT("Português(Portugal)",  "ptPT");

    private final String full;
    private final String abbr;

    private Languages(String full, String abbr) {
        this.full = full;
        this.abbr = abbr;
    }

    public String getFullName() {
        return full;
    }

    public String getAbbreviatedName() {
        return abbr;
    }
}
