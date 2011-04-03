/*
 * YajHFC - Yet another Java Hylafax client
 * Copyright (C) 2009 Jonas Wolz
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package yajhfc.customtrans;

import static yajhfc.Utils._;
import info.clearthought.layout.TableLayout;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.TreeMap;

import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import yajhfc.FaxOptions;
import yajhfc.FileTextField;
import yajhfc.Utils;
import yajhfc.options.AbstractOptionsPanel;
import yajhfc.options.OptionsWin;
import yajhfc.util.ExampleFileFilter;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.JTableTABAction;
import yajhfc.util.MapTableModel;

/**
 * Implements a crude and simple UI to set the three example options.
 * 
 * @author jonas
 *
 */
public class CustomTransOptionsPanel extends AbstractOptionsPanel<FaxOptions> { 
    public CustomTransOptionsPanel() {
		super(false);
	}

	JTable table;
	FileTextField ftfMsgcat;
	MapTableModel tableModel = null;
	Action deleteAction;
	DefaultCellEditor langEditor;
	FTFCellEditor fileEditor;
    
    @Override
    protected void createOptionsUI() {    
    	double[][] dLay = {
    			{OptionsWin.border, TableLayout.FILL, OptionsWin.border},
    			{OptionsWin.border, TableLayout.PREFERRED, OptionsWin.border, TableLayout.FILL, OptionsWin.border, TableLayout.PREFERRED, TableLayout.PREFERRED, OptionsWin.border}
    	};
    	setLayout(new TableLayout(dLay));
    	
        deleteAction = new ExcDialogAbstractAction() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0 && tableModel.rowIsDeletable(row)) {
                	tableModel.deleteRow(row);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        };
        deleteAction.putValue(Action.NAME, "Remove row");
        
    	table = new JTable();
    	table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                deleteAction.setEnabled(e.getFirstIndex() >= 0 && tableModel.rowIsDeletable(e.getFirstIndex()));
            }
        });
        JPopupMenu tablePopup = new JPopupMenu();
        tablePopup.add(deleteAction);
        table.setComponentPopupMenu(tablePopup);
        table.setShowGrid(true);
        //table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(keyCombo));
        table.getActionMap().put(deleteAction.getClass().getName(), deleteAction);
        table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), deleteAction.getClass().getName());
        JTableTABAction.wrapDefTabAction(table);
        
        JComboBox langCombo = new JComboBox(Locale.getISOLanguages());
        langCombo.setEditable(true);
        langEditor = new DefaultCellEditor(langCombo);
        
        FileTextField ftfFile = new FileTextField();
        ftfFile.setFileFilters(
        		new ExampleFileFilter(new String[] {"po", "properties"}, "All supported files"),
        		new ExampleFileFilter("po", "GNU gettext po files"),
        		new ExampleFileFilter("properties", "Java properties"));
        fileEditor = new FTFCellEditor(ftfFile);
        
    	ftfMsgcat = new FileTextField() {
    		protected String readTextFieldFileName() {
    			return Utils.extractExecutableFromCmdLine(super.readTextFieldFileName());
    		}
    		
    		protected void writeTextFieldFileName(String fName) {
    			super.writeTextFieldFileName("\"" + fName + "\" " + CustomTransOptions.DEF_MSGCAT_PARAMS);
    		}
    	};
    	
    	JLabel labelCaption = new JLabel("<html>You can use this page to override and add new languages to YajHFC for testing purposes.<br>In order to be able to select your new language, you will need to restart the application.</html>");
    	
    	add(labelCaption, "1,1");
    	add(new JScrollPane(table), "1,3");
    	Utils.addWithLabel(this, ftfMsgcat, "Command to convert .po -> .properties:", "1,6");
    }
    
    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#loadSettings(yajhfc.FaxOptions)
     */
    public void loadSettings(FaxOptions foEdit) {
    	// You can just ignore foEdit
    	
        CustomTransOptions opts = EntryPoint.getOptions();
        tableModel = new MapTableModel(new TreeMap<String,String>(opts.languages)) {
        	@Override
        	public String getColumnName(int column) {
        		switch (column) {
        		case 0:
        			return "Language";
        		case 1:
        			return "Translation file";
        		default:
        			return null;
        		}
        	}
        };
        ftfMsgcat.setText(opts.po2properties);
        
        table.setModel(tableModel);
        TableColumn col0 = table.getColumnModel().getColumn(0);
        col0.setPreferredWidth(40);
        col0.setCellEditor(langEditor);
        
        TableColumn col1 = table.getColumnModel().getColumn(1);
        col1.setPreferredWidth(300);
        col1.setCellEditor(fileEditor);
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#saveSettings(yajhfc.FaxOptions)
     */
    public void saveSettings(FaxOptions foEdit) {
    	// You can just ignore foEdit
        
        CustomTransOptions opts = EntryPoint.getOptions();
        opts.languages.clear();
        opts.languages.putAll(tableModel.getMapToEdit());
        opts.po2properties = ftfMsgcat.getText();

    }

    private boolean commandLineOK(String commandLine) {
        return commandLine != null && commandLine.length() > 0 && 
            Utils.searchExecutableInPath(Utils.extractExecutableFromCmdLine(commandLine)) != null;
    }
    
    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#validateSettings(yajhfc.options.OptionsWin)
     */
    public boolean validateSettings(OptionsWin optionsWin) {
    	// TODO: validate language files
    	
        if (tableModel.getMapToEdit().size() > 0 && !commandLineOK(ftfMsgcat.getText())) {
            optionsWin.focusComponent(ftfMsgcat.getJTextField());
            JOptionPane.showMessageDialog(optionsWin, "Please enter the command line to msgcat.", _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // If you do not need to validate settings, always return true
        return true;
    }

}
