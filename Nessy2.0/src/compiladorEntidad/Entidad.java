package compiladorEntidad;

import java.util.ArrayList;

/**
 *
 * @author Carlos
 */
public class Entidad {

    private String nombre;
    private int bitsEntrada;
    private int bitsSalida;
    private ArrayList<Entrada> entradas;
    private ArrayList<Salida> salidas;

    public Entidad() {
        entradas = new ArrayList<Entrada>();
        salidas = new ArrayList<Salida>();
        bitsEntrada = bitsSalida = 0;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getNumEntradas() {
        return entradas.size();
    }

    public int getNumSalidas() {
        return salidas.size();
    }

    public int getBitsEntrada() {
        return bitsEntrada;
    }

    public void setBitsEntrada(int bitsEntrada) {
        this.bitsEntrada = bitsEntrada;
    }

    public int getBitsSalida() {
        return bitsSalida;
    }

    public void setBitsSalida(int bitsSalida) {
        this.bitsSalida = bitsSalida;
    }

    public Entrada getEntrada(int i) {
        return entradas.get(i);
    }

    public Salida getSalida(int i) {
        return salidas.get(i);
    }

    public void anadeEntrada(Entrada e) {
        this.entradas.add(e);
        if (e.getNombre().equals("CLK") || e.getNombre().equals("CLOCK") ||
            e.getNombre().equals("RELOJ")) {
            e.ponerComoReloj();
        }else if(e.getNombre().equals("RST") || e.getNombre().equals("RESET")){
            e.ponerComoReset(true);
        } else {
            bitsEntrada += e.getNumBits();
        }
    }

    public void anadeSalida(Salida s) {
        this.salidas.add(s);
        bitsSalida += s.getNumBits();
    }

    /* Para mostrar por pantalla. Sólo de prueba */
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

    @Override
    public String toString() {
        String s = "";
        s += ("Entidad: " + nombre) + "\n";
        s += ("Entradas:") + "\n";
        for (int i = 0; i < entradas.size(); i++) {
            Entrada e = entradas.get(i);
            if (!e.getEsReloj() && !e.getEsReset()) {
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
