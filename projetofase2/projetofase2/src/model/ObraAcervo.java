package model;

import enums.StatusObra;

import javax.swing.JOptionPane;
import java.time.LocalDate;
import java.util.ArrayList;

public abstract class ObraAcervo {
    private final int idRegistro;
    private String titulo;
    private String autor;
    private int anoPublicacao;
    private int exemplaresTotais;
    private int exemplaresDisponiveis;
    private StatusObra status;
    private boolean registrada = false;
    private final ArrayList<String> filaReservas = new ArrayList<>();

    private static int totalVolumes = 0;

    public ObraAcervo(int idRegistro, String titulo, String autor,
                      int anoPublicacao, int exemplaresTotais,
                      int exemplaresDisponiveis) {
        if (idRegistro <= 0) {
            throw new IllegalArgumentException("O id de registro deve ser maior que zero.");
        }
        if (exemplaresTotais <= 0) {
            throw new IllegalArgumentException("O total de exemplares deve ser maior que zero.");
        }
        if (exemplaresDisponiveis < 0 || exemplaresDisponiveis > exemplaresTotais) {
            throw new IllegalArgumentException("Exemplares disponíveis deve estar entre 0 e o total.");
        }
        this.idRegistro = idRegistro;
        this.titulo = validarTexto(titulo, "título");
        this.autor = validarTexto(autor, "autor");
        this.anoPublicacao = validarAno(anoPublicacao);
        this.exemplaresTotais = exemplaresTotais;
        this.exemplaresDisponiveis = exemplaresDisponiveis;
        atualizarStatus();
    }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("O campo \"" + campo + "\" não pode ser vazio.");
        }
        return valor.trim();
    }

    private static int validarAno(int ano) {
        if (ano < 1 || ano > LocalDate.now().getYear()) {
            throw new IllegalArgumentException("Ano de publicação inválido: " + ano + ".");
        }
        return ano;
    }

    // ---------- contador estático do acervo ----------

    /** Conta os exemplares desta obra no total geral do acervo (chamado uma única vez, no cadastro). */
    public final void registrarNoAcervo() {
        if (registrada) {
            throw new IllegalStateException("A obra já foi registrada no acervo.");
        }
        registrada = true;
        totalVolumes += exemplaresTotais;
    }

    public static int getTotalVolumes() {
        return totalVolumes;
    }

    // ---------- getters e setters ----------

    public int getIdRegistro() { return idRegistro; }

    public String getTitulo() { return titulo; }

    public void setTitulo(String titulo) {
        this.titulo = validarTexto(titulo, "título");
    }

    public String getAutor() { return autor; }

    public void setAutor(String autor) {
        this.autor = validarTexto(autor, "autor");
    }

    public int getAnoPublicacao() { return anoPublicacao; }

    public void setAnoPublicacao(int anoPublicacao) {
        this.anoPublicacao = validarAno(anoPublicacao);
    }

    public int getExemplaresTotais() { return exemplaresTotais; }

    public int getExemplaresDisponiveis() { return exemplaresDisponiveis; }

    public StatusObra getStatus() { return status; }

    // ---------- controle de exemplares ----------

    public void diminuirExemplar() {
        if (exemplaresDisponiveis <= 0) {
            throw new IllegalStateException("Sem exemplares disponíveis.");
        }
        exemplaresDisponiveis--;
        atualizarStatus();
    }

    public void aumentarExemplar() {
        if (exemplaresDisponiveis < exemplaresTotais) {
            exemplaresDisponiveis++;
        }
        atualizarStatus();
    }

    public void adicionarExemplares(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        exemplaresTotais += quantidade;
        exemplaresDisponiveis += quantidade;
        if (registrada) {
            totalVolumes += quantidade;
        }
        atualizarStatus();
    }

    // ---------- reservas ----------

    public void adicionarReserva(String matricula) {
        if (matricula == null || matricula.isBlank()) {
            throw new IllegalArgumentException("Matrícula inválida para reserva.");
        }
        if (filaReservas.contains(matricula)) {
            throw new IllegalStateException("Este leitor já reservou esta obra.");
        }
        filaReservas.add(matricula);
        atualizarStatus();
    }

    public void removerReserva(String matricula) {
        filaReservas.remove(matricula);
        atualizarStatus();
    }

    public boolean temReserva() {
        return !filaReservas.isEmpty();
    }

    public String getProximoReservado() {
        if (filaReservas.isEmpty()) {
            return null;
        }
        return filaReservas.get(0);
    }

    public int getQuantidadeReservas() {
        return filaReservas.size();
    }

    /** RESERVADA se há fila de reservas; senão EMPRESTADA se não sobrou exemplar; senão DISPONIVEL. */
    private void atualizarStatus() {
        if (!filaReservas.isEmpty()) {
            status = StatusObra.RESERVADA;
        } else if (exemplaresDisponiveis == 0) {
            status = StatusObra.EMPRESTADA;
        } else {
            status = StatusObra.DISPONIVEL;
        }
    }

    // ---------- multa e ficha ----------

    public abstract double calcularMulta(int diasAtraso);

    /** Gancho para as subclasses acrescentarem seus dados à ficha. */
    protected String detalhesEspecificos() {
        return "";
    }

    public final String montarFicha() {
        String ficha = "=== FICHA CATALOGRÁFICA ===\n"
                + "Registro: " + String.format("REG-%05d", idRegistro) + "\n"
                + "Tipo: " + getClass().getSimpleName() + "\n"
                + "Título: " + titulo + "\n"
                + "Autor: " + autor + "\n"
                + "Ano de publicação: " + anoPublicacao + "\n"
                + "Exemplares: " + exemplaresDisponiveis + " disponíveis de " + exemplaresTotais + "\n"
                + "Status: " + status;
        String extras = detalhesEspecificos();
        if (!extras.isEmpty()) {
            ficha += "\n" + extras;
        }
        return ficha;
    }

    public final void exibirFichaCatalografica() {
        JOptionPane.showMessageDialog(null, montarFicha());
    }
}
