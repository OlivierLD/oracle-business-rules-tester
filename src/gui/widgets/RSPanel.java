package gui.widgets;

import java.awt.BorderLayout;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import java.util.ArrayList;

import java.util.Collections;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;

public class RSPanel
  extends JPanel
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JPanel rsPanel = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JCheckBox[] jCheckBoxArray = null;
  private JSpinner[]  jSpinnerArray  = null;
  private ArrayList<String> selected = null;

  public RSPanel(ArrayList<String> availableRS, ArrayList<String> selectedRS)
  {
    try
    {
      jCheckBoxArray = new JCheckBox[availableRS.size()];
      jSpinnerArray = new JSpinner[availableRS.size()];
      for (int i=0; i<availableRS.size(); i++)
      {
        jCheckBoxArray[i] = new JCheckBox(availableRS.get(i));
        jSpinnerArray[i]  = new JSpinner();
        jSpinnerArray[i].setValue(new Integer(i + 1));
      }
      selected = selectedRS;
      
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
    this.setLayout(borderLayout1);
    rsPanel.setLayout(gridBagLayout1);
    for (int i=0; i<jCheckBoxArray.length; i++)
    {
      if (selected != null && selected.contains(jCheckBoxArray[i].getText()))
        jCheckBoxArray[i].setSelected(true);
      rsPanel.add(jCheckBoxArray[i], new GridBagConstraints(0, i, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 0), 0, 0));
      rsPanel.add(jSpinnerArray[i], new GridBagConstraints(1, i, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 0), 0, 0));
    }
    jScrollPane1.getViewport().add(rsPanel, null);
    this.add(jScrollPane1, BorderLayout.CENTER);
  }
  
  public ArrayList<String> getSelectedRS()
  {
    ArrayList<OrderedRuleSet> select = new ArrayList<OrderedRuleSet>();
    for (int i=0; i<jCheckBoxArray.length; i++)
    {
      if (jCheckBoxArray[i].isSelected())
        select.add(new OrderedRuleSet(jCheckBoxArray[i].getText(), ((Integer)jSpinnerArray[i].getValue()).intValue()));
    }
    Collections.sort(select);
    
    ArrayList<String> returnedList = new ArrayList<String>(select.size());
    for (OrderedRuleSet ors : select)
    {
      String rs = ors.getName();
//    System.out.println("Sorted->" + rs);
      returnedList.add(rs);
    }

    return returnedList;
  }
  
  private class OrderedRuleSet implements Comparable
  {
    private String name;
    private int rank;
    
    public OrderedRuleSet(String n, int r)
    {
      this.name = n;
      this.rank = r;
    }
   
    public int compareTo(Object o)
    {
      int retVal = 0;
      if (o instanceof OrderedRuleSet)
      {
        OrderedRuleSet ors = (OrderedRuleSet)o;
        if (ors.getRank() < this.rank)
          retVal = 1;
        if (ors.getRank() > this.rank)
          retVal = -1;
        if (ors.getRank() == this.rank)
          retVal = 0;
      }
      return retVal;
    }

    public String getName()
    {
      return name;
    }

    public int getRank()
    {
      return rank;
    }
  }
}
