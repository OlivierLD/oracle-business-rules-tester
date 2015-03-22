# OBR Testing UI #
This utility allows you to "run" the Oracle Business Rules to be used from an SOA Composite **without having to deploy anything**. It is to be used during the development phase, you test the rules as you write them.

Oracle provides [an approach](http://docs.oracle.com/cd/E14571_01/integration.1111/e10228/testing.htm) for testing rules, that requires an additional function, and the rules to be deployed.

This tool does **not** require anything additional, nor anything to be deployed; that's the idea. It does not even require a server.
It is using nothing but documented rules API.

### How it works ###
The rules you use in an SOA environment rely on an XML schema. This schema is compiled using JAX-B, to turn the XML facts into Java facts.
This is why this utility is using the XML schema as the point of truth. Following a JSP-like paradigm, if the Schema is more recent than the JAXB-generated classes, then the schema is recompiled to produce new Java sources, themselves compiled and possibly archived.
To run a test on a rules repository, you will need to provide:
  * The location of the XML Schema describing the facts (with an .xsd extension)
  * The location of the rules repository containing the ruleset(s) to execute (with a .rules extension)
  * The location of the directory containing the JAXB-generated java sources (with .java extension)
  * The location of the directory containing the classes after the compilation of the above (with a .class extension)
  * The location of the file containing the XML Facts to assert before executing the rulesset(s) to test (with an .xml extension)
  * The location of the directory where to generate the output facts
  * The name of the ruleset(s) to execute, ordered if there are several of them.
All those parameters can be stored into a file (with a .test extension), so the test can be replayed later on.

### What you need ###
Obviously, you will need JAXB, to compile the XML Schema, and the Java compiler, implemented by the JDK in the archive tools.jar. You will also need a bunch of jar-files that implement the OBR APIs. The scripts named [run](http://code.google.com/p/oracle-business-rules-tester/source/browse/trunk/run) (for Linux and Mac OS) and [run.bat](http://code.google.com/p/oracle-business-rules-tester/source/browse/trunk/run.bat) (for Windows) give the list of the elements to include in the classpath to run this utility. Both rely on a system variable named JDEV\_HOME. Everything should be pretty much OK, but make sure the last line (tools.jar) is correct for your environment.

No jar file is provided in the Download section. This is to avoid gaps between the source and the jar. See below or [here](HowToBuildAndRun.md), to know how to build and run the tester on your environment.


_Warning: The classpath used to run the utility is the classpath the utility uses to run the JAX-B compiler when necessary. So keep the classpath as it is, even if all the jars are not all required to run the GUI._

#### Recommended approach: build it for yourself ####
> You need
    * An svn client, like [SlikSVN](http://www.sliksvn.com/en/download) (make sure the client executable is in your path)
    * Oracle's JDeveloper, at least version 11.1.1.5.0

_Note: **If you are behind a firewall**_
> You can setup the proxy you need for svn by modifying the file at `~/.subversion/servers`.
> You need to set it under the `[global]` section.

> Check out the code by typing:
```
 Prompt> svn checkout http://oracle-business-rules-tester.googlecode.com/svn/trunk/ OBRTester
```
> Then open JDeveloper, and navigate to the directory OBRTester created by the command   above, and open the project named `RulesTestingFramework.jpr`.
> In the project properties, section **Libraries and Classpath**, make sure you refer to  Tools.jar correctly. The other libraries should be OK.
> Then you can run the Graphical User Interface (GUI) from the menu Run > Choose Active Run Configuration > Rules GUI.

Examples of the test files are given in  this project, like [this one](http://code.google.com/p/oracle-business-rules-tester/source/browse/trunk/infection.test.definition.three.test).
The XML documents containing the input facts must refer to a specific namespace to hold the facts, see [here](http://code.google.com/p/oracle-business-rules-tester/source/browse/trunk/test-data/input-facts.xml) for an example.

For your tests, there is in this project a [rules repository](http://code.google.com/p/oracle-business-rules-tester/source/browse/trunk/InfectionDetectionRuled.rules) you can use, along with the appropriate [XML Schemas](http://code.google.com/p/oracle-business-rules-tester/source/browse/trunk/xsd/bacteriemia.xsd).

### Running ###
When you hit the run button, the following happen:
  * The last modification dates are checked on the fact schema and generated classes, and compilation happens if necessary. What has been generated (classes or jar-file) is added to the classpath (to the current classloader).
  * A rules session is created (or taken from the session pool)
  * Facts from the input fact document are asserted in the knowledge base
  * Rules sets are executed in the order defined by the test
  * Facts remaining in the knowledge base are generated in the output fact directory, and displayed in the UI

In addition, the java command used to run the test is put in the clipboard. You can re-use it from an Ant script for example, the utility also run without the GUI. It can be useful for Unit Tests...

![http://donpedro.lediouris.net/software/img/rules01.png](http://donpedro.lediouris.net/software/img/rules01.png)
<br>
Input facts tab, facts can be splitted across several tabs<br>
<br>
<img src='http://donpedro.lediouris.net/software/img/rules02.png' />
<br>
Rules language, generated at runtime<br>
<br>
<img src='http://donpedro.lediouris.net/software/img/rules03.png' />
<br>
Rules output (here with the verbose option)<br>
<br>
<img src='http://donpedro.lediouris.net/software/img/rules04.png' />
<br>
Output facts tab, facts can be splitted across several tabs<br>
<br>
<img src='http://donpedro.lediouris.net/software/img/rules05.png' />
<br>
A summary of the execution time is given in the last tab. These are elapsed times. Notice that the verbose checkbox has a significant impact on the elapsed times.<br>
<br>
This was useful to me, it might be useful to you. I obviously did not focus too much on the UI, don't tell me it is ugly: I know.<br>
<br>
<h3>A hint</h3>
This little tool is using JAX-B's marshaling feature to retrieve the facts remaining in the knowledge base after the execution of the designated rulesets.<br>
When the facts are generated after an <code>XSD ComplexType</code>, the marshaling operation throws a <code>MarshalException</code>, saying something ending with<br>
<pre><code> as an element because it is missing an @XmlRootElement annotation. <br>
</code></pre>
And as a matter of fact, this annotation is not generated in the java sources.<br>
<br>
There is a way to have this annotation generated in the Java code, by annotating the XML Schema, as follow:<br>
<pre><code>&lt;xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"<br>
            xmlns:dss="http://www.dss.org/"<br>
            targetNamespace="http://www.dss.org/"<br>
            elementFormDefault="qualified"<br>
            xmlns:jaxb="http://java.sun.com/xml/ns/jaxb"<br>
            jaxb:version="2.0"<br>
            xmlns:xjc="http://java.sun.com/xml/ns/jaxb/xjc"<br>
            jaxb:extensionBindingPrefixes="xjc"&gt;<br>
<br>
  &lt;xsd:annotation&gt;<br>
    &lt;xsd:appinfo&gt;<br>
      &lt;jaxb:globalBindings generateIsSetMethod="true"&gt;<br>
        &lt;xjc:simple /&gt;<br>
      &lt;/jaxb:globalBindings&gt;<br>
    &lt;/xsd:appinfo&gt;<br>
  &lt;/xsd:annotation&gt;<br>
<br>
  &lt;xsd:import schemaLocation="etc...<br>
<br>
</code></pre>
And as a rule of thumb, whenever you have a choice, use the facts based on the elements rather than the facts based on the types... That made my life with rules much easier.<br>
<hr />