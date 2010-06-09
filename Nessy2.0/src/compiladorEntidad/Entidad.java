package compiladorEntidad;

import java.util.ArrayList;

/**
 *
 * @author Carlos, David y Tony
 * Clase que representa una entidad vhdl.
 */
public class Entidad {

    /**
     * Nombre de la entidad
     */
    private String nombre;

    /**
     * Número de bits de entrada
     */
    private int bitsEntrada;

    /**
     * Número de bits de salida
     */
    private int bitsSalida;

    /**
     * Entradas a la entidad
     */
    private ArrayList<Entrada> entradas;

    /**
     * Salidas de la entidad
     */
    private ArrayList<Salida> salidas;

    /**
     * Indica la posición que ocupa el CLK dentro de la entidad.
     * Si no hay CLK valdrá por defecto -1
     */
    private int posicionClk;

    /**
     * Indica la posición que ocupa el RESET dentro de la entidad.
     * Si no hay RESET valdrá por defecto -1
     */
    private int posicionReset;



    /**
     * Crea una entidad vacía.
     *
     */
    public Entidad() {
        entradas = new ArrayList<Entrada>();
        salidas = new ArrayList<Salida>();
        bitsEntrada = bitsSalida = 0;
        posicionClk = -1;
        posicionReset = -1;
    }

    /**
     *
     * @return Devuelve el nombre de la entidad.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Cambia el nombre de la entidad.
     * @param nombre Nuevo nombre de la entidad
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Getter para consultar el número de entradas de la Entidad.
     * @return El número de Entradas de la entidad.
     */
    public int getNumEntradas() {
        return entradas.size();
    }

    /**
     * Getter para consultar el número de salidas de la Entidad.
     * @return El número de Salidas de la entidad.
     */
    public int getNumSalidas() {
        return salidas.size();
    }

    /**
     * Getter para consultar el número de bits de entrada de la Entidad
     * @return El número de bits de entrada de la entidad.
     */
    public int getBitsEntrada() {
        return bitsEntrada;
    }

    /**
     * Setter para establecer el número de bits de entrada de la Entidad.
     * @param bitsEntrada
     */
    public void setBitsEntrada(int bitsEntrada) {
        this.bitsEntrada = bitsEntrada;
    }

    /**
     * Getter para consultar el número de bits de salida de la Entidad.
     * @return El número de bits de salida de la entidad.
     */
    public int getBitsSalida() {
        return bitsSalida;
    }

    /**
     * Setter para establecer el número de bits de salida de la Entidad.
     * @param bitsSalida Número de bits de salida
     */
    public void setBitsSalida(int bitsSalida) {
        this.bitsSalida = bitsSalida;
    }

    /**
     * Getter que devuelve la entrada i del ArrayList de las entradas.
     * @param i Posición de la Entrada que queremos devolver.
     * @return La entrada i del ArrayList de Entradas.
     */
    public Entrada getEntrada(int i) {
        return entradas.get(i);
    }

    /**
     * Getter que devuelve la salida i del ArrayList de las entradas.
     * @param i Posición de la Salida que queremos devolver.
     * @return La entrada i del ArrayList de salidas.
     */
    public Salida getSalida(int i) {
        return salidas.get(i);
    }

    /**
     * Devuelve la posición relativa de las entradas en la que se encuentra
     * el reset. Esto será utulizdo para poder enviar al circuito la señal de reset
     * aislándola de todas las demás
     * @return Posisicón que ocupa el reset dentro de las entradas
     */
    public int getPosicionReset(){
        return this.posicionReset;
    }

    /**
     * Devuelve la posición relativa de las entradas en la que se encuentra
     * el clk. Esto será utilizado para pder hacer la asignación adecuada, por
     * tratarse esta de una entrada especial.
     * @return Posisicón que ocupa el clk dentro de las entradas
     */
    public int getPosicionClk(){
        return this.posicionClk;
    }

    /**
     * Añade el argumento e de la clase Entrada al array de entradas de la entidad.
     * Diferencia entre dos tipos de entradas especiales. Si el nombre de la
     * entrada es CLK, CLOCK O RELOJ, la entrada será marcada como entrada de reloj.
     * De la misma forma, si el nombre de la entrada es RST o RESET se marcará como
     * una entrada especial de reset.
     * @param e Entrada a añadir a la entidad
     */
    public void anadeEntrada(Entrada e) {
        this.entradas.add(e);
        if (e.getNombre().equals("CLK") || e.getNombre().equals("CLOCK") ||
            e.getNombre().equals("RELOJ")) {
            e.ponerComoReloj();
            this.posicionClk = entradas.size()-1;
        }else if(e.getNombre().equals("RST") || e.getNombre().equals("RESET")){
            e.ponerComoReset(true);
            bitsEntrada += e.getNumBits();
            this.posicionReset = entradas.size()-1;
        } else {
            bitsEntrada += e.getNumBits();
        }
    }

    /**
     * Añade el argumento s de la clase Salida al array de salidas de la entidad.
     * @param s Salida a añadir a la entidad
     */
    public void anadeSalida(Salida s) {
        this.salidas.add(s);
        bitsSalida += s.getNumBits();
    }

    /**
     *Para mostrar por pantalla. Sólo de prueba
     */
    public void muestra() {
        System.out.println("Entidad: " + nombre);
        System.out.println("Entradas:");
        for (int i = 0; i < entradas.size(); i++) {
            Entrada e = entradas.get(i);
            for (int j = 0; j < e.getNumBits(); j++) {
                System.out.println("\t" + e.getNombre() + "(" + j + ")");
            }
        }
        System.out.println("Salidas:");
        for (int i = 0; i < salidas.size(); i++) {
            Salida s = salidas.get(i);
            for (int j = 0; j < s.getNumBits(); j++) {
                System.out.println("\t" + s.getNombre() + "(" + j + ")");
            }
        }
        System.out.println("\nNum_entradas: " + bitsEntrada);
        System.out.println("Num_salidas: " + bitsSalida);
    }

    /**
     * Método para consultar el nombre de la entrada de reset.
     * @return Devuelve el Nombre de la Entrada de reset.
     */
    public String getNombreReset(){
        int i = 0;
        String reset = null;
        while (i < this.getNumEntradas() && reset == null){
            if (this.getEntrada(i).getEsReset()){
                reset = this.getEntrada(i).getNombre();
            }
            i++;
        }
        return reset;
    }

    /**
     * Devuelve una cadena con la descripción de las entradas y salidas de la entidad.
     * @return Cadena con las entradas y salidas de la Entidad.
     */
    @Override
    public String toString() {
        String s = "";
        s += ("Entidad: " + nombre) + "\n";
        s += ("Entradas:") + "\n";
        for (int i = 0; i < entradas.size(); i++) {
            Entrada e = entradas.get(i);
            if (!e.getEsReloj()) {
                for (int j = 0; j < e.getNumBits(); j++) {
                    s += ("\t" + e.getNombre() + "(" + j + ")") + "\n";
                }
            }
        }
        s += "Salidas:" + "\n";
        for (int i = 0; i < salidas.size(); i++) {
            Salida sal = salidas.get(i);
            for (int j = 0; j < sal.getNumBits(); j++) {
                s += ("\t" + sal.getNombre() + "(" + j + ")") + "\n";
            }
        }
        s += ("\nNum_entradas: " + bitsEntrada) + "\n";
        s += ("Num_salidas: " + bitsSalida) + "\n";
        return s;
    }
}
