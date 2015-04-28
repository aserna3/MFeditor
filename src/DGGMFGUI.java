// Authors: Benjamin Harris and Michael Paradis
// Java class that does layout and most of the general logic for MFeditor
// June 17, 2013


import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.text.DefaultCaret;
import javax.swing.text.html.*;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;


public class DGGMFGUI extends JPanel implements ActionListener{
	private static final long serialVersionUID = -1035287244006253731L;

	private Widget[] widgets;
	private HashMap<String, Widget> wMap;
	private Preset[] presets;
	private HashMap<String, Preset> pMap;
	private ScrollTab[] sts;
	private HashMap<String, ScrollTab> stMap;
	private SubTab[] subs;
	private HashMap<String, SubTab> subMap;
	private static DependencyList[] myDep;
	private JPanel mainArea, noteArea, topLeft, topRight, mainPane, buttonArea;
	private JLabel picLabel;

	private JEditorPane notice;
	private JScrollPane previewArea, noticeSP;
	private JSplitPane rightSplit;
	private JTabbedPane tp;
	private JTextArea preview;
	private JButton resetPane, resetAll, save;

	static JButton load;

	private JButton about;
	private JCheckBox ifShowDefaults, ifShowImpHead;
	private boolean showImpHead, showDefaults = false;
	private int numWidgets, numPresets, numTabs, numSubTabs;

	private File f = null;
	private String printable, note;
	private String bootError = "";
	private String impHeader = "";
	private boolean parsingFile = false;

	public String imageFile = "res/dgg1.png";
	public String imageFile2 = "res/dggtr.png";
	public BufferedImage dggridImage;
	public BufferedImage dggridImage2;


	public static final int scrollSpeed = 6;
	public static final int OX1 = 200;
	public static final int OX2 = 600;
	public static final int OY1 = 550;
	public static final int OY2 = 70;
	public static final int S = 10;
	private static final String presetWidgetName = "dggs_type";
	private static final String newLine = System.getProperty("line.separator");
	private static final String header = 	"# Metafile for DGGRID version 6.2" + newLine +
			"# Generated by MFeditor" + newLine;
	private JButton btnSaveMetafileAs;
	private Component rigidArea;


	public DGGMFGUI () {
		try {
			dggridImage = ImageIO.read(ResourceLoader.load(imageFile));
			dggridImage2 = ImageIO.read(ResourceLoader.load(imageFile2));
		} catch (Exception e) {
			System.out.println(e);
		} // end try-catch
		presets = Preset.makePresets();
		numPresets = presets.length;
		sts = ScrollTab.makeTabs();
		numTabs = sts.length;
		widgets = Widget.makeWidgets();
		numWidgets = widgets.length;
		subs = SubTab.makeSubTabs();
		numSubTabs = subs.length;
		makeMaps();

		myDep = Widget.makeDependencies();
		setDependencies();

		setPanels();

	} // end constructor DGGMFGUI()

	private void makeMaps () {
		pMap = new HashMap<String, Preset>();
		for (int i = 0; i < numPresets; i++) pMap.put(presets[i].getName(), presets[i]);
		wMap = new HashMap<String, Widget>();
		for (int i = 0; i < numWidgets; i++) wMap.put(widgets[i].getName(), widgets[i]);
		stMap = new HashMap<String, ScrollTab>();
		for (int i = 0; i < numTabs; i++) stMap.put(sts[i].getName(), sts[i]);
		subMap = new HashMap<String, SubTab>();
		for (int i = 0; i < numSubTabs; i++) subMap.put(subs[i].getName(), subs[i]);
	} // end makeMaps()

	private void setPanels () {

		tp = new JTabbedPane();

		addTabs();
		addSubTabs();
		addWidgets();

		preview = new JTextArea();
		preview.setEditable(false);
		previewArea = new JScrollPane(preview);
		previewArea.getVerticalScrollBar().setUnitIncrement(scrollSpeed);
		previewArea.setWheelScrollingEnabled(true);

		rightSplit = new JSplitPane(0, tp, previewArea);

		ifShowDefaults = new JCheckBox("Show Defaults");
		ifShowDefaults.setToolTipText("Check to display and output values regardless of being default");
		resetPane = new JButton("Reset Tab Defaults");
		resetPane.setToolTipText("Press to reset values on this tab to their default value");
		resetAll = new JButton("Reset All Defaults");
		resetAll.setToolTipText("Press to reset all parameter values to their default value");
		save = new JButton("Save Metafile");
		save.setToolTipText("Press for dialog to save metafile to disk");

		load = new JButton("Load Metafile");
		load.setToolTipText("Press for dialog to load metafile from disk");
		ifShowImpHead = new JCheckBox("Show Last Imported Header");
		ifShowImpHead.setSelected(true);
		showImpHead = true;
		ifShowImpHead.setToolTipText("Check to display and output the header of the last \nloaded metafile (no effect if none have been loaded)");
		about = new JButton("About");
		about.setToolTipText("Show info about this program and it's developers");

		picLabel = new JLabel(new ImageIcon(dggridImage2));

		buttonArea = new JPanel();
		buttonArea.setLayout(new BoxLayout(buttonArea, BoxLayout.PAGE_AXIS));
		buttonArea.add(Box.createRigidArea(new Dimension(1,10)));
		buttonArea.add(ifShowDefaults);
		buttonArea.add(Box.createRigidArea(new Dimension(1,10)));
		buttonArea.add(resetPane);
		buttonArea.add(Box.createRigidArea(new Dimension(1,10)));
		buttonArea.add(resetAll);
		buttonArea.add(Box.createRigidArea(new Dimension(1,10)));
		buttonArea.add(save);
		buttonArea.add(Box.createRigidArea(new Dimension(1,10)));
		
		btnSaveMetafileAs = new JButton("Save Metafile as");
		btnSaveMetafileAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				formPrintable();
				FileFuncs.saveFile(printable, null);
			}
		});
		buttonArea.add(btnSaveMetafileAs);
		
		rigidArea = Box.createRigidArea(new Dimension(1, 10));
		buttonArea.add(rigidArea);
		buttonArea.add(load);
		buttonArea.add(Box.createRigidArea(new Dimension(1,10)));
		buttonArea.add(ifShowImpHead);
		buttonArea.add(Box.createRigidArea(new Dimension(1,10)));
		buttonArea.add(about);
		buttonArea.add(Box.createRigidArea(new Dimension(1,10)));
		buttonArea.add(picLabel);
		buttonArea.add(Box.createVerticalGlue());

		ifShowDefaults.addActionListener(this);
		resetPane.addActionListener(this);
		resetAll.addActionListener(this);
		save.addActionListener(this);
		load.addActionListener(this);
		ifShowImpHead.addActionListener(this);
		about.addActionListener(this);

		topLeft = new JPanel();
		topLeft.add(buttonArea);
		topLeft.setMaximumSize(new Dimension(200, 950));

		mainPane = new JPanel();
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.PAGE_AXIS));
		mainPane.add(rightSplit);
		mainPane.add(Box.createVerticalGlue());

		topRight = new JPanel();
		topRight.setLayout(new BoxLayout(topRight, BoxLayout.LINE_AXIS));
		topRight.add(mainPane);
		topRight.add(Box.createHorizontalGlue());

		mainArea = new JPanel();
		mainArea.setLayout(new BoxLayout(mainArea, BoxLayout.LINE_AXIS));
		mainArea.setPreferredSize(new Dimension(S + OX1 + S + OX2 + S, OY1));
		mainArea.add(Box.createRigidArea(new Dimension(10, 1)));
		mainArea.add(topLeft);
		mainArea.add(Box.createRigidArea(new Dimension(10, 1)));
		mainArea.add(topRight);
		mainArea.add(Box.createRigidArea(new Dimension(10, 1)));
		mainArea.add(Box.createHorizontalGlue());

		notice = new JEditorPane();
		notice.setEditable(false);
		notice.setEditorKit(new HTMLEditorKit());

		noticeSP = new JScrollPane(notice);
		noticeSP.getVerticalScrollBar().setUnitIncrement(scrollSpeed);
		noticeSP.setWheelScrollingEnabled(true);

		noteArea = new JPanel();
		noteArea.setPreferredSize(new Dimension(S + OX1 + S, OY2));
		noteArea.setMinimumSize(new Dimension(OX1, OY2));
		noteArea.setMaximumSize(new Dimension(2000, OY2));
		noteArea.setLayout(new BoxLayout(noteArea, BoxLayout.LINE_AXIS));
		noteArea.add(Box.createRigidArea(new Dimension(15, 1)));
		noteArea.add(noticeSP);
		noteArea.add(Box.createRigidArea(new Dimension(15, 1)));

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(Box.createRigidArea(new Dimension(1, 10)));
		add(mainArea);
		add(Box.createRigidArea(new Dimension(1, 10)));
		add(noteArea);
		add(Box.createRigidArea(new Dimension(1, 10)));
		setPreferredSize(new Dimension(S + OX1 + S + OX2 + S, S + OY1 + S + OY2 + S));

		formPrintable();
		preview.setText(printable);
		note = "<html>Program Started <font color=red> " + bootError + "</font></html>";
		notice.setText(note);
		note = "";
		setButtons();
		
		//set cursor to the top of the metafile

		DefaultCaret caret = (DefaultCaret) preview.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
	} // end setPanels()

	private void addTabs () { 
		for (int i = 0; i < numTabs; i++) tp.add(sts[i].getName(), sts[i]);	
	} // end addTabs()

	private void addSubTabs () {
		for (int i = 0; i < numTabs; i++) for (int j = 0; j < numSubTabs; j++)
			if (subs[j].getTabAssign().equalsIgnoreCase(sts[i].getName())) sts[i].add(subs[j]);
	} // end addSubTabs()

	private void addWidgets () {
		for (int i = 0; i < numSubTabs; i++) for (int j = 0; j < numWidgets; j++)
			if (widgets[j].getPaneAssign().equalsIgnoreCase(subs[i].getName())) subs[i].add(widgets[j]);
	} // end addWidgets()

	private void formPrintable () {
		boolean allAreDefault = true;
		printable = "";
		String temp;
		if (showImpHead) temp = impHeader;
		else temp = header;
		temp += newLine;
		if (showDefaults) {
			for (int i = 0; i < numWidgets; i++) {
				if (widgets[i].isEnabled()) printable += widgets[i] + newLine;
			} // end for
			printable = temp + printable;
		} else {
			for (int i = 0; i < numWidgets; i++) {
				if (!widgets[i].checkIfDefault()) {
					if (widgets[i].isEnabled()) {
						printable += widgets[i] + newLine;
						allAreDefault = false;
					} // end if
				} // end if
			} // end for
			if (allAreDefault) {
				printable = "# all are default"; 
			} else {
				printable = temp + printable;
			} // end if-else
		} // end if-else
	} // end formPrintable()

	private void setButtons () {} // end setButtons

	public void actionPerformed(ActionEvent e) {
		String temp;
		String tempNote;
		boolean errorPresent = false;
		Object src = e.getSource();
		if (src == ifShowDefaults) {
			if (ifShowDefaults.isSelected()) showDefaults = true;
			else showDefaults = false;
			note = "Showing defaults: " + (showDefaults ? "enabled" : "disabled");
		} else if (src == resetPane) {
			resetCurrentPaneToDefaults();
		} else if (src == resetAll) {
			resetAllToDefaults();
		} else if (src == save) {
			formPrintable();
			FileFuncs.saveFile(printable, f);
		} else if (src == load) {
			temp = FileFuncs.readInFile(f);
			tempNote = note;
			if (temp != null) {
				resetAllToDefaults();
				note = tempNote;
				errorPresent = parseFile(temp);
				if (errorPresent) resetAllToDefaults();
				parsingFile = false; ///////////////////////////////////////////////
			} // end if
		} else if (src == ifShowImpHead) {
			if (ifShowImpHead.isSelected()) showImpHead = true;
			else showImpHead = false;
			note = "Showing imported header: " + (showImpHead ? "enabled" : "disabled");
		} else if (src == about) {
			notice.setText("");
			showAbout();
		} // end if-elseif-elseif-elseif-elseif-elseif-elseif
		////////////////////////////////////////////////////////////////////////////////////		
		if (!parsingFile) {
			formPrintable();
			//setButtons();
			notice.setText("<html>" + note + "</html>");
			note = "";
			preview.setText(printable);
		} // end if
	} // end actionPerformed

	private void resetCurrentPaneToDefaults () {
		String s = null;
		String name = sts[tp.getSelectedIndex()].getName();
		for (int i = 0; i < numWidgets; i++) {
			s = widgets[i].getPaneAssign();
			if (subMap.get(s).getTabAssign().equalsIgnoreCase(name)) widgets[i].setParaToDefault();
			checkWidgets(widgets[i].getCtrDep());
		} // end for
		if (subMap.get(wMap.get(presetWidgetName).getPaneAssign()).getTabAssign().equals(ScrollTab.G)) resetDefaults();
		note += "The parameters on the '" + name + "' tab have been set to their default values";
	} // end resetCurrentPaneToDefaults()

	private void resetAllToDefaults () {
		impHeader = "";
		resetDefaults();
		for (int i = 0; i < numWidgets; i++) {

			widgets[i].setParaToDefault();
			checkWidgets(widgets[i].getCtrDep());
		} // end for
		note += "All parameters have been set to their default values<br>";
	} // end resetAllToDefaults()

	private boolean parseFile(String st) {
		parsingFile = true; //////////////////////////////////////////////////////
		String errs = "File Loading encountered the following problems:  ";
		String nErrs = "Parameter doesn't exist:  ";
		String vErrs = "Value not accepted:  ";
		String gErrs = "Metafile isn't correctly formatted";
		boolean isGerrs = false;
		boolean isVerrs = false;
		boolean isNerrs = false;
		boolean changed;
		boolean widStart = false;
		String[] lin;
		String s = "";
		if (st.startsWith(header)) s = st.substring(header.length());
		else s = st;
		String[] sa = s.split("\n");

		impHeader = "";
		int sal = sa.length;
		Widget tmp;
		Preset prt;
		for (int i = 0; i < sal; i++) {
			int slb, sla;
			changed = true;
			sa[i] = sa[i].replaceAll("\r", " ");
			sa[i] = sa[i].replaceAll("\t", " ");
			sa[i] = sa[i].trim();
			while (changed) {
				changed = false;
				slb = sa[i].length();
				sa[i] = sa[i].replaceAll("  ", " ");
				sla = sa[i].length();
				changed = (slb - sla != 0);
			} // end while

			if (sa[i].startsWith("#") && !widStart) {
				impHeader += sa[i] + newLine;
			} else if (!sa[i].startsWith("#") && !sa[i].equals("")) {

				lin = sa[i].split(" ", 2);

				if (lin.length > 1 && lin[0] != null && lin[1] != null) {
					if (wMap.containsKey(lin[0])) {
						tmp = wMap.get(lin[0]);
						if (!tmp.setValueRemotely(lin[1])) {
							vErrs += " (" + lin[0] + " : " + lin[1] + ")";
							isVerrs = true;
							tmp = null;
						} // end if
						if(tmp!=null && (tmp.getName()).equals("dggs_type")){ //////////////////
							if(!tmp.getValue().equals("CUSTOM")){
								prt = pMap.get(tmp.getValue());
								setDefaultsToPresetDefaults(prt);
							}
						}
						if (tmp != null) checkWidgets(tmp.getCtrDep());
						widStart = true;
					} else {
						nErrs += " " + lin[0] + " ";
						isNerrs = true;
					} // end if-else
				} else if (sa[i].equals("")) {
					widStart = true;
				} else {
					isGerrs = true;  
				} // end if-else

			} // end if-elseif
		} // end for

		if (isNerrs || isVerrs || isGerrs) note += "<font color=red>" + errs + "<br>";
		if (isGerrs) note += gErrs + "<br>";
		if (isNerrs) note += nErrs + "<br>";
		if (isVerrs) note += vErrs + "<br>";
		if (isNerrs || isVerrs || isGerrs) note += "</font>";

		return isVerrs || isNerrs || isGerrs;	
	} // end parseFile()

	public void performAction (ActionEvent e, String name) {
		Preset ptmp;
		Widget wtmp = wMap.get(name);
		if (name.equalsIgnoreCase(presetWidgetName)) {
			ptmp = pMap.get(wtmp.getValue());
			if (ptmp != null) {
				setDefaultsToPresetDefaults(ptmp);
			} // end if
		} // end if

		note += ("Parameter '" + name + "' is " + wtmp.getValue());
		if (wtmp.checkIfDefault()) note += ", which is the default value<br>";
		else
			note += "<br>";		
		actionPerformed(e);
	} // end performAction()

	private void resetDefaults () {
		for (int i = 0; i < numWidgets; i++) {
			widgets[i].resetDefault();
		} // end for
	} // end resetDefaults()

	private void setDefaultsToPresetDefaults(Preset p) {
		Widget tmp;
		String[] tmpName = p.getParaName();
		String[] tmpVal = p.getParaVal();
		int nl = tmpName.length;
		int vl = tmpVal.length;
		String err = "ERROR: Preset File:";
		String vErr = "Contains value not accepted by parameter: ";
		String nErr = "Contains parameter that doesn't exist: ";
		boolean ver = false;
		boolean ner = false;

		if (nl != vl) {
			note += "<br><font color=red>" + err + "<br>Not the same amount of parameter names and values</font>";
			return;
		} else {
			for (int i = 0; i < nl; i++) {

				if (wMap.containsKey(tmpName[i])) {

					tmp = wMap.get(tmpName[i]);
					if (tmp.setDefau(tmpVal[i])) {
						tmp.setValueRemotely(tmpVal[i]);
						checkWidgets(tmp.getCtrDep());
					} else {
						vErr += " (" + tmpName[i] + " cannot be " + tmpVal[i] + ") ";
						ver = true;

					} // end if-else
				} else {
					nErr += " (" + tmpName[i] + ")";
					ner = true;
				} // end if-else
			} // end for
		} // end if-else
		if (ver || ner) note += "<font color=red>" + err + "<br>";
		if (ner) note += nErr + "<br>";
		if (ver) note += vErr + "<br>";
		if (ver || ner) note += "</font>";
	} // end setDefaultsToPresetDefaults()

	public void setNote (String n) { note += n; }

	public void setDependencies(){
		String controlList = null;
		String disableList = null;	

		Widget disTemp = null;

		DisableDependency tempDD; //disabling dependency value list


		for (int i = 0; i < myDep.length; i++){

			disableList = myDep[i].getListName();

			tempDD = myDep[i].disTop; //get the disableDependency

			disTemp = wMap.get(disableList); //grab the widget that needs a disable dependency list
			disTemp.insertDisableDependency(tempDD); //insert this newly create list into this widgets disable dependency list 

			while(tempDD != null){							
				controlList = tempDD.getCtrlWidget(); //enable the control List

				disTemp = wMap.get(controlList); //find the control widget
				disTemp.insertControlList(disableList);	
				tempDD = tempDD.next;
			} // end while	

		} // end for
		for (int i = 0; i < myDep.length; i++){
			tempDD = myDep[i].disTop;
			controlList = tempDD.getCtrlWidget();
			disTemp = wMap.get(controlList);
			checkWidgets(disTemp.getCtrDep());

		} // end for
	}//end setDependencies()

	public void checkWidgets(ControlDependency CDList){
		//Dependency List that holds the names of the widgets
		//That are controlled by this particular widget. 
		ControlDependency tempCD = CDList;
		//Dependency List that has disabling values and their widgets.
		DisableDependency tempDD = null;

		Widget disWidg = null;
		Widget ctrlWidg = null;
		Widget operation = wMap.get("dggrid_operation");

		String enableValue = null;
		String disableValue = null;
		String controlValue = null;

		//This while goes through the control widgets controlDependency list of widgets, it affects. 
		while (tempCD != null) {

			boolean flag = false; //flag for not enabling if already disabled prior
			disWidg = wMap.get(tempCD.disWidget); //the widget that is to be disabled

			tempDD = disWidg.getDisDep(); //the disabling widget's dependency list of values


			/* This method is for OR values. For example if the values you entered for the dependency were
			 * only used for enabling the widget, then this is the method you will drop down into.
			 * however, no matter what the value is, if the dggrid_operation widget's value is "OUTPUT_STATS"
			 *It will disable whatever wigdet that is using this method.	
			 */
			if (tempDD.getRev()) { //this is if you want the reverse enabeling
				String opVal = operation.getValue();
				while (tempDD != null && flag != true) {
					ctrlWidg = wMap.get(tempDD.getCtrlWidget()); //get control widget from widget map

					controlValue = ctrlWidg.getValue(); //get its current value

					enableValue = tempDD.getValue(); //get the value that enables this widget

					if (enableValue.equals(controlValue)) { //if the value matches drop in
						if (opVal.equals("GENERATE_GRID")) {
							disWidg.disableWidget(false); //enable the widget
							flag = true; //set flag to true fro leaving the loop
						} // end if
					} else {
						disWidg.disableWidget(true); //else the widget is disabled
						//wMap.put(disWidg.getName(), disWidg); /////////////////////////////////

					} // end if-else
					// wMap.put(disWidg.getName(), disWidg); ////////////////////////////////////
					tempDD = tempDD.next; //get next disable widget
				}//end of while
				flag = true;
			} //end of if

			/* This while is used to scroll through the widgets dependency list and to see if
			 *	it needs to be disabled or if it needs to stay disabled due to conflicting enabling.
			 * This would be the AND method where if there is one value that disables this widget then
			 * it is disabled without caring for conflicting values. 				
			 */	
			while (tempDD != null && flag != true) {						
				ctrlWidg = wMap.get(tempDD.getCtrlWidget()); //A temp widget for finding the corresponding control widget
				disableValue = tempDD.getValue();
				controlValue = ctrlWidg.getValue();
				if (disableValue.equals(controlValue)) { //if value matches then fall through
					disWidg.disableWidget(true);
					flag = true; //set flag to indicate that it had already been disabled
				} // end if							
				tempDD = tempDD.next;		
			} //end while
			if (flag == false) { //if flag is false, enable widget.
				disWidg.disableWidget(false);
			} // end if
			//wMap.put(disWidg.getName(), disWidg); ///////////////////////////////////////////////////
			tempCD = tempCD.next;
		} // end while
	} // end checkWidgets()

	private void showAbout () {
		ImageIcon ic = new ImageIcon(dggridImage);
		JOptionPane.showMessageDialog(null, "MFeditor  Version Beta 1.0\n" + "written by: Michael Paradis & Benjamin Harris\n" +
				"MFeditor  Version Beta 1.1\n"+
				"changes made by: Jeremy Anders & Anthony Serna\n"+
				"for making metafiles used by DGGRID v6.2\n" +
				"released: March 2015", "about the developers", JOptionPane.INFORMATION_MESSAGE, ic);
	} // end showAbout()

} // end class DGGMFGUI
