

package nessy20;

/**
 *
 * @author Tony,David y Carlos.
 */

import javax.swing.filechooser.*;
import java.io.File;

/**
 * Clase que sirve para crear un filtro de extensiones a la hora de mostrar
 * un selector de ficheros
 * @author David
 */
public class Filtro extends FileFilter {

  String[] extensions;
  String description;

  /**
   * Constructor de la clase.
   * @param ext Extensión de ficheros que deseamos que aparezcan.
   */
  public Filtro(String ext) {
    this (new String[] {ext}, null);
  }

  /**
   * Constructor de la clase.
   * @param exts Extensiones que deseamos que aparezcan.
   * @param descr Extensión de una de ellas.
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

    if (f.isDirectory()) { return true; }
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
