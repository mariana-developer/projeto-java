package exceptions;

public class LimiteEmprestimoExcedidoException extends Exception {
    private final String nomeDoLeitor;
    private final int limitePermitido;
    private final int emprestimosAtivos;

    public LimiteEmprestimoExcedidoException(String nomeDoLeitor, int limitePermitido, int emprestimosAtivos) {
        super("O leitor " + nomeDoLeitor + " excedeu o limite de empréstimos. "
                + "Limite permitido: " + limitePermitido
                + ", empréstimos ativos: " + emprestimosAtivos + ".");

        this.nomeDoLeitor = nomeDoLeitor;
        this.limitePermitido = limitePermitido;
        this.emprestimosAtivos = emprestimosAtivos;
    }

    public String getNomeDoLeitor() {
        return nomeDoLeitor;
    }

    public int getLimitePermitido() {
        return limitePermitido;
    }

    public int getEmprestimosAtivos() {
        return emprestimosAtivos;
    }
}