package logica;

/**
 * Detección de errores con bit de paridad par usando XOR (⊕).
 *
 * Emisor:   P = b₁ ⊕ b₂ ⊕ … ⊕ bₙ   y se envía b₁b₂…bₙP
 * Receptor: S = r₁ ⊕ r₂ ⊕ … ⊕ rₙ₊₁
 *           S = 0 → no se detecta error;  S = 1 → error detectado
 */
public class Paridad {

    public static int bit(char c) { return c == '1' ? 1 : 0; }

    /** XOR de todos los bits de la cadena. */
    public static int xorTotal(String bits) {
        int r = 0;
        for (char c : bits.toCharArray()) r ^= bit(c);
        return r;
    }

    /** Bit de paridad par del mensaje. */
    public static int bitParidad(String mensaje) { return xorTotal(mensaje); }

    /** Trama que se envía: mensaje + bit de paridad. */
    public static String agregarParidad(String mensaje) {
        return mensaje + bitParidad(mensaje);
    }

    /** Síndrome del receptor: 0 = sin error detectado, 1 = error detectado. */
    public static int sindrome(String trama) { return xorTotal(trama); }

    /** Paso a paso: "1 ⊕ 0 = 1, 1 ⊕ 1 = 0, …". */
    public static String pasoAPaso(String bits) {
        if (bits.isEmpty()) return "(vacío) = 0";
        StringBuilder sb = new StringBuilder();
        int acum = bit(bits.charAt(0));
        sb.append("  inicio: ").append(acum).append("\n");
        for (int i = 1; i < bits.length(); i++) {
            int b = bit(bits.charAt(i));
            int nuevo = acum ^ b;
            sb.append("  ").append(acum).append(" ⊕ ").append(b).append(" = ").append(nuevo).append("\n");
            acum = nuevo;
        }
        return sb.toString();
    }

    /** Expresión completa: "1 ⊕ 0 ⊕ 1 = 0". */
    public static String expresion(String bits) {
        if (bits.isEmpty()) return "0";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bits.length(); i++) {
            if (i > 0) sb.append(" ⊕ ");
            sb.append(bits.charAt(i));
        }
        return sb.append(" = ").append(xorTotal(bits)).toString();
    }
}
