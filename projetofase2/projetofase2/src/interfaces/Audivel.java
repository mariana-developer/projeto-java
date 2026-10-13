package interfaces;

import javax.swing.JOptionPane;

public interface Audivel {
    int LIMITE_REPRODUCOES_SIMULTANEAS = 2;

    void executarAudio(int capitulo);

    default void retomarLeitura(int ultimaPaginaOuvida) {
        if (ultimaPaginaOuvida <= 0) {
            JOptionPane.showMessageDialog(null,
                    "Nenhum marcador foi encontrado, a leitura vai começar do início.");
        } else {
            JOptionPane.showMessageDialog(null,
                    "Retomando o conteúdo a partir da página: " + ultimaPaginaOuvida + ".");
        }
    }
}