package sampletest;

import gui.util.GnlUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import static org.junit.Assert.fail;
import org.junit.Test;

import org.w3c.dom.NodeList;

import xmlfacts.AssertXMLFact;


public class SampleJUnit
{
  @Test
  public void rulesTester()
  {
    String[] prm = { 
       "-verbose",              "false",
       "-repository-path",      "C:\\_mywork\\FusionApps\\FATOOLS\\ElaboratedBarRules\\oracle\\rules\\elaboratedbarrules\\SpecialOracleRules.rules", 
       "-ruleset-name",         "RulesetOne", 
       "-schema-location",      "C:\\_mywork\\FusionApps\\FATOOLS\\ElaboratedBarRules\\xsd\\bar_customer.xsd", 
       "-xml-instance-name",    "C:\\_mywork\\FusionApps\\FATOOLS\\ElaboratedBarRules\\input\\input-facts.xml", 
       "-src-dest-directory",   "C:\\_mywork\\FusionApps\\FATOOLS\\ElaboratedBarRules\\gen-src", 
       "-dest-package",         "",
       "-class-dest-directory", "C:\\_mywork\\FusionApps\\FATOOLS\\ElaboratedBarRules\\gen-classes", 
       "-facts-output",         "C:\\_mywork\\FusionApps\\FATOOLS\\ElaboratedBarRules\\output" 
                   };
    // Run the requirted rulesets
    /* String code = */ AssertXMLFact.run(prm, null);
    // Now retrieve output facts, and do the tests
    try
    {
      BufferedReader br = new BufferedReader(new FileReader("C:\\_mywork\\FusionApps\\FATOOLS\\ElaboratedBarRules\\output" + File.separator + "allfacts.xml"));
      String content = "";
      String line = "";
      while (line != null)
      {
        line = br.readLine();
        if (line != null)
          content += (line + "\n");
      }
      br.close();
      DOMParser parser = new DOMParser();
      parser.parse(new StringReader(content));
      XMLDocument inputDoc = parser.getDocument();
      NodeList facts = inputDoc.selectNodes("/fact:all-facts/*", 
                                            new NSResolver()
                                            {
                                              public String resolveNamespacePrefix(String prefix)
                                              {
                                                return "urn:rules-unit";
                                              }
                                            });
      for (int i=0; i<facts.getLength(); i++)
      {
        XMLElement fact = (XMLElement)facts.item(i);
        StringWriter sw2 = new StringWriter();
        fact.print(sw2);
//      System.out.println(sw2.getBuffer().toString());
        System.out.println(GnlUtilities.prettyPrint(sw2.getBuffer().toString()));
        // TASK Do the tests here
      }
    }
    catch (Exception ex)
    {
      fail(ex.toString());
    }
  }
}