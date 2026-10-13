package exceptions;

public class ObraEsgotadaException extends Exception {

    private final String titulo;
    private final int exemplaresDisponiveis;

    public ObraEsgotadaException(String titulo, int exemplaresDisponiveis) {
        super("A obra \"" + titulo + "\" está esgotada. "
                + "Exemplares disponíveis: " + exemplaresDisponiveis + ".");
        this.titulo = titulo;
        this.exemplaresDisponiveis = exemplaresDisponiveis;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getExemplaresDisponiveis() {
        return exemplaresDisponiveis;
    }
}
