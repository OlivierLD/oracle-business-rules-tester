package gui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;

public class RTFGuiMain
{
  public RTFGuiMain()
  {
    JFrame frame = new RTFFrame();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height)
    {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width)
    {
      frameSize.width = screenSize.width;
    }
    frame.setLocation( ( screenSize.width - frameSize.width ) / 2, ( screenSize.height - frameSize.height ) / 2 );
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.setVisible(true);
  }

  public static void main(String[] args)
  {
    try
    {
      if (System.getProperty("swing.defaultlaf", "").trim().length() == 0)
      {
        System.out.println(".Setting Default Look & Feel");
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      else 
      {
        System.out.println(".Setting Look and Feel to " + System.getProperty("swing.defaultlaf"));    
      }  
      JFrame.setDefaultLookAndFeelDecorated(true);
//    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      System.out.println(UIManager.getLookAndFeel().getClass().getName());
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    new RTFGuiMain();
  }
}
