/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nessy20;

/**
 *
 * @author Tony
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author User
 */
import javax.swing.filechooser.*;
import java.io.File;

/**
 *
 * @author Tony
 */
public class Filtro extends FileFilter {

  String[] extensions;
  String description;

  /**
   *
   * @param ext
   */
  public Filtro(String ext) {
    this (new String[] {ext}, null);
  }

  /**
   *
   * @param exts
   * @param descr
   */
  public Filtro(String[] exts, String descr) {
    // Clone and lowercase the extensions
    extensions = new String[exts.length];
    for (int i = exts.length - 1; i >= 0; i--) {
      extensions[i] = exts[i].toLowerCase();
    }
    // Make sure we have a valid (if simplistic) description
    description = (descr == null ? exts[0] + " files" : descr);
  }

  public boolean accept(File f) {
    // We always allow directories, regardless of their extension
    if (f.isDirectory()) { return true; }

    // Ok, itвЂ™s a regular file, so check the extension
    String name = f.getName().toLowerCase();
    for (int i = extensions.length - 1; i >= 0; i--) {
      if (name.endsWith(extensions[i])) {
        return true;
      }
    }
    return false;
  }

  public String getDescription() { return description; }
}
