package model;

import interfaces.Audivel;
import javax.swing.JOptionPane;

public final class Ebook extends PublicacaoDigital implements Audivel {
    public static final double TAXA_FIXA_MULTA = 2.50;

    private String chaveProtecao;
    private int limiteLeitoresSimultaneos;
    private int leitoresAtuais;
    private int reproducoesAtuais;

    public Ebook(int id, String titulo, String autor, int anoPublicacao, int exemplaresTotais,
                 String formato, double tamanhoMB, int licencasTotais,
                 String chaveProtecao, int limiteLeitoresSimultaneos) {
        super(id, titulo, autor, anoPublicacao, exemplaresTotais, formato, tamanhoMB, licencasTotais);
        setChaveProtecao(chaveProtecao);
        setLimiteLeitoresSimultaneos(limiteLeitoresSimultaneos);
        this.leitoresAtuais = 0;
        this.reproducoesAtuais = 0;
    }

    public String getChaveProtecao() { return chaveProtecao; }

    public void setChaveProtecao(String chaveProtecao) {
        if (chaveProtecao == null || chaveProtecao.isBlank()) {
            throw new IllegalArgumentException("A chave de proteção não pode ser vazia.");
        }
        this.chaveProtecao = chaveProtecao.trim();
    }

    public int getLimiteLeitoresSimultaneos() { return limiteLeitoresSimultaneos; }

    public void setLimiteLeitoresSimultaneos(int limite) {
        if (limite <= 0) {
            throw new IllegalArgumentException("O limite de leitores simultâneos deve ser maior que zero.");
        }
        this.limiteLeitoresSimultaneos = limite;
    }

    public int getLeitoresAtuais() { return leitoresAtuais; }

    public int getReproducoesAtuais() { return reproducoesAtuais; }

    public void entrarLeitor() {
        if (leitoresAtuais >= limiteLeitoresSimultaneos) {
            throw new IllegalStateException("Limite de leitores simultâneos atingido ("
                    + limiteLeitoresSimultaneos + ").");
        }
        leitoresAtuais++;
    }

    public void sairLeitor() {
        if (leitoresAtuais <= 0) {
            throw new IllegalStateException("Nenhum leitor conectado no momento.");
        }
        leitoresAtuais--;
    }

    @Override
    public double calcularMulta(int diasAtraso) {
        if (diasAtraso <= 0) {
            return 0;
        }
        return TAXA_FIXA_MULTA;
    }

    @Override
    public void executarAudio(int capitulo) {
        if (capitulo <= 0) {
            throw new IllegalArgumentException("Capítulo inválido.");
        }
        if (reproducoesAtuais >= Audivel.LIMITE_REPRODUCOES_SIMULTANEAS) {
            throw new IllegalStateException("Limite de " + Audivel.LIMITE_REPRODUCOES_SIMULTANEAS
                    + " reproduções simultâneas atingido.");
        }
        reproducoesAtuais++;
        JOptionPane.showMessageDialog(null, "Reproduzindo o capítulo " + capitulo
                + " de \"" + getTitulo() + "\".");
    }

    public void finalizarAudio() {
        if (reproducoesAtuais <= 0) {
            throw new IllegalStateException("Nenhum áudio está sendo reproduzido.");
        }
        reproducoesAtuais--;
    }

    public void ajustarAcessibilidade(int tamanhoFonte, boolean sintetizadorVoz) {
        if (tamanhoFonte < 8 || tamanhoFonte > 48) {
            throw new IllegalArgumentException("O tamanho da fonte deve estar entre 8 e 48.");
        }
        JOptionPane.showMessageDialog(null,
                "Acessibilidade ajustada para \"" + getTitulo() + "\":"
                        + "\nTamanho da fonte: " + tamanhoFonte
                        + "\nSintetizador de voz: " + (sintetizadorVoz ? "ligado" : "desligado"));
    }

    @Override
    protected String detalhesEspecificos() {
        return super.detalhesEspecificos() + "\n"
                + "Leitores simultâneos: " + leitoresAtuais + " de " + limiteLeitoresSimultaneos + "\n"
                + "Proteção contra cópia: ativa";
    }
}
