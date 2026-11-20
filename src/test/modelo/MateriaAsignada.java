package test.modelo;

/** Materia y grado asociados a una asignación docente. */
public final class MateriaAsignada {

    private final String codigo;
    private final String nombre;
    private final String grado;

    public MateriaAsignada(String codigo, String nombre, String grado) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.grado = grado;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getGrado() {
        return grado;
    }
}
