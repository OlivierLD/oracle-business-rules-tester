package sampletest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.fail;
import org.junit.Test;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xmlfacts.AssertXMLFact;


public class SampleJUnit02
{
  private static String VERBOSE           = "false";
  private static String RULES_DICTIONNARY = "C:\\_mywork\\FusionApps\\FATOOLS\\ElaboratedBarRules\\oracle\\rules\\elaboratedbarrules\\SpecialOracleRules.rules";
  private static String RULESETS          = "RulesetOne";
  private static String INPUT_XML         = "C:\\_mywork\\FusionApps\\FATOOLS\\ElaboratedBarRules\\input\\input-facts.xml";
  private static String PACKAGE_NAME      = "org.party:org.party.v2";
  private static String OUTPUT_DIR        = "C:\\_mywork\\FusionApps\\FATOOLS\\ElaboratedBarRules\\output";
  
  @Test
  public void rulesTester()
  {
    String[] prm = 
     { 
       AssertXMLFact.VERBOSE_PRM,         VERBOSE,
       AssertXMLFact.REPOSITORY_PATH_PRM, RULES_DICTIONNARY, 
       AssertXMLFact.RULESET_NAME_PRM,    RULESETS, 
       AssertXMLFact.INSTANCE_NAME_PRM,   INPUT_XML, 
       AssertXMLFact.PACKAGE_NAME_PRM,    PACKAGE_NAME,
       AssertXMLFact.SKIP_JAXB_PRM, 
       AssertXMLFact.FACT_OUTPUT_PRM,     OUTPUT_DIR
     };
    // Run the requirted rulesets
    /* String code = */ AssertXMLFact.run(prm, null);
    // Now retrieve output facts, and do the tests
    try
    {
      BufferedReader br = new BufferedReader(new FileReader(OUTPUT_DIR + File.separator + "allfacts.xml"));
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
      
//    inputDoc.print(System.out);
      
      NSResolver factResolver = new NSResolver()
                                {
                                  public String resolveNamespacePrefix(String prefix)
                                  {
                                    return "urn:rules-unit";
                                  }
                                };
      
      NSResolver guyResolver = new NSResolver()
                                {
                                  public String resolveNamespacePrefix(String prefix)
                                  {
                                    return "http://www.party.org";
                                  }
                                };
      
      NodeList facts = inputDoc.selectNodes("/fact:all-facts/*", factResolver);
      int nbMatchingNodes = 0;
      System.out.println("Found " + facts.getLength() + " fact(s)");
      for (int i=0; i<facts.getLength(); i++)
      {
        XMLElement fact = (XMLElement)facts.item(i);
//      fact.print(System.out);
        
        XMLDocument doc = new XMLDocument();
        Node n = doc.adoptNode(fact);
        doc.appendChild(n);
        
        if (false)
        {
//        System.out.println("Fact:" + fact.getNodeName() + ", ns:" + fact.getNamespaceURI());
          try 
          { 
            NodeList nl = doc.selectNodes("/ns:guy/ns:name", guyResolver);
            for (int j=0; j<nl.getLength(); j++)            
              System.out.println("- Name:" + nl.item(j).getTextContent());
          } 
          catch (Exception ignore) 
          {
            ignore.printStackTrace();
          }
        }
        
        try 
        { 
          NodeList nl = doc.selectNodes("/ns:guy[./ns:name = 'Jack the Killer']/ns:access-status", guyResolver);
          String status = null;
          if (nl.getLength() > 0)
          {
            for (int j=0; j<nl.getLength(); j++)
            {
              status = nl.item(j).getTextContent();
//            System.out.println("  ->" + status);
            }
          }
          if (status != null)
          {
//          System.out.println("Found status:" + status);
            assertTrue("Jack the Killer should have been granted access", "GRANTED".equals(status));
            nbMatchingNodes++;
          }
        } 
        catch (Exception ignore) { ignore.printStackTrace(); }
      }
      if (nbMatchingNodes != 1)
        fail(Integer.toString(nbMatchingNodes) + " status found for Jack the Killer. Should have been 1");
    }
    catch (Exception ex)
    {
      fail(ex.toString());
    }
  }
}