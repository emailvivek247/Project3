package com.fdt.ecomadmin.applet;

import java.awt.BorderLayout;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JApplet;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintHyperlink;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JRHyperlinkListener;
import net.sf.jasperreports.view.JRViewer;

public class ReportApplet extends JApplet implements JRHyperlinkListener{

    private static final long serialVersionUID = -7082051614983898806L;

    private JasperPrint jasperPrint;

    private JPanel pnlMain;

    public ReportApplet() {
        initComponents();
    }

    public void init() {
        try {
            String reportURL = this.getParameter("REPORT_URL");
            String JSESSIONID = this.getParameter("JSESSIONID");
            String SITE_ID = this.getParameter("SITE_ID");
            String FROM_DATE = this.getParameter("FROM_DATE");
            String TO_DATE = this.getParameter("TO_DATE");
            String TX_TYPE = this.getParameter("TX_TYPE");
            String LOCATION_ID = this.getParameter("LOCATION_ID");
            String IS_CERTIFIED = this.getParameter("IS_CERTIFIED");
            String APPLICATION = this.getParameter("APPLICATION");

            System.out.println("THE HTTP REPORT URL ===========>" + reportURL);
            System.out.println("THE JSESSIONID ===========>" + JSESSIONID);
            System.out.println("THE SITE ID ===========>" + SITE_ID);
            System.out.println("THE FROM_DATE ID ===========>" + FROM_DATE);
            System.out.println("THE TO_DATE ID ===========>" + TO_DATE);
            System.out.println("THE TX_TYPE ID ===========>" + TX_TYPE);
            System.out.println("THE LOCATION_ID ===========>" + LOCATION_ID);
            System.out.println("THE IS_CERTIFIED ===========>" + IS_CERTIFIED);

            Map <String, String> parameters =  new HashMap<String, String>();
            parameters.put("siteId", SITE_ID);
            parameters.put("fromDate", FROM_DATE);
            parameters.put("toDate", TO_DATE);
            parameters.put("txType", TX_TYPE);
            parameters.put("locationId", LOCATION_ID);
            parameters.put("isCertified", IS_CERTIFIED);
            parameters.put("application", APPLICATION);

            this.jasperPrint = (JasperPrint)JRLoader.loadObject(this.doSubmit(reportURL, parameters, JSESSIONID));
            if (this.jasperPrint == null)
                return;
            JRViewer viewer = new JRViewer(this.jasperPrint);
            viewer.addHyperlinkListener(this);
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
            if(StringUtils.isBlank(data.get(key))) {
            	continue;
            } else {
            	content += key + "=" + URLEncoder.encode(data.get(key), "UTF-8");
            }

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

    public void gotoHyperlink(JRPrintHyperlink hyperlink) throws JRException {

		System.out.println("jrPrintHyperlink.getHyperlinkAnchor(): " + hyperlink.getHyperlinkAnchor());
		System.out.println("jrPrintHyperlink.getHyperlinkReference(): " + hyperlink.getHyperlinkReference());
		System.out.println("jrPrintHyperlink.getHyperlinkTooltip(): " + hyperlink.getHyperlinkTooltip());
		System.out.println("jrPrintHyperlink.getLinkTarget(): " + hyperlink.getLinkTarget());
		System.out.println("jrPrintHyperlink.getLinkType(): " + hyperlink.getLinkType());
		System.out.println("jrPrintHyperlink.getHyperlinkParameters(): " + hyperlink.getHyperlinkParameters());
		System.out.println("jrPrintHyperlink.getHyperlinkTargetValue(): " + hyperlink.getHyperlinkTargetValue());
		System.out.println("jrPrintHyperlink.getHyperlinkTypeValue(): " + hyperlink.getHyperlinkTypeValue());

		switch(hyperlink.getHyperlinkTypeValue())
		{
			case REFERENCE :
			{
				try
				{
					getAppletContext().showDocument(new URL(hyperlink.getHyperlinkReference()), "_blank");
				}
				catch (MalformedURLException e)
				{
					JOptionPane.showMessageDialog(this, e.getMessage());
				}
				break;
			}
			case LOCAL_ANCHOR :
			case LOCAL_PAGE :
			{
				break;
			}
			case REMOTE_ANCHOR :
			case REMOTE_PAGE :
			{
				JOptionPane.showMessageDialog(this, "Implement your own JRHyperlinkListener to manage this type of event.");
				break;
			}
			case NONE :
			default :
			{
				break;
			}
		}
	}
}
