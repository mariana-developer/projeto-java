package model;

public class LivroFisico extends PublicacaoImpressa {
    public static final double CUSTO_CONSERVACAO_PAPEL = 5.00;

    private String isbn;
    private String editora;

    public LivroFisico(int idRegistro, String titulo, String autor,
                       int anoPublicacao, int exemplaresTotais,
                       int numeroPaginas, double pesoFisico, String tipoEncadernacao,
                       String isbn, String editora) {
        super(idRegistro, titulo, autor, anoPublicacao, exemplaresTotais,
                numeroPaginas, pesoFisico, tipoEncadernacao);
        setIsbn(isbn);
        setEditora(editora);
    }

    public String getIsbn() { return isbn; }

    public void setIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("O ISBN não pode ser vazio.");
        }
        String limpo = isbn.trim().replace("-", "").replace(" ", "");
        if (!limpo.matches("\\d{9}[\\dXx]") && !limpo.matches("\\d{13}")) {
            throw new IllegalArgumentException("ISBN inválido. Use 10 ou 13 dígitos (hífens são aceitos).");
        }
        this.isbn = isbn.trim();
    }

    public String getEditora() { return editora; }

    public void setEditora(String editora) {
        if (editora == null || editora.isBlank()) {
            throw new IllegalArgumentException("A editora não pode ser vazia.");
        }
        this.editora = editora.trim();
    }

    /** Conferência do volume da estante física: quantos deveriam estar na prateleira. */
    public String conferirVolumeEstante() {
        int naEstante = getExemplaresDisponiveis();
        int foraDaBiblioteca = getExemplaresTotais() - naEstante;
        return "Conferência de estante: \"" + getTitulo() + "\" (ISBN " + isbn + ")\n"
                + "Devem estar na estante: " + naEstante + "\n"
                + "Emprestados (fora da biblioteca): " + foraDaBiblioteca + "\n"
                + "Total do acervo: " + getExemplaresTotais();
    }

    @Override
    public double calcularMulta(int diasAtraso) {
        if (diasAtraso <= 0) {
            return 0;
        }
        return super.calcularMulta(diasAtraso) + CUSTO_CONSERVACAO_PAPEL;
    }

    @Override
    protected String detalhesEspecificos() {
        return super.detalhesEspecificos() + "\nISBN: " + isbn + "\nEditora: " + editora;
    }
}
