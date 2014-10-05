package com.welovecoding.nbeditorconfig.netbeans;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.text.StyledDocument;
import org.editorconfig.netbeans.model.EditorConfigProperty;
import org.editorconfig.netbeans.parser.EditorConfigParser;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Michael Koppen
 */
@LookupProvider.Registration(projectType = {
	"org-netbeans-modules-java-j2seproject",
	"org-netbeans-modules-web-project",
	"org.netbeans.modules.web.clientproject",
	"org-netbeans-modules-web-clientproject",
	"org-netbeans-modules-maven",
	"org-netbeans-modules-apisupport-project"}
)
/**
 * Listener for newly opened Projects.
 */
public class ECProjectOpenedHook implements LookupProvider {

	private boolean ecFound = false;
	private Map<FileObject, ECChangeListener> listeners = new HashMap<>();

	@Override
	public Lookup createAdditionalLookup(Lookup lookup) {
		System.out.println("CREATE ADDITIONAL LOOKUP");
		final Project p = lookup.lookup(Project.class);
		return Lookups.fixed(new ProjectOpenedHook() {
			@Override
			protected void projectOpened() {
				System.out.println("PROJECT " + p.getProjectDirectory().getName() + " OPENED");
				FileObject projFo = p.getProjectDirectory();

				attachListeners(projFo, p);

				ECProjectPreferences.setLineEnding(BaseDocument.LS_CRLF, p);
			}

			@Override
			protected void projectClosed() {
				System.out.println("PROJECT " + p.getProjectDirectory().getName() + " CLOSED");
			}
		});
	}

	/**
	 * recursively attaches recursive listeners to folders containing a .editorconfig file.
	 * <p>
	 * @param root
	 * @param p
	 */
	private void attachListeners(FileObject root, Project p) {
		if (p.getProjectDirectory().equals(root)) {
			System.out.println("ALWAYS ATTACHING LISTENER TO PROJECT ROOT");
			ECChangeListener rootListener = new ECChangeListener(p, null);
			root.addRecursiveListener(rootListener);
			listeners.put(root, rootListener);
		}
		for (FileObject file : root.getChildren()) {
			System.out.println("INSPECTING FILE " + file.getPath());
			System.out.println("NAME " + file.getName());
			System.out.println("EXT " + file.getExt());
			if (file.isFolder()) {
				attachListeners(file, p);
			} else if (file.getName().equals(".editorconfig")) {
				System.out.println("FOUND EDITORCONFIG " + file.getPath());
				ECChangeListener newListener = new ECChangeListener(p, file);
				file.getParent().addRecursiveListener(newListener);
				listeners.put(file.getParent(), newListener);
				System.out.println("ATTACHED LISTENER TO " + file.getParent().getPath());
			}
		}
	}

	private class ECChangeListener extends FileChangeAdapter {

		private Project p;
		private FileObject ecFile;

		public ECChangeListener(Project p, FileObject ecFile) {
			this.p = p;
			this.ecFile = ecFile;

			if (ecFile != null) {
				EditorConfigParser parser = new EditorConfigParser();
				Map<String, List<EditorConfigProperty>> config = parser.parseConfig(FileUtil.toFile(ecFile));

				for (Map.Entry<String, List<EditorConfigProperty>> entry : config.entrySet()) {
					String key = entry.getKey();
					List<EditorConfigProperty> value = entry.getValue();
					System.out.println("Key: " + key);
					for (EditorConfigProperty editorConfigProperty : value) {
						System.out.println(editorConfigProperty.getKey() + " : " + editorConfigProperty.getValue());
					}
				}
			}//TODO remove line

		}

		@Override
		public void fileAttributeChanged(FileAttributeEvent fe) {
			super.fileAttributeChanged(fe);
			System.out.println("ECChangeListener :: fileAttributeChanged \n" + fe.getFile().getPath());
		}

		@Override
		public void fileRenamed(FileRenameEvent fe) {
			super.fileRenamed(fe);
			System.out.println("ECChangeListener :: fileRenamed \n" + fe.getFile().getPath());
		}

		@Override
		public void fileDeleted(FileEvent fe) {
			super.fileDeleted(fe);
			System.out.println("ECChangeListener :: fileDeleted \n" + fe.getFile().getPath());
			//TODO processDeletedEditorConfig
			//TODO processDeletedFolderWhichMayContainsFoldersWithListeners -> remove them
		}

		private void processDeletedEditorConfig() {

		}

		/**
		 * method is triggered when content has changed
		 * <p>
		 * @param fe
		 */
		@Override
		public void fileChanged(FileEvent fe) {
			super.fileChanged(fe);
			System.out.println("ECChangeListener :: fileChanged \n" + fe.getFile().getPath());
		}

		/**
		 * Method is triggered when content has changed and its possible to display content in netbeans. Method is also
		 * triggered when project will be opened.
		 * <p>
		 * @param fe
		 */
		@Override
		public void fileDataCreated(FileEvent fe) {
			super.fileDataCreated(fe);
			System.out.println("ECChangeListener :: fileDataCreated \n" + fe.getFile().getPath());

			FileObject file = fe.getFile();
			DataObject dobj;
			try {
				dobj = DataObject.find(file);
				StyledDocument doc = NbDocument.getDocument(dobj);
//				if (doc != null) {
//					doc.putProperty(BaseDocument.READ_LINE_SEPARATOR_PROP, BaseDocument.LS_CRLF);
//				} else {
//					System.out.println("DOC IS NULL :/");
//				}

				final DataFolder dof = dobj.getFolder();

				if (file.getNameExt().equals(".editorconfig")) {
//					file.addFileChangeListener(new FileChangeAdapter() {
//						@Override
//						public void fileChanged(FileEvent fe) {
//							applyEditorConfigToFolder(dof);
//						}
//					});
//					NotificationDisplayer.getDefault().notify(
//						".editorconfig",
//						ImageUtilities.loadImageIcon("org/netbeans/ec/editorconfig.png", false),
//						"Affected folder: " + dof.getName(),
//						null);
//					applyEditorConfigToFolder(dof);
				} else {
					// if it isn't an editorconfig that's been added,
					// apply the editorconfig file to it:
//					applyEditorConfigToFile(dobj);
				}
			} catch (DataObjectNotFoundException ex) {
				Exceptions.printStackTrace(ex);
			}
		}

		@Override
		public void fileFolderCreated(FileEvent fe) {
			super.fileFolderCreated(fe);
			System.out.println("ECChangeListener :: fileFolderCreated \n" + fe.getFile().getPath());
			//TODO search for editor-configs and attach listeners
		}

		private void applyEditorConfigToFolder(DataFolder dof) {
			for (DataObject dobj : dof.getChildren()) {
				applyEditorConfigToFile(dobj);
			}
		}

		private void applyEditorConfigToFile(DataObject dobj) throws NumberFormatException {
			System.out.println("APPLY");

//			EditorConfig ec;
//			try {
//				ec = new EditorConfig();
//				List<EditorConfig.OutPair> l = null;
//				l = ec.getProperties(dobj.getPrimaryFile().getPath());
//				for (int i = 0; i < l.size(); ++i) {
//					if (l.get(i).getKey().equals("indent_size")) {
//						doIndentSize(
//							dobj.getPrimaryFile(),
//							Integer.valueOf(l.get(i).getVal()));
//					}
//				}
//			} catch (PythonException ex) {
//				Exceptions.printStackTrace(ex);
//			} catch (EditorConfigException ex) {
//				Exceptions.printStackTrace(ex);
//			}
		}
		public static final String indentSize = SimpleValueNames.INDENT_SHIFT_WIDTH;

		private void doIndentSize(FileObject file, int value) {
			Preferences prefs = CodeStylePreferences.get(file, file.getMIMEType()).getPreferences();
			prefs.putInt(indentSize, value);
			try {
				prefs.flush();
			} catch (BackingStoreException ex) {
				Exceptions.printStackTrace(ex);
			}
		}

	}
}
