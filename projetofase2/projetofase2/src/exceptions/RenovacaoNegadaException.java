package exceptions;

public class RenovacaoNegadaException extends Exception {
    private final String tituloObra;
    private final int renovacoesFeitas;
    private final String motivo;

    public RenovacaoNegadaException(String tituloObra, int renovacoesFeitas, String motivo) {
        super("A renovação da obra \"" + tituloObra + "\" foi negada. "
                + "Renovações já feitas: " + renovacoesFeitas
                + ". Motivo: " + motivo + ".");

        this.tituloObra = tituloObra;
        this.renovacoesFeitas = renovacoesFeitas;
        this.motivo = motivo;
    }

    public String getTituloObra() {
        return tituloObra;
    }

    public int getRenovacoesFeitas() {
        return renovacoesFeitas;
    }

    public String getMotivo() {
        return motivo;
    }
}

