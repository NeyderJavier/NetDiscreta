package vista;

/**
 * Un módulo que debe actualizarse cuando el usuario entra a su pestaña,
 * porque otro módulo pudo cambiar la red, los grupos o el mensaje.
 */
public interface Refrescable {
    void refrescar();
}
