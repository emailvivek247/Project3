package com.fdt.ecomadmin.applet;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JApplet;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JRViewer;

public class CheckPrintApplet extends JApplet {

    private static final long serialVersionUID = -7082051614983898806L;

    private JasperPrint jasperPrint;

    private JPanel pnlMain;

    public CheckPrintApplet() {
        initComponents();
    }

    public void init() {
        try {
            String reportURL = this.getParameter("REPORT_URL");
            String JSESSIONID = this.getParameter("JSESSIONID");
            String SITE_ID = this.getParameter("SITE_ID");
            String PAYMENT_TYPE = this.getParameter("PAYMENT_TYPE");

            /** Register the Font for Check Printing Applet **/
            try {
                System.out.println("Registering IDAutomationMICR font ==============================>");
                InputStream fontFile = this.getClass().getResourceAsStream("IDAutomationMICR.ttf");
                Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                System.out.println("font: " +  font);
                GraphicsEnvironment localGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
                localGraphicsEnvironment.registerFont(font);
                System.out.println("END Registering IDAutomationMICR font ==============================>");
            } catch (Exception excp) {
                System.out.println("Exception Occured While Loading the Font ==============================>" + excp);
            }

            System.out.println("THE HTTP REPORT URL ===========>" + reportURL);
            System.out.println("THE JSESSIONID      ===========>" + JSESSIONID);
            System.out.println("THE SITE ID         ===========>" + SITE_ID);
            System.out.println("THE PAYMENT TYPE IS      ===========>" + PAYMENT_TYPE);

            Map <String, String> parameters =  new HashMap<String, String>();
            parameters.put("siteId", SITE_ID);
            parameters.put("paymentType", PAYMENT_TYPE);

            this.jasperPrint = (JasperPrint)JRLoader.loadObject(this.doSubmit(reportURL, parameters, JSESSIONID));
            if (this.jasperPrint == null)
                return;
            JRViewer viewer = new JRViewer(this.jasperPrint);
            this.pnlMain.add(viewer, "Center");
        } catch (IOException iOException) {
            StringWriter swriter = new StringWriter();
            PrintWriter pwriter = new PrintWriter(swriter);
            iOException.printStackTrace(pwriter);
            JOptionPane.showMessageDialog(this, "IOException" + swriter.toString());
        } catch (JRException jrException) {
            StringWriter swriter = new StringWriter();
            PrintWriter pwriter = new PrintWriter(swriter);
            jrException.printStackTrace(pwriter);
            JOptionPane.showMessageDialog(this, "JRException" + swriter.toString());
        }
    }

    private InputStream doSubmit(String url, Map<String, String> data, String jSessionId) throws IOException {
        URL siteUrl = new URL(url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) siteUrl.openConnection();
        httpURLConnection.setRequestProperty("Cookie", "JSESSIONID=" + jSessionId);
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);
        httpURLConnection.setUseCaches (false);
        DataOutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());

        Set<String> keys = data.keySet();
        Iterator<String> keyIter = keys.iterator();
        String content = "";
        for(int i=0; keyIter.hasNext(); i++) {
            Object key = keyIter.next();
            if(i!=0) {
                content += "&";
            }
            content += key + "=" + URLEncoder.encode(data.get(key), "UTF-8");
        }

        System.out.println("THE CONTENTS =====>" + content);
        out.writeBytes(content);
        out.flush();
        out.close();
        return httpURLConnection.getInputStream();
    }

    private void initComponents() {
        this.pnlMain = new JPanel();
        this.pnlMain.setLayout(new BorderLayout());
        this.getContentPane().add(this.pnlMain, "Center");
    }
}
