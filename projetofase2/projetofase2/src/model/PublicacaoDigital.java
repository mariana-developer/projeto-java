package model;

public class PublicacaoDigital extends ObraAcervo {
    public static final int LIMITE_DOWNLOADS = 5;
    public static final double TAXA_ADMINISTRATIVA_MULTA = 4.00;

    private String formato;
    private double tamanhoMB;
    private int licencasTotais;
    private int licencasAtivas;
    private int downloadsTemporarios;

    public PublicacaoDigital(int id, String titulo, String autor, int anoPublicacao,
                             int exemplaresTotais, String formato, double tamanhoMB,
                             int licencasTotais) {
        super(id, titulo, autor, anoPublicacao, exemplaresTotais, exemplaresTotais);
        setFormato(formato);
        setTamanhoMB(tamanhoMB);
        setLicencasTotais(licencasTotais);
        this.licencasAtivas = 0;
        this.downloadsTemporarios = 0;
    }

    public void usarLicenca() {
        if (licencasAtivas >= licencasTotais) {
            throw new IllegalStateException("Sem licenças disponíveis para: " + getTitulo());
        }
        licencasAtivas++;
    }

    public void liberarLicenca() {
        if (licencasAtivas <= 0) {
            throw new IllegalStateException("Nenhuma licença em uso para: " + getTitulo());
        }
        licencasAtivas--;
        if (licencasAtivas == 0) {
            downloadsTemporarios = 0;
        }
    }

    public void registrarDownloadTemporario() {
        if (licencasAtivas <= 0) {
            throw new IllegalStateException("Não há licença ativa para autorizar download.");
        }
        if (downloadsTemporarios >= LIMITE_DOWNLOADS) {
            throw new IllegalStateException("Limite de downloads temporários atingido ("
                    + LIMITE_DOWNLOADS + ").");
        }
        downloadsTemporarios++;
    }

    /** Cada novo exemplar digital recebido também libera uma licença nova. */
    @Override
    public void adicionarExemplares(int quantidade) {
        super.adicionarExemplares(quantidade);
        licencasTotais += quantidade;
    }

    public String getFormato() { return formato; }

    public void setFormato(String formato) {
        if (formato == null || !(formato.trim().equalsIgnoreCase("PDF") || formato.trim().equalsIgnoreCase("EPUB"))) {
            throw new IllegalArgumentException("Formato inválido. Use PDF ou EPUB.");
        }
        this.formato = formato.trim().toUpperCase();
    }

    public double getTamanhoMB() { return tamanhoMB; }

    public void setTamanhoMB(double tamanhoMB) {
        if (tamanhoMB <= 0) {
            throw new IllegalArgumentException("O tamanho em MB deve ser maior que zero.");
        }
        this.tamanhoMB = tamanhoMB;
    }

    public int getLicencasTotais() { return licencasTotais; }

    public void setLicencasTotais(int licencasTotais) {
        if (licencasTotais <= 0) {
            throw new IllegalArgumentException("O total de licenças deve ser maior que zero.");
        }
        this.licencasTotais = licencasTotais;
    }

    public int getLicencasAtivas() { return licencasAtivas; }

    public int getDownloadsTemporarios() { return downloadsTemporarios; }

    @Override
    public double calcularMulta(int diasAtraso) {
        if (diasAtraso <= 0) {
            return 0;
        }
        return TAXA_ADMINISTRATIVA_MULTA;
    }

    @Override
    protected String detalhesEspecificos() {
        return "Formato: " + formato + "\n"
                + "Tamanho: " + tamanhoMB + " MB\n"
                + "Licenças ativas: " + licencasAtivas + " de " + licencasTotais + "\n"
                + "Downloads temporários: " + downloadsTemporarios + " de " + LIMITE_DOWNLOADS;
    }
}
