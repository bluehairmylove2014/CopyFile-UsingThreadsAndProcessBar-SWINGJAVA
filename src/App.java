
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;   

public class App implements ActionListener {
    JFrame jfrm = new JFrame("Users_main");
    private String srcfilepath, destfilepath;
    JProgressBar pcbar = new JProgressBar();
    long oldsumchar = 0; 
    long sumchar = 0; 
    long filesize = 1; 
    volatile boolean copyend = false;

    App() {

        // Chỉ định một BorderLayout
        jfrm.setLayout(new BorderLayout());
        jfrm.setResizable(false);

        // Tùy chỉnh kích thước     
        jfrm.setSize(412, 500);
        
        jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Header
        JPanel hder = new JPanel();
        hder.setLayout(new FlowLayout());
        JLabel title = new JLabel("COPY FILE APPLICATION");
        title.setBorder(new EmptyBorder(20, 0, 30, 0));
        hder.add(title);

        // Body Jpanel
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel dataPanel = new JPanel();
        dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.X_AXIS));
        dataPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dataPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

            // Label
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBorder(new EmptyBorder(0, 0, 0, 30));

        JLabel srclbl = new JLabel("Source File");
        srclbl.setBorder(new EmptyBorder(0, 5, 10, 0));
        JLabel deslbl = new JLabel("Destination File");
        deslbl.setBorder(new EmptyBorder(0, 5, 0, 0));

        titlePanel.add(srclbl);
        titlePanel.add(deslbl);

            // Input field
        JPanel textfieldPanel = new JPanel();
        textfieldPanel.setLayout(new BoxLayout(textfieldPanel, BoxLayout.Y_AXIS));
        textfieldPanel.setPreferredSize(new Dimension(200, 50));

        JTextField srcFile = new JTextField();
        srcFile.setMaximumSize(new Dimension(200, 50));
        srcFile.setEditable(false);
        JTextField destFile = new JTextField();
        destFile.setMaximumSize(new Dimension(200, 50));
        destFile.setEditable(false);

        textfieldPanel.add(srcFile);
        textfieldPanel.add(destFile);

            // File chooser btn

        JPanel chooserPanel = new JPanel();
        chooserPanel.setLayout(new BoxLayout(chooserPanel, BoxLayout.Y_AXIS));
        chooserPanel.setBorder(new EmptyBorder(0, 0, 2, 5));

        JButton chooseSrcFile = new JButton("Select");
        JButton chooseDestFile = new JButton("Select");

        chooserPanel.add(chooseSrcFile);
        chooserPanel.add(chooseDestFile);

        chooseSrcFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser srcfileChooser = new JFileChooser();
                int returnVal = srcfileChooser.showOpenDialog(jfrm);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    srcfilepath = srcfileChooser.getSelectedFile().getAbsolutePath();  
                    srcFile.setText(srcfilepath);
                } 
                else System.out.println("Error");
            }
        });
        chooseDestFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser destfileChooser = new JFileChooser();
                int returnVal = destfileChooser.showOpenDialog(jfrm);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    destfilepath = destfileChooser.getSelectedFile().getAbsolutePath();   
                    destFile.setText(destfilepath); 
                } 
                else System.out.println("Error");
            }
        });

        JButton copyProcessBtn = new JButton("Start");
        copyProcessBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        copyProcessBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CopyThread cpythrd = new CopyThread("Copy Thread" ,srcfilepath, destfilepath);
                ProcessGUIThread pcssGUIthrd = new ProcessGUIThread("Process bar");
                Thread thrd1 = new Thread(cpythrd);
                Thread thrd2 = new Thread(pcssGUIthrd);
                thrd1.start(); 
                thrd2.start(); 
            }
        });

        // Process bar
        pcbar.setStringPainted(true);
        pcbar.setMinimum(0);
        pcbar.setMaximum(100);
        
        dataPanel.add(titlePanel);
        dataPanel.add(textfieldPanel);
        dataPanel.add(chooserPanel);
        body.add(dataPanel);
        body.add(pcbar);
        body.add(copyProcessBtn);

        // Footer
        JPanel fter = new JPanel();
        fter.setLayout(new BoxLayout(fter, BoxLayout.Y_AXIS));
        

        // ------------ Create main panels ------------

        // ------------ Add main panels to content page ------------ 
        jfrm.getContentPane().add(hder, BorderLayout.NORTH);
        jfrm.getContentPane().add(body, BorderLayout.CENTER);
        jfrm.getContentPane().add(fter, BorderLayout.SOUTH);

        jfrm.pack();
        jfrm.setVisible(true);
    }
        
    public void actionPerformed(ActionEvent ae) {
        // Lấy action command.
        String comStr = ae.getActionCommand();
        // Thiết lập ngắt khi bấm quit
        if (comStr.equals("Exit")) 
            System.exit(0);
        // Nếu không phải quit
    }

    public class CopyThread implements Runnable {
        String thrdName, src, dest; 
        CopyThread(String name, String srcfile, String destfile) {
            thrdName = name;
            src = srcfile;
            dest = destfile;
        }
        // Entry point of thread.
        public void run() {
            System.out.println(thrdName + " starting.");
            try {
                InputStream is = null;
                OutputStream os = null;
                try {
                    File sourcefile =new File(src);
                    is = new FileInputStream(src);
                    os = new FileOutputStream(dest);
                    byte[] buffer = new byte[1024];
                    filesize = sourcefile.length();
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                        sumchar += length;
                    }
                    copyend = true;
                } finally {
                    is.close();
                    os.close();
                }
            } catch (FileNotFoundException exc) {
                System.out.println(thrdName + " file not found.");
            } catch (IOException exc) {
                System.out.println(thrdName + " IOException.");
            }
            System.out.println(thrdName + " end.");
        }
    }  

    public class ProcessGUIThread implements Runnable {
        String thrdName;
        ProcessGUIThread(String name) {
            thrdName = name;
        }
        // Entry point of thread.
        public void run() {
            System.out.println(thrdName + " starting.");
            try {
                while(copyend == false) {
                    int oldvar = (int)((oldsumchar * 100) / filesize);
                    int newvar = (int)((sumchar * 100) / filesize);
                    if(oldvar != newvar) {
                        pcbar.setValue(newvar);
                        pcbar.setString(Integer.toString(newvar) + "%");
                        oldsumchar = sumchar;
                    }
                }
                pcbar.setValue(100);
                pcbar.setString("Copied Successfully");
                oldsumchar = 0; 
                sumchar = 0; 
                filesize = 1; 
                copyend = false;
            } catch (Exception exc) {
                System.out.println(thrdName + " interrupted.");
            }
            System.out.println(thrdName + " end.");
        }
    } 

    public static void main(String args[]) throws Exception {
        // Create the frame on the event dispatching thread.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() { new App(); }
        });
    }

    
}
