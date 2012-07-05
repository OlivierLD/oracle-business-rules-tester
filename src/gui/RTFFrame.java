package gui;

import gui.util.GnlUtilities;

import gui.widgets.RSPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.io.FileReader;
import java.io.IOException;

import java.io.StringReader;
import java.io.StringWriter;

import java.util.ArrayList;

import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import xmlfacts.AssertXMLFact;

public class RTFFrame
  extends JFrame
{
  private String testFileName = "";
  private transient DOMParser parser = new DOMParser();
  private final static String TESTER_URI      = "urn:rules-testing-framework";
  private final static String DICTIONNARY_URI = "http://xmlns.oracle.com/rules/dictionary";
  
  private JPanel northPanel = new JPanel();
  private JPanel centerPanel = new JPanel();
  
  private BorderLayout layoutMain = new BorderLayout();
  private JPanel panelCenter = new JPanel();
  private JMenuBar menuBar = new JMenuBar();
  private JMenu menuFile = new JMenu();
  private JMenuItem menuFileExit = new JMenuItem();
  private JMenu menuHelp = new JMenu();
  private JMenuItem menuHelpAbout = new JMenuItem();
  
  private JMenu menuClean = new JMenu();
  private JMenuItem menuCleanFactOutput = new JMenuItem();
  private JMenuItem menuCleanJAXBDir    = new JMenuItem();
  
  private JLabel statusBar = new JLabel();
  private JToolBar toolBar = new JToolBar();
  private JButton buttonOpen = new JButton();
  private JButton buttonClose = new JButton();
  private JButton buttonHelp = new JButton();
  private ImageIcon imageOpen  = new ImageIcon(RTFFrame.class.getResource("img/open.png"));
  private ImageIcon imageClose = new ImageIcon(RTFFrame.class.getResource("img/save.png"));
  private ImageIcon imageHelp  = new ImageIcon(RTFFrame.class.getResource("img/help.png"));
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel jLabel1 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private JLabel jLabel3 = new JLabel();
  private JLabel jLabel4 = new JLabel();
  private JLabel jLabel5 = new JLabel();
  private JLabel jLabel6 = new JLabel();
  private JLabel jLabel7 = new JLabel();
  private JLabel jLabel9 = new JLabel();
  private JLabel testDataFileLabel = new JLabel();
  private JTextField repositoryPathTextField = new JTextField();
  private JTextField factsSchemaTextField = new JTextField();
  private JTextField generatedSourcesTextField = new JTextField();
  private JTextField packageNameTextField = new JTextField();
  private JTextField classesDirectoryTextField = new JTextField();
  private JTextField xmlInputFactsTextField = new JTextField();
  private JTextField outputFactsTextField = new JTextField();
  private JCheckBox useAssertTreeCheckBox = new JCheckBox();
  private JCheckBox generateJarFileCheckBox = new JCheckBox();
  private JButton browseRulesRepositoryButton = new JButton();
  private JButton browseFactSchemaButton = new JButton();
  private JButton browseSourceButton = new JButton();
  private JButton browseClassesButton = new JButton();
  private JButton browseInputFactsButton = new JButton();
  private JButton browseOutFactsButton = new JButton();
  private JLabel jLabel10 = new JLabel();
  private JPanel runButtonPanel = new JPanel();
  private JButton runTestButton = new JButton();
  private JCheckBox verboseCheckBox = new JCheckBox();
  private JLabel ruleSetLabel = new JLabel(" - ");
  private JButton ruleSetButton = new JButton();
  private JPanel tabbedPanel = new JPanel();
  private JTabbedPane jTabbedPane = new JTabbedPane();
  private JTabbedPane inputTabbedPane = new JTabbedPane();
  private JTabbedPane outputTabbedPane = new JTabbedPane();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane rlCodeScrollPane = new JScrollPane();
  private JTextArea rlTextArea = new JTextArea();
  private JTextArea elapsedTextArea = new JTextArea();
  private JScrollPane rulesOutputScrollPane = new JScrollPane();
  private JScrollPane timeOutputScrollPane = new JScrollPane();
  private JTextArea rulesOutputTextArea = new JTextArea();
  private JCheckBox splitInputCheckBox = new JCheckBox();
  private JCheckBox splitOutputCheckBox = new JCheckBox();
  private JCheckBox eraseFactCheckBox = new JCheckBox();
  private JCheckBox forceRebuildCheckBox = new JCheckBox();

  public RTFFrame()
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    this.setJMenuBar( menuBar );
    this.getContentPane().setLayout( layoutMain );
    northPanel.setLayout(gridBagLayout1);
    centerPanel.setLayout(new BorderLayout());
    
    panelCenter.setLayout(new BorderLayout());
    this.setSize(new Dimension(700, 680));
    this.setTitle( "Rules Testing Utility - User Interface" );
    menuFile.setText( "File" );
    menuFileExit.setText( "Exit" );
    menuFileExit.addActionListener( new ActionListener() { public void actionPerformed( ActionEvent ae ) { fileExit_ActionPerformed( ae ); } } );
    
    menuClean.setText("Clean");
    menuCleanFactOutput.setText("Clean fact output directory");
    menuCleanJAXBDir.setText("Clean generated sources and classes");
    menuFile.add(menuClean);
    menuFile.add(new JSeparator());
    menuClean.add(menuCleanFactOutput);
    menuClean.add(menuCleanJAXBDir);
    menuCleanFactOutput.addActionListener( new ActionListener() 
                                           { 
                                             public void actionPerformed( ActionEvent ae ) 
                                             { cleanFacts_ActionPerformed( ae ); } 
                                           } );
    menuCleanJAXBDir.addActionListener( new ActionListener() 
                                        { 
                                          public void actionPerformed( ActionEvent ae ) 
                                          { cleanJAXB_ActionPerformed( ae ); } 
                                        } );
    menuClean.setEnabled(false);
    
    menuHelp.setText( "Help" );
    menuHelpAbout.setText( "About" );
    menuHelpAbout.addActionListener( new ActionListener() { public void actionPerformed( ActionEvent ae ) { helpAbout_ActionPerformed( ae ); } } );
    statusBar.setText( "" );
    buttonOpen.setToolTipText( "Open Test Description" );
    buttonOpen.setIcon( imageOpen );
    buttonOpen.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            buttonOpen_actionPerformed(e);
          }
        });
    buttonClose.setToolTipText( "Save Test Description" );
    buttonClose.setIcon( imageClose );
    buttonClose.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            buttonSave_actionPerformed(e);
          }
        });
    buttonHelp.setToolTipText( "About" );
    buttonHelp.setIcon( imageHelp );
    buttonHelp.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            buttonHelp_actionPerformed(e);
          }
        });
    jLabel1.setText("Test Description File");
    jLabel2.setText("Rules Dictionary Path");
    jLabel3.setText("Fact Schema");
    jLabel4.setText("XML Input Facts file");
    jLabel5.setText("Generated Java sources directory");
    jLabel6.setText("Package Name");
    jLabel7.setText("Classes Directory");
    jLabel9.setText("Output Facts directory");
    testDataFileLabel.setText("- none -");
    repositoryPathTextField.setPreferredSize(new Dimension(250, 20));
    repositoryPathTextField.setHorizontalAlignment(JTextField.TRAILING);
    factsSchemaTextField.setHorizontalAlignment(JTextField.TRAILING);
    factsSchemaTextField.setPreferredSize(new Dimension(250, 20));
    generatedSourcesTextField.setPreferredSize(new Dimension(250, 20));
    generatedSourcesTextField.setHorizontalAlignment(JTextField.TRAILING);
    packageNameTextField.setPreferredSize(new Dimension(250, 20));
    classesDirectoryTextField.setPreferredSize(new Dimension(250, 20));
    classesDirectoryTextField.setHorizontalAlignment(JTextField.TRAILING);
//  jaxbJarFilesTextField.setPreferredSize(new Dimension(250, 20));
    xmlInputFactsTextField.setPreferredSize(new Dimension(250, 20));
    xmlInputFactsTextField.setHorizontalAlignment(JTextField.TRAILING);
    outputFactsTextField.setPreferredSize(new Dimension(250, 20));
    outputFactsTextField.setHorizontalAlignment(JTextField.TRAILING);
    useAssertTreeCheckBox.setText("use assertTree()");
    generateJarFileCheckBox.setText("Generate jar-file");
    browseRulesRepositoryButton.setText("...");
    browseRulesRepositoryButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            browseRulesRepositoryButton_actionPerformed(e);
          }
        });
    browseFactSchemaButton.setText("...");
    browseFactSchemaButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            browseFactSchemaButton_actionPerformed(e);
          }
        });
    browseSourceButton.setText("...");
    browseSourceButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            browseSourceButton_actionPerformed(e);
          }
        });
    browseClassesButton.setText("...");
    browseClassesButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            browseClassesButton_actionPerformed(e);
          }
        });
    browseInputFactsButton.setText("...");
    browseInputFactsButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            browseInputFactsButton_actionPerformed(e);
          }
        });
    browseOutFactsButton.setText("...");
    browseOutFactsButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            browseOutFactsButton_actionPerformed(e);
          }
        });
    jLabel10.setText("Rules Set(s)");
    runTestButton.setText("Run Test");
    runTestButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            runTestButton_actionPerformed(e);
          }
        });
    verboseCheckBox.setText("verbose");
    ruleSetButton.setText("...");
    ruleSetButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            ruleSetButton_actionPerformed(e);
          }
        });
    tabbedPanel.setPreferredSize(new Dimension(575, 300));
    tabbedPanel.setLayout(borderLayout1);
//  outputTextArea.setFont(new Font("Courier New", 0, 11));
//  outputTextArea.setEditable(false);
    rlTextArea.setFont(new Font("Courier New", Font.PLAIN, 11));
    elapsedTextArea.setFont(new Font("Courier new", Font.PLAIN, 11));
    rulesOutputTextArea.setFont(new Font("Courier New", Font.PLAIN, 11));
    splitInputCheckBox.setText("Split Input Facts");
    splitOutputCheckBox.setText("Split Output Facts");
    eraseFactCheckBox.setText("Erase Before");
    eraseFactCheckBox.setToolTipText("<html>Cleanup output facts directory and <br>knowledge base before running the test.</html>");
    forceRebuildCheckBox.setText("Force Rebuild");
    menuFile.add( menuFileExit );
    menuBar.add( menuFile );
    menuHelp.add( menuHelpAbout );
    menuBar.add( menuHelp );
    this.getContentPane().add( statusBar, BorderLayout.SOUTH );
    toolBar.add( buttonOpen );
    toolBar.add( buttonClose );
    toolBar.add( buttonHelp );
    this.getContentPane().add( toolBar, BorderLayout.NORTH );
    northPanel.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(0, 0, 10, 10), 0, 0));
    northPanel.add(jLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 10), 0, 0));
    northPanel.add(jLabel3, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 10), 0, 0));
    northPanel.add(jLabel4, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 10), 0, 0));
    northPanel.add(jLabel5, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 10), 0, 0));
    northPanel.add(jLabel6, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 10), 0, 0));
    northPanel.add(jLabel7, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 10), 0, 0));
//  northPanel.add(jLabel8, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
//        new Insets(0, 0, 0, 10), 0, 0));
    northPanel.add(jLabel9, new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 10), 0, 0));
    northPanel.add(testDataFileLabel,
                    new GridBagConstraints(1, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 10, 0), 0, 0));
    northPanel.add(repositoryPathTextField,
                    new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
          new Insets(0, 0, 0, 0), 0, 0));
    northPanel.add(factsSchemaTextField,
                    new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    northPanel.add(generatedSourcesTextField,
                    new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    northPanel.add(packageNameTextField,
                    new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    northPanel.add(classesDirectoryTextField,
                    new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
//  northPanel.add(jaxbJarFilesTextField,
//                  new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
//        new Insets(0, 0, 0, 0), 0, 0));
    northPanel.add(xmlInputFactsTextField,
                    new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    northPanel.add(outputFactsTextField,
                    new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    northPanel.add(useAssertTreeCheckBox,
                    new GridBagConstraints(3, 8, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 10, 0, 0), 0, 0));
    northPanel.add(generateJarFileCheckBox,
                    new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 10, 0, 0), 0, 0));
    northPanel.add(browseRulesRepositoryButton,
                    new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    northPanel.add(browseFactSchemaButton,
                    new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    northPanel.add(browseSourceButton,
                    new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    northPanel.add(browseClassesButton,
                    new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    northPanel.add(browseInputFactsButton,
                    new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    northPanel.add(browseOutFactsButton,
                    new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    northPanel.add(jLabel10, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 10), 0, 0));
    runButtonPanel.add(runTestButton, null);
    runButtonPanel.add(verboseCheckBox, null);
    runButtonPanel.add(splitInputCheckBox, null);
    runButtonPanel.add(splitOutputCheckBox, null);
    runButtonPanel.add(eraseFactCheckBox, null);
    runButtonPanel.add(forceRebuildCheckBox, null);
    forceRebuildCheckBox.setToolTipText("<html>Invalidate Rules Session if checked.<br>Will reuse the Rules Session otherwise.</html>");
    northPanel.add(runButtonPanel,
                   new GridBagConstraints(0, 10, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                          new Insets(0, 0, 0, 0), 0, 0));
    northPanel.add(ruleSetLabel,
                   new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                          new Insets(0, 0, 0, 0), 0, 0));
    northPanel.add(ruleSetButton,
                   new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                          new Insets(0, 0, 0, 0), 0, 0));
    //  inputScrollPane.getViewport().add(inputTextArea, null);
    //  jTabbedPane.addTab("Input Facts", inputScrollPane);
    jTabbedPane.addTab("Input Facts", inputTabbedPane);
    inputTabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
    inputTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

    rlCodeScrollPane.getViewport().add(rlTextArea, null);
    jTabbedPane.addTab("RL", rlCodeScrollPane);

    rulesOutputScrollPane.getViewport().add(rulesOutputTextArea, null);
    jTabbedPane.addTab("Rules Output", rulesOutputScrollPane);

    //  outputScrollPane.getViewport().add(outputTextArea, null);
    jTabbedPane.addTab("Output Facts", outputTabbedPane);
    outputTabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
    outputTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

    timeOutputScrollPane.getViewport().add(elapsedTextArea, null);
    jTabbedPane.addTab("Time", timeOutputScrollPane);

    tabbedPanel.add(jTabbedPane, BorderLayout.CENTER);
    centerPanel.add(tabbedPanel, BorderLayout.CENTER);
    //                        new GridBagConstraints(0, 11, 4, 1, 0.0, 0.0,
    //                                               GridBagConstraints.CENTER,
    //                                               GridBagConstraints.BOTH,
    //                                               new Insets(0, 0, 0, 0), 0, 0));
    panelCenter.add(northPanel, BorderLayout.NORTH);
    panelCenter.add(centerPanel, BorderLayout.CENTER);
    this.getContentPane().add(panelCenter, BorderLayout.CENTER);
  }

  void fileExit_ActionPerformed(ActionEvent e)
  {
    System.exit(0);
  }

  void helpAbout_ActionPerformed(ActionEvent e)
  {
    JOptionPane.showMessageDialog(this, new RTF_About(), "About", JOptionPane.PLAIN_MESSAGE);
  }

  private void browseRulesRepositoryButton_actionPerformed(ActionEvent e)
  {
    String fName = GnlUtilities.chooseFile(this, JFileChooser.FILES_ONLY, new String[] { "rules" }, "Rules Repositories", ".", "Select", "Rules Repositories");
    if (fName != null && fName.trim().length() > 0)
      repositoryPathTextField.setText(fName); 
  }

  private void browseFactSchemaButton_actionPerformed(ActionEvent e)
  {
    String fName = GnlUtilities.chooseFile(this, JFileChooser.FILES_ONLY, new String[] { "xsd" }, "Fact Schema", ".", "Select", "Fact Schemas");
    if (fName != null && fName.trim().length() > 0)
      factsSchemaTextField.setText(fName); 
  }

  private void browseSourceButton_actionPerformed(ActionEvent e)
  {
    String fName = GnlUtilities.chooseFile(this, JFileChooser.DIRECTORIES_ONLY, new String[] { "" }, "Source Directory", ".", "Select", "JAX-B Generated sources directory");
    if (fName != null && fName.trim().length() > 0)
      generatedSourcesTextField.setText(fName); 
  }

  private void browseClassesButton_actionPerformed(ActionEvent e)
  {
    String fName = GnlUtilities.chooseFile(this, JFileChooser.DIRECTORIES_ONLY, new String[] { "" }, "Classes Directory", ".", "Select", "JAX-B Generated classes directory");
    if (fName != null && fName.trim().length() > 0)
      classesDirectoryTextField.setText(fName); 
  }

  private void browseInputFactsButton_actionPerformed(ActionEvent e)
  {
    String fName = GnlUtilities.chooseFile(this, JFileChooser.FILES_ONLY, new String[] { "xml" }, "Input Facts", ".", "Select", "Input Facts Instances");
    if (fName != null && fName.trim().length() > 0)
      xmlInputFactsTextField.setText(fName); 
  }

  private void browseOutFactsButton_actionPerformed(ActionEvent e)
  {
    String fName = GnlUtilities.chooseFile(this, JFileChooser.DIRECTORIES_ONLY, new String[] { "" }, "Output Facts Directory", ".", "Select", "Output Facts directory");
    if (fName != null && fName.trim().length() > 0)
      outputFactsTextField.setText(fName); 
  }

  private void runTestButton_actionPerformed(ActionEvent e)
  {
    // TODO Validate fields...
    String[] prms = new String[20];
    int p = 0;
    prms[p++] = "-verbose";
    prms[p++] = Boolean.toString(verboseCheckBox.isSelected());
    prms[p++] = "-repository-path";
    prms[p++] = repositoryPathTextField.getText();
    prms[p++] = "-ruleset-name";
    prms[p++] = ruleSetLabel.getText();
    prms[p++] = "-schema-location";
    prms[p++] = factsSchemaTextField.getText();
    prms[p++] = "-xml-instance-name";
    prms[p++] = xmlInputFactsTextField.getText();
    prms[p++] = "-src-dest-directory";
    prms[p++] = generatedSourcesTextField.getText();
    prms[p++] = "-dest-package";
    prms[p++] = packageNameTextField.getText();
    prms[p++] = "-class-dest-directory";
    prms[p++] = classesDirectoryTextField.getText();
    prms[p++] = ""; // "-jaxb-jars";
    prms[p++] = ""; // jaxbJarFilesTextField.getText();
    prms[p++] = "-facts-output";
    prms[p++] = outputFactsTextField.getText();
    System.setProperty("use.assert.tree", Boolean.toString(useAssertTreeCheckBox.isSelected()));
    
    // TODO Generate Jar file option?
    String cmd = "java -Duse.assert.tree=" + Boolean.toString(useAssertTreeCheckBox.isSelected()) + 
                 " -classpath " + System.getProperty("java.class.path") +
                 " xmlfacts.AssertXMLFact";
    for (int j=0; j<prms.length; j++)
      cmd += (" " + prms[j]);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    StringSelection stringSelection = new StringSelection(cmd);
    clipboard.setContents(stringSelection, null);      
    if (verboseCheckBox.isSelected())
      System.out.println("Command:" + cmd);
    System.out.println("-------------------------------");
    System.out.println("The command is in the clipboard");
    System.out.println("-------------------------------");
    
    if (eraseFactCheckBox.isSelected())
    {
      cleanFacts_ActionPerformed(false);
      AssertXMLFact.cleanKnowledgeBase();
    }
    // Clean output tabs
//  rlTextArea.setText("");
    rulesOutputTextArea.setText("");
    
    try 
    { 
      BufferedReader br = new BufferedReader(new FileReader(xmlInputFactsTextField.getText()));
      String content = "";
      String line = "";
      while ((line = br.readLine()) != null)
        content += (line + "\n");
      br.close();
      inputTabbedPane.removeAll();
      if (splitInputCheckBox.isSelected())
      {
        parser.parse(new StringReader(content));
        XMLDocument inputDoc = parser.getDocument();
        NodeList facts = inputDoc.selectNodes("/fact:unit-testing-facts/fact:fact", new NSResolver()
                                              {
                                                public String resolveNamespacePrefix(String prefix)
                                                {
                                                  return "urn:rules-unit";
                                                }
                                              });
        for (int i=0; i<facts.getLength(); i++)
        {
          JPanel panel = new JPanel();
          panel.setLayout(new BorderLayout());
          JScrollPane scrollPane = new JScrollPane();
          JTextArea textArea = new JTextArea();
          textArea.setEditable(false);
          panel.add(scrollPane, BorderLayout.CENTER);
          scrollPane.getViewport().add(textArea, null);
          XMLElement fact = (XMLElement)facts.item(i);
          String factId = fact.getAttribute("id");
          String tabLabel = "Id [" + factId + "]";
          StringWriter sw = new StringWriter();
          NodeList data = fact.getChildNodes();
          {
            for (int j=0; j<data.getLength(); j++)
            {
              Node n = data.item(j);
              if (n.getNodeType() == Node.ELEMENT_NODE)
              {
                XMLElement elem = (XMLElement)n;
                tabLabel = "[" + elem.getNodeName() + "] " + tabLabel;
                (elem).print(sw);
//              System.out.println("Fact:\n");
//              System.out.println(sw.getBuffer().toString());
                textArea.setText(sw.getBuffer().toString());
                textArea.repaint();
                break;
              }
            }
          }
          inputTabbedPane.add(tabLabel, panel);
        }
      }
      else
      {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane();
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        panel.add(scrollPane, BorderLayout.CENTER);
        scrollPane.getViewport().add(textArea, null);
        textArea.setText(content);
        inputTabbedPane.add("All Input facts", panel);
      }
    } 
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    StringWriter sw = new StringWriter();
    boolean isCodeGenerated = true;
    try 
    { 
      AssertXMLFact.setElapsedTimeString("");
      if (forceRebuildCheckBox.isSelected())
        AssertXMLFact.setInvalidateRulesSession(true);
      
      String code = AssertXMLFact.run(prms, sw); 
      if (code.trim().length() > 0)
      {
        rlTextArea.setText(code); 
      }
      isCodeGenerated = (rlTextArea.getText().trim().length() > 0);      
      String elapsed = AssertXMLFact.getElapsedTimeString();
      elapsedTextArea.setText(elapsed);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    
    try
    {
      outputTabbedPane.removeAll(); // Redundant, but OK for now.
      JPanel panel = new JPanel();
      panel.setLayout(new BorderLayout());
      JScrollPane scrollPane = new JScrollPane();
      JTextArea textArea = new JTextArea();
      textArea.setEditable(false);
      panel.add(scrollPane, BorderLayout.CENTER);
      scrollPane.getViewport().add(textArea, null);
      textArea.setText("");
      outputTabbedPane.add("All Output facts", panel);
      outputTabbedPane.repaint();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    
    String rulesOutput = sw.getBuffer().toString();
    rulesOutputTextArea.setText(rulesOutput);
    rulesOutputTextArea.repaint();

    if (isCodeGenerated)
    {
      try 
      { 
        
        BufferedReader br = new BufferedReader(new FileReader(outputFactsTextField.getText() + File.separator + "allfacts.xml"));
        String content = "";
        String line = "";
        while ((line = br.readLine()) != null)
          content += (line + "\n");
        br.close();
  
        outputTabbedPane.removeAll();
        if (splitOutputCheckBox.isSelected())
        {
          parser.parse(new StringReader(content));
          XMLDocument inputDoc = parser.getDocument();
          NodeList facts = inputDoc.selectNodes("/fact:all-facts/*", new NSResolver()
                                                {
                                                  public String resolveNamespacePrefix(String prefix)
                                                  {
                                                    return "urn:rules-unit";
                                                  }
                                                });
          for (int i=0; i<facts.getLength(); i++)
          {
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            JScrollPane scrollPane = new JScrollPane();
            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            panel.add(scrollPane, BorderLayout.CENTER);
            scrollPane.getViewport().add(textArea, null);
            XMLElement fact = (XMLElement)facts.item(i);
            String tabLabel = "Id [" + Integer.toString(i + 1) + "]";
            StringWriter sw2 = new StringWriter();
            tabLabel = "[" + fact.getNodeName() + "] " + tabLabel;
            (fact).print(sw2);
  //        System.out.println(sw2.getBuffer().toString());
            textArea.setText(sw2.getBuffer().toString());
            textArea.repaint();
            outputTabbedPane.add(tabLabel, panel);
          }
        }
        else
        {
          JPanel panel = new JPanel();
          panel.setLayout(new BorderLayout());
          JScrollPane scrollPane = new JScrollPane();
          JTextArea textArea = new JTextArea();
          textArea.setEditable(false);
          panel.add(scrollPane, BorderLayout.CENTER);
          scrollPane.getViewport().add(textArea, null);
          textArea.setText(content);
          outputTabbedPane.add("All Output facts", panel);
        }
      } 
      catch (FileNotFoundException fnfe)
      {
        System.err.println(fnfe.toString());
      }
      catch (Exception ex)
      {
        System.err.println(ex.toString());
  //    ex.printStackTrace();
      }
    }
  }

  private void buttonOpen_actionPerformed(ActionEvent e)
  {
    String fName = GnlUtilities.chooseFile(this, JFileChooser.FILES_ONLY, new String[] { "test" }, "Test Description", ".", "Select", "Open a Test Description File");
    // TODO Validate the file, create a schema for that...
    if (fName.trim().length() == 0)
      return;
    
    AssertXMLFact.setInvalidateRulesSession(true);
    rlTextArea.setText("");
    menuClean.setEnabled(true);
    testFileName = fName;
    if (fName.trim().length() > 65)
      fName = fName.substring(0, 5) + "..." + fName.substring(fName.trim().length() - 60);
    testDataFileLabel.setText(fName.replace('/', File.separatorChar));
    try
    {
      parser.parse(new File(testFileName).toURI().toURL());
      XMLDocument doc = parser.getDocument();
      NSResolver nsr = new NSResolver()
        {
          public String resolveNamespacePrefix(String string)
          {
            return TESTER_URI;
          }
        };
      String str = doc.selectNodes("//rtf:repository-path", nsr).item(0).getFirstChild().getNodeValue();
      repositoryPathTextField.setText(str.replace('/', File.separatorChar));
      str = doc.selectNodes("//rtf:facts-schema", nsr).item(0).getFirstChild().getNodeValue();
      factsSchemaTextField.setText(str.replace('/', File.separatorChar));
      str = doc.selectNodes("//rtf:java-source-dir", nsr).item(0).getFirstChild().getNodeValue();
      generatedSourcesTextField.setText(str.replace('/', File.separatorChar));

      NodeList nl = doc.selectNodes("//rtf:rulesets/rtf:ruleset-name", nsr);
      String strs = "";
      for (int i=0; i<nl.getLength(); i++)
        strs += ((strs.length()==0?"":",") + nl.item(i).getFirstChild().getNodeValue());
      ruleSetLabel.setText(strs);
      
      str = doc.selectNodes("//rtf:java-source-dir", nsr).item(0).getFirstChild().getNodeValue();
      generatedSourcesTextField.setText(str.replace('/', File.separatorChar));
      packageNameTextField.setText("");
      try
      {
        str = doc.selectNodes("//rtf:package-name", nsr).item(0).getFirstChild().getNodeValue();
        packageNameTextField.setText(str);
      }
      catch (Exception ignore) {}
      str = doc.selectNodes("//rtf:java-classes-dir", nsr).item(0).getFirstChild().getNodeValue();
      classesDirectoryTextField.setText(str.replace('/', File.separatorChar));
//    str = doc.selectNodes("//rtf:jaxb-jars", nsr).item(0).getFirstChild().getNodeValue();
//    jaxbJarFilesTextField.setText(str);
      str = doc.selectNodes("//rtf:input-facts", nsr).item(0).getFirstChild().getNodeValue();
      xmlInputFactsTextField.setText(str.replace('/', File.separatorChar));
      str = doc.selectNodes("//rtf:output-facts", nsr).item(0).getFirstChild().getNodeValue();
      outputFactsTextField.setText(str.replace('/', File.separatorChar));
      boolean b = ((XMLElement)doc.selectNodes("//rtf:use-assert-tree", nsr).item(0)).getAttribute("flag").equals("true");
      useAssertTreeCheckBox.setSelected(b);
      b = ((XMLElement)doc.selectNodes("//rtf:generate-jar-file", nsr).item(0)).getAttribute("flag").equals("true");
      generateJarFileCheckBox.setSelected(b);
      b = ((XMLElement)doc.selectNodes("//rtf:verbose", nsr).item(0)).getAttribute("flag").equals("true");
      verboseCheckBox.setSelected(b);
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(this, ex.toString(), "Reading Test Config", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void buttonSave_actionPerformed(ActionEvent e)
  {
    String fName = GnlUtilities.chooseFile(this, JFileChooser.FILES_ONLY, new String[] { "test" }, "Test Description", ".", "Save", "Save your Test Description File");
    testFileName = fName;
    if (fName.trim().length() > 35)
      fName = fName.substring(0, 5) + "..." + fName.substring(fName.trim().length() - 30);
    testDataFileLabel.setText(fName);
    
    XMLDocument doc = new XMLDocument();
    XMLElement root = (XMLElement)doc.createElementNS(TESTER_URI, "rtf:test-description");
    doc.appendChild(root);
    
    XMLElement repos = (XMLElement)doc.createElementNS(TESTER_URI, "rtf:repository-path");
    root.appendChild(repos);
    Text txt = doc.createTextNode("#txt");
    txt.setNodeValue(repositoryPathTextField.getText().replace(File.separatorChar, '/'));
    repos.appendChild(txt);
    
    XMLElement schema = (XMLElement)doc.createElementNS(TESTER_URI, "rtf:facts-schema");
    root.appendChild(schema);
    txt = doc.createTextNode("#txt");
    txt.setNodeValue(factsSchemaTextField.getText().replace(File.separatorChar, '/'));
    schema.appendChild(txt);
    
    XMLElement java = (XMLElement)doc.createElementNS(TESTER_URI, "rtf:java-source-dir");
    root.appendChild(java);
    txt = doc.createTextNode("#txt");
    txt.setNodeValue(generatedSourcesTextField.getText().replace(File.separatorChar, '/'));
    java.appendChild(txt);
       
    XMLElement rsList = (XMLElement)doc.createElementNS(TESTER_URI, "rtf:rulesets");
    root.appendChild(rsList);
    
    String selectedRS = ruleSetLabel.getText();
    StringTokenizer strTok = new StringTokenizer(selectedRS, ",");
    try
    {
      while (true)
      {
        String rset = strTok.nextToken();
        XMLElement rs = (XMLElement)doc.createElementNS(TESTER_URI, "rtf:ruleset-name");
        rsList.appendChild(rs);
        txt = doc.createTextNode("#txt");
        txt.setNodeValue(rset);
        rs.appendChild(txt);
      }
    }
    catch (Exception ignore) { }        
    
    XMLElement pn = (XMLElement)doc.createElementNS(TESTER_URI, "rtf:package-name");
    root.appendChild(pn);
    txt = doc.createTextNode("#txt");
    txt.setNodeValue(packageNameTextField.getText());
    pn.appendChild(txt);
    
    XMLElement cls = (XMLElement)doc.createElementNS(TESTER_URI, "rtf:java-classes-dir");
    root.appendChild(cls);
    txt = doc.createTextNode("#txt");
    txt.setNodeValue(classesDirectoryTextField.getText().replace(File.separatorChar, '/'));
    cls.appendChild(txt);
    
//  XMLElement jaxbjars = (XMLElement)doc.createElementNS(TESTER_URI, "rtf:jaxb-jars");
//  root.appendChild(jaxbjars);
//  txt = doc.createTextNode("#txt");
//  txt.setNodeValue(jaxbJarFilesTextField.getText());
//  jaxbjars.appendChild(txt);
    
    XMLElement input = (XMLElement)doc.createElementNS(TESTER_URI, "rtf:input-facts");
    root.appendChild(input);
    txt = doc.createTextNode("#txt");
    txt.setNodeValue(xmlInputFactsTextField.getText().replace(File.separatorChar, '/'));
    input.appendChild(txt);
    
    XMLElement output = (XMLElement)doc.createElementNS(TESTER_URI, "rtf:output-facts");
    root.appendChild(output);
    txt = doc.createTextNode("#txt");
    txt.setNodeValue(outputFactsTextField.getText().replace(File.separatorChar, '/'));
    output.appendChild(txt);
    
    XMLElement useAssertTree = (XMLElement)doc.createElementNS(TESTER_URI, "rtf:use-assert-tree");
    root.appendChild(useAssertTree);
    useAssertTree.setAttribute("flag", (useAssertTreeCheckBox.isSelected()?"true":"false"));
    
    XMLElement genJar = (XMLElement)doc.createElementNS(TESTER_URI, "rtf:generate-jar-file");
    root.appendChild(genJar);
    genJar.setAttribute("flag", (generateJarFileCheckBox.isSelected()?"true":"false"));
    
    XMLElement verbose = (XMLElement)doc.createElementNS(TESTER_URI, "rtf:verbose");
    root.appendChild(verbose);
    verbose.setAttribute("flag", (verboseCheckBox.isSelected()?"true":"false"));
    
    // Generate the file
    try { doc.print(new FileOutputStream(testFileName)); }
    catch (IOException ioe)
    {
      JOptionPane.showMessageDialog(this, ioe.toString(), "Saving Test Config", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void buttonHelp_actionPerformed(ActionEvent e)
  {
    JOptionPane.showMessageDialog(this, new RTF_About(), "About", JOptionPane.PLAIN_MESSAGE);
  }

  private void ruleSetButton_actionPerformed(ActionEvent e)
  {
    String repos = repositoryPathTextField.getText();
    if (repos.trim().length() == 0)
    {
      JOptionPane.showMessageDialog(this, "Enter the repository location first", "Rules Sets", JOptionPane.WARNING_MESSAGE);
    }
    else
    {
      try
      {
        parser.parse(new File(repos).toURI().toURL());
        NSResolver nsr = new NSResolver()
          {
            public String resolveNamespacePrefix(String string)
            {
              return DICTIONNARY_URI;
            }
          };
        XMLDocument rules = parser.getDocument();
        NodeList nl = rules.selectNodes("/rules:RuleDictionary//rules:RuleSet/rules:Name", nsr);
        ArrayList<String> rsList = new ArrayList<String>(nl.getLength());
        for (int i=0; i<nl.getLength(); i++)
        {
          String rsName = nl.item(i).getFirstChild().getNodeValue();
//        System.out.println("RuleSet:" + rsName);
          rsList.add(rsName);
        }
        // Get selected ones
        String selectedRS = ruleSetLabel.getText();
        ArrayList<String> selected = new ArrayList<String>();
        StringTokenizer strTok = new StringTokenizer(selectedRS, ",");
        try
        {
          while (true)
          {
            String rs = strTok.nextToken();
            selected.add(rs);
          }
        }
        catch (Exception ignore) { }        
        
        RSPanel rsPanel = new RSPanel(rsList, selected);
        int resp = JOptionPane.showConfirmDialog(this, rsPanel, "Select your Ruleset(s)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (resp == JOptionPane.OK_OPTION)
        {  
          selectedRS = "";
          for (String rs : rsPanel.getSelectedRS())
          {
            selectedRS += ((selectedRS.trim().length() > 0?",":"") + rs);
          }
          ruleSetLabel.setText(selectedRS);
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }
  
  private void cleanFacts_ActionPerformed(ActionEvent ae) 
  {
    cleanFacts_ActionPerformed(true);
  }
  private void cleanFacts_ActionPerformed(boolean ask)
  {
    int resp = 0;
    if (ask)
    {
      String mess = "You are about to clean the Output Facts directory.\nDo you really want to proceed?";
      resp = JOptionPane.showConfirmDialog(this, mess, "Cleaning the Facts", JOptionPane.OK_CANCEL_OPTION);
    }
    if (!ask || resp == JOptionPane.OK_OPTION)
    {
      String factDir = outputFactsTextField.getText();
      try
      {
        File dir = new File(factDir);
        if (dir.isDirectory() && dir.exists())
        {
          deleteDir(dir);
          System.out.println(factDir + " has been deleted");
        }
        else
        {
          System.out.println(factDir + " does not exist or is not a directory");
        }
      }
      catch (Exception e)
      {
        JOptionPane.showMessageDialog(this, e.toString(), "Deleting Facts directory", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
      }
    }
  }
  
  private void cleanJAXB_ActionPerformed(ActionEvent ae)
  {
    String mess = "You are about to clean the JAX-B directories.\nDo you really want to proceed?";
    int resp = JOptionPane.showConfirmDialog(this, mess, "Cleaning the Fact Sources and Classes", JOptionPane.OK_CANCEL_OPTION);
    if (resp == JOptionPane.OK_OPTION)
    {
      String dir = generatedSourcesTextField.getText();
      try
      {
        File d = new File(dir);
        if (d.exists() && d.isDirectory())
        {
          deleteDir(d);
          System.out.println(dir + " has been deleted");
        }
        else
          System.out.println(dir + " does not exist or is not a directory");
      }
      catch (Exception e)
      {
        JOptionPane.showMessageDialog(this, e.toString(), "Deleting Sources directory", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
      }
      dir = classesDirectoryTextField.getText();
      try
      {
        File d = new File(dir);
        if (d.exists() && d.isDirectory())
        {
          deleteDir(d);
          System.out.println(dir + " has been deleted");
        }
        else
          System.out.println(dir + " does not exist or is not a directory");
      }
      catch (Exception e)
      {
        JOptionPane.showMessageDialog(this, e.toString(), "Deleting Classes directory", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
      }
    }    
  }

  public static boolean deleteDir(File dir) 
  {
    if (dir.isDirectory()) 
    {
      String[] children = dir.list();
      for (int i=0; i<children.length; i++) 
      {
        boolean success = deleteDir(new File(dir, children[i]));
        if (!success) 
        {
          return false;
        }
      }
    }      
    // The directory is now empty so delete it
    return dir.delete();
  }  
}
