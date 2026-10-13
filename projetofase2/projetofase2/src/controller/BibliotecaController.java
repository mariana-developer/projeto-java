package controller;

import exceptions.LimiteEmprestimoExcedidoException;
import exceptions.ObraEsgotadaException;
import exceptions.RenovacaoNegadaException;
import model.Emprestimo;
import model.Leitor;
import model.ObraAcervo;
import model.PublicacaoDigital;

import java.time.LocalDate;
import java.util.ArrayList;

public class BibliotecaController {
    public static final int LIMITE_ESTANTE = 100;

    private final ArrayList<ObraAcervo> obras = new ArrayList<>();
    private final ArrayList<Leitor> leitores = new ArrayList<>();
    private final ArrayList<Emprestimo> emprestimos = new ArrayList<>();

    // ---------- cadastros ----------

    public void cadastrarObra(ObraAcervo obra) {
        if (obra == null) {
            throw new IllegalArgumentException("Obra inválida.");
        }
        if (buscarObra(obra.getIdRegistro()) != null) {
            throw new IllegalArgumentException("Já existe uma obra com o id " + obra.getIdRegistro() + ".");
        }
        if (ocupaEstante(obra)) {
            verificarEspacoEstante(obra.getExemplaresTotais());
        }
        obras.add(obra);
        obra.registrarNoAcervo();   // só agora o contador estático do acervo é incrementado
    }

    public void cadastrarLeitor(Leitor leitor) {
        if (leitor == null) {
            throw new IllegalArgumentException("Leitor inválido.");
        }
        if (buscarLeitor(leitor.getMatricula()) != null) {
            throw new IllegalArgumentException("Já existe um leitor com a matrícula "
                    + leitor.getMatricula() + ".");
        }
        leitores.add(leitor);
    }

    // ---------- buscas ----------

    public ObraAcervo buscarObra(int id) {
        for (ObraAcervo o : obras) {
            if (o.getIdRegistro() == id) {
                return o;
            }
        }
        return null;
    }

    public Leitor buscarLeitor(String matricula) {
        for (Leitor l : leitores) {
            if (l.getMatricula().equals(matricula)) {
                return l;
            }
        }
        return null;
    }

    public Emprestimo buscarEmprestimo(int codigo) {
        for (Emprestimo e : emprestimos) {
            if (e.getCodigo() == codigo) {
                return e;
            }
        }
        return null;
    }

    private int contarAtivos(Leitor leitor) {
        int total = 0;
        for (Emprestimo e : emprestimos) {
            if (e.isAtivo() && e.getLeitor() == leitor) {
                total++;
            }
        }
        return total;
    }

    private boolean leitorTemEmprestimoAtivo(Leitor leitor, ObraAcervo obra) {
        for (Emprestimo e : emprestimos) {
            if (e.isAtivo() && e.getLeitor() == leitor && e.getObra() == obra) {
                return true;
            }
        }
        return false;
    }

    private Leitor exigirLeitor(String matricula) {
        Leitor leitor = buscarLeitor(matricula);
        if (leitor == null) {
            throw new IllegalArgumentException("Leitor não encontrado.");
        }
        return leitor;
    }

    private ObraAcervo exigirObra(int idObra) {
        ObraAcervo obra = buscarObra(idObra);
        if (obra == null) {
            throw new IllegalArgumentException("Obra não encontrada.");
        }
        return obra;
    }

    // ---------- opção 3: realizar empréstimo ----------

    public Emprestimo realizarEmprestimo(String matricula, int idObra, LocalDate dataSaida)
            throws LimiteEmprestimoExcedidoException, ObraEsgotadaException {
        if (dataSaida == null) {
            throw new IllegalArgumentException("Data de saída inválida.");
        }
        Leitor leitor = exigirLeitor(matricula);
        ObraAcervo obra = exigirObra(idObra);

        int ativos = contarAtivos(leitor);
        if (ativos >= leitor.getLimiteEmprestimos()) {
            throw new LimiteEmprestimoExcedidoException(leitor.getNome(),
                    leitor.getLimiteEmprestimos(), ativos);
        }

        if (obra.getExemplaresDisponiveis() <= 0) {
            throw new ObraEsgotadaException(obra.getTitulo(), obra.getExemplaresDisponiveis());
        }

        if (leitorTemEmprestimoAtivo(leitor, obra)) {
            throw new IllegalStateException("O leitor já está com um exemplar desta obra.");
        }

        if (obra.temReserva() && !leitor.getMatricula().equals(obra.getProximoReservado())) {
            throw new IllegalStateException("A obra está reservada para outro leitor.");
        }

        // se for digital, consome uma licença (pode falhar antes de qualquer alteração)
        if (obra instanceof PublicacaoDigital) {
            ((PublicacaoDigital) obra).usarLicenca();
        }

        obra.diminuirExemplar();
        obra.removerReserva(leitor.getMatricula());   // se ele era o primeiro da fila, a reserva é cumprida

        Emprestimo emprestimo = new Emprestimo(leitor, obra, dataSaida);
        emprestimos.add(emprestimo);
        leitor.adicionarTitulo(obra.getTitulo());
        return emprestimo;
    }

    public void reservarObra(String matricula, int idObra) {
        Leitor leitor = exigirLeitor(matricula);
        ObraAcervo obra = exigirObra(idObra);
        if (obra.getExemplaresDisponiveis() > 0) {
            throw new IllegalStateException("Ainda há exemplares disponíveis; faça o empréstimo normalmente.");
        }
        if (leitorTemEmprestimoAtivo(leitor, obra)) {
            throw new IllegalStateException("O leitor já está com um exemplar desta obra.");
        }
        obra.adicionarReserva(leitor.getMatricula());
    }

    // ---------- opção 4: registrar devolução ----------

    public double registrarDevolucao(int codigoEmprestimo, LocalDate dataDevolucao) {
        Emprestimo emprestimo = buscarEmprestimo(codigoEmprestimo);
        if (emprestimo == null) {
            throw new IllegalArgumentException("Empréstimo não encontrado.");
        }

        double multa = emprestimo.devolver(dataDevolucao);

        ObraAcervo obra = emprestimo.getObra();
        obra.aumentarExemplar();
        if (obra instanceof PublicacaoDigital) {
            ((PublicacaoDigital) obra).liberarLicenca();
        }
        emprestimo.getLeitor().removerTitulo(obra.getTitulo());
        return multa;
    }

    // ---------- opção 5: consultar acervo (SOBRECARGA) ----------

    private String linhaObra(ObraAcervo o) {
        return "Id " + o.getIdRegistro()
                + " | " + o.getClass().getSimpleName()
                + " | " + o.getTitulo()
                + " | " + o.getAutor()
                + " | " + o.getAnoPublicacao()
                + " | disponíveis: " + o.getExemplaresDisponiveis() + "/" + o.getExemplaresTotais()
                + " | " + o.getStatus();
    }

    public String buscarAcervo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Informe um título (ou parte dele).");
        }
        StringBuilder texto = new StringBuilder("Resultado da busca por \"" + titulo + "\":\n");
        boolean achou = false;
        for (ObraAcervo o : obras) {
            if (o.getTitulo().toLowerCase().contains(titulo.toLowerCase())) {
                achou = true;
                texto.append("\n").append(linhaObra(o));
            }
        }
        if (!achou) {
            texto.append("\nNenhuma obra encontrada.");
        }
        return texto.toString();
    }

    public String buscarAcervo(int ano, String autor) {
        if (autor == null || autor.isBlank()) {
            throw new IllegalArgumentException("Informe o nome do autor.");
        }
        StringBuilder texto = new StringBuilder("Resultado da busca por " + autor + " em " + ano + ":\n");
        boolean achou = false;
        for (ObraAcervo o : obras) {
            if (o.getAnoPublicacao() == ano
                    && o.getAutor().toLowerCase().contains(autor.toLowerCase())) {
                achou = true;
                texto.append("\n").append(linhaObra(o));
            }
        }
        if (!achou) {
            texto.append("\nNenhuma obra encontrada.");
        }
        return texto.toString();
    }

    // ---------- opção 6: renovar ----------

    public Emprestimo renovarEmprestimo(int codigoEmprestimo) throws RenovacaoNegadaException {
        Emprestimo emprestimo = buscarEmprestimo(codigoEmprestimo);
        if (emprestimo == null) {
            throw new IllegalArgumentException("Empréstimo não encontrado.");
        }
        emprestimo.renovar();
        return emprestimo;
    }

    // ---------- opção 7: doação e reposição (MÉTODO FINAL) ----------

    private boolean ocupaEstante(ObraAcervo obra) {
        return !(obra instanceof PublicacaoDigital);
    }

    public int calcularEspacoOcupado() {
        int total = 0;
        for (ObraAcervo o : obras) {
            if (ocupaEstante(o)) {
                total += o.getExemplaresTotais();
            }
        }
        return total;
    }

    public int calcularEspacoLivre() {
        return LIMITE_ESTANTE - calcularEspacoOcupado();
    }

    public void verificarEspacoEstante(int quantidade) {
        int espacoLivre = calcularEspacoLivre();
        if (quantidade > espacoLivre) {
            throw new IllegalStateException("Estante cheia. Espaço livre: "
                    + espacoLivre + " volume(s).");
        }
    }

    public final void processarDoacao(int idObra, int quantidade) {
        ObraAcervo obra = exigirObra(idObra);
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        if (ocupaEstante(obra)) {
            verificarEspacoEstante(quantidade);
        }
        obra.adicionarExemplares(quantidade);
    }

    // ---------- opção 8: histórico do leitor ----------

    public String historicoDoLeitor(String matricula) {
        Leitor leitor = exigirLeitor(matricula);
        LocalDate hoje = LocalDate.now();
        StringBuilder texto = new StringBuilder("Histórico de " + leitor.getNome() + ":\n");

        ArrayList<String> comLeitor = leitor.getTitulosEmprestados();
        texto.append("\nAtualmente com o leitor: ")
                .append(comLeitor.isEmpty() ? "nenhum título" : String.join(", ", comLeitor))
                .append("\n");

        boolean achou = false;
        for (Emprestimo e : emprestimos) {
            if (e.getLeitor() == leitor) {
                achou = true;
                texto.append("\nCódigo ").append(e.getCodigo())
                        .append(" | ").append(e.getObra().getTitulo())
                        .append(" | saída: ").append(e.getDataSaida().format(Emprestimo.FORMATO_DATA))
                        .append(" | prazo: ").append(e.getPrazoDevolucao().format(Emprestimo.FORMATO_DATA))
                        .append(" | renovações: ").append(e.getRenovacoes())
                        .append(" | ").append(e.getSituacao(hoje));
            }
        }
        if (!achou) {
            texto.append("\nNenhum empréstimo registrado.");
        }
        return texto.toString();
    }

    // ---------- opção 9: relatório de pendentes ----------

    public String relatorioPendentes() {
        LocalDate hoje = LocalDate.now();
        StringBuilder texto = new StringBuilder("Obras fora da biblioteca:\n");
        boolean achou = false;
        for (Emprestimo e : emprestimos) {
            if (e.isAtivo()) {
                achou = true;
                texto.append("\n").append(e.getObra().getTitulo())
                        .append(" | leitor: ").append(e.getLeitor().getNome())
                        .append(" (").append(e.getLeitor().getMatricula()).append(")")
                        .append(" | prazo: ").append(e.getPrazoDevolucao().format(Emprestimo.FORMATO_DATA))
                        .append(" | ").append(e.getSituacao(hoje));
            }
        }
        if (!achou) {
            texto.append("\nNenhuma obra pendente.");
        }
        return texto.toString();
    }
}
