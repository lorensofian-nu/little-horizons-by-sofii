package test.modelo;

/** Datos del profesor necesarios para el reporte de carga académica. */
public final class Profesor {

    private final int id;
    private final String tipoDocumento;
    private final String numeroDocumento;
    private final String primerNombre;
    private final String segundoNombre;
    private final String primerApellido;
    private final String segundoApellido;

    public Profesor(int id, String tipoDocumento, String numeroDocumento,
                    String primerNombre, String segundoNombre,
                    String primerApellido, String segundoApellido) {
        this.id = id;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
    }

    public int getId() {
        return id;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    /** Construye el nombre sin espacios dobles cuando hay campos nulos o vacíos. */
    public String getNombreCompleto() {
        return unirPartes(primerNombre, segundoNombre, primerApellido, segundoApellido);
    }

    public String getDocumentoCompleto() {
        return unirPartes(tipoDocumento, numeroDocumento);
    }

    private static String unirPartes(String... partes) {
        StringBuilder resultado = new StringBuilder();
        for (String parte : partes) {
            if (parte != null && !parte.isBlank()) {
                if (resultado.length() > 0) {
                    resultado.append(' ');
                }
                resultado.append(parte.trim());
            }
        }
        return resultado.toString();
    }
}
