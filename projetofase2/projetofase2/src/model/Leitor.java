package model;

import java.util.ArrayList;

public class Leitor {
    public static final String TIPO_ALUNO = "ALUNO";
    public static final String TIPO_PROFESSOR = "PROFESSOR";
    public static final int LIMITE_ALUNO = 3;
    public static final int LIMITE_PROFESSOR = 5;

    private final String matricula;
    private String nome;
    private final String tipo;
    private final int limiteEmprestimos;
    private final ArrayList<String> titulosEmprestados = new ArrayList<>();

    public Leitor(String matricula, String nome, String tipo) {
        if (matricula == null || matricula.isBlank()) {
            throw new IllegalArgumentException("A matrícula não pode ser vazia.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome não pode ser vazio.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo de usuário é obrigatório.");
        }
        String tipoNormalizado = tipo.trim().toUpperCase();
        int limite;
        if (tipoNormalizado.equals(TIPO_ALUNO)) {
            limite = LIMITE_ALUNO;
        } else if (tipoNormalizado.equals(TIPO_PROFESSOR)) {
            limite = LIMITE_PROFESSOR;
        } else {
            throw new IllegalArgumentException("Tipo de usuário inválido. Use ALUNO ou PROFESSOR.");
        }
        this.matricula = matricula.trim();
        this.nome = nome.trim();
        this.tipo = tipoNormalizado;
        this.limiteEmprestimos = limite;
    }

    public String getMatricula() { return matricula; }

    public String getNome() { return nome; }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome não pode ser vazio.");
        }
        this.nome = nome.trim();
    }

    public String getTipo() { return tipo; }

    public int getLimiteEmprestimos() { return limiteEmprestimos; }

    public void adicionarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Título inválido.");
        }
        titulosEmprestados.add(titulo);
    }

    public void removerTitulo(String titulo) {
        titulosEmprestados.remove(titulo);
    }

    public ArrayList<String> getTitulosEmprestados() {
        return new ArrayList<>(titulosEmprestados);
    }

    public int getQuantidadeEmprestados() {
        return titulosEmprestados.size();
    }
}
