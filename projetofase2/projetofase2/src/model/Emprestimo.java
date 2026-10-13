package model;

import enums.StatusObra;
import exceptions.RenovacaoNegadaException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;

public class Emprestimo {
    public static final int PRAZO_PADRAO_DIAS = 7;
    public static final int MAX_RENOVACOES = 2;
    public static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private static int contadorCodigos = 0;

    private final int codigo;
    private final Leitor leitor;
    private final ObraAcervo obra;
    private final LocalDate dataSaida;
    private LocalDate prazoDevolucao;
    private LocalDate dataDevolucao;
    private int renovacoes;
    private boolean ativo;
    private int diasAtrasoNaDevolucao;
    private double multaCobrada;

    public Emprestimo(Leitor leitor, ObraAcervo obra, LocalDate dataSaida) {
        if (leitor == null || obra == null) {
            throw new IllegalArgumentException("Leitor e obra são obrigatórios.");
        }
        if (dataSaida == null) {
            throw new IllegalArgumentException("A data de saída é obrigatória.");
        }
        contadorCodigos++;
        this.codigo = contadorCodigos;
        this.leitor = leitor;
        this.obra = obra;
        this.dataSaida = dataSaida;
        this.prazoDevolucao = dataSaida.plusDays(PRAZO_PADRAO_DIAS);
        this.renovacoes = 0;
        this.ativo = true;
    }

    public void renovar() throws RenovacaoNegadaException {
        if (!ativo) {
            throw new RenovacaoNegadaException(obra.getTitulo(), renovacoes,
                    "Empréstimo já encerrado");
        }
        if (renovacoes >= MAX_RENOVACOES) {
            throw new RenovacaoNegadaException(obra.getTitulo(), renovacoes,
                    "Limite de renovações atingido");
        }
        if (obra.getStatus() == StatusObra.RESERVADA) {
            throw new RenovacaoNegadaException(obra.getTitulo(), renovacoes,
                    "Há reserva pendente para esta obra");
        }
        prazoDevolucao = prazoDevolucao.plusDays(PRAZO_PADRAO_DIAS);
        renovacoes++;
    }

    public int calcularDiasAtraso(LocalDate referencia) {
        long atraso = ChronoUnit.DAYS.between(prazoDevolucao, referencia);
        if (atraso > 0) {
            return (int) atraso;
        }
        return 0;
    }

    public double devolver(LocalDate dataDevolucao) {
        if (!ativo) {
            throw new IllegalStateException("Este empréstimo já foi devolvido.");
        }
        if (dataDevolucao == null) {
            throw new IllegalArgumentException("A data de devolução é obrigatória.");
        }
        if (dataDevolucao.isBefore(dataSaida)) {
            throw new IllegalArgumentException("A devolução não pode ser anterior à data de saída ("
                    + dataSaida.format(FORMATO_DATA) + ").");
        }
        int dias = calcularDiasAtraso(dataDevolucao);
        double multa = obra.calcularMulta(dias);   // polimorfismo: cada tipo de obra calcula do seu jeito
        this.ativo = false;
        this.dataDevolucao = dataDevolucao;
        this.diasAtrasoNaDevolucao = dias;
        this.multaCobrada = multa;
        return multa;
    }

    public String getSituacao(LocalDate hoje) {
        if (ativo) {
            int atraso = calcularDiasAtraso(hoje);
            if (atraso > 0) {
                return "EM ATRASO (" + atraso + " dia(s))";
            }
            return "EM ABERTO";
        }
        String s = "DEVOLVIDO em " + dataDevolucao.format(FORMATO_DATA);
        if (diasAtrasoNaDevolucao > 0) {
            s += " com " + diasAtrasoNaDevolucao + " dia(s) de atraso";
        }
        return s;
    }

    public int getCodigo() { return codigo; }

    public Leitor getLeitor() { return leitor; }

    public ObraAcervo getObra() { return obra; }

    public LocalDate getDataSaida() { return dataSaida; }

    public LocalDate getPrazoDevolucao() { return prazoDevolucao; }

    public LocalDate getDataDevolucao() { return dataDevolucao; }

    public int getRenovacoes() { return renovacoes; }

    public boolean isAtivo() { return ativo; }

    public int getDiasAtrasoNaDevolucao() { return diasAtrasoNaDevolucao; }

    public double getMultaCobrada() { return multaCobrada; }
}
