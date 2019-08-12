// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public final class Section
{
    public static final int QUESTION = 0;
    public static final int ANSWER = 1;
    public static final int AUTHORITY = 2;
    public static final int ADDITIONAL = 3;
    public static final int ZONE = 0;
    public static final int PREREQ = 1;
    public static final int UPDATE = 2;
    private static Mnemonic sections;
    private static String[] longSections;
    private static String[] updateSections;
    
    private Section() {
    }
    
    public static String string(final int i) {
        return Section.sections.getText(i);
    }
    
    public static String longString(final int i) {
        Section.sections.check(i);
        return Section.longSections[i];
    }
    
    public static String updString(final int i) {
        Section.sections.check(i);
        return Section.updateSections[i];
    }
    
    public static int value(final String s) {
        return Section.sections.getValue(s);
    }
    
    static {
        Section.sections = new Mnemonic("Message Section", 3);
        Section.longSections = new String[4];
        Section.updateSections = new String[4];
        Section.sections.setMaximum(3);
        Section.sections.setNumericAllowed(true);
        Section.sections.add(0, "qd");
        Section.sections.add(1, "an");
        Section.sections.add(2, "au");
        Section.sections.add(3, "ad");
        Section.longSections[0] = "QUESTIONS";
        Section.longSections[1] = "ANSWERS";
        Section.longSections[2] = "AUTHORITY RECORDS";
        Section.longSections[3] = "ADDITIONAL RECORDS";
        Section.updateSections[0] = "ZONE";
        Section.updateSections[1] = "PREREQUISITES";
        Section.updateSections[2] = "UPDATE RECORDS";
        Section.updateSections[3] = "ADDITIONAL RECORDS";
    }
}
