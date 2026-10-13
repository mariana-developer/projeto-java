package model;

import enums.EstadoConservacao;

import java.time.LocalDate;

public class PublicacaoImpressa extends ObraAcervo {
    public static final double TAXA_DIARIA_MULTA = 2.00;

    private int numeroPaginas;
    private double pesoFisico;
    private String tipoEncadernacao;
    private EstadoConservacao estadoConservacao = EstadoConservacao.NOVO;
    private boolean restauroAgendado = false;
    private LocalDate ultimaVistoria = null;

    public PublicacaoImpressa(int idRegistro, String titulo, String autor,
                              int anoPublicacao, int exemplaresTotais,
                              int numeroPaginas, double pesoFisico,
                              String tipoEncadernacao) {
        super(idRegistro, titulo, autor, anoPublicacao, exemplaresTotais, exemplaresTotais);
        setNumeroPaginas(numeroPaginas);
        setPesoFisico(pesoFisico);
        setTipoEncadernacao(tipoEncadernacao);
    }

    public int getNumeroPaginas() { return numeroPaginas; }

    public void setNumeroPaginas(int numeroPaginas) {
        if (numeroPaginas <= 0) {
            throw new IllegalArgumentException("O número de páginas deve ser maior que zero.");
        }
        this.numeroPaginas = numeroPaginas;
    }

    public double getPesoFisico() { return pesoFisico; }

    public void setPesoFisico(double pesoFisico) {
        if (pesoFisico <= 0) {
            throw new IllegalArgumentException("O peso deve ser maior que zero.");
        }
        this.pesoFisico = pesoFisico;
    }

    public String getTipoEncadernacao() { return tipoEncadernacao; }

    public void setTipoEncadernacao(String tipoEncadernacao) {
        if (tipoEncadernacao == null || tipoEncadernacao.isBlank()) {
            throw new IllegalArgumentException("O tipo de encadernação não pode ser vazio.");
        }
        this.tipoEncadernacao = tipoEncadernacao.trim();
    }

    public EstadoConservacao getEstadoConservacao() { return estadoConservacao; }

    public boolean isRestauroAgendado() { return restauroAgendado; }

    /** Agenda restauro; só faz sentido para obras desgastadas ou danificadas. */
    public String agendarRestauro() {
        if (estadoConservacao == EstadoConservacao.NOVO || estadoConservacao == EstadoConservacao.BOM) {
            throw new IllegalStateException("A obra está em estado " + estadoConservacao
                    + " e não precisa de restauro.");
        }
        if (restauroAgendado) {
            throw new IllegalStateException("Já existe um restauro agendado para esta obra.");
        }
        restauroAgendado = true;
        return "Restauro agendado para \"" + getTitulo() + "\" (estado: " + estadoConservacao + ").";
    }

    /** Vistoria periódica: registra o estado observado da capa. */
    public String inspecionarDesgasteCapa(EstadoConservacao estadoObservado) {
        if (estadoObservado == null) {
            throw new IllegalArgumentException("Informe o estado de conservação observado.");
        }
        estadoConservacao = estadoObservado;
        ultimaVistoria = LocalDate.now();
        String msg = "Vistoria realizada em \"" + getTitulo() + "\".\nEstado da capa: " + estadoObservado + ".";
        if (estadoObservado == EstadoConservacao.DESGASTADO || estadoObservado == EstadoConservacao.DANIFICADO) {
            msg += "\nRecomenda-se agendar restauro.";
        }
        return msg;
    }

    @Override
    public double calcularMulta(int diasAtraso) {
        if (diasAtraso <= 0) {
            return 0;
        }
        return diasAtraso * TAXA_DIARIA_MULTA;
    }

    @Override
    protected String detalhesEspecificos() {
        String texto = "Páginas: " + numeroPaginas + "\n"
                + "Peso: " + pesoFisico + " kg\n"
                + "Encadernação: " + tipoEncadernacao + "\n"
                + "Conservação: " + estadoConservacao
                + (restauroAgendado ? " (restauro agendado)" : "");
        if (ultimaVistoria != null) {
            texto += "\nÚltima vistoria: " + ultimaVistoria.format(Emprestimo.FORMATO_DATA);
        }
        return texto;
    }
}
