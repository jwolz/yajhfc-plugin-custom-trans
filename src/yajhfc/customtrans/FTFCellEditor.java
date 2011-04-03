/**
 * 
 */
package yajhfc.customtrans;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;

import yajhfc.FileTextField;

/**
 * @author jonas
 *
 */
public class FTFCellEditor extends DefaultCellEditor {
	protected final FileTextField ftf;

	public FTFCellEditor(FileTextField ftf) {
		super(ftf.getJTextField());
		this.ftf = ftf;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		super.getTableCellEditorComponent(table, value, isSelected, row, column);
		return ftf;
	}
	
	public FileTextField getFTF() {
		return ftf;
	}
}
