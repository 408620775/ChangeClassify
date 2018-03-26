package pers.bbn.changeBug.extraction;

public class Main {
    public static void main(String[] args) throws Exception {
        Extraction1 extraction1 = new Extraction1("MyItextpdf",501,800);
        FileOperation.writeStringBuffer(extraction1.getLOCFile(),"MyItextpdfLOC");
    }

}
