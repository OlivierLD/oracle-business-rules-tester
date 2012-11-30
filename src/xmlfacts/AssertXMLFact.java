package xmlfacts;

import java.beans.XMLEncoder;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import java.lang.reflect.Method;

import java.net.URL;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javatools.jaxb.DynamicCompilationV2;

import javax.swing.JOptionPane;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import oracle.rules.rl.RLClass;
import oracle.rules.rl.RLObject;
import oracle.rules.rl.RLProperty;
import oracle.rules.rl.RuleSession;
import oracle.rules.rl.Ruleset;
import oracle.rules.rl.exceptions.RLException;
import oracle.rules.rl.extensions.pool.PoolableObject;
import oracle.rules.rl.extensions.pool.RuleSessionPool;
import oracle.rules.rl.trace.DecisionTrace;
import oracle.rules.rl.trace.FactTrace;
import oracle.rules.rl.trace.TraceEntry;
import oracle.rules.sdk2.decisionpoint.DecisionPointDictionaryFinder;
import oracle.rules.sdk2.dictionary.DOID;
import oracle.rules.sdk2.dictionary.RuleDictionary;
import oracle.rules.sdk2.exception.SDKWarning;
import oracle.rules.sdk2.search.DictionarySearch;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AssertXMLFact
{
  private static boolean use_assert_tree = false; // set by system property use.assert.tree [true|false]
  
  private final static String REPOSITORY_PATH = "";
  private static final String RULESET_NAME    = "";
  
  private final static String XML_INSTANCE    = "";
  private final static String SCHEMA_LOCATION = "";
  
  private final static String DESTINATION_SRC_DIRECTORY = "";
  private final static String DESTINATION_PACKAGE       = ""; // Based on the Schema name..., 
  private final static String DESTINATION_CLASSES_DIRECTORY = "";
  private final static String FACTS_OUTPUT_DIRECTORY = ".";
  
  private static String repositoryPath              = REPOSITORY_PATH;
  private static String ruleSetName                 = RULESET_NAME;
  private static String schemaLocation              = SCHEMA_LOCATION;
  private static String xmlInstanceName             = XML_INSTANCE;
  private static String destinationSrcDirectory     = DESTINATION_SRC_DIRECTORY;
  private static String destinationPackage          = DESTINATION_PACKAGE;
  private static String destinationClassesDirectory = DESTINATION_CLASSES_DIRECTORY;
  private static String factsOutput                 = FACTS_OUTPUT_DIRECTORY;
  
  public final static String VERBOSE_PRM         = "-verbose";
  public final static String REPOSITORY_PATH_PRM = "-repository-path"; 
  public static final String SCHENA_LOC_PRM      = "-schema-location";
  public final static String RULESET_NAME_PRM    = "-ruleset-name"; 
  public final static String INSTANCE_NAME_PRM   = "-xml-instance-name"; 
  public final static String PACKAGE_NAME_PRM    = "-dest-package";
  public final static String SKIP_JAXB_PRM       = "-skip-jaxb"; 
  public final static String FACT_OUTPUT_PRM     = "-facts-output"; 
  public final static String SRC_DIRECTORY_PRM   = "-src-dest-directory"; 
  public final static String CLS_DIRECTORY_PRM   = "-class-dest-directory"; 
  
  
  public final static DecimalFormat ELAPSED_TIME_FORMAT = new DecimalFormat("#0.000");
  
  private static boolean verbose = true;
  
  private static JAXBContext context = null;
  private static DOMParser parser = new DOMParser();
  
  private static String elapsedTimeString = "";
  private static RuleSessionPool pool = null;
  private static ArrayList<String> rulesSets = null;
  private static RuleSession session = null;
  private static PoolableObject<RuleSession> po = null;
  
  private static boolean invalidateRulesSession = true;
  private static boolean forceInvalidate = false;
  
  private static boolean skipJAXB = false;
  
  private final static boolean executeRulesets = false;

  private final static void getPrms(String[] prms, boolean print)
  {
    for (int i=0; i<prms.length; i++)
    {
      if (prms[i].equals(VERBOSE_PRM))
      {
        verbose = (prms[i+1].equalsIgnoreCase("Y") ||
                   prms[i+1].equalsIgnoreCase("YES") ||
                   prms[i+1].equalsIgnoreCase("TRUE"));
        if (print)
          System.out.println(prms[i] + ":\t" + Boolean.toString(verbose));
      }
      if (prms[i].equals(SKIP_JAXB_PRM))
        skipJAXB = true;
      if (prms[i].equals(REPOSITORY_PATH_PRM))      
      {
        repositoryPath = prms[i+1];
        if (print)
          System.out.println(prms[i] + ":\t" + repositoryPath);
      }
      if (prms[i].equals(RULESET_NAME_PRM))
      {
        ruleSetName = prms[i+1];
        if (print)
          System.out.println(prms[i] + ":\t" + ruleSetName);
      }
      if (prms[i].equals(SCHENA_LOC_PRM))      
      {
        schemaLocation = prms[i+1];
        if (print)
          System.out.println(prms[i] + ":\t" + schemaLocation);
      }
      if (prms[i].equals(INSTANCE_NAME_PRM))   
      {
        xmlInstanceName = prms[i+1];
        if (print)
          System.out.println(prms[i] + ":\t" + xmlInstanceName);
      }
      if (prms[i].equals(SRC_DIRECTORY_PRM))
      {
        destinationSrcDirectory = prms[i+1];
        if (print)
          System.out.println(prms[i] + ":\t" + destinationSrcDirectory);
      }
      if (prms[i].equals(PACKAGE_NAME_PRM)) 
      {
        destinationPackage = prms[i+1];
        if (print)
          System.out.println(prms[i] + ":\t" + destinationPackage);
      }
      if (prms[i].equals(CLS_DIRECTORY_PRM)) 
      {
        destinationClassesDirectory = prms[i+1];
        if (print)
          System.out.println(prms[i] + ":\t" + destinationClassesDirectory);
      }
//      if (prms[i].equals("-jaxb-jars"))
//      {
//        jaxbCompilerJars = prms[i+1];
//        if (print)
//          System.out.println(prms[i] + ":\t" + jaxbCompilerJars);
//      }
      if (prms[i].equals(FACT_OUTPUT_PRM)) 
      {
        factsOutput = prms[i+1];
        if (print)
          System.out.println(prms[i] + ":\t" + factsOutput);
      }
    }
  }    
  
  private static void dumpFact(Object fact) throws Exception
  {
    if (fact instanceof RLObject)
    {
      RLObject rlobject = (RLObject)fact;
      System.out.println(" - RLObject:" + rlobject.toString());
      RLClass rlclass = rlobject.getRLClass();
      System.out.println(" -- RLClass:" + rlclass.getName());
      RLProperty[] proparray = rlclass.getProperties();
      for (int i=0; i<proparray.length; i++)
      {
        RLProperty rlp = proparray[i];
        System.out.print(" --- property [" + rlp.getName() + "] = [" + rlp.getType().getName() + "]");
        if (rlp.getType() == String.class)
        {
          String val = (String)rlp.get(rlobject);
          System.out.println(" value [" + val + "]");
        }
      }
    }
    else
    {
      System.out.println("Fact is a " + fact.getClass().getName() + " (" + fact.toString() + ")");
    }    
  }
  
  /**
   * Can be used by ant as a Java task.
   * @param args See above, and
   * @see #AssertXMLFact.getPrms
   * 
   * you can also use -Duse.assert.tree=true|false to 
   * systematically use assertTree or just assert 
   * for the input facts.
   */
  public static void main(String[] args)
  {
    run(args);
  }
  
  public static String run(String[] args)
  {
    return run(args, null);
  }
  
  public static String run(String[] args, Writer rulesOutput)
  {
    if (verbose)
    {
      System.out.println("AssertXMLFact.run arguments:");
      for (String s : args)
        System.out.println(s);
    }
    
    String retRulesCode = "";
    long before = System.nanoTime(), after = 0L;

    use_assert_tree = (System.getProperty("use.assert.tree", "false").equals("true"));
//  if (rulesSets == null || invalidateRulesSession)
      rulesSets = new ArrayList<String>(1);

    getPrms(args, System.getProperty("display.msg", "false").equals("true"));
    if (skipJAXB && destinationPackage.trim().length() == 0)
    {
      System.out.println("Please provide the JAX-B Context in the destination package (-dest-pack) if you want to skip the JAX-B Compilation");
      System.out.println("The elements of the JAX-B Context must also be available in the current classpath.");
      System.exit(1);
    }
    try
    {
      // Compilation Part
      DynamicCompilationV2 dc = null; 
      ArrayList<String> pl = null;
      before = System.nanoTime();
      if (!skipJAXB)
      {
        URL schemaUrl = null;
        File sf = new File(schemaLocation);
        if (!sf.exists())
          schemaUrl = new URL(schemaLocation);
        else
          schemaUrl = sf.toURI().toURL();
        dc = new DynamicCompilationV2(schemaUrl, 
                                      destinationSrcDirectory, 
                                      destinationPackage, // blank: default
                                      destinationClassesDirectory, 
//                                    jaxbCompilerJars, 
                                      verbose, 
                                      DynamicCompilationV2.CREATE_JAR_FILE);    
        pl = dc.generate();
        invalidateRulesSession = !dc.isAlreadyCompiled();
        String clsName = pl.get(0) + ".ObjectFactory";
        try
        {
        /* Class generatedClass = */ Class.forName(clsName);
          if (verbose) System.out.println("Loaded!");
        }
        catch (ClassNotFoundException cnfe)
        {
          System.out.println("------------------------------");
          System.out.println("Problem loading [" + clsName + "]");
          System.out.println("------------------------------");
        }
        after =  System.nanoTime();
        if (!dc.isAlreadyCompiled())
          elapsedTimeString += "Schema compiled\tin " + ELAPSED_TIME_FORMAT.format((double)(after - before)/1E9) + " s (" +  Long.toString(after - before) + " ns).\n";
      }
      else
        invalidateRulesSession = false;

      if (forceInvalidate)
      {
        invalidateRulesSession = true;
        forceInvalidate = false; // Reset
      }
      if (System.getProperty("display.msg", "false").equals("true")) System.out.println(" == [[ Schema compiled in " + ELAPSED_TIME_FORMAT.format((double)(after - before)/1E9) + " s (" +  Long.toString(after - before) + " ns). ]]");
      // Load the XML Instance
      String jaxbCtxPackName = "";
      if (pl != null)
      {
        for (String s : pl)
          jaxbCtxPackName += ((jaxbCtxPackName.length()>0?":":"") + s);
      }
      else
        jaxbCtxPackName = destinationPackage;

      if (verbose) System.out.println("JAXBContext [" + jaxbCtxPackName + "]");
      context = JAXBContext.newInstance(jaxbCtxPackName);
      Unmarshaller um = context.createUnmarshaller();
      
      File f = new File(xmlInstanceName);
      if (!f.exists())
      {
        throw new RuntimeException(xmlInstanceName + " not found");
      }
      else
      {
        URL instanceDoc = f.toURI().toURL();
        before = System.nanoTime();
        parser.parse(instanceDoc);
        XMLDocument factCollection = parser.getDocument();
        after =  System.nanoTime();
        elapsedTimeString += "Input Document parsed\tin " + ELAPSED_TIME_FORMAT.format((double)(after - before)/1E9) + " s (" +  Long.toString(after - before) + " ns).\n";
        if (System.getProperty("display.msg", "false").equals("true")) System.out.println(" == [[ Document parsed in " + ELAPSED_TIME_FORMAT.format((double)(after - before)/1E9) + " s (" +  Long.toString(after - before) + " ns). ]]");
        
  //      System.out.println(System.getProperty("java.class.path"));
        // Rules part
        String dmrl = null;
        ArrayList<String> rsrl = new ArrayList<String>();

        long bigBefore = System.nanoTime();
        if (pool == null || invalidateRulesSession)
        {
          // load dictionary  
          RuleDictionary dict = null;
          Reader reader = null;
          boolean ok2go = true;
          try
          {
            if (verbose) System.out.println("Reading Rules Repository:" + new File(repositoryPath).getCanonicalPath());
            before = System.nanoTime();
            reader = new FileReader(new File(repositoryPath));
            dict = RuleDictionary.readDictionary(reader, new DecisionPointDictionaryFinder(null)); // TASK See other flavors, if any.
            // validate dictionary
            List<SDKWarning> warnings = new ArrayList<SDKWarning>();
            dict.update(warnings);
            after =  System.nanoTime();
            elapsedTimeString += ("Dictionary read\tin " + ELAPSED_TIME_FORMAT.format((double)(after - before)/1E9) + " s (" +  Long.toString(after - before) + " ns).\n");
            if (System.getProperty("display.msg", "false").equals("true")) System.out.println(" == [[ Dictionary read  in " + ELAPSED_TIME_FORMAT.format((double)(after - before)/1E9) + " s (" +  Long.toString(after - before) + " ns). ]]");
  
            // RL can only be generated if the dictionary is valid
            if (warnings.size() > 0)
            {
              String str = "Validation warnings:\n" + warnings;
              System.out.println(str);
              JOptionPane.showMessageDialog(null, str, "RL Warnings", JOptionPane.ERROR_MESSAGE);
              ok2go = false;
              throw new RuntimeException(Integer.toString(warnings.size()) + " RL Warning(s).");
            }
          }
          catch (Exception e)
          {
            e.printStackTrace();
            ok2go = false;
          }
          finally
          {
            if (reader != null)
            {
              try
              {
                reader.close();
              }
              catch (IOException ioe)
              {
                ioe.printStackTrace();
              }
            }
          }
          
          if (ok2go)
          {
            try
            {
              // generate RL code
              before = System.nanoTime();
              dmrl = dict.dataModelRL();          
              rsrl.add(dmrl);
              after =  System.nanoTime();
              elapsedTimeString += "RL Generated\tin " + ELAPSED_TIME_FORMAT.format((double)(after - before)/1E9) + " s (" +  Long.toString(after - before) + " ns).\n";
              if (System.getProperty("display.msg", "false").equals("true")) System.out.println(" == [[ RL Generated in " + ELAPSED_TIME_FORMAT.format((double)(after - before)/1E9) + " s (" +  Long.toString(after - before) + " ns). ]]");
              StringTokenizer strtok = new StringTokenizer(ruleSetName, ",");
              while (strtok.hasMoreTokens())
              {
                String rs = strtok.nextToken().trim();
                if (System.getProperty("display.msg", "false").equals("true")) System.out.println("Adding RuleSet [" + rs + "]");
                rulesSets.add(rs);
              }
              for (String str : rulesSets)
                rsrl.add(dict.ruleSetRL(str));
            }
            catch (Exception e)
            {
              System.err.println("Reading the Dictionnary...");
              JOptionPane.showMessageDialog(null, e.toString(), "Oops", JOptionPane.ERROR_MESSAGE);
              e.printStackTrace();
            }
    
            // A test
            if (false)
            {
              DictionarySearch ds = new DictionarySearch(dict);
              List<DOID> searchResult = ds.find("DENIED");
              System.out.println("Search returns " + searchResult.size() + " entry(ies).");
              for (DOID doid: searchResult)
              {
                dumpDoid(doid);
              }
            }
            try { pool = new RuleSessionPool(rsrl); }
            catch (Exception ex)
            {
              JOptionPane.showMessageDialog(null, ex.getLocalizedMessage(), "OBR Error message", JOptionPane.ERROR_MESSAGE);
              ex.printStackTrace();
              System.err.println("------------------------");
              System.err.println("Generated RL:\n----------\n" + dmrl);
              throw ex;
            }
            System.out.println("Pool contains " + pool.getInuse() + " session(s) in use");
    
            try
            {
              // init a rule session
              before = System.nanoTime();
           // session = new RuleSession();
              po = pool.getPoolableRuleSession();
              session = po.getPooledObject(); 
              
              invalidateRulesSession = false;
              after =  System.nanoTime();
              elapsedTimeString += "Session created\tin " + ELAPSED_TIME_FORMAT.format((double)(after - before)/1E9) + " s (" +  Long.toString(after - before) + " ns).\n";
              if (System.getProperty("display.msg", "false").equals("true")) System.out.println(" == [[ Session created in " + ELAPSED_TIME_FORMAT.format((double)(after - before)/1E9) + " s (" +  Long.toString(after - before) + " ns). ]]");
  //          StringTokenizer strtok = new StringTokenizer(ruleSetName, ",");
              long beforeExecuteRuleset = 0L;  
              if (executeRulesets)
              {
                elapsedTimeString += "-- executeRuleset --\n";
                beforeExecuteRuleset = System.nanoTime();
                before = beforeExecuteRuleset;
                session.executeRuleset(dmrl); // Parameter is the rules generated code.
                after =  System.nanoTime();
                elapsedTimeString += "    " + ELAPSED_TIME_FORMAT.format((double)(after - before)/1E9) + " s (" +  Long.toString(after - before) + " ns).\n";
    //          for (String str : rulesSets)
    //            session.setRulesetName(str);
              }  
              retRulesCode = dmrl + "\n";
              
              for (String codeLine : rsrl)
              {
                retRulesCode += (codeLine + "\n");
                if (executeRulesets)
                {
                  before = System.nanoTime();
                  session.executeRuleset(codeLine);
                  after =  System.nanoTime();
                  elapsedTimeString += "    " + ELAPSED_TIME_FORMAT.format((double)(after - before)/1E9) + " s (" +  Long.toString(after - before) + " ns).\n";
                }
              }
              if (executeRulesets)
              {
                after =  System.nanoTime();
                elapsedTimeString += "--------------------\nTotal:" + Long.toString(after - beforeExecuteRuleset) + " ns.\n";
              }
            }
            catch (Exception ex)
            {
              System.err.println(ex.getLocalizedMessage());
            }
          }
        }
        else
        {
          // From the pool
          System.out.println("-> Pool now contains " + pool.getInuse() + " session(s) in use");
          po = pool.getPoolableRuleSession();
          session = po.getPooledObject(); 
          System.out.println(" ... now " + pool.getInuse() + " session(s) in use");
          System.out.println("-- Rulesets in the session --");
          Map<String, Ruleset> msr = session.getRulesets();
          Set<String> k = msr.keySet();
          for (String s : k)
            System.out.println("Ruleset: " + msr.get(s).getName());
          System.out.println("-----------------------------");
        }
        if (rulesOutput == null)
          rulesOutput = new PrintWriter(System.out);
        session.setOutputWriter(rulesOutput);                         
        
        // Initialization completed
        after = System.nanoTime();
        elapsedTimeString += "Initialization completed in " + ELAPSED_TIME_FORMAT.format((double)(after - bigBefore)/1E9) + " s (" +  Long.toString(after - bigBefore) + " ns).\n";        
        
        try
        {
          if (System.getProperty("display.msg", "false").equals("true")) 
          {
            System.out.println("-- Rulesets in the session --");
            Map<String, Ruleset> msr = session.getRulesets();
            Set<String> k = msr.keySet();
            for (String s : k)
              System.out.println("Ruleset: " + msr.get(s).getName());
            System.out.println("-----------------------------");
          }
//          elapsedTimeString += "RL executed\tin " + DF.format((double)(after - before)/1E9) + " s (" +  Long.toString(after - before) + " ns).\n";
          if (System.getProperty("display.msg", "false").equals("true")) 
            System.out.println(" == [[ RL executed in " + ELAPSED_TIME_FORMAT.format((double)(after - before)/1E9) + " s (" +  Long.toString(after - before) + " ns). ]]");
          if (verbose)
          {
            // Print out RL code
            System.out.println("Rules Code:");
            System.out.println("-----------------------");
            System.out.println(retRulesCode);
            System.out.println("-----------------------");
          }

          NodeList facts = factCollection.selectNodes("fact:unit-testing-facts/fact:fact", new NSResolver()
                                                      {
                                                        public String resolveNamespacePrefix(String prefix) 
                                                        {
                                                          return "urn:rules-unit";
                                                        }
                                                      });
          if (verbose) System.out.println("Package name: [" + destinationPackage + "]");
          if (verbose) System.out.println(Integer.toString(facts.getLength()) + " fact(s) to assert.");
          bigBefore = System.nanoTime();
          before = bigBefore;
          for (int i=0; i<facts.getLength(); i++)
          {
            if (verbose) System.out.println("============ Fact ===========");
            // Look for the first child Element (not Text)
            NodeList nl = facts.item(i).getChildNodes();
            Node fact = null;
            for (int j=0; j<nl.getLength(); j++)
            {
              Node n = nl.item(j);
              if (n.getNodeType() == Node.ELEMENT_NODE)
              {
                fact = n;
                break;
              }
            }
            if (verbose) 
            {
              System.out.println("== Extracted Fact: ==");
              ((XMLElement)fact).print(System.out);
            }
            try
            {
              Object unmarshalled = um.unmarshal(/*(Node)*/fact);
              if (unmarshalled instanceof JAXBElement) // 01-Jul-2008
              {
                JAXBElement jaxbElement = (JAXBElement)unmarshalled;
  //            Class c = jaxbElement.getDeclaredType();
  //            System.out.println("Unmarshalling returned a [" + c.getName() + "]");
                unmarshalled = jaxbElement.getValue();
              }
              if (verbose) 
                System.out.println("Unmarshalling returned a [" + unmarshalled.getClass().getName() + "]");      
              // Destination Package?
              if (destinationPackage.trim().length() == 0)
              {
                String className = unmarshalled.getClass().getName();
                String packName  = className.substring(0, className.lastIndexOf("."));
                String objFact   = packName + ".ObjectFactory";
                try
                {
               /* Class ofClass = */ Class.forName(objFact);
                  destinationPackage = packName;
                }
                catch (ClassNotFoundException cnfe)
                {
                  System.err.println(cnfe.getLocalizedMessage());
                }
              }
              
              // Temp, for tests
              if (false && unmarshalled.getClass().getName().equals("org.dyndf.Input"))
              {
                System.out.println("========= DF, for tests ============================");
                String df = "mar23_02.OBR_DynDecisionFunction.DecisionFunction";
                System.out.println("Executing Decision Function [" + df +"]");
                Object prm = unmarshalled;
                List result = (List)session.callFunctionWithArgument(df, prm);
                System.out.println("DF Executed.");
                System.out.println("Returned a list of " + result.size() + " element(s).");
                for (Object o : result)
                  System.out.println("- We have a " + o.getClass().getName());
                System.out.println("============= End of Test =========================");
              }
              if (verbose)
              {
                Iterator<Object> iterator = session.getFactObjects();
                int nbf = 0;
                while (iterator.hasNext())
                {
                  nbf++;
                  iterator.next();
                }
                System.out.println("There are " + nbf + " fact(s) in the working memory");
              }
              if (verbose) System.out.print("Now asserting fact... (using " + (use_assert_tree?"assertTree":"assert") + ")");
              before = System.nanoTime();
              session.callFunctionWithArgument((use_assert_tree?"assertTree":"assert"), unmarshalled); 
              after =  System.nanoTime();
              elapsedTimeString += "  - 1 fact asserted\tin " + ELAPSED_TIME_FORMAT.format((double)(after - bigBefore)/1E9) + " s (" +  Long.toString(after - bigBefore) + " ns).\n";
              if (verbose) System.out.println("... Fact" + (use_assert_tree?" (tree)":"") + " asserted");
            }
            catch (Exception wow)
            {
              System.out.println("===============");
              System.out.println(wow.getMessage());
              System.out.println("===============");
              wow.printStackTrace();
            }
          }
          after =  System.nanoTime();
          elapsedTimeString += "Facts asserted\tin " + ELAPSED_TIME_FORMAT.format((double)(after - bigBefore)/1E9) + " s (" +  Long.toString(after - bigBefore) + " ns).\n";
          if (System.getProperty("display.msg", "false").equals("true")) System.out.println(" == [[ Facts asserted in " + ELAPSED_TIME_FORMAT.format((double)(after - bigBefore)/1E9) + " s (" +  Long.toString(after - bigBefore) + " ns). ]]");
          
          if (verbose)
          {
            session.callFunction("watchAll");
            session.callFunction("showFacts");
            session.callFunction("showActivations");
            session.callFunction("watchFacts");
            session.callFunction("watchRules");
            session.callFunction("watchActivations");
            session.callFunctionWithArgument("setDecisionTraceLevel", RuleSession.DECISION_TRACE_DEVELOPMENT);
          }
          else
          {
            session.callFunction("clearWatchFacts");
            session.callFunction("clearWatchRules");
            session.callFunction("clearWatchActivations");
            session.callFunction("clearWatchAll");
            session.callFunctionWithArgument("setDecisionTraceLevel", RuleSession.DECISION_TRACE_OFF);
          }
          // Running Rulesets here
          before = System.nanoTime();
          
          if (rulesSets.size() == 0) // From the pool...
          {
            Map<String, Ruleset> mapRS = session.getRulesets();
            for (String s : mapRS.keySet())
            {
              System.out.println("Getting RS [" + s + "] from the pool");
              rulesSets.add(s);
            }
          }            
          
          if (verbose)
            session.callFunction("showFacts");     // Before

          for (String rs : rulesSets)
          {
            if (verbose) System.out.println("================================================================");
            if (verbose) System.out.println("Now running " + rs);
            long beforeRS = System.nanoTime();
            session.callFunctionWithArgument("run", rs); // Here is the execution of the ruleset
            long afterRS = System.nanoTime();
            elapsedTimeString += "- Ruleset [" + rs + "] executed\tin " + ELAPSED_TIME_FORMAT.format((double)(afterRS - beforeRS)/1E9) + " s (" +  Long.toString(afterRS - beforeRS) + " ns).\n";
          }          
          after =  System.nanoTime();
          elapsedTimeString += "Ruleset(s) executed\tin " + ELAPSED_TIME_FORMAT.format((double)(after - before)/1E9) + " s (" +  Long.toString(after - before) + " ns).\n";
          if (System.getProperty("display.msg", "false").equals("true")) 
            System.out.println(" == [[ Ruleset(s) executed in " + ELAPSED_TIME_FORMAT.format((double)(after - before)/1E9) + " s (" +  Long.toString(after - before) + " ns). ]]");
          
          if (verbose) System.out.println("================================================================");

          if (verbose)
            session.callFunction("showFacts");     // After     
          
          Integer tl = (Integer)session.callFunction("getDecisionTraceLevel");
          System.out.println("Trace Level tl is [" + tl.intValue() + "]");

          Object dt = session.callFunction("getDecisionTrace");
//        System.out.println("DecisionTrace dt is a [" + dt.getClass().getName() + "]");
          if (dt instanceof DecisionTrace)
          {
            DecisionTrace dTrace = (DecisionTrace)dt;
            List<TraceEntry> entryList = dTrace.getTraceEntries();
//          System.out.println(entryList.size() + " entry(ies)");
            for (TraceEntry te : entryList)
            {
//            System.out.println("TraceEntry is a " + te.getClass().getName() + ", rule [" + te.getInRule() + "]");
              if (te instanceof FactTrace)
              {
                String inRule = te.getInRule();
                if (inRule != null)
                  System.out.println("-> In rule [" + inRule + "] " + ((FactTrace)te).getOperation().value());
              }
            }
          }
          
          // parse rules output to find rules execution
          if (verbose)
          {
//          System.out.println("Rules output is a " + rulesOutput.getClass().getName() );
            if (rulesOutput instanceof StringWriter)
            {
              StringWriter sw = (StringWriter)rulesOutput;
              String[] lines = sw.getBuffer().toString().split("\\n");
//            System.out.println("==============================================");
//            System.out.println("Found " + lines.length + " line(s) in the output.");
//            System.out.println("==============================================");
              String pattern = "(Fire [0-9]+)(\\s)([a-zA-Z\\.]*)(\\s)(f\\-[0-9]+(.*))";
              Pattern p = Pattern.compile(pattern);
              
              Map<String, Integer> rulesCount = new HashMap<String, Integer>();
              
              for (String s : lines)
              {
                Matcher matcher = p.matcher(s);
                if (matcher.find())
                {
//                System.out.println("===> [" + s + "], " + matcher.groupCount() + " group(s).");
                  // We want group 3
                  String rule = matcher.group(3);
                  Integer n = rulesCount.get(rule);
                  if (n == null)
                    n = new Integer(0);
                  rulesCount.put(rule, new Integer(n.intValue() + 1));
//                for (int i=0; i<matcher.groupCount(); i++)
//                  System.out.println("Group " + i + "=" + matcher.group(i));
                }
              }
              elapsedTimeString += "\n\n";
              Set<String> keys = rulesCount.keySet();
              for (String k : keys)
              {
                elapsedTimeString += ("- Rule [" + k + "] fired " + rulesCount.get(k).intValue() + " time(s)\n");
              }
            }
          }

          if (verbose) 
            System.out.println("Now retrieving facts");
          Marshaller m = context.createMarshaller();
          m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.valueOf(true));
          int factIndex = 1;
          NumberFormat nf = new DecimalFormat("0000");
          File outputDir = new File(factsOutput);
          if (!outputDir.exists())
            outputDir.mkdirs();
          BufferedWriter allfacts = new BufferedWriter(new FileWriter(factsOutput + File.separator + "allfacts.xml"));
          allfacts.write("<?xml version='1.0'?>\n");
          allfacts.write("<all-facts xmlns='urn:rules-unit'>\n");
          Iterator<Object> iterator = session.getFactObjects();
          while (iterator.hasNext())
          {
            boolean javaDump = false;
            Object fact = iterator.next();              
            if (verbose) 
              dumpFact(fact);              
            try
            {
              OutputStream baos = new ByteArrayOutputStream();              
              try
              {
                try
                {
                  m.marshal(fact, baos);                    
                }
                catch (Exception ex)
                {
    //            ObjectFactory of = new ObjectFactory();
                  Class ofClass = Class.forName(destinationPackage + ".ObjectFactory");
                  Object of = ofClass.newInstance();
    //            System.out.println("We have a " + of.getClass().getName());
                  String className = fact.getClass().getName();
    //            System.out.println("Fact is a " + className);
                  String methodName = "create" + className.substring(className.lastIndexOf(".") + 1);
//                Method method = of.getClass().getMethod(methodName, new Class[] { fact.getClass() });
                  Method method = of.getClass().getMethod(methodName, new java.lang.Class[] {}); // (Class<?>[])null);
//                Object o = method.invoke(of, new Object[] { fact });
                  Object o = method.invoke(of); //, null);
                  try { m.marshal(o, baos); }
                  catch (MarshalException e)
                  {
                    System.err.println("MarshalException:");
                    e.printStackTrace();
                    javaDump = true;
                    XMLEncoder encoder = new XMLEncoder(baos);
                    encoder.writeObject(fact);
                    encoder.close();
                  }
                }
              }
              catch (Exception ex2)
              {
                if (verbose && ex2 instanceof ClassNotFoundException)
                {
                  System.err.println("Class Not Found [" + destinationPackage + ".ObjectFactory" + "]");
                }
                if (verbose && ex2 instanceof NoSuchMethodException)
                {
                  System.err.println("Not such method...");
                }
             // System.out.println(ex2.toString());
                if (fact instanceof oracle.rules.rl.session.Link)
                  ; // Skip
                else
                {  
                  try
                  {
                    m.marshal(fact, baos);
                  }
                  catch (Exception ex3)
                  {
                    if (verbose || System.getProperty("display.msg", "false").equals("true"))
                    {
                      System.out.println(ex3.toString());
                      System.out.println("-- No way to display the " + fact.getClass().getName() + " --");
                    }
                    // Last Resort...
    //                  try
    //                  {
    //                    XMLEncoder encoder = new XMLEncoder(System.out);
    //                    encoder.writeObject(fact);
    //                    encoder.close();
    //                  }
    //                  catch (Exception ex4)
    //                  {
    //                    System.out.println("-- No way to display the " + fact.getClass().getName() + " --");
    //                  }
                  }
                }
              }
              String strDoc = baos.toString();
              if (strDoc.trim().length() > 0)
              {
                if (verbose) System.out.println(strDoc);
                BufferedWriter bw = new BufferedWriter(new FileWriter(factsOutput + File.separator + "fact_" + nf.format(factIndex++) + ".xml"));
                bw.write(strDoc);
                bw.close();
                parser.parse(new StringReader(strDoc));
                XMLDocument doc = parser.getDocument();
                XMLElement root = (XMLElement)doc.getDocumentElement();
                if (javaDump)
                {
                  Comment comment = doc.createComment("comment");
                  comment.setData("Impossible to marshal [" + fact.getClass().getName() + "], consider reworking your schema (use elements instead of types...)");
                  root.appendChild(comment);
                }
                
                ByteArrayOutputStream baos4all = new ByteArrayOutputStream();
                root.print(baos4all);
                allfacts.write(baos4all.toString());
              }
            }
            catch (Exception ex)
            {
              if (verbose) System.out.println("Marshalling Exception:" + ex.toString());
            }
          }
          allfacts.write("</all-facts>\n");
          allfacts.close();
          if (po != null)            
            pool.returnPoolableRuleSession(po);              
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }    
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return retRulesCode;
  }

  private static void dumpDoid(DOID doid)
  {
    System.out.println("==> " + doid.toString() + 
                       ", JavaClassName:" + doid.getJavaClassName() +
                       ", " + (doid.isValid()?"Valid":"Invalid"));
  }
  
  public static void setElapsedTimeString(String elapsedTimeString)
  {
    AssertXMLFact.elapsedTimeString = elapsedTimeString;
  }

  public static String getElapsedTimeString()
  {
    return elapsedTimeString;
  }

  public static void setInvalidateRulesSession(boolean invalidateRulesSession)
  {
    AssertXMLFact.forceInvalidate = invalidateRulesSession;
  }

  public static boolean isInvalidateRulesSession()
  {
    return invalidateRulesSession;
  }
  
  public static void cleanKnowledgeBase()
  {
    if (session != null)
    {
      Iterator<Object> iterator = session.getFactObjects();
      while (iterator.hasNext())
      {
        try
        {
          Object fact = iterator.next();
          session.callFunctionWithArgument("retract", fact);
        }
        catch (RLException rle)
        {
          System.err.println(rle.getLocalizedMessage());
        }
      }
    }
  }
}
