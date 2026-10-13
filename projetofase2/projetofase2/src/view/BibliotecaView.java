package view;

import controller.BibliotecaController;
import enums.EstadoConservacao;
import exceptions.LimiteEmprestimoExcedidoException;
import exceptions.ObraEsgotadaException;
import exceptions.RenovacaoNegadaException;
import model.Ebook;
import model.Emprestimo;
import model.Leitor;
import model.LivroFisico;
import model.ObraAcervo;
import model.PublicacaoDigital;
import model.PublicacaoImpressa;

import javax.swing.JOptionPane;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class BibliotecaView {
    private final BibliotecaController controller = new BibliotecaController();

    /** Lançada quando o usuário clica em Cancelar numa caixa de diálogo. */
    private static class OperacaoCancelada extends RuntimeException {
    }

    public void iniciar() {
        int opcao = -1;
        while (opcao != 0) {
            opcao = lerOpcao("=== BIBLIOTECA ===\n\n"
                    + "1 - Cadastrar obra\n"
                    + "2 - Cadastrar leitor\n"
                    + "3 - Realizar empréstimo\n"
                    + "4 - Registrar devolução\n"
                    + "5 - Consultar acervo\n"
                    + "6 - Renovar empréstimo\n"
                    + "7 - Doação e reposição\n"
                    + "8 - Histórico do leitor\n"
                    + "9 - Relatório de obras pendentes\n"
                    + "0 - Sair\n\nEscolha:");

            try {
                switch (opcao) {
                    case 1: cadastrarObra(); break;
                    case 2: cadastrarLeitor(); break;
                    case 3: realizarEmprestimo(); break;
                    case 4: registrarDevolucao(); break;
                    case 5: consultarAcervo(); break;
                    case 6: renovarEmprestimo(); break;
                    case 7: doacaoReposicao(); break;
                    case 8: historicoLeitor(); break;
                    case 9: relatorioPendentes(); break;
                    case 0: aviso("Encerrando o sistema."); break;
                    default: aviso("Opção inválida.");
                }
            } catch (OperacaoCancelada e) {
                aviso("Operação cancelada.");
            } catch (IllegalArgumentException | IllegalStateException e) {
                aviso("Erro: " + e.getMessage());
            }
        }
    }

    // ---------- opção 1 ----------
    private void cadastrarObra() {
        try {
            int tipo = lerInt("Tipo de obra:\n1 - Publicação impressa\n2 - Publicação digital\n"
                    + "3 - Livro físico\n4 - E-book");
            if (tipo < 1 || tipo > 4) {
                aviso("Tipo inválido.");
                return;
            }

            int id = lerInt("Id da obra:");
            if (controller.buscarObra(id) != null) {
                throw new IllegalArgumentException("Já existe uma obra com o id " + id + ".");
            }
            String titulo = lerTexto("Título:");
            String autor = lerTexto("Autor:");
            int ano = lerInt("Ano de publicação:");
            int exemplares = lerInt("Exemplares totais:");

            boolean impressa = (tipo == 1 || tipo == 3);
            if (impressa) {
                controller.verificarEspacoEstante(exemplares);
            }

            ObraAcervo obra;
            if (tipo == 1) {
                int paginas = lerInt("Número de páginas:");
                double peso = lerDouble("Peso (kg):");
                String encadernacao = lerTexto("Tipo de encadernação:");
                obra = new PublicacaoImpressa(id, titulo, autor, ano, exemplares,
                        paginas, peso, encadernacao);
            } else if (tipo == 2) {
                String formato = lerTexto("Formato (PDF ou EPUB):");
                double tamanho = lerDouble("Tamanho em MB:");
                int licencas = lerInt("Total de licenças:");
                obra = new PublicacaoDigital(id, titulo, autor, ano, exemplares,
                        formato, tamanho, licencas);
            } else if (tipo == 3) {
                int paginas = lerInt("Número de páginas:");
                double peso = lerDouble("Peso (kg):");
                String encadernacao = lerTexto("Tipo de encadernação:");
                String isbn = lerTexto("ISBN:");
                String editora = lerTexto("Editora:");
                obra = new LivroFisico(id, titulo, autor, ano, exemplares,
                        paginas, peso, encadernacao, isbn, editora);
            } else {
                String formato = lerTexto("Formato (PDF ou EPUB):");
                double tamanho = lerDouble("Tamanho em MB:");
                int licencas = lerInt("Total de licenças:");
                String chave = lerTexto("Chave de proteção contra cópia:");
                int leitores = lerInt("Limite de leitores simultâneos:");
                obra = new Ebook(id, titulo, autor, ano, exemplares,
                        formato, tamanho, licencas, chave, leitores);
            }

            controller.cadastrarObra(obra);
            aviso("Obra cadastrada com sucesso! (" + obra.getClass().getSimpleName() + ")"
                    + "\nTotal de volumes no acervo: " + ObraAcervo.getTotalVolumes());
        } catch (IllegalArgumentException | IllegalStateException e) {
            aviso("Erro no cadastro: " + e.getMessage());
        }
    }

    // ---------- opção 2 ----------
    private void cadastrarLeitor() {
        try {
            String matricula = lerTexto("Matrícula:");
            String nome = lerTexto("Nome completo:");
            int tipo = lerInt("Tipo (1 = Aluno, 2 = Professor):");

            String tipoUsuario;
            if (tipo == 1) {
                tipoUsuario = Leitor.TIPO_ALUNO;
            } else if (tipo == 2) {
                tipoUsuario = Leitor.TIPO_PROFESSOR;
            } else {
                aviso("Tipo inválido.");
                return;
            }
            Leitor leitor = new Leitor(matricula, nome, tipoUsuario);
            controller.cadastrarLeitor(leitor);
            aviso("Leitor cadastrado! Limite de empréstimos: " + leitor.getLimiteEmprestimos());
        } catch (IllegalArgumentException e) {
            aviso("Erro no cadastro: " + e.getMessage());
        }
    }

    // ---------- opção 3 ----------
    private void realizarEmprestimo() {
        String matricula = "";
        int idObra = -1;
        try {
            matricula = lerTexto("Matrícula do leitor:");
            idObra = lerInt("Id da obra:");
            LocalDate data = lerData("Data da saída (dd/MM/aaaa, vazio = hoje):");

            Emprestimo e = controller.realizarEmprestimo(matricula, idObra, data);
            aviso("Empréstimo realizado!\nCódigo: " + e.getCodigo()
                    + "\nPrazo de devolução: " + e.getPrazoDevolucao().format(Emprestimo.FORMATO_DATA));

            ObraAcervo obra = controller.buscarObra(idObra);
            if (obra instanceof Ebook) {
                usarEbook((Ebook) obra);
            }
        } catch (LimiteEmprestimoExcedidoException e) {
            aviso("LIMITE EXCEDIDO\n\nLeitor: " + e.getNomeDoLeitor()
                    + "\nLimite permitido: " + e.getLimitePermitido()
                    + "\nEmpréstimos ativos: " + e.getEmprestimosAtivos());
        } catch (ObraEsgotadaException e) {
            aviso("OBRA ESGOTADA\n\nTítulo: " + e.getTitulo()
                    + "\nExemplares disponíveis: " + e.getExemplaresDisponiveis());
            oferecerReserva(matricula, idObra);
        } catch (IllegalArgumentException | IllegalStateException e) {
            aviso("Não foi possível emprestar: " + e.getMessage());
        }
    }

    private void oferecerReserva(String matricula, int idObra) {
        int resposta = JOptionPane.showConfirmDialog(null,
                "Deseja reservar esta obra para o leitor?", "Reserva",
                JOptionPane.YES_NO_OPTION);
        if (resposta == JOptionPane.YES_OPTION) {
            try {
                controller.reservarObra(matricula, idObra);
                aviso("Reserva registrada! A obra será guardada para este leitor quando for devolvida.");
            } catch (IllegalArgumentException | IllegalStateException e) {
                aviso("Não foi possível reservar: " + e.getMessage());
            }
        }
    }

    // ---------- uso do e-book (interface Audivel e controle de acesso) ----------
    private void usarEbook(Ebook ebook) {
        try {
            ebook.entrarLeitor();   // controle de leitores simultâneos
        } catch (IllegalStateException e) {
            aviso("Não foi possível abrir o e-book: " + e.getMessage());
            return;
        }

        try {
            int opcao = -1;
            while (opcao != 0) {
                opcao = lerOpcao("E-BOOK: " + ebook.getTitulo() + "\n\n"
                        + "1 - Ouvir capítulo\n"
                        + "2 - Parar áudio\n"
                        + "3 - Retomar leitura\n"
                        + "4 - Ajustar acessibilidade\n"
                        + "5 - Baixar para leitura offline\n"
                        + "0 - Fechar e-book\n\nEscolha:");
                try {
                    switch (opcao) {
                        case 1:
                            ebook.executarAudio(lerInt("Número do capítulo:"));
                            break;
                        case 2:
                            ebook.finalizarAudio();
                            aviso("Áudio encerrado.");
                            break;
                        case 3:
                            ebook.retomarLeitura(lerInt("Última página ouvida (0 se nenhuma):"));
                            break;
                        case 4: {
                            int fonte = lerInt("Tamanho da fonte (8 a 48):");
                            boolean voz = JOptionPane.showConfirmDialog(null,
                                    "Ligar o sintetizador de voz?", "Acessibilidade",
                                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
                            ebook.ajustarAcessibilidade(fonte, voz);
                            break;
                        }
                        case 5:
                            ebook.registrarDownloadTemporario();
                            aviso("Download temporário autorizado. Total: "
                                    + ebook.getDownloadsTemporarios());
                            break;
                        case 0:
                            break;
                        default:
                            aviso("Opção inválida.");
                    }
                } catch (OperacaoCancelada e) {
                    // cancelou a pergunta: volta ao menu do e-book
                } catch (IllegalArgumentException | IllegalStateException e) {
                    aviso("Erro: " + e.getMessage());
                }
            }
        } finally {
            ebook.sairLeitor();
        }
    }

    // ---------- opção 4 ----------
    private void registrarDevolucao() {
        try {
            int codigo = lerInt("Código do empréstimo:");
            LocalDate data = lerData("Data da devolução (dd/MM/aaaa, vazio = hoje):");

            double multa = controller.registrarDevolucao(codigo, data);
            Emprestimo e = controller.buscarEmprestimo(codigo);
            aviso("Devolução registrada!\nObra: " + e.getObra().getTitulo()
                    + "\nDias de atraso: " + e.getDiasAtrasoNaDevolucao()
                    + "\nMulta: R$ " + String.format("%.2f", multa));
        } catch (IllegalArgumentException | IllegalStateException e) {
            aviso("Não foi possível devolver: " + e.getMessage());
        }
    }

    // ---------- opção 5 ----------
    private void consultarAcervo() {
        try {
            int tipo = lerInt("Consultar acervo:\n1 - Buscar por título\n2 - Buscar por ano e autor\n"
                    + "3 - Ver ficha e ações de uma obra (por id)");
            if (tipo == 1) {
                String titulo = lerTexto("Título (ou parte dele):");
                aviso(controller.buscarAcervo(titulo));
            } else if (tipo == 2) {
                int ano = lerInt("Ano de publicação:");
                String autor = lerTexto("Autor:");
                aviso(controller.buscarAcervo(ano, autor));
            } else if (tipo == 3) {
                acoesDaObra();
            } else {
                aviso("Opção inválida.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            aviso("Erro: " + e.getMessage());
        }
    }

    private void acoesDaObra() {
        int id = lerInt("Id da obra:");
        ObraAcervo obra = controller.buscarObra(id);
        if (obra == null) {
            aviso("Obra não encontrada.");
            return;
        }
        obra.exibirFichaCatalografica();
        if (obra instanceof PublicacaoImpressa) {
            acoesImpressa((PublicacaoImpressa) obra);
        } else if (obra instanceof PublicacaoDigital) {
            acoesDigital((PublicacaoDigital) obra);
        }
    }

    private void acoesImpressa(PublicacaoImpressa obra) {
        boolean livro = obra instanceof LivroFisico;
        int opcao = -1;
        while (opcao != 0) {
            opcao = lerOpcao("OBRA IMPRESSA: " + obra.getTitulo() + "\n\n"
                    + "1 - Vistoria de desgaste da capa\n"
                    + "2 - Agendar restauro\n"
                    + (livro ? "3 - Conferir volume na estante\n" : "")
                    + "0 - Voltar\n\nEscolha:");
            try {
                switch (opcao) {
                    case 1:
                        aviso(obra.inspecionarDesgasteCapa(lerEstado()));
                        break;
                    case 2:
                        aviso(obra.agendarRestauro());
                        break;
                    case 3:
                        if (livro) {
                            aviso(((LivroFisico) obra).conferirVolumeEstante());
                        } else {
                            aviso("Opção inválida.");
                        }
                        break;
                    case 0:
                        break;
                    default:
                        aviso("Opção inválida.");
                }
            } catch (OperacaoCancelada e) {
                // cancelou a pergunta: volta ao menu da obra
            } catch (IllegalArgumentException | IllegalStateException e) {
                aviso("Erro: " + e.getMessage());
            }
        }
    }

    private void acoesDigital(PublicacaoDigital obra) {
        int opcao = -1;
        while (opcao != 0) {
            opcao = lerOpcao("OBRA DIGITAL: " + obra.getTitulo() + "\n\n"
                    + "1 - Ver licenças ativas e downloads\n"
                    + "2 - Registrar download temporário\n"
                    + "0 - Voltar\n\nEscolha:");
            try {
                switch (opcao) {
                    case 1:
                        aviso("Licenças ativas: " + obra.getLicencasAtivas() + " de " + obra.getLicencasTotais()
                                + "\nDownloads temporários: " + obra.getDownloadsTemporarios()
                                + " de " + PublicacaoDigital.LIMITE_DOWNLOADS);
                        break;
                    case 2:
                        obra.registrarDownloadTemporario();
                        aviso("Download temporário autorizado. Total: " + obra.getDownloadsTemporarios());
                        break;
                    case 0:
                        break;
                    default:
                        aviso("Opção inválida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                aviso("Erro: " + e.getMessage());
            }
        }
    }

    private EstadoConservacao lerEstado() {
        int n = lerInt("Estado observado da capa:\n1 - NOVO\n2 - BOM\n3 - DESGASTADO\n4 - DANIFICADO");
        if (n < 1 || n > EstadoConservacao.values().length) {
            throw new IllegalArgumentException("Estado inválido.");
        }
        return EstadoConservacao.values()[n - 1];
    }

    // ---------- opção 6 ----------
    private void renovarEmprestimo() {
        try {
            int codigo = lerInt("Código do empréstimo:");
            Emprestimo e = controller.renovarEmprestimo(codigo);
            aviso("Empréstimo renovado com sucesso!\nNovo prazo: "
                    + e.getPrazoDevolucao().format(Emprestimo.FORMATO_DATA)
                    + "\nRenovações: " + e.getRenovacoes() + "/" + Emprestimo.MAX_RENOVACOES);
        } catch (RenovacaoNegadaException e) {
            aviso("RENOVAÇÃO NEGADA\n\nObra: " + e.getTituloObra()
                    + "\nRenovações já feitas: " + e.getRenovacoesFeitas()
                    + "\nMotivo: " + e.getMotivo());
        } catch (IllegalArgumentException e) {
            aviso("Erro: " + e.getMessage());
        }
    }

    // ---------- opção 7 ----------
    private void doacaoReposicao() {
        try {
            int idObra = lerInt("Id da obra que recebeu doação/reposição:");
            int quantidade = lerInt("Quantidade de exemplares:");
            controller.processarDoacao(idObra, quantidade);
            aviso("Exemplares adicionados!\nEspaço ocupado na estante: "
                    + controller.calcularEspacoOcupado() + "/" + BibliotecaController.LIMITE_ESTANTE
                    + "\nTotal de volumes no acervo: " + ObraAcervo.getTotalVolumes());
        } catch (IllegalArgumentException | IllegalStateException e) {
            aviso("Não foi possível processar: " + e.getMessage());
        }
    }

    // ---------- opção 8 ----------
    private void historicoLeitor() {
        try {
            String matricula = lerTexto("Matrícula do leitor:");
            aviso(controller.historicoDoLeitor(matricula));
        } catch (IllegalArgumentException e) {
            aviso("Erro: " + e.getMessage());
        }
    }

    // ---------- opção 9 ----------
    private void relatorioPendentes() {
        aviso(controller.relatorioPendentes());
    }

    // ---------- métodos auxiliares ----------
    private void aviso(String mensagem) {
        JOptionPane.showMessageDialog(null, mensagem);
    }

    private String lerTexto(String mensagem) {
        String s = JOptionPane.showInputDialog(null, mensagem);
        if (s == null) {
            throw new OperacaoCancelada();
        }
        return s.trim();
    }

    private int lerInt(String mensagem) {
        String s = JOptionPane.showInputDialog(null, mensagem);
        if (s == null) {
            throw new OperacaoCancelada();
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Digite apenas números inteiros.");
        }
    }

    private double lerDouble(String mensagem) {
        String s = JOptionPane.showInputDialog(null, mensagem);
        if (s == null) {
            throw new OperacaoCancelada();
        }
        try {
            return Double.parseDouble(s.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Digite um número válido.");
        }
    }

    private LocalDate lerData(String mensagem) {
        String s = JOptionPane.showInputDialog(null, mensagem);
        if (s == null) {
            throw new OperacaoCancelada();
        }
        if (s.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(s.trim(), Emprestimo.FORMATO_DATA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data inválida. Use o formato dd/MM/aaaa.");
        }
    }

    private int lerOpcao(String texto) {
        String s = JOptionPane.showInputDialog(null, texto);
        if (s == null) {
            return 0;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
